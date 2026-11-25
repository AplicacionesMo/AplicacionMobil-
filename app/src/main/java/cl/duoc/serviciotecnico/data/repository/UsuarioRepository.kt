package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.remote.api.UsuariosApi
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioDto
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioRequestDto
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.Usuario
import retrofit2.HttpException

/**
 * Administracion de usuarios contra ms-usuarios.
 * En cada escritura se envia el rol de quien esta conectado (header X-Rol);
 * si no es administrador, el servidor responde 403.
 */
class UsuarioRepository(
    private val api: UsuariosApi,
    private val sesion: SesionRepository
) {
    suspend fun listar(): List<Usuario> = api.listar().map { it.aDominio() }

    suspend fun crear(nombre: String, correo: String, clave: String, rol: Rol): Usuario {
        val respuesta = api.crear(
            request = UsuarioRequestDto(
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                clave = clave,
                rol = rol.name
            ),
            rol = sesion.rolActual().name
        )
        return respuesta.aDominio()
    }

    suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id, sesion.rolActual().name)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }

    private fun UsuarioDto.aDominio() = Usuario(
        id = id ?: 0L,
        nombre = nombre.orEmpty(),
        correo = correo.orEmpty(),
        rol = Rol.desde(rol),
        activo = activo ?: true
    )
}
