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
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.validation.Validaciones
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

/** Maximo de fotos que se pueden adjuntar a un cambio de estado. */
const val MAX_FOTOS_CAMBIO = 5

/**
 * Estados que el tecnico puede elegir: los siete intermedios y Cerrado.
 * Cancelado no aparece porque solo lo puede hacer el coordinador.
 */
val estadosParaTecnico: List<EstadoTicket> =
    EstadoTicket.intermedios + EstadoTicket.CERRADO

/** Formulario de cambio de estado: estado nuevo, comentario y fotos. */
data class FormularioCambioEstado(
    val estadoSeleccionado: EstadoTicket? = null,
    val comentario: String = "",
    val fotos: List<String> = emptyList(),
    val latitud: Double? = null,
    val longitud: Double? = null,
    val errorEstado: String? = null,
    val errorComentario: String? = null,
    val errorUbicacion: String? = null,
    val enviando: Boolean = false
) {
    val puedeAgregarFotos: Boolean get() = fotos.size < MAX_FOTOS_CAMBIO
    val tieneUbicacion: Boolean get() = latitud != null && longitud != null
}

data class DetalleTicketUiState(
    val cargando: Boolean = true,
    val ticket: Ticket? = null,
    val equipo: Equipo? = null,
    val guias: List<Guia> = emptyList(),
    val historial: List<CambioEstado> = emptyList(),
    val formulario: FormularioCambioEstado = FormularioCambioEstado(),
    val mensaje: String? = null
) {
    /** Cerrado o cancelado = solo lectura. */
    val editable: Boolean get() = ticket?.editable == true
}

private data class EstadoLocal(
    val formulario: FormularioCambioEstado = FormularioCambioEstado(),
    val mensaje: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleTicketViewModel(
    private val ticketId: String,
    private val repository: TicketRepository,
    guiaRepository: GuiaRepository,
    equipoRepository: EquipoRepository
) : ViewModel() {

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
        datos.copy(formulario = l.formulario, mensaje = l.mensaje)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleTicketUiState()
    )

    // ---------- Eventos del formulario ----------

    fun seleccionarEstado(nuevo: EstadoTicket) = actualizarFormulario {
        it.copy(estadoSeleccionado = nuevo, errorEstado = null)
    }

    fun cambiarComentario(texto: String) = actualizarFormulario {
        it.copy(comentario = texto, errorComentario = null)
    }

    fun agregarFoto(uri: String) {
        val actual = local.value.formulario
        if (!actual.puedeAgregarFotos) {
            mostrar("Máximo $MAX_FOTOS_CAMBIO fotos por cambio de estado")
            return
        }
        actualizarFormulario { it.copy(fotos = it.fotos + uri) }
    }

    fun quitarFoto(uri: String) = actualizarFormulario { it.copy(fotos = it.fotos - uri) }

    fun ubicacionObtenida(latitud: Double, longitud: Double) = actualizarFormulario {
        it.copy(latitud = latitud, longitud = longitud, errorUbicacion = null)
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
        val errorComentario = Validaciones.largoMinimo(f.comentario, 5, "El comentario")
        val errorUbicacion =
            if (!f.tieneUbicacion) "Debes registrar tu ubicación para cambiar el estado" else null

        if (errorEstado != null || errorComentario != null || errorUbicacion != null) {
            actualizarFormulario {
                it.copy(
                    errorEstado = errorEstado,
                    errorComentario = errorComentario,
                    errorUbicacion = errorUbicacion
                )
            }
            return
        }
        val nuevo = f.estadoSeleccionado ?: return

        actualizarFormulario { it.copy(enviando = true) }
        viewModelScope.launch {
            // Regla de negocio: no se cierra un ticket sin al menos una guia emitida
            if (nuevo == EstadoTicket.CERRADO && !repository.tieneGuiasEmitidas(ticketId)) {
                actualizarFormulario {
                    it.copy(
                        enviando = false,
                        errorEstado = "Para cerrar el ticket debes emitir al menos una guía"
                    )
                }
                return@launch
            }
            repository.registrarCambioDeEstado(
                ticketId = ticketId,
                nuevoEstado = nuevo,
                comentario = f.comentario.trim(),
                urisDeFotos = f.fotos,
                latitud = f.latitud,
                longitud = f.longitud
            )
            // Formulario limpio para el siguiente cambio
            local.value = EstadoLocal(mensaje = "Estado cambiado a ${nuevo.etiqueta}")
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
        fun factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                DetalleTicketViewModel(
                    ticketId = ticketId,
                    repository = app.ticketRepository,
                    guiaRepository = app.guiaRepository,
                    equipoRepository = app.equipoRepository
                )
            }
        }
    }
}
