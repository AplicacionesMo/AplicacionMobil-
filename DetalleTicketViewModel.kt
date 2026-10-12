
package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cl.duoc.serviciotecnico.data.repository.GuiaRepository



class DetalleTicketViewModel(
    private val repository: TicketRepository,
    private val guiaRepository: GuiaRepository
) : ViewModel() {


    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    fun cambiarEstado(
        ticketId: String,
        nuevoEstado: EstadoTicket
    ) {
        viewModelScope.launch {
            val ticket = repository.obtenerTicket(ticketId)

            if (ticket == null) {
                _mensaje.value = "Ticket no encontrado"
                return@launch
            }

            if (ticket.estado == EstadoTicket.CERRADO ||
                ticket.estado == EstadoTicket.CANCELADO
            ) {
                _mensaje.value = "El ticket ya está finalizado"
                return@launch
            }

            if (nuevoEstado == EstadoTicket.CERRADO ||
                nuevoEstado == EstadoTicket.CANCELADO
            ) {
                _mensaje.value = "Utiliza cerrar o cancelar"
                return@launch
            }

            repository.cambiarEstado(ticketId, nuevoEstado)
            _mensaje.value = "Estado actualizado correctamente"
        }
    }

    fun cerrarTicket(
        ticketId: String,
        solucion: String
    ) {
        viewModelScope.launch {
            val ticket = repository.obtenerTicket(ticketId)

            if (ticket == null) {
                _mensaje.value = "Ticket no encontrado"
                return@launch
            }

            if (ticket.estado == EstadoTicket.CERRADO ||
                ticket.estado == EstadoTicket.CANCELADO
            ) {
                _mensaje.value = "El ticket ya está finalizado"
                return@launch
            }

            if (solucion.isBlank()) {
                _mensaje.value = "Debes ingresar una solución"
                return@launch
            }

            if (!repository.tieneGuiasEmitidas(ticketId)) {
                _mensaje.value = "Debes registrar una guía antes de cerrar"
                return@launch
            }

            repository.cerrar(ticketId, solucion.trim())
            _mensaje.value = "Ticket cerrado correctamente"
        }
    }

    fun cancelarTicket(
        ticketId: String,
        motivo: String
    ) {
        viewModelScope.launch {
            val ticket = repository.obtenerTicket(ticketId)

            if (ticket == null) {
                _mensaje.value = "Ticket no encontrado"
                return@launch
            }

            if (ticket.estado == EstadoTicket.CERRADO ||
                ticket.estado == EstadoTicket.CANCELADO
            ) {
                _mensaje.value = "El ticket ya está finalizado"
                return@launch
            }

            if (motivo.isBlank()) {
                _mensaje.value = "Debes indicar un motivo"
                return@launch
            }

            repository.cancelar(ticketId, motivo.trim())
            _mensaje.value = "Ticket cancelado correctamente"
        }
    }

    fun observarTicket(ticketId: String) =
        repository.observarTicket(ticketId)


    fun observarGuias(ticketId: String) =
        guiaRepository.observarGuiasDeTicket(ticketId)


}
