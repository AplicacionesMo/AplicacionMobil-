package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.data.repository.UsuarioRepository
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.Usuario
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Los campos del formulario. Cada uno sabe si ya fue tocado por el usuario. */
enum class CampoTicket { TITULO, DESCRIPCION, CLIENTE, DIRECCION, NOMBRE_EQUIPO, MODELO, SERIE }

data class FormularioTicketUiState(
    // Ticket (formulario 1 de 5)
    val titulo: String = "",
    val descripcion: String = "",
    val cliente: String = "",
    val direccion: String = "",
    val prioridad: Prioridad = Prioridad.MEDIA,
    // Equipo (formulario 2 de 5)
    val nombreEquipo: String = "",
    val modelo: String = "",
    val serie: String = "",
    val propiedad: Propiedad = Propiedad.PROPIO_DEL_CLIENTE,
    // Asignacion (formulario 3): tecnico que atendera el ticket
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null,
    val tecnicosDisponibles: List<Usuario> = emptyList(),
    val cargandoTecnicos: Boolean = false,
    // Estado de la pantalla
    val esEdicion: Boolean = false,
    val numero: Int? = null,
    val cargando: Boolean = false,
    val soloLectura: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null,
    // Errores ya calculados: la pantalla solo los muestra
    val errores: Map<CampoTicket, String> = emptyMap(),
    val camposTocados: Set<CampoTicket> = emptySet(),
    val intentoGuardar: Boolean = false
) {
    /** Error visible de un campo: solo si el usuario ya lo toco o intento guardar. */
    fun errorVisible(campo: CampoTicket): String? =
        if (intentoGuardar || campo in camposTocados) errores[campo] else null

    val formularioValido: Boolean get() = errores.isEmpty()

    /** Texto del selector: el tecnico elegido o la invitacion a elegir. */
    val tecnicoElegido: String get() = tecnicoNombre ?: "Sin asignar"
}

/**
 * Formulario de ticket (crear o editar).
 *
 * Lo escrito se replica en el SavedStateHandle: si Android mata el proceso mientras el
 * formulario esta abierto, al volver el texto sigue ahi. Es la misma proteccion que usa
 * el formulario de guias.
 */
