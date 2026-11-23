package cl.duoc.serviciotecnico.data.remote.api

import cl.duoc.serviciotecnico.data.remote.dto.LoginRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Microservicio propio ms-usuarios (puerto 8081). */
interface UsuariosApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): UsuarioDto
}
