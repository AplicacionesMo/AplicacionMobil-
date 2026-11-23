package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.ClimaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.ClimaActual
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.UsuarioSesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la tarjeta de clima (API externa). */
sealed interface EstadoClima {
    data object Cargando : EstadoClima
    data class Listo(val clima: ClimaActual) : EstadoClima
    data class Error(val mensaje: String) : EstadoClima
}

/** Coordenadas de la oficina en Santiago, donde se concentra la operacion. */
const val LATITUD_SANTIAGO = -33.4489
const val LONGITUD_SANTIAGO = -70.6693

class ListadoTicketsViewModel(
    private val repository: TicketRepository,
    private val sesionRepository: SesionRepository,
    private val climaRepository: ClimaRepository
) : ViewModel() {

    // null = todos los estados
    private val _filtro = MutableStateFlow<EstadoTicket?>(null)
    val filtro: StateFlow<EstadoTicket?> = _filtro.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _clima = MutableStateFlow<EstadoClima>(EstadoClima.Cargando)
    val clima: StateFlow<EstadoClima> = _clima.asStateFlow()

    val usuario: UsuarioSesion? = sesionRepository.usuarioActual()

    // Room vuelve a emitir cada vez que cambia la tabla, asi que la lista se actualiza sola
    val tickets: StateFlow<List<Ticket>> =
        combine(repository.observarTickets(), _filtro) { lista, estado ->
            if (estado == null) lista else lista.filter { it.estado == estado }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        refrescar()
        cargarClima()
    }

    /** GET /api/v1/tickets: trae los tickets del microservicio y actualiza la cache. */
    fun refrescar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            capturarErrores { repository.sincronizar() }
                .onSuccess { _error.value = null }
                .onFailure { e -> _error.value = "${ErrorDeRed.mensaje(e)} Se muestran los datos guardados." }
            _sincronizando.value = false
        }
    }

    /** API externa Open-Meteo: clima actual en Santiago. */
    fun cargarClima() {
        viewModelScope.launch {
            _clima.value = EstadoClima.Cargando
            _clima.value = capturarErrores { climaRepository.climaActual(LATITUD_SANTIAGO, LONGITUD_SANTIAGO) }
                .fold(
                    onSuccess = { EstadoClima.Listo(it) },
                    onFailure = { EstadoClima.Error("No se pudo obtener el clima") }
                )
        }
    }

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
                ListadoTicketsViewModel(app.ticketRepository, app.sesionRepository, app.climaRepository)
            }
        }
    }
}
