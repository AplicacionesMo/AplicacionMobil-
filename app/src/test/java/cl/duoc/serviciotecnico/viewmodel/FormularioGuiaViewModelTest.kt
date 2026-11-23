package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleGuiaViewModel
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioGuiaViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FormularioGuiaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val tickets: TicketRepository = mockk()
    private val guias: GuiaRepository = mockk()

    private fun TestScope.crearVm(): FormularioGuiaViewModel {
        every { tickets.observarTicket("t-1") } returns MutableStateFlow<Ticket?>(Datos.ticket())
        val vm = FormularioGuiaViewModel("t-1", tickets, guias)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.ticket.collect {} }
        return vm
    }

    private fun FormularioGuiaViewModel.llenar() {
        heredarDireccion("Av. Providencia 1234")
        cambiarTecnico("Juan Perez")
        cambiarTrabajo("Cambio de capacitor y limpieza de filtros")
        cambiarRecibidoPor("Maria Soto")
        registrarUbicacion(-33.42, -70.61)
        agregarFoto("content://foto/1")
    }

    @Test
    fun `la direccion se hereda del ticket una sola vez`() = runTest {
        val vm = crearVm()

        vm.heredarDireccion("Av. Providencia 1234")
        vm.cambiarDireccion("Otra direccion 99")
        vm.heredarDireccion("Av. Providencia 1234")

        assertEquals("Otra direccion 99", vm.estado.value.direccion)
    }

    @Test
    fun `guia incompleta no se envia`() = runTest {
        val vm = crearVm()

        vm.guardar()

        assertEquals("Revisa los campos marcados", vm.mensaje.value)
        assertTrue(vm.estado.value.tocado)
        coVerify(exactly = 0) { guias.crear(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `guia valida se envia con el equipo del ticket, fotos y ubicacion`() = runTest {
        coEvery { guias.crear(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Datos.guia
        val vm = crearVm()
        vm.llenar()

        vm.guardar()

        assertTrue(vm.estado.value.guardado)
        coVerify {
            guias.crear(
                ticketId = "t-1",
                equipoId = "eq-1",
                direccion = "Av. Providencia 1234",
                tecnico = "Juan Perez",
                trabajoRealizado = "Cambio de capacitor y limpieza de filtros",
                repuestos = "",
                observaciones = "",
                recibidoPor = "Maria Soto",
                latitud = -33.42,
                longitud = -70.61,
                urisDeFotos = listOf("content://foto/1")
            )
        }
    }

    @Test
    fun `sin conexion se informa y no queda guardando`() = runTest {
        coEvery { guias.crear(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
            IOException("sin red")
        val vm = crearVm()
        vm.llenar()

        vm.guardar()

        assertFalse(vm.estado.value.guardado)
        assertFalse(vm.estado.value.guardando)
        assertTrue(vm.mensaje.value!!.startsWith("No hay conexión"))
    }

    @Test
    fun `quitar foto la saca de la lista`() = runTest {
        val vm = crearVm()
        vm.agregarFoto("content://a")
        vm.agregarFoto("content://b")

        vm.quitarFoto("content://a")

        assertEquals(listOf("content://b"), vm.estado.value.fotos)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleGuiaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val guias: GuiaRepository = mockk()
    private val equipos: EquipoRepository = mockk()

    private fun TestScope.crearVm(): DetalleGuiaViewModel {
        every { guias.observarGuia("g-1") } returns flowOf(Datos.guia)
        every { guias.observarFotos("g-1") } returns flowOf(emptyList())
        every { equipos.observarEquipo("eq-1") } returns flowOf(Datos.equipo)
        val vm = DetalleGuiaViewModel("g-1", guias, equipos)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect {} }
        return vm
    }

    @Test
    fun `combina la guia con su equipo`() = runTest {
        coEvery { guias.sincronizarGuia("g-1") } returns Unit
        val vm = crearVm()

        assertEquals("Juan Perez", vm.estado.value.guia?.tecnico)
        assertEquals("ABC123", vm.estado.value.equipo?.serie)
    }

    @Test
    fun `si falla la sincronizacion avisa y muestra lo guardado`() = runTest {
        coEvery { guias.sincronizarGuia("g-1") } throws IOException("sin red")
        val vm = crearVm()

        assertTrue(vm.estado.value.mensaje!!.startsWith("No hay conexión"))
        assertEquals(1, vm.estado.value.guia?.numero)

        vm.mensajeMostrado()
        assertEquals(null, vm.estado.value.mensaje)
    }
}
