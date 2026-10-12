
package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class FormularioGuiaViewModel(
    private val repository: GuiaRepository
) : ViewModel() {

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado = _guardado.asStateFlow()

    fun guardarGuia(
        ticketId: String,
        equipoId: String,
        direccion: String,
        tecnico: String,
        trabajoRealizado: String,
        repuestos: String,
        observaciones: String,
        recibidoPor: String
    ) {
        if (ticketId.isBlank() || equipoId.isBlank()) {
            _mensaje.value = "Falta seleccionar ticket o equipo"
            return
        }

        if (direccion.isBlank()) {
            _mensaje.value = "Debes ingresar la dirección"
            return
        }

        if (tecnico.isBlank()) {
            _mensaje.value = "Debes ingresar el técnico"
            return
        }

        if (trabajoRealizado.isBlank()) {
            _mensaje.value = "Debes indicar el trabajo realizado"
            return
        }

        if (recibidoPor.isBlank()) {
            _mensaje.value = "Debes indicar quién recibió"
            return
        }

        _guardado.value = false

        viewModelScope.launch {
            try {
                repository.crear(
                    ticketId = ticketId,
                    equipoId = equipoId,
                    direccion = direccion.trim(),
                    fecha = LocalDateTime.now(),
                    tecnico = tecnico.trim(),
                    trabajoRealizado = trabajoRealizado.trim(),
                    repuestos = repuestos.trim(),
                    observaciones = observaciones.trim(),
                    recibidoPor = recibidoPor.trim()
                )

                _mensaje.value = "Guía guardada correctamente"
                _guardado.value = true

            } catch (e: Exception) {
                _mensaje.value = "Error al guardar la guía"
                _guardado.value = false
            }
        }
    }
}
