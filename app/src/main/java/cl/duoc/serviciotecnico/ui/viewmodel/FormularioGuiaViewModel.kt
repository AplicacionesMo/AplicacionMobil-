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
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lo que el tecnico escribe en la guia.
 * La direccion y el nombre del tecnico ya no son campos: la primera la trae el ticket
 * y el segundo sale de la sesion activa.
 */
data class FormularioGuiaState(
    val trabajoRealizado: String = "",
    val repuestos: String = "",
    val observaciones: String = "",
    val recibidoPor: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotos: List<String> = emptyList(),
    val tocado: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val errorTrabajo: String? get() = Validaciones.largoMinimo(trabajoRealizado, 10, "El trabajo realizado")
    val errorRecibidoPor: String? get() = Validaciones.noVacio(recibidoPor, "Quien recibe el servicio")

    val esValido: Boolean
        get() = Validaciones.todoCorrecto(errorTrabajo, errorRecibidoPor)

    val tieneUbicacion: Boolean get() = latitud != null && longitud != null

    fun errorVisible(error: String?): String? = if (tocado) error else null
}

/**
 * Formulario de una guia nueva.
 *
 * El texto escrito se replica en el SavedStateHandle. Es necesario porque abrir la camara
 * puede hacer que Android mate el proceso de la app por falta de memoria: al volver, el
 * ViewModel se crea de nuevo y sin esto el formulario aparecia en blanco.
 */