class FormularioTicketViewModel(
    private val ticketId: String?,
    private val estadoGuardado: SavedStateHandle,
    private val ticketRepository: TicketRepository,
    private val equipoRepository: EquipoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val rol: Rol
) : ViewModel() {

    // Un tecnico que intenta crear un ticket nuevo ve el formulario bloqueado
    private val _estado = MutableStateFlow(
        if (ticketId == null && !rol.puedeCrearTickets) {
            FormularioTicketUiState(soloLectura = true, mensaje = MENSAJE_SIN_PERMISO)
        } else {
            conBorrador(FormularioTicketUiState(esEdicion = ticketId != null))
        }
    )
    val estado: StateFlow<FormularioTicketUiState> = _estado.asStateFlow()

    /** Hay texto respaldado de una sesion anterior que Android interrumpio. */
    private val hayBorrador: Boolean get() = estadoGuardado.contains(CLAVE_TITULO)

    init {
        if (ticketId != null) cargarTicket(ticketId) else recalcularErrores()
        cargarTecnicos()
    }

    /** GET /api/v1/usuarios en ms-usuarios; se queda solo con los del rol TECNICO. */
    private fun cargarTecnicos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoTecnicos = true) }
            capturarErrores { usuarioRepository.listar() }
                .onSuccess { usuarios ->
                    val tecnicos = usuarios.filter { it.rol == Rol.TECNICO && it.activo }
                    _estado.update { it.copy(tecnicosDisponibles = tecnicos, cargandoTecnicos = false) }
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(cargandoTecnicos = false, mensaje = ErrorDeRed.mensaje(error))
                    }
                }
        }
    }

    /** Se elige un tecnico de la lista; volver a tocar el campo la abre de nuevo. */
    fun seleccionarTecnico(tecnico: Usuario) {
        _estado.update { it.copy(tecnicoId = tecnico.id, tecnicoNombre = tecnico.nombre) }
        respaldar()
    }

    fun quitarTecnico() {
        _estado.update { it.copy(tecnicoId = null, tecnicoNombre = null) }
        respaldar()
    }

    private fun cargarTicket(id: String) {
        _estado.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val ticket = ticketRepository.obtenerTicket(id)
            val equipo = ticket?.let { equipoRepository.obtenerEquipo(it.equipoId) }
            if (ticket == null || equipo == null) {
                _estado.update { it.copy(cargando = false, mensaje = "No se encontro el ticket") }
                return@launch
            }
            // Si hay un borrador respaldado, manda el borrador: es lo ultimo que escribio
            val conservarBorrador = hayBorrador
            _estado.update {
                val base = if (conservarBorrador) it else it.copy(
                    titulo = ticket.titulo,
                    descripcion = ticket.descripcion,
                    cliente = ticket.cliente,
                    direccion = ticket.direccion,
                    prioridad = ticket.prioridad,
                    nombreEquipo = equipo.nombre,
                    modelo = equipo.modelo,
                    serie = equipo.serie,
                    propiedad = equipo.propiedad,
                    tecnicoId = ticket.tecnicoId,
                    tecnicoNombre = ticket.tecnicoNombre
                )
                base.copy(
                    numero = ticket.numero,
                    cargando = false,
                    // Regla: un ticket finalizado o cancelado no se puede editar
                    soloLectura = !ticket.editable,
                    mensaje = if (!ticket.editable) {
                        "Este ticket esta ${ticket.estado.etiqueta.lowercase()} y no se puede editar"
                    } else null
                )
            }
            recalcularErrores()
        }
    }

    // ---------- Eventos de la pantalla ----------

    fun cambiarCampo(campo: CampoTicket, valor: String) {
        _estado.update {
            val tocados = it.camposTocados + campo
            when (campo) {
                CampoTicket.TITULO -> it.copy(titulo = valor, camposTocados = tocados)
                CampoTicket.DESCRIPCION -> it.copy(descripcion = valor, camposTocados = tocados)
                CampoTicket.CLIENTE -> it.copy(cliente = valor, camposTocados = tocados)
                CampoTicket.DIRECCION -> it.copy(direccion = valor, camposTocados = tocados)
                CampoTicket.NOMBRE_EQUIPO -> it.copy(nombreEquipo = valor, camposTocados = tocados)
                CampoTicket.MODELO -> it.copy(modelo = valor, camposTocados = tocados)
                CampoTicket.SERIE -> it.copy(serie = valor.uppercase(), camposTocados = tocados)
            }
        }
        recalcularErrores()
        respaldar()
    }

    fun cambiarPrioridad(prioridad: Prioridad) {
        _estado.update { it.copy(prioridad = prioridad) }
        respaldar()
    }

    fun cambiarPropiedad(propiedad: Propiedad) {
        _estado.update { it.copy(propiedad = propiedad) }
        respaldar()
    }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    // ---------- Borrador que sobrevive a que Android mate el proceso ----------

    private fun respaldar() {
        val s = _estado.value
        estadoGuardado[CLAVE_TITULO] = s.titulo
        estadoGuardado[CLAVE_DESCRIPCION] = s.descripcion
        estadoGuardado[CLAVE_CLIENTE] = s.cliente
        estadoGuardado[CLAVE_DIRECCION] = s.direccion
        estadoGuardado[CLAVE_PRIORIDAD] = s.prioridad.name
        estadoGuardado[CLAVE_NOMBRE_EQUIPO] = s.nombreEquipo
        estadoGuardado[CLAVE_MODELO] = s.modelo
        estadoGuardado[CLAVE_SERIE] = s.serie
        estadoGuardado[CLAVE_PROPIEDAD] = s.propiedad.name
        estadoGuardado[CLAVE_TECNICO_ID] = s.tecnicoId
        estadoGuardado[CLAVE_TECNICO_NOMBRE] = s.tecnicoNombre
        estadoGuardado[CLAVE_TOCADOS] = ArrayList(s.camposTocados.map { it.name })
    }

    private fun conBorrador(base: FormularioTicketUiState): FormularioTicketUiState {
        if (!estadoGuardado.contains(CLAVE_TITULO)) return base
        return base.copy(
            titulo = estadoGuardado[CLAVE_TITULO] ?: base.titulo,
            descripcion = estadoGuardado[CLAVE_DESCRIPCION] ?: base.descripcion,
            cliente = estadoGuardado[CLAVE_CLIENTE] ?: base.cliente,
            direccion = estadoGuardado[CLAVE_DIRECCION] ?: base.direccion,
            prioridad = estadoGuardado.get<String>(CLAVE_PRIORIDAD)
                ?.let { nombre -> Prioridad.entries.firstOrNull { it.name == nombre } }
                ?: base.prioridad,
            nombreEquipo = estadoGuardado[CLAVE_NOMBRE_EQUIPO] ?: base.nombreEquipo,
            modelo = estadoGuardado[CLAVE_MODELO] ?: base.modelo,
            serie = estadoGuardado[CLAVE_SERIE] ?: base.serie,
            propiedad = estadoGuardado.get<String>(CLAVE_PROPIEDAD)
                ?.let { nombre -> Propiedad.entries.firstOrNull { it.name == nombre } }
                ?: base.propiedad,
            tecnicoId = estadoGuardado[CLAVE_TECNICO_ID] ?: base.tecnicoId,
            tecnicoNombre = estadoGuardado[CLAVE_TECNICO_NOMBRE] ?: base.tecnicoNombre,
            camposTocados = estadoGuardado.get<ArrayList<String>>(CLAVE_TOCADOS)
                ?.mapNotNull { nombre -> CampoTicket.entries.firstOrNull { it.name == nombre } }
                ?.toSet()
                ?: base.camposTocados
        )
    }

    private fun limpiarBorrador() {
        listOf(
            CLAVE_TITULO, CLAVE_DESCRIPCION, CLAVE_CLIENTE, CLAVE_DIRECCION, CLAVE_PRIORIDAD,
            CLAVE_NOMBRE_EQUIPO, CLAVE_MODELO, CLAVE_SERIE, CLAVE_PROPIEDAD, CLAVE_TOCADOS,
            CLAVE_TECNICO_ID, CLAVE_TECNICO_NOMBRE
        ).forEach { estadoGuardado.remove<Any>(it) }
    }

    // ---------- Validacion: toda aqui, nada en el composable ----------

    private fun recalcularErrores() {
        val s = _estado.value
        val errores = buildMap {
            Validaciones.largoMinimo(s.titulo, 5, "El titulo")?.let { put(CampoTicket.TITULO, it) }
            Validaciones.largoMinimo(s.descripcion, 10, "La descripcion")?.let { put(CampoTicket.DESCRIPCION, it) }
            Validaciones.noVacio(s.cliente, "El cliente")?.let { put(CampoTicket.CLIENTE, it) }
            Validaciones.largoMinimo(s.direccion, 5, "La direccion")?.let { put(CampoTicket.DIRECCION, it) }
            Validaciones.noVacio(s.nombreEquipo, "El nombre del equipo")?.let { put(CampoTicket.NOMBRE_EQUIPO, it) }
            Validaciones.noVacio(s.modelo, "El modelo")?.let { put(CampoTicket.MODELO, it) }
            Validaciones.serieValida(s.serie)?.let { put(CampoTicket.SERIE, it) }
        }
        _estado.update { it.copy(errores = errores) }
    }

    // ---------- Guardado ----------

    fun guardar() {
        _estado.update { it.copy(intentoGuardar = true) }
        val s = _estado.value
        if (s.soloLectura || s.guardando) return
        if (!s.formularioValido) {
            _estado.update { it.copy(mensaje = "Revisa los campos marcados") }
            return
        }

        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val datos = s.aDatos()
            // POST (crear) o PUT (editar) contra ms-tickets; el servidor asigna numero y fecha
            capturarErrores {
                if (ticketId != null) ticketRepository.actualizar(ticketId, datos)
                else ticketRepository.crear(datos, rol)
            }
                .onSuccess {
                    limpiarBorrador()
                    _estado.update { it.copy(guardando = false, guardado = true) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(guardando = false, mensaje = ErrorDeRed.mensaje(error)) }
                }
        }
    }

    private fun FormularioTicketUiState.aDatos() = DatosTicket(
        titulo = titulo,
        descripcion = descripcion,
        cliente = cliente,
        direccion = direccion,
        prioridad = prioridad,
        nombreEquipo = nombreEquipo,
        modelo = modelo,
        serie = serie,
        propiedad = propiedad,
        tecnicoId = tecnicoId,
        tecnicoNombre = tecnicoNombre
    )

    companion object {
        const val MENSAJE_SIN_PERMISO = "Solo el coordinador o el administrador pueden crear tickets"

        private const val CLAVE_TITULO = "ticket.titulo"
        private const val CLAVE_DESCRIPCION = "ticket.descripcion"
        private const val CLAVE_CLIENTE = "ticket.cliente"
        private const val CLAVE_DIRECCION = "ticket.direccion"
        private const val CLAVE_PRIORIDAD = "ticket.prioridad"
        private const val CLAVE_NOMBRE_EQUIPO = "ticket.nombreEquipo"
        private const val CLAVE_MODELO = "ticket.modelo"
        private const val CLAVE_SERIE = "ticket.serie"
        private const val CLAVE_PROPIEDAD = "ticket.propiedad"
        private const val CLAVE_TOCADOS = "ticket.camposTocados"
        private const val CLAVE_TECNICO_ID = "ticket.tecnicoId"
        private const val CLAVE_TECNICO_NOMBRE = "ticket.tecnicoNombre"

        fun factory(ticketId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                FormularioTicketViewModel(
                    ticketId,
                    createSavedStateHandle(),
                    app.ticketRepository,
                    app.equipoRepository,
                    app.usuarioRepository,
                    app.sesionRepository.rolActual()
                )
            }
        }
    }
}