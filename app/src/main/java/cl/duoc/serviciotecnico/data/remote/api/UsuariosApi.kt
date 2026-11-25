package cl.duoc.serviciotecnico.data.remote.api

import cl.duoc.serviciotecnico.data.remote.dto.LoginRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioDto
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/** Microservicio propio ms-usuarios (puerto 8081). */
interface UsuariosApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): UsuarioDto

    @GET("api/v1/usuarios")
    suspend fun listar(): List<UsuarioDto>

    /** Solo el administrador: el servidor revisa el header X-Rol. */
    @POST("api/v1/usuarios")
    suspend fun crear(@Body request: UsuarioRequestDto, @Header("X-Rol") rol: String): UsuarioDto

    /** Solo el administrador. 204 No Content si se elimino. */
    @DELETE("api/v1/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long, @Header("X-Rol") rol: String): Response<Unit>
}
