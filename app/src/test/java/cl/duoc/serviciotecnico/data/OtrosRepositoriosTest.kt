package cl.duoc.serviciotecnico.data

import cl.duoc.serviciotecnico.data.local.dao.EquipoDao
import cl.duoc.serviciotecnico.data.local.dao.FotoGuiaDao
import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.remote.api.ClimaApi
import cl.duoc.serviciotecnico.data.remote.api.GuiasApi
import cl.duoc.serviciotecnico.data.remote.api.UsuariosApi
import cl.duoc.serviciotecnico.data.remote.dto.ClimaActualDto
import cl.duoc.serviciotecnico.data.remote.dto.ClimaRespuestaDto
import cl.duoc.serviciotecnico.data.remote.dto.GuiaRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.UsuarioDto
import cl.duoc.serviciotecnico.data.repository.AlmacenSesion
import cl.duoc.serviciotecnico.data.repository.ClimaRepository
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.UsuarioSesion
import cl.duoc.serviciotecnico.util.Datos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuiaRepositoryTest {

    private val api: GuiasApi = mockk()
    private val guiaDao: GuiaDao = mockk(relaxed = true)
    private val fotoDao: FotoGuiaDao = mockk(relaxed = true)
    private val repository = GuiaRepository(api, guiaDao, fotoDao)

    @Test
    fun `crear envia la guia y guarda sus fotos en la cache`() = runTest {
        val peticion = slot<GuiaRequestDto>()
        coEvery { api.crear(capture(peticion)) } returns Datos.guiaDto(fotos = listOf("content://1", "content://2"))

        val guia = repository.crear(
            ticketId = "t-1", equipoId = "eq-1", direccion = "Av. Providencia 1234",
            tecnico = "Juan Perez", trabajoRealizado = "Cambio de capacitor", repuestos = "",
            observaciones = "", recibidoPor = "Maria Soto", urisDeFotos = listOf("content://1", "content://2")
        )

        assertEquals("g-1", guia.id)
        assertEquals(2, peticion.captured.urisDeFotos.size)
        coVerify { guiaDao.upsert(match { it.id == "g-1" }) }
        coVerify { fotoDao.eliminarDeGuia("g-1") }
        coVerify { fotoDao.insertarTodas(match { it.size == 2 }) }
    }

    @Test
    fun `guia sin fotos no inserta filas de fotos`() = runTest {
        coEvery { api.obtener("g-1") } returns Datos.guiaDto(fotos = emptyList())

        repository.sincronizarGuia("g-1")

        coVerify(exactly = 0) { fotoDao.insertarTodas(any()) }
    }

    @Test
    fun `sincronizar guias de un ticket quita las que ya no existen`() = runTest {
        coEvery { api.listarDeTicket("t-1") } returns listOf(Datos.guiaDto(id = "g-1"))

        repository.sincronizarGuiasDeTicket("t-1")

        coVerify { guiaDao.eliminarDeTicketExcepto("t-1", listOf("g-1")) }
    }

    @Test
    fun `sincronizar sin guias en el servidor borra las del ticket`() = runTest {
        coEvery { api.listarDeTicket("t-1") } returns emptyList()

        repository.sincronizarGuiasDeTicket("t-1")

        coVerify { guiaDao.eliminarDeTicket("t-1") }
    }
}

class SesionRepositoryTest {

    private val api: UsuariosApi = mockk()
    private val almacen: AlmacenSesion = mockk(relaxed = true)
    private val repository = SesionRepository(api, almacen)

    @Test
    fun `login correcto guarda el usuario con su rol`() = runTest {
        coEvery { api.login(any()) } returns UsuarioDto(2L, "Coordinador", "coordinador@tacticaldev.cl", "COORDINADOR", true)
        val guardado = slot<UsuarioSesion>()
        every { almacen.guardar(capture(guardado)) } returns Unit

        val usuario = repository.iniciarSesion("  Coordinador@TacticalDev.cl ", "coord123")

        assertEquals(Rol.COORDINADOR, usuario.rol)
        assertEquals(Rol.COORDINADOR, guardado.captured.rol)
        // El correo se envia limpio y en minusculas
        coVerify { api.login(match { it.correo == "coordinador@tacticaldev.cl" }) }
    }

    @Test
    fun `login incorrecto no guarda sesion`() = runTest {
        coEvery { api.login(any()) } throws Datos.errorHttp(401, "Correo o contraseña incorrectos")

        val resultado = runCatching { repository.iniciarSesion("x@x.cl", "mala") }

        assertTrue(resultado.isFailure)
        verify(exactly = 0) { almacen.guardar(any()) }
    }

    @Test
    fun `sin sesion el rol por defecto es tecnico`() {
        every { almacen.leer() } returns null

        assertFalse(repository.haySesionActiva())
        assertNull(repository.usuarioActual())
        assertEquals(Rol.TECNICO, repository.rolActual())
    }

    @Test
    fun `con sesion devuelve el rol guardado`() {
        every { almacen.leer() } returns UsuarioSesion(2L, "Coord", "c@t.cl", Rol.COORDINADOR)

        assertTrue(repository.haySesionActiva())
        assertEquals(Rol.COORDINADOR, repository.rolActual())
    }

    @Test
    fun `cerrar sesion borra el almacen`() {
        repository.cerrarSesion()

        verify { almacen.borrar() }
    }
}

class ClimaYEquipoRepositoryTest {

    @Test
    fun `clima consulta open-meteo con las coordenadas`() = runTest {
        val api: ClimaApi = mockk()
        coEvery { api.climaActual(-33.45, -70.66, any(), any()) } returns
            ClimaRespuestaDto(ClimaActualDto(temperatura = 12.0, humedad = 60, codigo = 3, viento = 5.0))

        val clima = ClimaRepository(api).climaActual(-33.45, -70.66)

        assertEquals(12.0, clima.temperatura, 0.0)
        assertEquals("Nublado", clima.descripcion)
    }

    @Test
    fun `equipo se lee desde la cache`() = runTest {
        val dao: EquipoDao = mockk()
        coEvery { dao.obtenerPorId("eq-1") } returns Datos.equipo

        assertEquals("ABC123", EquipoRepository(dao).obtenerEquipo("eq-1")?.serie)
    }
}
