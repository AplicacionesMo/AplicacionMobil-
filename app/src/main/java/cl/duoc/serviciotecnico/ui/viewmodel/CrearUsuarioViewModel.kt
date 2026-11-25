package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.data.repository.UsuarioRepository
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CampoUsuario { NOMBRE, CORREO, CLAVE, CONFIRMACION }

data class CrearUsuarioUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val rol: Rol = Rol.TECNICO,
    val errores: Map<CampoUsuario, String> = emptyMap(),
    val camposTocados: Set<CampoUsuario> = emptySet(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false,
    val creado: Boolean = false,
    val mensaje: String? = null,
    /** Si quien esta conectado no es administrador, la pantalla queda bloqueada. */
    val permitido: Boolean = true
) {
    fun errorVisible(campo: CampoUsuario): String? =
        if (intentoGuardar || campo in camposTocados) errores[campo] else null

    val formularioValido: Boolean get() = errores.isEmpty()
}

/** Formulario para que el administrador cree usuarios en ms-usuarios. */
class CrearUsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    rolActual: Rol
) : ViewModel() {

    private val _estado = MutableStateFlow(
        CrearUsuarioUiState(permitido = rolActual.puedeGestionarUsuarios)
    )
    val estado: StateFlow<CrearUsuarioUiState> = _estado.asStateFlow()

    init {
        recalcularErrores()
    }

    fun cambiarCampo(campo: CampoUsuario, valor: String) {
        _estado.update {
            val conValor = when (campo) {
                CampoUsuario.NOMBRE -> it.copy(nombre = valor)
                CampoUsuario.CORREO -> it.copy(correo = valor)
                CampoUsuario.CLAVE -> it.copy(clave = valor)
                CampoUsuario.CONFIRMACION -> it.copy(confirmacion = valor)
            }
            conValor.copy(camposTocados = it.camposTocados + campo, mensaje = null)
        }
        recalcularErrores()
    }

    fun cambiarRol(rol: Rol) {
        _estado.update { it.copy(rol = rol) }
    }

    // Las mismas reglas que valida ms-usuarios, para avisar antes de enviar
    private fun recalcularErrores() {
        val s = _estado.value
        val errores = buildMap {
            Validaciones.largoMinimo(s.nombre, 3, "El nombre")?.let { put(CampoUsuario.NOMBRE, it) }
            Validaciones.correoValido(s.correo)?.let { put(CampoUsuario.CORREO, it) }
            Validaciones.largoMinimo(s.clave, 6, "La contraseña")?.let { put(CampoUsuario.CLAVE, it) }
            if (s.confirmacion != s.clave) put(CampoUsuario.CONFIRMACION, "Las contraseñas no coinciden")
        }
        _estado.update { it.copy(errores = errores) }
    }

    fun guardar() {
        _estado.update { it.copy(intentoGuardar = true) }
        val s = _estado.value
        if (!s.permitido || s.guardando) return
        if (!s.formularioValido) {
            _estado.update { it.copy(mensaje = "Revisa los campos marcados") }
            return
        }

        _estado.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            // POST /api/v1/usuarios en ms-usuarios, con el header X-Rol
            capturarErrores { usuarioRepository.crear(s.nombre, s.correo, s.clave, s.rol) }
                .onSuccess { usuario ->
                    _estado.update {
                        it.copy(guardando = false, creado = true, mensaje = "Usuario ${usuario.correo} creado")
                    }
                }
                .onFailure { error ->
                    _estado.update { it.copy(guardando = false, mensaje = ErrorDeRed.mensaje(error)) }
                }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                CrearUsuarioViewModel(app.usuarioRepository, app.sesionRepository.rolActual())
            }
        }
    }
}
