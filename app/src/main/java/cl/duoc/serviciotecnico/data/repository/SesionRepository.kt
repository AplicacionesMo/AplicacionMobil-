package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.remote.api.UsuariosApi
import cl.duoc.serviciotecnico.data.remote.dto.LoginRequestDto
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.UsuarioSesion

/**
 * Sesion del usuario.
 * El login se valida contra el microservicio ms-usuarios (clave cifrada con BCrypt en el servidor).
 * El usuario y su rol quedan guardados localmente para no pedir login cada vez que se abre la app.
 */
class SesionRepository(
    private val api: UsuariosApi,
    private val almacen: AlmacenSesion
) {
    fun haySesionActiva(): Boolean = almacen.leer() != null

    fun usuarioActual(): UsuarioSesion? = almacen.leer()

    fun rolActual(): Rol = almacen.leer()?.rol ?: Rol.TECNICO

    /** Lanza HttpException (401) si las credenciales son incorrectas, IOException si no hay red. */
    suspend fun iniciarSesion(correo: String, clave: String): UsuarioSesion {
        val respuesta = api.login(LoginRequestDto(correo.trim().lowercase(), clave))
        val usuario = UsuarioSesion(
            id = respuesta.id ?: 0L,
            nombre = respuesta.nombre.orEmpty(),
            correo = respuesta.correo.orEmpty(),
            rol = Rol.desde(respuesta.rol)
        )
        almacen.guardar(usuario)
        return usuario
    }

    fun cerrarSesion() {
        almacen.borrar()
    }

    companion object {
        /** Usuarios de prueba creados por ms-usuarios al iniciar por primera vez. */
        const val USUARIO_TECNICO = "tecnico@tacticaldev.cl"
        const val CLAVE_TECNICO = "tecnico123"
        const val USUARIO_COORDINADOR = "coordinador@tacticaldev.cl"
        const val CLAVE_COORDINADOR = "coord123"
    }
}
