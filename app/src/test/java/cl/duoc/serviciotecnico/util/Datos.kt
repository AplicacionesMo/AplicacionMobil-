package cl.duoc.serviciotecnico.util

import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.dto.EquipoDto
import cl.duoc.serviciotecnico.data.remote.dto.GuiaDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketDto
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Datos de ejemplo reutilizados por todas las pruebas. */
object Datos {

    val equipo = Equipo(
        id = "eq-1",
        nombre = "Split muro",
        modelo = "LG 12000 BTU",
        serie = "ABC123",
        propiedad = Propiedad.PROPIO_DEL_CLIENTE
    )

    fun ticket(estado: EstadoTicket = EstadoTicket.ABIERTO) = Ticket(
        id = "t-1",
        numero = 1,
        titulo = "Equipo no enfria",
        descripcion = "El equipo de la sala de servidores no enfria",
        cliente = "Banco Uno",
        direccion = "Av. Providencia 1234",
        prioridad = Prioridad.ALTA,
        estado = estado,
        equipoId = "eq-1"
    )

    val guia = Guia(
        id = "g-1",
        numero = 1,
        ticketId = "t-1",
        equipoId = "eq-1",
        direccion = "Av. Providencia 1234",
        tecnico = "Juan Perez",
        trabajoRealizado = "Cambio de capacitor y limpieza de filtros",
        repuestos = "Capacitor 35uF",
        observaciones = "",
        recibidoPor = "Maria Soto"
    )

    val equipoDto = EquipoDto("eq-1", "Split muro", "LG 12000 BTU", "ABC123", "PROPIO_DEL_CLIENTE")

    fun ticketDto(estado: String = "ABIERTO", id: String = "t-1") = TicketDto(
        id = id,
        numero = 1,
        titulo = "Equipo no enfria",
        descripcion = "El equipo de la sala de servidores no enfria",
        cliente = "Banco Uno",
        direccion = "Av. Providencia 1234",
        prioridad = "ALTA",
        estado = estado,
        equipo = equipoDto,
        fechaCreacion = "2026-11-20T10:15:30.123456",
        fechaCierre = null,
        solucion = null,
        motivoCancelacion = null
    )

    fun guiaDto(id: String = "g-1", fotos: List<String> = listOf("content://foto/1")) = GuiaDto(
        id = id,
        numero = 1,
        ticketId = "t-1",
        equipoId = "eq-1",
        direccion = "Av. Providencia 1234",
        latitud = -33.42,
        longitud = -70.61,
        fecha = "2026-11-20T11:00:00",
        tecnico = "Juan Perez",
        trabajoRealizado = "Cambio de capacitor y limpieza de filtros",
        repuestos = "Capacitor 35uF",
        observaciones = null,
        recibidoPor = "Maria Soto",
        urisDeFotos = fotos
    )

    /** Simula una respuesta de error de un microservicio, con su JSON {"mensaje": ...}. */
    fun errorHttp(codigo: Int, mensaje: String? = null): HttpException {
        val cuerpo = (if (mensaje == null) "" else """{"estado":$codigo,"mensaje":"$mensaje"}""")
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(codigo, cuerpo))
    }
}
