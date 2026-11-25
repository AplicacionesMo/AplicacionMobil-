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

/**
 * Formulario de cambio de estado: solo el estado nuevo.
 * Las fotos, la ubicacion y el detalle del trabajo se registran en la guia de servicio,
 * que es el documento que respalda la visita; pedirlos dos veces no aportaba nada.
 */
data class FormularioCambioEstado(
    val estadoSeleccionado: EstadoTicket? = null,
    val motivo: String = "",
    val errorEstado: String? = null,
    val errorMotivo: String? = null,
    val enviando: Boolean = false
) {
    /** Cancelar exige explicar por que: queda como motivo en el ticket y en el historial. */
    val pideMotivo: Boolean get() = estadoSeleccionado == EstadoTicket.CANCELADO
}

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
    val eliminado: Boolean = false,
    /** El cambio de estado se guardo: la pantalla se cierra y vuelve al listado. */
    val cambioAplicado: Boolean = false
) {
    /** Finalizado o cancelado = solo lectura. */
    val editable: Boolean get() = ticket?.editable == true

    /** Un ticket ya cerrado solo puede volver a Abierto. */
    val reabriendo: Boolean get() = ticket?.estado?.esFinal == true

    /** El tecnico no ve "Cancelado" en la lista; el coordinador si. */
    val estadosDisponibles: List<EstadoTicket>
        get() = if (reabriendo) {
            listOf(EstadoTicket.ABIERTO)
        } else {
            rol.estadosPermitidos.filter { it != ticket?.estado }
        }

    /** Coordinador y administrador pueden devolver a Abierto un ticket cerrado. */
    val puedeReabrir: Boolean get() = reabriendo && rol.puedeReabrirTickets

    /** Se pide motivo al cancelar y tambien al reabrir: ambas son excepcionales. */
    val exigeMotivo: Boolean get() = formulario.pideMotivo || reabriendo

    val puedeEliminar: Boolean get() = rol.puedeEliminarTickets && ticket != null

    /** Solo el tecnico emite guias; el coordinador y el admin no. */
    val puedeCrearGuias: Boolean get() = rol.puedeCrearGuias
}

private data class EstadoLocal(
    val formulario: FormularioCambioEstado = FormularioCambioEstado(),
    val mensaje: String? = null,
    val sincronizando: Boolean = false,
    val eliminando: Boolean = false,
    val eliminado: Boolean = false,
    val cambioAplicado: Boolean = false
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
            eliminado = l.eliminado,
            cambioAplicado = l.cambioAplicado
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
        // Al salir de Cancelado el motivo se descarta, salvo que se este reabriendo:
        // ahi el motivo es obligatorio igual
        val conservar = nuevo == EstadoTicket.CANCELADO || estado.value.reabriendo
        it.copy(
            estadoSeleccionado = nuevo,
            motivo = if (conservar) it.motivo else "",
            errorEstado = null,
            errorMotivo = null
        )
    }

    fun cambiarMotivo(texto: String) = actualizarFormulario {
        it.copy(motivo = texto, errorMotivo = null)
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
        val reabriendo = ticket.estado.esFinal
        val errorMotivo = when {
            reabriendo -> Validaciones.largoMinimo(f.motivo, 10, "El motivo de la reapertura")
            f.pideMotivo -> Validaciones.largoMinimo(f.motivo, 10, "El motivo de la cancelacion")
            else -> null
        }

        if (errorEstado != null || errorMotivo != null) {
            actualizarFormulario { it.copy(errorEstado = errorEstado, errorMotivo = errorMotivo) }
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
                    rol = rol,
                    // ms-tickets lo guarda como motivoCancelacion
                    comentario = f.motivo.trim()
                )
            }
                .onSuccess {
                    // Se guardo: la pantalla se cierra y vuelve al listado
                    local.update {
                        EstadoLocal(
                            mensaje = if (reabriendo) {
                                "Ticket reabierto"
                            } else {
                                "Estado cambiado a ${nuevo.etiqueta}"
                            },
                            cambioAplicado = true
                        )
                    }
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
