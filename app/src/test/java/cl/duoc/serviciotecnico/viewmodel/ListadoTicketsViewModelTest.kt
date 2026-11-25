package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.repository.ClimaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.ClimaActual
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.viewmodel.EstadoClima
import cl.duoc.serviciotecnico.ui.viewmodel.ListadoTicketsViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListadoTicketsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val tickets: TicketRepository = mockk(relaxed = true)
    private val sesion: SesionRepository = mockk(relaxed = true)
    private val clima: ClimaRepository = mockk()

    // Simula la tabla de Room: al cambiar su valor, el Flow emite de nuevo
    private val tabla = MutableStateFlow(
        listOf(
            Datos.ticket(EstadoTicket.ABIERTO),
            Datos.ticket(EstadoTicket.FINALIZADO).copy(id = "t-2", numero = 2)
        )
    )

    @Before
    fun setUp() {
        every { tickets.observarTickets() } returns tabla
        every { sesion.usuarioActual() } returns null
        coEvery { clima.climaActual(any(), any()) } returns ClimaActual(30.0, 25, 8.0, 0)
    }

    @Test
    fun `al abrir sincroniza con el microservicio y carga el clima`() {
        val vm = ListadoTicketsViewModel(tickets, sesion, clima)

        coVerify { tickets.sincronizar() }
        assertNull(vm.error.value)
        assertFalse(vm.sincronizando.value)
        val estadoClima = vm.clima.value
        assertTrue(estadoClima is EstadoClima.Listo)
        assertEquals(30.0, (estadoClima as EstadoClima.Listo).clima.temperatura, 0.0)
    }

    @Test
    fun `si el servidor no responde muestra aviso pero no se cae`() {
        coEvery { tickets.sincronizar() } throws IOException("sin red")

        val vm = ListadoTicketsViewModel(tickets, sesion, clima)

        assertTrue(vm.error.value!!.contains("Se muestran los datos guardados"))
    }

    @Test
    fun `si falla la api externa la tarjeta de clima muestra error`() {
        coEvery { clima.climaActual(any(), any()) } throws IOException("sin red")

        val vm = ListadoTicketsViewModel(tickets, sesion, clima)

        assertTrue(vm.clima.value is EstadoClima.Error)
    }

    @Test
    fun `el filtro muestra solo los tickets del estado elegido`() = runTest {
        val vm = ListadoTicketsViewModel(tickets, sesion, clima)
        // stateIn(WhileSubscribed) necesita a alguien escuchando, como lo haria la pantalla
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.tickets.collect {} }

        assertEquals(2, vm.tickets.value.size)

        vm.cambiarFiltro(EstadoTicket.FINALIZADO)
        assertEquals(listOf("t-2"), vm.tickets.value.map { it.id })

        vm.cambiarFiltro(null)
        assertEquals(2, vm.tickets.value.size)
    }

    @Test
    fun `la lista reacciona cuando cambia la cache`() = runTest {
        val vm = ListadoTicketsViewModel(tickets, sesion, clima)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.tickets.collect {} }

        tabla.value = emptyList()

        assertTrue(vm.tickets.value.isEmpty())
    }

    @Test
    fun `cerrar sesion delega en el repositorio`() {
        val vm = ListadoTicketsViewModel(tickets, sesion, clima)

        vm.cerrarSesion()

        verify { sesion.cerrarSesion() }
    }
}
