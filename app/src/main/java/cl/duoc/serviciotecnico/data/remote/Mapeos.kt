package cl.duoc.serviciotecnico.data.remote

import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.data.local.entity.FotoGuia
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoDto
import cl.duoc.serviciotecnico.data.remote.dto.ClimaRespuestaDto
import cl.duoc.serviciotecnico.data.remote.dto.EquipoDto
import cl.duoc.serviciotecnico.data.remote.dto.EquipoRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.GuiaDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketRequestDto
import cl.duoc.serviciotecnico.domain.model.ClimaActual
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import java.time.LocalDateTime

/*
 * Conversion entre lo que viaja por la red (DTO) y lo que se guarda en Room (entidad).
 * Asi un cambio en el JSON del servidor solo obliga a tocar este archivo.
 */

fun aFecha(texto: String?): LocalDateTime? =
    texto?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

fun aEstado(texto: String?): EstadoTicket =
    EstadoTicket.entries.firstOrNull { it.name == texto } ?: EstadoTicket.ABIERTO

fun aPrioridad(texto: String?): Prioridad =
    Prioridad.entries.firstOrNull { it.name == texto } ?: Prioridad.MEDIA

fun aPropiedad(texto: String?): Propiedad =
    Propiedad.entries.firstOrNull { it.name == texto } ?: Propiedad.PROPIO_DEL_CLIENTE

// ---------- Respuestas -> entidades ----------

fun EquipoDto.aEntidad(): Equipo = Equipo(
    id = requireNotNull(id) { "El equipo llego sin id" },
    nombre = nombre.orEmpty(),
    modelo = modelo.orEmpty(),
    serie = serie.orEmpty(),
    propiedad = aPropiedad(propiedad)
)

fun TicketDto.aEntidad(): Ticket = Ticket(
    id = requireNotNull(id) { "El ticket llego sin id" },
    numero = numero ?: 0,
    titulo = titulo.orEmpty(),
    descripcion = descripcion.orEmpty(),
    cliente = cliente.orEmpty(),
    direccion = direccion.orEmpty(),
    prioridad = aPrioridad(prioridad),
    estado = aEstado(estado),
    equipoId = requireNotNull(equipo?.id) { "El ticket llego sin equipo" },
    fechaCreacion = aFecha(fechaCreacion) ?: LocalDateTime.now(),
    fechaCierre = aFecha(fechaCierre),
    solucion = solucion,
    motivoCancelacion = motivoCancelacion,
    tecnicoId = tecnicoId,
    tecnicoNombre = tecnicoNombre
)

fun CambioEstadoDto.aEntidad(): CambioEstado = CambioEstado(
    id = requireNotNull(id) { "El cambio de estado llego sin id" },
    ticketId = ticketId.orEmpty(),
    estadoAnterior = aEstado(estadoAnterior),
    estadoNuevo = aEstado(estadoNuevo),
    comentario = comentario.orEmpty(),
    urisDeFotos = urisDeFotos.orEmpty(),
    latitud = latitud,
    longitud = longitud,
    fecha = aFecha(fecha) ?: LocalDateTime.now()
)

fun GuiaDto.aEntidad(): Guia = Guia(
    id = requireNotNull(id) { "La guia llego sin id" },
    numero = numero ?: 0,
    ticketId = ticketId.orEmpty(),
    equipoId = equipoId.orEmpty(),
    direccion = direccion.orEmpty(),
    latitud = latitud,
    longitud = longitud,
    fecha = aFecha(fecha) ?: LocalDateTime.now(),
    tecnico = tecnico.orEmpty(),
    trabajoRealizado = trabajoRealizado.orEmpty(),
    repuestos = repuestos.orEmpty(),
    observaciones = observaciones.orEmpty(),
    recibidoPor = recibidoPor.orEmpty()
)

/** Cada URI de la guia se transforma en una fila de fotos_guia. */
fun GuiaDto.fotos(): List<FotoGuia> {
    val guiaId = requireNotNull(id)
    return urisDeFotos.orEmpty().mapIndexed { indice, uri ->
        FotoGuia(id = "$guiaId-$indice", guiaId = guiaId, uri = uri)
    }
}

fun ClimaRespuestaDto.aDominio(): ClimaActual {
    val actual = requireNotNull(current) { "La API de clima no envio datos" }
    return ClimaActual(
        temperatura = actual.temperatura ?: 0.0,
        humedad = actual.humedad ?: 0,
        viento = actual.viento ?: 0.0,
        codigo = actual.codigo ?: -1
    )
}

// ---------- Datos del formulario -> peticion ----------

fun DatosTicket.aRequest(): TicketRequestDto = TicketRequestDto(
    titulo = titulo.trim(),
    descripcion = descripcion.trim(),
    cliente = cliente.trim(),
    direccion = direccion.trim(),
    prioridad = prioridad.name,
    equipo = EquipoRequestDto(
        nombre = nombreEquipo.trim(),
        modelo = modelo.trim(),
        serie = serie.trim(),
        propiedad = propiedad.name
    ),
    tecnicoId = tecnicoId,
    tecnicoNombre = tecnicoNombre
)
