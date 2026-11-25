package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Formulario de cambio de estado: solo el estado nuevo.
 * Las fotos, la ubicacion y el detalle del trabajo se registran en la guia de servicio,
 * que es el documento que respalda la visita; pedirlos dos veces no aportaba nada.
 */
data class FormularioCambioEstado(
    val estadoSeleccionado: EstadoTicket? = null,
    val errorEstado: String? = null,
    val enviando: Boolean = false
)

data class DetalleTicketUiState(
    val cargando: Boolean = true,
    val ticket: Ticket? = null,
    val equipo: Equipo? = null,
    val guias: List<Guia> = emptyList(),
    val historial: List<CambioEstado> = emptyList(),
    val formulario: FormularioCambioEstado = FormularioCambioEstado(),
    val mensaje: String? = null,
    val rol: Rol = Rol.TECNICO,
    val sincronizando: Boolean = false,
    val eliminando: Boolean = false,
    val eliminado: Boolean = false
) {
    /** Cerrado o cancelado = solo lectura. */
    val editable: Boolean get() = ticket?.editable == true

    /** El tecnico no ve "Cancelado" en la lista; el coordinador si. */
    val estadosDisponibles: List<EstadoTicket>
        get() = rol.estadosPermitidos.filter { it != ticket?.estado }

    val puedeEliminar: Boolean get() = rol.puedeEliminarTickets && ticket != null
}

private data class EstadoLocal(
    val formulario: FormularioCambioEstado = FormularioCambioEstado(),
    val mensaje: String? = null,
    val sincronizando: Boolean = false,
    val eliminando: Boolean = false,
    val eliminado: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleTicketViewModel(
    private val ticketId: String,
    private val repository: TicketRepository,
    private val guiaRepository: GuiaRepository,
    equipoRepository: EquipoRepository,
    sesionRepository: SesionRepository
) : ViewModel() {

    private val rol: Rol = sesionRepository.rolActual()

    private val local = MutableStateFlow(EstadoLocal())

    private val ticketFlow = repository.observarTicket(ticketId)

    private val equipoFlow = ticketFlow.flatMapLatest { ticket ->
        if (ticket == null) flowOf(null) else equipoRepository.observarEquipo(ticket.equipoId)
    }

    private val datosFlow = combine(
        ticketFlow,
        equipoFlow,
        guiaRepository.observarGuiasDeTicket(ticketId),
        repository.observarHistorial(ticketId)
    ) { ticket, equipo, guias, historial ->
        DetalleTicketUiState(
            cargando = false,
            ticket = ticket,
            equipo = equipo,
            guias = guias,
            historial = historial
        )
    }

    val estado: StateFlow<DetalleTicketUiState> = combine(datosFlow, local) { datos, l ->
        datos.copy(
            formulario = l.formulario,
            mensaje = l.mensaje,
            rol = rol,
            sincronizando = l.sincronizando,
            eliminando = l.eliminando,
            eliminado = l.eliminado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleTicketUiState()
    )

    init {
        sincronizar()
    }

    /** GET del ticket, su historial (ms-tickets) y sus guias (ms-guias). */
    fun sincronizar() {
        if (local.value.sincronizando) return
        viewModelScope.launch {
            local.update { it.copy(sincronizando = true) }
            val resultado = capturarErrores {
                repository.sincronizarTicket(ticketId)
                guiaRepository.sincronizarGuiasDeTicket(ticketId)
            }
            local.update {
                it.copy(
                    sincronizando = false,
                    mensaje = resultado.exceptionOrNull()?.let { e -> ErrorDeRed.mensaje(e) } ?: it.mensaje
                )
            }
        }
    }

    // ---------- Eventos del formulario ----------

    fun seleccionarEstado(nuevo: EstadoTicket) = actualizarFormulario {
        it.copy(estadoSeleccionado = nuevo, errorEstado = null)
    }

    // ---------- Envio: aqui vive toda la validacion ----------

    fun enviarCambioDeEstado() {
        val f = local.value.formulario
        if (f.enviando) return
        val ticket = estado.value.ticket ?: return

        val errorEstado = when {
            f.estadoSeleccionado == null -> "Selecciona el nuevo estado"
            f.estadoSeleccionado == ticket.estado -> "El ticket ya está en ese estado"
            else -> null
        }
        if (errorEstado != null) {
            actualizarFormulario { it.copy(errorEstado = errorEstado) }
            return
        }
        val nuevo = f.estadoSeleccionado ?: return

        actualizarFormulario { it.copy(enviando = true) }
        viewModelScope.launch {
            // Regla de negocio: no se finaliza un ticket sin al menos una guia emitida.
            // Abierto, En transito y Cancelado se alcanzan sin guia.
            if (nuevo.exigeGuia && !repository.tieneGuiasEmitidas(ticketId)) {
                actualizarFormulario {
                    it.copy(enviando = false, errorEstado = MENSAJE_SIN_GUIA)
                }
                mostrar(MENSAJE_SIN_GUIA)
                return@launch
            }
            // PATCH /api/v1/tickets/{id}/estado en ms-tickets (el servidor vuelve a validar todo)
            capturarErrores {
                repository.registrarCambioDeEstado(
                    ticketId = ticketId,
                    nuevoEstado = nuevo,
                    rol = rol
                )
            }
                .onSuccess {
                    // Formulario limpio para el siguiente cambio
                    local.update { EstadoLocal(mensaje = "Estado cambiado a ${nuevo.etiqueta}") }
                }
                .onFailure { e ->
                    actualizarFormulario { it.copy(enviando = false) }
                    mostrar(ErrorDeRed.mensaje(e))
                }
        }
    }

    // ---------- Eliminar (solo coordinador) ----------

    /** DELETE /api/v1/tickets/{id}. El servidor rechaza si el ticket tiene guias. */
    fun eliminarTicket() {
        if (!rol.puedeEliminarTickets || local.value.eliminando) return
        viewModelScope.launch {
            local.update { it.copy(eliminando = true) }
            capturarErrores { repository.eliminar(ticketId, rol) }
                .onSuccess { local.update { it.copy(eliminando = false, eliminado = true) } }
                .onFailure { e -> local.update { it.copy(eliminando = false, mensaje = ErrorDeRed.mensaje(e)) } }
        }
    }

    // ---------- Mensajes ----------

    private fun actualizarFormulario(cambio: (FormularioCambioEstado) -> FormularioCambioEstado) {
        local.update { it.copy(formulario = cambio(it.formulario)) }
    }

    private fun mostrar(texto: String) {
        local.update { it.copy(mensaje = texto) }
    }

    fun mensajeMostrado() {
        local.update { it.copy(mensaje = null) }
    }

    companion object {
        const val MENSAJE_SIN_GUIA = "Debes crear una guía"

        fun factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                DetalleTicketViewModel(
                    ticketId = ticketId,
                    repository = app.ticketRepository,
                    guiaRepository = app.guiaRepository,
                    equipoRepository = app.equipoRepository,
                    sesionRepository = app.sesionRepository
                )
            }
        }
    }
}
