package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class FormularioGuiaState(
    val direccion: String = "",
    val tecnico: String = "",
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
    val errorDireccion: String? get() = Validaciones.noVacio(direccion, "La direccion")
    val errorTecnico: String? get() = Validaciones.noVacio(tecnico, "El nombre del tecnico")
    val errorTrabajo: String? get() = Validaciones.largoMinimo(trabajoRealizado, 10, "El trabajo realizado")
    val errorRecibidoPor: String? get() = Validaciones.noVacio(recibidoPor, "Quien recibe el servicio")

    val esValido: Boolean
        get() = Validaciones.todoCorrecto(
            errorDireccion,
            errorTecnico,
            errorTrabajo,
            errorRecibidoPor
        )

    val tieneUbicacion: Boolean get() = latitud != null && longitud != null

    fun errorVisible(error: String?): String? = if (tocado) error else null
}

class FormularioGuiaViewModel(
    private val ticketId: String,
    ticketRepository: TicketRepository,
    private val guiaRepository: GuiaRepository
) : ViewModel() {

    val ticket: StateFlow<Ticket?> = ticketRepository.observarTicket(ticketId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _estado = MutableStateFlow(FormularioGuiaState())
    val estado: StateFlow<FormularioGuiaState> = _estado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var direccionHeredada = false

    fun heredarDireccion(direccion: String) {
        if (direccionHeredada || direccion.isBlank()) return
        direccionHeredada = true
        _estado.update { it.copy(direccion = direccion) }
    }

    fun cambiarDireccion(valor: String) = _estado.update { it.copy(direccion = valor, tocado = true) }

    fun cambiarTecnico(valor: String) = _estado.update { it.copy(tecnico = valor, tocado = true) }

    fun cambiarTrabajo(valor: String) = _estado.update { it.copy(trabajoRealizado = valor, tocado = true) }

    fun cambiarRepuestos(valor: String) = _estado.update { it.copy(repuestos = valor) }

    fun cambiarObservaciones(valor: String) = _estado.update { it.copy(observaciones = valor) }

    fun cambiarRecibidoPor(valor: String) = _estado.update { it.copy(recibidoPor = valor, tocado = true) }

    fun registrarUbicacion(latitud: Double, longitud: Double) {
        _estado.update { it.copy(latitud = latitud, longitud = longitud) }
        _mensaje.value = "Ubicacion registrada"
    }

    fun agregarFoto(uri: String) {
        _estado.update { it.copy(fotos = it.fotos + uri) }
    }

    fun quitarFoto(uri: String) {
        _estado.update { it.copy(fotos = it.fotos - uri) }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun guardar() {
        val actual = _estado.value
        if (!actual.esValido) {
            _estado.update { it.copy(tocado = true) }
            _mensaje.value = "Revisa los campos marcados"
            return
        }
        val equipoId = ticket.value?.equipoId
        if (equipoId.isNullOrBlank()) {
            _mensaje.value = "El ticket no tiene equipo asociado"
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            try {
                guiaRepository.crear(
                    ticketId = ticketId,
                    equipoId = equipoId,
                    direccion = actual.direccion.trim(),
                    fecha = LocalDateTime.now(),
                    tecnico = actual.tecnico.trim(),
                    trabajoRealizado = actual.trabajoRealizado.trim(),
                    repuestos = actual.repuestos.trim(),
                    observaciones = actual.observaciones.trim(),
                    recibidoPor = actual.recibidoPor.trim(),
                    latitud = actual.latitud,
                    longitud = actual.longitud,
                    urisDeFotos = actual.fotos
                )
                _estado.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false) }
                _mensaje.value = "No se pudo guardar la guia"
            }
        }
    }

    companion object {
        fun Factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                FormularioGuiaViewModel(
                    ticketId = ticketId,
                    ticketRepository = app.ticketRepository,
                    guiaRepository = app.guiaRepository
                )
            }
        }
    }
}
