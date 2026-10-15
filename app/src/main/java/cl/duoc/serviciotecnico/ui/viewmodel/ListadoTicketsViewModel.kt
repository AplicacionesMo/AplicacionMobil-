package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ListadoTicketsViewModel(
    repository: TicketRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    // null = todos los estados
    private val _filtro = MutableStateFlow<EstadoTicket?>(null)
    val filtro: StateFlow<EstadoTicket?> = _filtro.asStateFlow()

    // Room vuelve a emitir cada vez que cambia la tabla, asi que la lista se actualiza sola
    val tickets: StateFlow<List<Ticket>> =
        combine(repository.observarTickets(), _filtro) { lista, estado ->
            if (estado == null) lista else lista.filter { it.estado == estado }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun cambiarFiltro(estado: EstadoTicket?) {
        _filtro.value = estado
    }

    fun cerrarSesion() {
        sesionRepository.cerrarSesion()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                ListadoTicketsViewModel(app.ticketRepository, app.sesionRepository)
            }
        }
    }
}