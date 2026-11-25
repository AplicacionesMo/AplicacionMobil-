package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pantalla mas compleja: cambio de estado con validaciones, regla de cierre,
 * permisos por rol y eliminacion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleTicketViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val tickets: TicketRepository = mockk(relaxed = true)
    private val guias: GuiaRepository = mockk(relaxed = true)
    private val equipos: EquipoRepository = mockk()
    private val sesion: SesionRepository = mockk()

    private val ticket = MutableStateFlow<Ticket?>(Datos.ticket(EstadoTicket.TRANSITO))

    private fun TestScope.crearVm(rol: Rol = Rol.TECNICO): DetalleTicketViewModel {
        every { sesion.rolActual() } returns rol
        every { tickets.observarTicket("t-1") } returns ticket
        every { tickets.observarHistorial("t-1") } returns flowOf(emptyList())
        every { guias.observarGuiasDeTicket("t-1") } returns flowOf(listOf(Datos.guia))
        every { equipos.observarEquipo("eq-1") } returns flowOf(Datos.equipo)
        val vm = DetalleTicketViewModel("t-1", tickets, guias, equipos, sesion)
        // La pantalla observa el estado; en la prueba lo hace este colector
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect {} }
        return vm
    }

    private fun DetalleTicketViewModel.llenarCambio(estado: EstadoTicket) {
        seleccionarEstado(estado)
    }

    @Test
    fun `al abrir sincroniza ticket y guias y combina los datos`() = runTest {
        val vm = crearVm()

        coVerify { tickets.sincronizarTicket("t-1") }
        coVerify { guias.sincronizarGuiasDeTicket("t-1") }
        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertEquals("ABC123", estado.equipo?.serie)
        assertEquals(1, estado.guias.size)
    }

    @Test
    fun `sin elegir estado no se envia nada`() = runTest {
        val vm = crearVm()

        vm.enviarCambioDeEstado()

        assertNotNull(vm.estado.value.formulario.errorEstado)
        coVerify(exactly = 0) { tickets.registrarCambioDeEstado(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `no se puede finalizar sin guias emitidas`() = runTest {
        coEvery { tickets.tieneGuiasEmitidas("t-1") } returns false
        val vm = crearVm()
        vm.llenarCambio(EstadoTicket.FINALIZADO)

        vm.enviarCambioDeEstado()

        assertEquals("Debes crear una guía", vm.estado.value.formulario.errorEstado)
        assertEquals("Debes crear una guía", vm.estado.value.mensaje)
        coVerify(exactly = 0) { tickets.registrarCambioDeEstado(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `cancelar no exige guia pero si motivo`() = runTest {
        coEvery { tickets.tieneGuiasEmitidas("t-1") } returns false
        val vm = crearVm(Rol.COORDINADOR)
        vm.seleccionarEstado(EstadoTicket.CANCELADO)
        vm.cambiarMotivo("El cliente desistio del servicio")

        vm.enviarCambioDeEstado()

        coVerify {
            tickets.registrarCambioDeEstado(
                ticketId = "t-1",
                nuevoEstado = EstadoTicket.CANCELADO,
                rol = Rol.COORDINADOR,
                comentario = "El cliente desistio del servicio"
            )
        }
    }

    @Test
    fun `el motivo solo se pide al cancelar`() = runTest {
        val vm = crearVm(Rol.COORDINADOR)

        vm.seleccionarEstado(EstadoTicket.CANCELADO)
        assertTrue(vm.estado.value.formulario.pideMotivo)

        vm.seleccionarEstado(EstadoTicket.ABIERTO)
        assertFalse(vm.estado.value.formulario.pideMotivo)
    }

    @Test
    fun `al salir de cancelado el motivo escrito se descarta`() = runTest {
        val vm = crearVm(Rol.COORDINADOR)
        vm.seleccionarEstado(EstadoTicket.CANCELADO)
        vm.cambiarMotivo("El cliente desistio del servicio")

        vm.seleccionarEstado(EstadoTicket.ABIERTO)

        assertEquals("", vm.estado.value.formulario.motivo)
    }

    @Test
    fun `cancelar sin motivo no se envia`() = runTest {
        val vm = crearVm(Rol.COORDINADOR)
        vm.seleccionarEstado(EstadoTicket.CANCELADO)

        vm.enviarCambioDeEstado()

        assertNotNull(vm.estado.value.formulario.errorMotivo)
        coVerify(exactly = 0) { tickets.registrarCambioDeEstado(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `cambio valido se envia con el rol y limpia el formulario`() = runTest {
        coEvery { tickets.tieneGuiasEmitidas("t-1") } returns true
        val vm = crearVm()
        vm.llenarCambio(EstadoTicket.FINALIZADO)

        vm.enviarCambioDeEstado()

        coVerify {
            tickets.registrarCambioDeEstado(
                ticketId = "t-1",
                nuevoEstado = EstadoTicket.FINALIZADO,
                rol = Rol.TECNICO
            )
        }
        assertNull(vm.estado.value.formulario.estadoSeleccionado)
        assertEquals("Estado cambiado a Finalizado", vm.estado.value.mensaje)
        // La pantalla se cierra sola al guardar
        assertTrue(vm.estado.value.cambioAplicado)
    }

    @Test
    fun `si el cambio falla la pantalla no se cierra`() = runTest {
        coEvery { tickets.tieneGuiasEmitidas("t-1") } returns true
        coEvery {
            tickets.registrarCambioDeEstado(any(), any(), any(), any(), any(), any(), any())
        } throws Datos.errorHttp(409, "La operacion no esta permitida")
        val vm = crearVm()
        vm.llenarCambio(EstadoTicket.FINALIZADO)

        vm.enviarCambioDeEstado()

        assertFalse(vm.estado.value.cambioAplicado)
    }

    @Test
    fun `si el servidor rechaza el cambio se muestra su mensaje`() = runTest {
        coEvery {
            tickets.registrarCambioDeEstado(any(), any(), any(), any(), any(), any(), any())
        } throws Datos.errorHttp(403, "Solo el coordinador puede cancelar un ticket")
        val vm = crearVm()
        // El tecnico no deberia poder cancelar: quien lo rechaza es el servidor
        vm.seleccionarEstado(EstadoTicket.CANCELADO)
        vm.cambiarMotivo("El cliente desistio del servicio")

        vm.enviarCambioDeEstado()

        assertEquals("Solo el coordinador puede cancelar un ticket", vm.estado.value.mensaje)
        assertFalse(vm.estado.value.formulario.enviando)
    }

    @Test
    fun `el tecnico no ve cancelar ni eliminar`() = runTest {
        val vm = crearVm(Rol.TECNICO)

        val estado = vm.estado.value
        assertFalse(EstadoTicket.CANCELADO in estado.estadosDisponibles)
        assertFalse(EstadoTicket.TRANSITO in estado.estadosDisponibles) // el actual no aparece
        assertFalse(estado.puedeEliminar)
    }

    @Test
    fun `el coordinador puede cancelar y eliminar`() = runTest {
        coEvery { tickets.eliminar("t-1", Rol.COORDINADOR) } returns Unit
        val vm = crearVm(Rol.COORDINADOR)

        assertTrue(EstadoTicket.CANCELADO in vm.estado.value.estadosDisponibles)
        assertTrue(vm.estado.value.puedeEliminar)

        vm.eliminarTicket()

        assertTrue(vm.estado.value.eliminado)
    }

    @Test
    fun `el tecnico no puede eliminar aunque llame a la funcion`() = runTest {
        val vm = crearVm(Rol.TECNICO)

        vm.eliminarTicket()

        coVerify(exactly = 0) { tickets.eliminar(any(), any()) }
    }

    @Test
    fun `eliminar rechazado muestra el motivo`() = runTest {
        coEvery { tickets.eliminar("t-1", Rol.COORDINADOR) } throws
            Datos.errorHttp(409, "No se puede eliminar un ticket que tiene guias emitidas")
        val vm = crearVm(Rol.COORDINADOR)

        vm.eliminarTicket()

        assertFalse(vm.estado.value.eliminado)
        assertEquals("No se puede eliminar un ticket que tiene guias emitidas", vm.estado.value.mensaje)
    }

    @Test
    fun `ticket finalizado es de solo lectura`() = runTest {
        ticket.value = Datos.ticket(EstadoTicket.FINALIZADO)
        val vm = crearVm()

        assertFalse(vm.estado.value.editable)
    }
}
