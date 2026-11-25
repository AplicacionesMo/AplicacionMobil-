package cl.duoc.serviciotecnico.data.remote.api

import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoDto
import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Microservicio propio ms-tickets (puerto 8082).
 * Retrofit genera la implementacion: cada anotacion es el verbo HTTP y la ruta.
 */
interface TicketsApi {

    /** El servidor filtra: el tecnico solo recibe los tickets que le asignaron. */
    @GET("api/v1/tickets")
    suspend fun listar(
        @Header("X-Rol") rol: String,
        @Header("X-Usuario-Id") usuarioId: Long
    ): List<TicketDto>

    @GET("api/v1/tickets/{id}")
    suspend fun obtener(
        @Path("id") id: String,
        @Header("X-Rol") rol: String,
        @Header("X-Usuario-Id") usuarioId: Long
    ): TicketDto

    /** Solo coordinador o administrador: el servidor revisa el header X-Rol. */
    @POST("api/v1/tickets")
    suspend fun crear(@Body request: TicketRequestDto, @Header("X-Rol") rol: String): TicketDto

    @PUT("api/v1/tickets/{id}")
    suspend fun actualizar(
        @Path("id") id: String,
        @Body request: TicketRequestDto,
        @Header("X-Rol") rol: String,
        @Header("X-Usuario-Id") usuarioId: Long
    ): TicketDto

    @PATCH("api/v1/tickets/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: String,
        @Body request: CambioEstadoRequestDto,
        @Header("X-Rol") rol: String,
        @Header("X-Usuario-Id") usuarioId: Long
    ): TicketDto

    /** 204 No Content: se usa Response para revisar el codigo sin esperar cuerpo. */
    @DELETE("api/v1/tickets/{id}")
    suspend fun eliminar(@Path("id") id: String, @Header("X-Rol") rol: String): Response<Unit>

    @GET("api/v1/tickets/{id}/historial")
    suspend fun historial(@Path("id") id: String): List<CambioEstadoDto>
}