class FormularioGuiaViewModel(
    private val ticketId: String,
    private val estadoGuardado: SavedStateHandle,
    ticketRepository: TicketRepository,
    private val guiaRepository: GuiaRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    val ticket: StateFlow<Ticket?> = ticketRepository.observarTicket(ticketId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _estado = MutableStateFlow(recuperar())
    val estado: StateFlow<FormularioGuiaState> = _estado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Nombre que quedara en la guia: el del usuario con la sesion abierta. */
    val tecnico: String get() = nombreDelTecnico()

    fun cambiarTrabajo(valor: String) = actualizar { it.copy(trabajoRealizado = valor, tocado = true) }

    fun cambiarRepuestos(valor: String) = actualizar { it.copy(repuestos = valor) }

    fun cambiarObservaciones(valor: String) = actualizar { it.copy(observaciones = valor) }

    fun cambiarRecibidoPor(valor: String) = actualizar { it.copy(recibidoPor = valor, tocado = true) }

    fun registrarUbicacion(latitud: Double, longitud: Double) {
        actualizar { it.copy(latitud = latitud, longitud = longitud) }
        _mensaje.value = "Ubicacion registrada"
    }

    fun agregarFoto(uri: String) = actualizar { it.copy(fotos = it.fotos + uri) }

    fun quitarFoto(uri: String) = actualizar { it.copy(fotos = it.fotos - uri) }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando) return
        if (!actual.esValido) {
            actualizar { it.copy(tocado = true) }
            _mensaje.value = "Revisa los campos marcados"
            return
        }
        val ticketActual = ticket.value
        val equipoId = ticketActual?.equipoId
        if (equipoId.isNullOrBlank()) {
            _mensaje.value = "El ticket no tiene equipo asociado"
            return
        }
        val direccion = ticketActual.direccion.trim()
        if (direccion.isBlank()) {
            _mensaje.value = "El ticket no tiene direccion registrada"
            return
        }
        val tecnico = nombreDelTecnico()
        if (tecnico.isBlank()) {
            _mensaje.value = "No hay sesion activa: vuelve a iniciar sesion"
            return
        }

        viewModelScope.launch {
            actualizar { it.copy(guardando = true) }
            // POST /api/v1/guias en ms-guias; el servidor asigna el numero correlativo
            capturarErrores {
                guiaRepository.crear(
                    ticketId = ticketId,
                    equipoId = equipoId,
                    direccion = direccion,
                    tecnico = tecnico,
                    trabajoRealizado = actual.trabajoRealizado.trim(),
                    repuestos = actual.repuestos.trim(),
                    observaciones = actual.observaciones.trim(),
                    recibidoPor = actual.recibidoPor.trim(),
                    latitud = actual.latitud,
                    longitud = actual.longitud,
                    urisDeFotos = actual.fotos
                )
            }
                .onSuccess {
                    limpiarGuardado()
                    actualizar { it.copy(guardando = false, guardado = true) }
                }
                .onFailure { error ->
                    actualizar { it.copy(guardando = false) }
                    _mensaje.value = ErrorDeRed.mensaje(error)
                }
        }
    }

    // ---------- Estado que sobrevive a que Android mate el proceso ----------

    private fun actualizar(cambio: (FormularioGuiaState) -> FormularioGuiaState) {
        val nuevo = cambio(_estado.value)
        _estado.value = nuevo
        respaldar(nuevo)
    }

    private fun respaldar(estado: FormularioGuiaState) {
        estadoGuardado[CLAVE_TRABAJO] = estado.trabajoRealizado
        estadoGuardado[CLAVE_REPUESTOS] = estado.repuestos
        estadoGuardado[CLAVE_OBSERVACIONES] = estado.observaciones
        estadoGuardado[CLAVE_RECIBIDO_POR] = estado.recibidoPor
        estadoGuardado[CLAVE_LATITUD] = estado.latitud
        estadoGuardado[CLAVE_LONGITUD] = estado.longitud
        estadoGuardado[CLAVE_FOTOS] = ArrayList(estado.fotos)
        estadoGuardado[CLAVE_TOCADO] = estado.tocado
    }

    private fun recuperar(): FormularioGuiaState = FormularioGuiaState(
        trabajoRealizado = estadoGuardado[CLAVE_TRABAJO] ?: "",
        repuestos = estadoGuardado[CLAVE_REPUESTOS] ?: "",
        observaciones = estadoGuardado[CLAVE_OBSERVACIONES] ?: "",
        recibidoPor = estadoGuardado[CLAVE_RECIBIDO_POR] ?: "",
        latitud = estadoGuardado[CLAVE_LATITUD],
        longitud = estadoGuardado[CLAVE_LONGITUD],
        fotos = estadoGuardado.get<ArrayList<String>>(CLAVE_FOTOS)?.toList() ?: emptyList(),
        tocado = estadoGuardado[CLAVE_TOCADO] ?: false
    )

    private fun limpiarGuardado() {
        listOf(
            CLAVE_TRABAJO, CLAVE_REPUESTOS, CLAVE_OBSERVACIONES, CLAVE_RECIBIDO_POR,
            CLAVE_LATITUD, CLAVE_LONGITUD, CLAVE_FOTOS, CLAVE_TOCADO
        ).forEach { estadoGuardado.remove<Any>(it) }
    }

    private fun nombreDelTecnico(): String {
        val usuario = sesionRepository.usuarioActual() ?: return ""
        return usuario.nombre.trim().ifBlank { usuario.correo.trim() }
    }

    companion object {
        private const val CLAVE_TRABAJO = "guia.trabajo"
        private const val CLAVE_REPUESTOS = "guia.repuestos"
        private const val CLAVE_OBSERVACIONES = "guia.observaciones"
        private const val CLAVE_RECIBIDO_POR = "guia.recibidoPor"
        private const val CLAVE_LATITUD = "guia.latitud"
        private const val CLAVE_LONGITUD = "guia.longitud"
        private const val CLAVE_FOTOS = "guia.fotos"
        private const val CLAVE_TOCADO = "guia.tocado"

        fun Factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                FormularioGuiaViewModel(
                    ticketId = ticketId,
                    estadoGuardado = createSavedStateHandle(),
                    ticketRepository = app.ticketRepository,
                    guiaRepository = app.guiaRepository,
                    sesionRepository = app.sesionRepository
                )
            }
        }
    }
}
