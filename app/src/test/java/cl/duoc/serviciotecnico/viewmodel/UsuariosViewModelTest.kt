package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.repository.UsuarioRepository
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.Usuario
import cl.duoc.serviciotecnico.ui.viewmodel.UsuariosViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class UsuariosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo: UsuarioRepository = mockk()

    private val admin = Usuario(1L, "Administrador", "admin@tacticaldev.cl", Rol.ADMIN, true)
    private val tecnico = Usuario(2L, "Tecnico", "tecnico@tacticaldev.cl", Rol.TECNICO, true)

    @Test
    fun `admin ve la lista de usuarios`() {
        coEvery { repo.listar() } returns listOf(admin, tecnico)
        val vm = UsuariosViewModel(repo, Rol.ADMIN)

        vm.cargar()

        assertEquals(2, vm.estado.value.usuarios.size)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun `eliminar saca al usuario de la lista`() {
        coEvery { repo.listar() } returns listOf(admin, tecnico)
        coEvery { repo.eliminar(2L) } returns Unit
        val vm = UsuariosViewModel(repo, Rol.ADMIN)
        vm.cargar()

        vm.eliminar(tecnico)

        assertEquals(listOf(admin), vm.estado.value.usuarios)
        assertNull(vm.estado.value.eliminandoId)
        assertEquals("Usuario tecnico@tacticaldev.cl eliminado", vm.estado.value.mensaje)
    }

    @Test
    fun `si el servidor rechaza eliminar se muestra el motivo y no se quita`() {
        coEvery { repo.listar() } returns listOf(admin)
        coEvery { repo.eliminar(1L) } throws Datos.errorHttp(409, "No se puede eliminar al ultimo administrador")
        val vm = UsuariosViewModel(repo, Rol.ADMIN)
        vm.cargar()

        vm.eliminar(admin)

        assertEquals(listOf(admin), vm.estado.value.usuarios)
        assertEquals("No se puede eliminar al ultimo administrador", vm.estado.value.mensaje)
    }

    @Test
    fun `sin conexion avisa`() {
        coEvery { repo.listar() } throws IOException("sin red")
        val vm = UsuariosViewModel(repo, Rol.ADMIN)

        vm.cargar()

        assertEquals(ErrorDeRed.SIN_CONEXION, vm.estado.value.mensaje)
    }

    @Test
    fun `un coordinador no puede listar ni eliminar`() {
        val vm = UsuariosViewModel(repo, Rol.COORDINADOR)

        vm.cargar()
        vm.eliminar(tecnico)

        assertFalse(vm.estado.value.permitido)
        coVerify(exactly = 0) { repo.listar() }
        coVerify(exactly = 0) { repo.eliminar(any()) }
    }
}
