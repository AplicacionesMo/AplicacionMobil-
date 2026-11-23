package cl.duoc.serviciotecnico.data

import cl.duoc.serviciotecnico.data.local.dao.CambioEstadoDao
import cl.duoc.serviciotecnico.data.local.dao.EquipoDao
import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.local.dao.TicketDao
import cl.duoc.serviciotecnico.data.remote.api.TicketsApi
import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoRequestDto
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.util.Datos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Repositorio de tickets. La API (Retrofit) y los DAO (Room) son mocks de MockK:
 * se verifica que el repositorio llame al microservicio y guarde la respuesta en la cache.
 */
class TicketRepositoryTest {

    private lateinit var api: TicketsApi
    private lateinit var ticketDao: TicketDao
    private lateinit var equipoDao: EquipoDao
    private lateinit var guiaDao: GuiaDao
    private lateinit var cambioEstadoDao: CambioEstadoDao
    private lateinit var repository: TicketRepository

    private val datos = DatosTicket(
        titulo = "Equipo no enfria", descripcion = "Sala de servidores caliente", cliente = "Banco Uno",
        direccion = "Av. Providencia 1234", prioridad = Prioridad.ALTA, nombreEquipo = "Split muro",
        modelo = "LG", serie = "ABC123", propiedad = Propiedad.PROPIO_DEL_CLIENTE
    )

    @Before
    fun setUp() {
        api = mockk()
        // relaxed = true: los metodos que no se configuran simplemente no hacen nada
        ticketDao = mockk(relaxed = true)
        equipoDao = mockk(relaxed = true)
        guiaDao = mockk(relaxed = true)
        cambioEstadoDao = mockk(relaxed = true)
        repository = TicketRepository(api, ticketDao, equipoDao, guiaDao, cambioEstadoDao)
    }

    @Test
    fun `crear envia el POST y guarda primero el equipo y luego el ticket`() = runTest {
        coEvery { api.crear(any()) } returns Datos.ticketDto()

        val ticket = repository.crear(datos)

        assertEquals("t-1", ticket.id)
        coVerifyOrder {
            api.crear(match { it.titulo == "Equipo no enfria" && it.equipo.serie == "ABC123" })
            equipoDao.upsert(match { it.id == "eq-1" })
            ticketDao.upsert(match { it.id == "t-1" })
        }
    }

    @Test
    fun `actualizar usa PUT con el id del ticket`() = runTest {
        coEvery { api.actualizar("t-1", any()) } returns Datos.ticketDto()

        repository.actualizar("t-1", datos)

        coVerify { api.actualizar("t-1", any()) }
        coVerify { ticketDao.upsert(any()) }
    }

    @Test
    fun `sincronizar deja la cache igual al servidor`() = runTest {
        coEvery { api.listar() } returns listOf(Datos.ticketDto(id = "t-1"), Datos.ticketDto(id = "t-2"))

        repository.sincronizar()

        coVerify(exactly = 2) { ticketDao.upsert(any()) }
        coVerify { ticketDao.eliminarExcepto(listOf("t-1", "t-2")) }
    }

    @Test
    fun `sincronizar con servidor vacio limpia la cache`() = runTest {
        coEvery { api.listar() } returns emptyList()

        repository.sincronizar()

        coVerify { ticketDao.eliminarTodos() }
    }

    @Test
    fun `si el servidor responde 404 el ticket se borra de la cache`() = runTest {
        coEvery { api.obtener("t-1") } throws Datos.errorHttp(404, "Ticket no encontrado")

        repository.sincronizarTicket("t-1")

        coVerify { ticketDao.eliminarPorId("t-1") }
    }

    @Test(expected = HttpException::class)
    fun `otros errores del servidor se propagan`() = runTest {
        coEvery { api.obtener("t-1") } throws Datos.errorHttp(500)

        repository.sincronizarTicket("t-1")
    }

    @Test
    fun `cambio de estado envia el rol en el header y refresca el historial`() = runTest {
        val peticion = slot<CambioEstadoRequestDto>()
        coEvery { api.cambiarEstado("t-1", capture(peticion), "COORDINADOR") } returns Datos.ticketDto("CANCELADO")
        coEvery { api.historial("t-1") } returns emptyList()

        val ticket = repository.registrarCambioDeEstado(
            ticketId = "t-1",
            nuevoEstado = EstadoTicket.CANCELADO,
            comentario = "Cliente desiste",
            urisDeFotos = emptyList(),
            latitud = -33.4,
            longitud = -70.6,
            rol = Rol.COORDINADOR
        )

        assertEquals(EstadoTicket.CANCELADO, ticket.estado)
        assertEquals("CANCELADO", peticion.captured.estadoNuevo)
        assertEquals("Cliente desiste", peticion.captured.comentario)
        coVerify { cambioEstadoDao.eliminarDeTicket("t-1") }
        coVerify { cambioEstadoDao.insertarTodos(emptyList()) }
    }

    @Test
    fun `eliminar exitoso borra el ticket local`() = runTest {
        coEvery { api.eliminar("t-1", "COORDINADOR") } returns Response.success(Unit)

        repository.eliminar("t-1", Rol.COORDINADOR)

        coVerify { ticketDao.eliminarPorId("t-1") }
    }

    @Test
    fun `eliminar rechazado no borra nada local`() = runTest {
        val cuerpo = """{"mensaje":"No se puede eliminar"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.eliminar("t-1", "COORDINADOR") } returns Response.error(409, cuerpo)

        val resultado = runCatching { repository.eliminar("t-1", Rol.COORDINADOR) }

        assertTrue(resultado.exceptionOrNull() is HttpException)
        coVerify(exactly = 0) { ticketDao.eliminarPorId(any()) }
    }

    @Test
    fun `tieneGuiasEmitidas consulta la cache de guias`() = runTest {
        coEvery { guiaDao.contarPorTicket("t-1") } returns 0
        assertFalse(repository.tieneGuiasEmitidas("t-1"))

        coEvery { guiaDao.contarPorTicket("t-1") } returns 2
        assertTrue(repository.tieneGuiasEmitidas("t-1"))
    }
}
