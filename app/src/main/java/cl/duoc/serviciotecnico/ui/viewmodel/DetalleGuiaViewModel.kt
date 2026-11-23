package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.FotoGuia
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleGuiaUiState(
    val cargando: Boolean = true,
    val guia: Guia? = null,
    val equipo: Equipo? = null,
    val fotos: List<FotoGuia> = emptyList(),
    val mensaje: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleGuiaViewModel(
    private val guiaId: String,
    private val repository: GuiaRepository,
    equipoRepository: EquipoRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)

    private val guiaFlow = repository.observarGuia(guiaId)

    private val equipoFlow = guiaFlow.flatMapLatest { guia ->
        if (guia == null) flowOf(null) else equipoRepository.observarEquipo(guia.equipoId)
    }

    val estado: StateFlow<DetalleGuiaUiState> = combine(
        guiaFlow,
        equipoFlow,
        repository.observarFotos(guiaId),
        mensaje
    ) { guia, equipo, fotos, texto ->
        DetalleGuiaUiState(cargando = false, guia = guia, equipo = equipo, fotos = fotos, mensaje = texto)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleGuiaUiState()
    )

    init {
        sincronizar()
    }

    /** GET /api/v1/guias/{id} en ms-guias; si falla se muestra lo guardado. */
    fun sincronizar() {
        viewModelScope.launch {
            capturarErrores { repository.sincronizarGuia(guiaId) }
                .onFailure { mensaje.value = ErrorDeRed.mensaje(it) }
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }

    companion object {
        fun factory(guiaId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                DetalleGuiaViewModel(guiaId, app.guiaRepository, app.equipoRepository)
            }
        }
    }
}
