package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.remote.aDominio
import cl.duoc.serviciotecnico.data.remote.api.ClimaApi
import cl.duoc.serviciotecnico.domain.model.ClimaActual

/**
 * API externa. A diferencia de los microservicios, no se guarda en Room ni se mezcla
 * con los tickets: solo se consulta y se muestra.
 */
class ClimaRepository(
    private val api: ClimaApi
) {
    suspend fun climaActual(latitud: Double, longitud: Double): ClimaActual =
        api.climaActual(latitud = latitud, longitud = longitud).aDominio()
}
