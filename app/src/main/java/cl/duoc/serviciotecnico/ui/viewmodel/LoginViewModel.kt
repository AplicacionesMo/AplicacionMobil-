package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CampoLogin { CORREO, CLAVE }

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: Map<CampoLogin, String> = emptyMap(),
    val camposTocados: Set<CampoLogin> = emptySet(),
    val intentoIngresar: Boolean = false,
    val ingresando: Boolean = false,
    val ingresado: Boolean = false,
    val mensaje: String? = null
) {
    /** Error visible de un campo: solo si el usuario ya lo toco o intento ingresar. */
    fun errorVisible(campo: CampoLogin): String? =
        if (intentoIngresar || campo in camposTocados) errores[campo] else null

    val formularioValido: Boolean get() = errores.isEmpty()
}

class LoginViewModel(
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    init {
        recalcularErrores()
    }

    fun cambiarCorreo(valor: String) {
        _estado.update {
            it.copy(correo = valor, camposTocados = it.camposTocados + CampoLogin.CORREO, mensaje = null)
        }
        recalcularErrores()
    }

    fun cambiarClave(valor: String) {
        _estado.update {
            it.copy(clave = valor, camposTocados = it.camposTocados + CampoLogin.CLAVE, mensaje = null)
        }
        recalcularErrores()
    }

    // Toda la validacion vive aqui, nada en el composable
    private fun recalcularErrores() {
        val s = _estado.value
        val errores = buildMap {
            Validaciones.correoValido(s.correo)?.let { put(CampoLogin.CORREO, it) }
            Validaciones.largoMinimo(s.clave, 6, "La contraseña")?.let { put(CampoLogin.CLAVE, it) }
        }
        _estado.update { it.copy(errores = errores) }
    }

    fun ingresar() {
        _estado.update { it.copy(intentoIngresar = true) }
        val s = _estado.value
        if (!s.formularioValido || s.ingresando) return

        _estado.update { it.copy(ingresando = true, mensaje = null) }
        viewModelScope.launch {
            delay(600) // simula la espera de un servidor real
            val correcto = sesionRepository.iniciarSesion(s.correo, s.clave)
            _estado.update {
                if (correcto) {
                    it.copy(ingresando = false, ingresado = true)
                } else {
                    it.copy(ingresando = false, mensaje = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                LoginViewModel(app.sesionRepository)
            }
        }
    }
}