package cl.duoc.serviciotecnico.data.remote

import cl.duoc.serviciotecnico.data.remote.dto.ErrorDto
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** Traduce cualquier falla de red a un mensaje que el usuario entienda. */
object ErrorDeRed {

    const val SIN_CONEXION =
        "No hay conexión con el servidor. Revisa que los microservicios estén encendidos."

    private val gson = Gson()

    fun mensaje(error: Throwable): String = when (error) {
        // El microservicio respondio con un codigo 4xx/5xx: se usa su "mensaje"
        is HttpException -> leerMensajeDelServidor(error) ?: mensajePorCodigo(error.code())
        // No se pudo llegar al servidor (apagado, sin red, IP incorrecta)
        is IOException -> SIN_CONEXION
        else -> error.message ?: "Ocurrió un error inesperado"
    }

    fun mensajePorCodigo(codigo: Int): String = when (codigo) {
        400 -> "Los datos enviados no son válidos"
        401 -> "Correo o contraseña incorrectos"
        403 -> "No tienes permiso para esta acción"
        404 -> "No se encontró el recurso"
        409 -> "La operación no está permitida en el estado actual"
        in 500..599 -> "El servidor tuvo un problema. Inténtalo nuevamente"
        else -> "Error de comunicación ($codigo)"
    }

    private fun leerMensajeDelServidor(error: HttpException): String? = try {
        val cuerpo = error.response()?.errorBody()?.string()
        if (cuerpo.isNullOrBlank()) null else gson.fromJson(cuerpo, ErrorDto::class.java)?.mensaje
    } catch (e: Exception) {
        null
    }
}

/**
 * Ejecuta una llamada y devuelve Result en vez de lanzar la excepcion.
 * La cancelacion de la corrutina se vuelve a lanzar: no es un error, es el ciclo de vida.
 */
inline fun <T> capturarErrores(bloque: () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
