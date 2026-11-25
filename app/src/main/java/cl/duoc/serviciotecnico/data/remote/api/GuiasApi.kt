package cl.duoc.serviciotecnico.data.remote.api

import cl.duoc.serviciotecnico.data.remote.dto.GuiaDto
import cl.duoc.serviciotecnico.data.remote.dto.GuiaRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Microservicio propio ms-guias (puerto 8083). */
interface GuiasApi {

    @GET("api/v1/guias")
    suspend fun listarDeTicket(@Query("ticketId") ticketId: String): List<GuiaDto>

    @GET("api/v1/guias/{id}")
    suspend fun obtener(@Path("id") id: String): GuiaDto

    @POST("api/v1/guias")
    /** Solo el tecnico: el servidor revisa el header X-Rol. */
    suspend fun crear(@Body request: GuiaRequestDto, @Header("X-Rol") rol: String): GuiaDto
}
