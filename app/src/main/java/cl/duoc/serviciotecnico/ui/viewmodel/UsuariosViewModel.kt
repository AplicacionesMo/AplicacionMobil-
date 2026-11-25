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
import cl.duoc.serviciotecnico.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuariosUiState(
    val usuarios: List<Usuario> = emptyList(),
    val cargando: Boolean = false,
    /** Id del usuario que se esta eliminando, para deshabilitar su boton. */
    val eliminandoId: Long? = null,
    val mensaje: String? = null,
    val permitido: Boolean = true
)

/** Administracion de usuarios: listar y eliminar (crear esta en CrearUsuarioViewModel). */
class UsuariosViewModel(
    private val repository: UsuarioRepository,
    rolActual: Rol
) : ViewModel() {

    private val _estado = MutableStateFlow(UsuariosUiState(permitido = rolActual.puedeGestionarUsuarios))
    val estado: StateFlow<UsuariosUiState> = _estado.asStateFlow()

    /** GET /api/v1/usuarios. La pantalla lo llama cada vez que se muestra. */
    fun cargar() {
        if (!_estado.value.permitido) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            capturarErrores { repository.listar() }
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, usuarios = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, mensaje = ErrorDeRed.mensaje(e)) } }
        }
    }

    /** DELETE /api/v1/usuarios/{id}. El servidor no deja eliminar al ultimo administrador. */
    fun eliminar(usuario: Usuario) {
        if (!_estado.value.permitido || _estado.value.eliminandoId != null) return
        viewModelScope.launch {
            _estado.update { it.copy(eliminandoId = usuario.id) }
            capturarErrores { repository.eliminar(usuario.id) }
                .onSuccess {
                    _estado.update {
                        it.copy(
                            eliminandoId = null,
                            usuarios = it.usuarios.filterNot { u -> u.id == usuario.id },
                            mensaje = "Usuario ${usuario.correo} eliminado"
                        )
                    }
                }
                .onFailure { e -> _estado.update { it.copy(eliminandoId = null, mensaje = ErrorDeRed.mensaje(e)) } }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ServicioTecnicoApp
                UsuariosViewModel(app.usuarioRepository, app.sesionRepository.rolActual())
            }
        }
    }
}
