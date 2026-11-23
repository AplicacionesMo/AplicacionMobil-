package cl.duoc.serviciotecnico.data

import cl.duoc.serviciotecnico.data.remote.aDominio
import cl.duoc.serviciotecnico.data.remote.aEntidad
import cl.duoc.serviciotecnico.data.remote.aEstado
import cl.duoc.serviciotecnico.data.remote.aFecha
import cl.duoc.serviciotecnico.data.remote.aRequest
import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoDto
import cl.duoc.serviciotecnico.data.remote.dto.ClimaActualDto
import cl.duoc.serviciotecnico.data.remote.dto.ClimaRespuestaDto
import cl.duoc.serviciotecnico.data.remote.fotos
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import cl.duoc.serviciotecnico.util.Datos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Conversion JSON (DTO) <-> entidad de Room. */
class MapeosTest {

    @Test
    fun `ticketDto se convierte en entidad con enums y fecha`() {
        val ticket = Datos.ticketDto(estado = "TRANSITO").aEntidad()

        assertEquals("t-1", ticket.id)
        assertEquals(EstadoTicket.TRANSITO, ticket.estado)
        assertEquals(Prioridad.ALTA, ticket.prioridad)
        assertEquals("eq-1", ticket.equipoId)
        assertEquals(2026, ticket.fechaCreacion.year)
        assertNull(ticket.fechaCierre)
    }

    @Test
    fun `valores desconocidos usan un valor por defecto seguro`() {
        assertEquals(EstadoTicket.ABIERTO, aEstado("INEXISTENTE"))
        assertNull(aFecha("no es fecha"))
        assertNull(aFecha(null))
    }

    @Test
    fun `equipoDto conserva la propiedad`() {
        val equipo = Datos.equipoDto.aEntidad()
        assertEquals(Propiedad.PROPIO_DEL_CLIENTE, equipo.propiedad)
        assertEquals("ABC123", equipo.serie)
    }

    @Test
    fun `guiaDto genera una fila de foto por cada uri`() {
        val dto = Datos.guiaDto(fotos = listOf("content://a", "content://b"))

        val guia = dto.aEntidad()
        val fotos = dto.fotos()

        assertEquals("", guia.observaciones)
        assertEquals(2, fotos.size)
        assertEquals("g-1-0", fotos[0].id)
        assertEquals("content://b", fotos[1].uri)
    }

    @Test
    fun `cambioEstadoDto se convierte con sus fotos`() {
        val cambio = CambioEstadoDto(
            "c-1", "t-1", "ABIERTO", "TRANSITO", "En camino",
            listOf("content://x"), -33.4, -70.6, "2026-11-20T12:00:00"
        ).aEntidad()

        assertEquals(EstadoTicket.ABIERTO, cambio.estadoAnterior)
        assertEquals(EstadoTicket.TRANSITO, cambio.estadoNuevo)
        assertEquals(1, cambio.urisDeFotos.size)
    }

    @Test
    fun `datos del formulario se envian limpios y con enums como texto`() {
        val request = DatosTicket(
            titulo = "  No enfria ", descripcion = "Sala de servidores caliente", cliente = "Banco",
            direccion = "Av. Uno 123", prioridad = Prioridad.BAJA, nombreEquipo = "Split",
            modelo = "LG", serie = " ABC ", propiedad = Propiedad.ARRENDADO
        ).aRequest()

        assertEquals("No enfria", request.titulo)
        assertEquals("BAJA", request.prioridad)
        assertEquals("ABC", request.equipo.serie)
        assertEquals("ARRENDADO", request.equipo.propiedad)
    }

    @Test
    fun `respuesta de open-meteo se convierte a dominio`() {
        val clima = ClimaRespuestaDto(ClimaActualDto(29.5, 30, 0, 12.0)).aDominio()

        assertEquals(29.5, clima.temperatura, 0.0)
        assertEquals(30, clima.humedad)
        assertEquals("Despejado", clima.descripcion)
    }
}
