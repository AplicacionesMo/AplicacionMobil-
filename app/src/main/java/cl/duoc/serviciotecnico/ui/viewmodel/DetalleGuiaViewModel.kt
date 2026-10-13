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
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class DetalleGuiaUiState(
    val guia: Guia? = null,
    val equipo: Equipo? = null,
    val fotos: List<FotoGuia> = emptyList(),
    val cargando: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleGuiaViewModel(
    guiaId: String,
    guiaRepository: GuiaRepository,
    equipoRepository: EquipoRepository
) : ViewModel() {

    val uiState: StateFlow<DetalleGuiaUiState> =
        guiaRepository.observarGuia(guiaId)
            .flatMapLatest { guia ->
                if (guia == null) {
                    flowOf(DetalleGuiaUiState(cargando = false))
                } else {
                    combine(
                        equipoRepository.observarEquipo(guia.equipoId),
                        guiaRepository.observarFotos(guiaId)
                    ) { equipo, fotos ->
                        DetalleGuiaUiState(
                            guia = guia,
                            equipo = equipo,
                            fotos = fotos,
                            cargando = false
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetalleGuiaUiState()
            )

    companion object {
        fun Factory(guiaId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                DetalleGuiaViewModel(
                    guiaId = guiaId,
                    guiaRepository = app.guiaRepository,
                    equipoRepository = app.equipoRepository
                )
            }
        }
    }
}
