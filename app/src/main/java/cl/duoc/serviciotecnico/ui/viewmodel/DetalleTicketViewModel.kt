package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleTicketUiState(
    val ticket: Ticket? = null,
    val equipo: Equipo? = null,
    val guias: List<Guia> = emptyList(),
    val cargando: Boolean = true
) {
    val editable: Boolean get() = ticket?.editable == true
    val puedeCerrarse: Boolean get() = editable && guias.isNotEmpty()
}

data class FormularioFinalizacionState(
    val texto: String = "",
    val error: String? = null,
    val tocado: Boolean = false,
    val guardando: Boolean = false
) {
    val esValido: Boolean get() = error == null && texto.isNotBlank()
    val errorVisible: String? get() = if (tocado) error else null
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleTicketViewModel(
    private val ticketId: String,
    private val ticketRepository: TicketRepository,
    private val equipoRepository: EquipoRepository,
    private val guiaRepository: GuiaRepository
) : ViewModel() {

    val uiState: StateFlow<DetalleTicketUiState> =
        ticketRepository.observarTicket(ticketId)
            .flatMapLatest { ticket ->
                if (ticket == null) {
                    flowOf(DetalleTicketUiState(cargando = false))
                } else {
                    combine(
                        equipoRepository.observarEquipo(ticket.equipoId),
                        guiaRepository.observarGuiasDeTicket(ticketId)
                    ) { equipo, guias ->
                        DetalleTicketUiState(
                            ticket = ticket,
                            equipo = equipo,
                            guias = guias,
                            cargando = false
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetalleTicketUiState()
            )

    private val _cierre = MutableStateFlow(FormularioFinalizacionState())
    val cierre: StateFlow<FormularioFinalizacionState> = _cierre.asStateFlow()

    private val _cancelacion = MutableStateFlow(FormularioFinalizacionState())
    val cancelacion: StateFlow<FormularioFinalizacionState> = _cancelacion.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _dialogoVisible = MutableStateFlow(DialogoFinalizacion.NINGUNO)
    val dialogoVisible: StateFlow<DialogoFinalizacion> = _dialogoVisible.asStateFlow()

    fun abrirDialogo(dialogo: DialogoFinalizacion) {
        _dialogoVisible.value = dialogo
    }

    fun cerrarDialogo() {
        _dialogoVisible.value = DialogoFinalizacion.NINGUNO
        _cierre.value = FormularioFinalizacionState()
        _cancelacion.value = FormularioFinalizacionState()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun cambiarSolucion(valor: String) {
        _cierre.update {
            it.copy(
                texto = valor,
                tocado = true,
                error = Validaciones.largoMinimo(valor, 5, "El comentario de solucion")
            )
        }
    }

    fun cambiarMotivo(valor: String) {
        _cancelacion.update {
            it.copy(
                texto = valor,
                tocado = true,
                error = Validaciones.largoMinimo(valor, 5, "El motivo de cancelacion")
            )
        }
    }

    fun cambiarEstado(nuevoEstado: EstadoTicket) {
        viewModelScope.launch {
            val ticket = ticketRepository.obtenerTicket(ticketId)
            when {
                ticket == null -> _mensaje.value = "Ticket no encontrado"
                ticket.estado.esFinal -> _mensaje.value = "El ticket ya esta finalizado"
                nuevoEstado.esFinal -> _mensaje.value = "Usa cerrar o cancelar"
                nuevoEstado == ticket.estado -> Unit
                else -> {
                    ticketRepository.cambiarEstado(ticketId, nuevoEstado)
                    _mensaje.value = "Estado actualizado a ${nuevoEstado.etiqueta}"
                }
            }
        }
    }

    fun confirmarCierre() {
        val estado = _cierre.value
        if (!estado.esValido) {
            _cierre.update { it.copy(tocado = true) }
            return
        }
        viewModelScope.launch {
            _cierre.update { it.copy(guardando = true) }
            if (!ticketRepository.tieneGuiasEmitidas(ticketId)) {
                _cierre.update { it.copy(guardando = false) }
                _mensaje.value = "Debes emitir al menos una guia antes de cerrar"
                return@launch
            }
            ticketRepository.cerrar(ticketId, estado.texto.trim())
            _cierre.update { it.copy(guardando = false) }
            _mensaje.value = "Ticket cerrado"
            cerrarDialogo()
        }
    }

    fun confirmarCancelacion() {
        val estado = _cancelacion.value
        if (!estado.esValido) {
            _cancelacion.update { it.copy(tocado = true) }
            return
        }
        viewModelScope.launch {
            _cancelacion.update { it.copy(guardando = true) }
            ticketRepository.cancelar(ticketId, estado.texto.trim())
            _cancelacion.update { it.copy(guardando = false) }
            _mensaje.value = "Ticket cancelado"
            cerrarDialogo()
        }
    }

    companion object {
        fun Factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                DetalleTicketViewModel(
                    ticketId = ticketId,
                    ticketRepository = app.ticketRepository,
                    equipoRepository = app.equipoRepository,
                    guiaRepository = app.guiaRepository
                )
            }
        }
    }
}

enum class DialogoFinalizacion {
    NINGUNO,
    CIERRE,
    CANCELACION
}
