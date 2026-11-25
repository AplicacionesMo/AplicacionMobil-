package cl.duoc.serviciotecnico.viewmodel

import androidx.lifecycle.SavedStateHandle

import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.data.repository.UsuarioRepository
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.Usuario
import cl.duoc.serviciotecnico.ui.viewmodel.CampoTicket
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioTicketViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FormularioTicketViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val tickets: TicketRepository = mockk()
    private val equipos: EquipoRepository = mockk()
    private val usuarios: UsuarioRepository = mockk {
        coEvery { listar() } returns listOf(
            Usuario(1L, "Tecnico de Prueba", "tecnico@tacticaldev.cl", Rol.TECNICO, true),
            Usuario(2L, "Coordinador", "coordinador@tacticaldev.cl", Rol.COORDINADOR, true)
        )
    }

    private fun llenar(vm: FormularioTicketViewModel) {
        vm.cambiarCampo(CampoTicket.TITULO, "Equipo no enfria")
        vm.cambiarCampo(CampoTicket.DESCRIPCION, "Sala de servidores sobre 30 grados")
        vm.cambiarCampo(CampoTicket.CLIENTE, "Banco Uno")
        vm.cambiarCampo(CampoTicket.DIRECCION, "Av. Providencia 1234")
        vm.cambiarCampo(CampoTicket.NOMBRE_EQUIPO, "Split muro")
        vm.cambiarCampo(CampoTicket.MODELO, "LG 12000")
        vm.cambiarCampo(CampoTicket.SERIE, "abc123")
        vm.cambiarPrioridad(Prioridad.ALTA)
        vm.cambiarPropiedad(Propiedad.ARRENDADO)
    }

    @Test
    fun `formulario vacio no se envia y marca los errores`() {
        val vm = FormularioTicketViewModel(null, SavedStateHandle(), tickets, equipos, usuarios, Rol.COORDINADOR)

        vm.guardar()

        assertNotNull(vm.estado.value.errorVisible(CampoTicket.TITULO))
        assertNotNull(vm.estado.value.errorVisible(CampoTicket.SERIE))
        assertEquals("Revisa los campos marcados", vm.estado.value.mensaje)
        coVerify(exactly = 0) { tickets.crear(any(), any()) }
    }

    @Test
    fun `ticket nuevo valido se crea en el microservicio`() {
        val enviado = slot<DatosTicket>()
        coEvery { tickets.crear(capture(enviado), Rol.COORDINADOR) } returns Datos.ticket()
        val vm = FormularioTicketViewModel(null, SavedStateHandle(), tickets, equipos, usuarios, Rol.COORDINADOR)
        llenar(vm)

        vm.guardar()

        assertTrue(vm.estado.value.guardado)
        assertEquals("ABC123", enviado.captured.serie) // la serie se escribe en mayusculas
        assertEquals(Prioridad.ALTA, enviado.captured.prioridad)
        assertEquals(Propiedad.ARRENDADO, enviado.captured.propiedad)
    }

    @Test
    fun `error del servidor se muestra y permite reintentar`() {
        coEvery { tickets.crear(any(), any()) } throws Datos.errorHttp(400, "El titulo debe tener al menos 5 caracteres")
        val vm = FormularioTicketViewModel(null, SavedStateHandle(), tickets, equipos, usuarios, Rol.COORDINADOR)
        llenar(vm)

        vm.guardar()

        assertFalse(vm.estado.value.guardado)
        assertFalse(vm.estado.value.guardando)
        assertEquals("El titulo debe tener al menos 5 caracteres", vm.estado.value.mensaje)
    }

    @Test
    fun `un tecnico no puede crear tickets`() {
        val vm = FormularioTicketViewModel(null, SavedStateHandle(), tickets, equipos, usuarios, Rol.TECNICO)
        llenar(vm)

        vm.guardar()

        assertTrue(vm.estado.value.soloLectura)
        assertEquals(FormularioTicketViewModel.MENSAJE_SIN_PERMISO, vm.estado.value.mensaje)
        coVerify(exactly = 0) { tickets.crear(any(), any()) }
    }

    @Test
    fun `el admin tambien puede crear tickets`() {
        coEvery { tickets.crear(any(), Rol.ADMIN) } returns Datos.ticket()
        val vm = FormularioTicketViewModel(null, SavedStateHandle(), tickets, equipos, usuarios, Rol.ADMIN)
        llenar(vm)

        vm.guardar()

        assertTrue(vm.estado.value.guardado)
    }

    @Test
    fun `editar carga los datos y guarda con PUT`() {
        coEvery { tickets.obtenerTicket("t-1") } returns Datos.ticket()
        coEvery { equipos.obtenerEquipo("eq-1") } returns Datos.equipo
        coEvery { tickets.actualizar("t-1", any()) } returns Datos.ticket()
        val vm = FormularioTicketViewModel("t-1", SavedStateHandle(), tickets, equipos, usuarios, Rol.TECNICO)

        assertEquals("Equipo no enfria", vm.estado.value.titulo)
        assertEquals("ABC123", vm.estado.value.serie)

        vm.cambiarCampo(CampoTicket.CLIENTE, "Banco Dos")
        vm.guardar()

        coVerify { tickets.actualizar("t-1", match { it.cliente == "Banco Dos" }) }
        assertTrue(vm.estado.value.guardado)
    }

    @Test
    fun `ticket finalizado queda en solo lectura y no se guarda`() {
        coEvery { tickets.obtenerTicket("t-1") } returns Datos.ticket(EstadoTicket.FINALIZADO)
        coEvery { equipos.obtenerEquipo("eq-1") } returns Datos.equipo
        val vm = FormularioTicketViewModel("t-1", SavedStateHandle(), tickets, equipos, usuarios, Rol.TECNICO)

        vm.guardar()

        assertTrue(vm.estado.value.soloLectura)
        coVerify(exactly = 0) { tickets.actualizar(any(), any()) }
    }

    @Test
    fun `ticket inexistente informa el problema`() {
        coEvery { tickets.obtenerTicket("x") } returns null
        val vm = FormularioTicketViewModel("x", SavedStateHandle(), tickets, equipos, usuarios, Rol.TECNICO)

        assertEquals("No se encontro el ticket", vm.estado.value.mensaje)
    }
}
