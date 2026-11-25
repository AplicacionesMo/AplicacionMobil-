package cl.duoc.serviciotecnico.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * DTO = objetos que viajan por la red, con la misma forma que el JSON.
 * Las respuestas son nullable porque Gson no garantiza que el servidor envie cada campo;
 * la conversion a entidad (Mapeos.kt) decide los valores por defecto.
 */

// ---------- ms-usuarios ----------

data class LoginRequestDto(
    val correo: String,
    val clave: String
)

/** Para crear un usuario nuevo (POST /api/v1/usuarios). */
data class UsuarioRequestDto(
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String
)

data class UsuarioDto(
    val id: Long?,
    val nombre: String?,
    val correo: String?,
    val rol: String?,
    val activo: Boolean?
)

// ---------- ms-tickets ----------

data class EquipoRequestDto(
    val nombre: String,
    val modelo: String,
    val serie: String,
    val propiedad: String
)

data class TicketRequestDto(
    val titulo: String,
    val descripcion: String,
    val cliente: String,
    val direccion: String,
    val prioridad: String,
    val equipo: EquipoRequestDto,
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null
)

data class CambioEstadoRequestDto(
    val estadoNuevo: String,
    val comentario: String,
    val urisDeFotos: List<String>,
    val latitud: Double?,
    val longitud: Double?
)

data class EquipoDto(
    val id: String?,
    val nombre: String?,
    val modelo: String?,
    val serie: String?,
    val propiedad: String?
)

data class TicketDto(
    val id: String?,
    val numero: Int?,
    val titulo: String?,
    val descripcion: String?,
    val cliente: String?,
    val direccion: String?,
    val prioridad: String?,
    val estado: String?,
    val equipo: EquipoDto?,
    val fechaCreacion: String?,
    val fechaCierre: String?,
    val solucion: String?,
    val motivoCancelacion: String?,
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null
)

data class CambioEstadoDto(
    val id: String?,
    val ticketId: String?,
    val estadoAnterior: String?,
    val estadoNuevo: String?,
    val comentario: String?,
    val urisDeFotos: List<String>?,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: String?
)

// ---------- ms-guias ----------

data class GuiaRequestDto(
    val ticketId: String,
    val equipoId: String,
    val direccion: String,
    val tecnico: String,
    val trabajoRealizado: String,
    val repuestos: String,
    val observaciones: String,
    val recibidoPor: String,
    val latitud: Double?,
    val longitud: Double?,
    val urisDeFotos: List<String>
)

data class GuiaDto(
    val id: String?,
    val numero: Int?,
    val ticketId: String?,
    val equipoId: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: String?,
    val tecnico: String?,
    val trabajoRealizado: String?,
    val repuestos: String?,
    val observaciones: String?,
    val recibidoPor: String?,
    val urisDeFotos: List<String>?
)

// ---------- Errores de los microservicios: {"estado":409,"mensaje":"..."} ----------

data class ErrorDto(
    val estado: Int?,
    val mensaje: String?
)

// ---------- API externa: Open-Meteo ----------

data class ClimaRespuestaDto(
    val current: ClimaActualDto?
)

data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double?,
    @SerializedName("relative_humidity_2m") val humedad: Int?,
    @SerializedName("weather_code") val codigo: Int?,
    @SerializedName("wind_speed_10m") val viento: Double?
)
