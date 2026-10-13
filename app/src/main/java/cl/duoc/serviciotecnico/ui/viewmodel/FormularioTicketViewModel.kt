package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
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
}

class FormularioTicketViewModel(
    private val ticketId: String?,
    private val ticketRepository: TicketRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioTicketUiState(esEdicion = ticketId != null))
    val estado: StateFlow<FormularioTicketUiState> = _estado.asStateFlow()

    // Se guardan para poder hacer copy() al editar sin perder estado, fechas, etc.
    private var ticketOriginal: Ticket? = null
    private var equipoOriginal: Equipo? = null

    init {
        if (ticketId != null) cargarTicket(ticketId) else recalcularErrores()
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
            ticketOriginal = ticket
            equipoOriginal = equipo
            _estado.update {
                it.copy(
                    titulo = ticket.titulo,
                    descripcion = ticket.descripcion,
                    cliente = ticket.cliente,
                    direccion = ticket.direccion,
                    prioridad = ticket.prioridad,
                    nombreEquipo = equipo.nombre,
                    modelo = equipo.modelo,
                    serie = equipo.serie,
                    propiedad = equipo.propiedad,
                    numero = ticket.numero,
                    cargando = false,
                    // Regla: un ticket cerrado o cancelado no se puede editar
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
    }

    fun cambiarPrioridad(prioridad: Prioridad) = _estado.update { it.copy(prioridad = prioridad) }

    fun cambiarPropiedad(propiedad: Propiedad) = _estado.update { it.copy(propiedad = propiedad) }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

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
            try {
                if (s.esEdicion) actualizar(s) else crear(s)
                _estado.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, mensaje = "No se pudo guardar el ticket") }
            }
        }
    }

    private suspend fun crear(s: FormularioTicketUiState) {
        // Si el equipo ya existe (misma serie), se reutiliza con los datos nuevos
        val existente = equipoRepository.buscarPorSerie(s.serie.trim())
        val equipo = if (existente != null) {
            existente.copy(
                nombre = s.nombreEquipo.trim(),
                modelo = s.modelo.trim(),
                propiedad = s.propiedad
            ).also { equipoRepository.actualizar(it) }
        } else {
            equipoRepository.crear(
                nombre = s.nombreEquipo.trim(),
                modelo = s.modelo.trim(),
                serie = s.serie.trim(),
                propiedad = s.propiedad
            )
        }
        // El repositorio asigna el numero correlativo y la fecha de creacion
        ticketRepository.crear(
            titulo = s.titulo.trim(),
            descripcion = s.descripcion.trim(),
            cliente = s.cliente.trim(),
            direccion = s.direccion.trim(),
            prioridad = s.prioridad,
            equipoId = equipo.id
        )
    }

    private suspend fun actualizar(s: FormularioTicketUiState) {
        val ticket = ticketOriginal ?: return
        val equipo = equipoOriginal ?: return
        equipoRepository.actualizar(
            equipo.copy(
                nombre = s.nombreEquipo.trim(),
                modelo = s.modelo.trim(),
                serie = s.serie.trim(),
                propiedad = s.propiedad
            )
        )
        ticketRepository.actualizar(
            ticket.copy(
                titulo = s.titulo.trim(),
                descripcion = s.descripcion.trim(),
                cliente = s.cliente.trim(),
                direccion = s.direccion.trim(),
                prioridad = s.prioridad
            )
        )
    }

    companion object {
        fun factory(ticketId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                FormularioTicketViewModel(ticketId, app.ticketRepository, app.equipoRepository)
            }
        }
    }
}