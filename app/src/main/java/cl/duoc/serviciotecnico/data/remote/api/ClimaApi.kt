package cl.duoc.serviciotecnico.data.remote.api

import cl.duoc.serviciotecnico.data.remote.dto.ClimaRespuestaDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API externa Open-Meteo (https://open-meteo.com). Gratuita y sin llave.
 * Ejemplo: /v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,...
 */
interface ClimaApi {

    @GET("v1/forecast")
    suspend fun climaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m",
        @Query("timezone") zonaHoraria: String = "auto"
    ): ClimaRespuestaDto
}
