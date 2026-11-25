package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.repository.UsuarioRepository
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.Usuario
import cl.duoc.serviciotecnico.ui.viewmodel.CampoUsuario
import cl.duoc.serviciotecnico.ui.viewmodel.CrearUsuarioViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CrearUsuarioViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val usuarios: UsuarioRepository = mockk()

    private fun CrearUsuarioViewModel.llenar(confirmacion: String = "camilo123") {
        cambiarCampo(CampoUsuario.NOMBRE, "Camilo Godoy")
        cambiarCampo(CampoUsuario.CORREO, "camilo@tacticaldev.cl")
        cambiarCampo(CampoUsuario.CLAVE, "camilo123")
        cambiarCampo(CampoUsuario.CONFIRMACION, confirmacion)
    }

    @Test
    fun `formulario vacio no llama al microservicio`() {
        val vm = CrearUsuarioViewModel(usuarios, Rol.ADMIN)

        vm.guardar()

        assertNotNull(vm.estado.value.errorVisible(CampoUsuario.NOMBRE))
        assertNotNull(vm.estado.value.errorVisible(CampoUsuario.CORREO))
        assertEquals("Revisa los campos marcados", vm.estado.value.mensaje)
        coVerify(exactly = 0) { usuarios.crear(any(), any(), any(), any()) }
    }

    @Test
    fun `las contrasenas deben coincidir`() {
        val vm = CrearUsuarioViewModel(usuarios, Rol.ADMIN)
        vm.llenar(confirmacion = "otra123")

        vm.guardar()

        assertEquals("Las contraseñas no coinciden", vm.estado.value.errorVisible(CampoUsuario.CONFIRMACION))
        coVerify(exactly = 0) { usuarios.crear(any(), any(), any(), any()) }
    }

    @Test
    fun `admin crea un usuario con el rol elegido`() {
        coEvery { usuarios.crear(any(), any(), any(), any()) } returns
            Usuario(3L, "Camilo Godoy", "camilo@tacticaldev.cl", Rol.COORDINADOR, true)
        val vm = CrearUsuarioViewModel(usuarios, Rol.ADMIN)
        vm.llenar()
        vm.cambiarRol(Rol.COORDINADOR)

        vm.guardar()

        assertTrue(vm.estado.value.creado)
        coVerify { usuarios.crear("Camilo Godoy", "camilo@tacticaldev.cl", "camilo123", Rol.COORDINADOR) }
    }

    @Test
    fun `correo repetido muestra el mensaje del servidor`() {
        coEvery { usuarios.crear(any(), any(), any(), any()) } throws
            Datos.errorHttp(409, "Ya existe un usuario con ese correo")
        val vm = CrearUsuarioViewModel(usuarios, Rol.ADMIN)
        vm.llenar()

        vm.guardar()

        assertFalse(vm.estado.value.creado)
        assertFalse(vm.estado.value.guardando)
        assertEquals("Ya existe un usuario con ese correo", vm.estado.value.mensaje)
    }

    @Test
    fun `coordinador y tecnico no pueden crear usuarios`() {
        listOf(Rol.COORDINADOR, Rol.TECNICO).forEach { rol ->
            val vm = CrearUsuarioViewModel(usuarios, rol)
            vm.llenar()

            vm.guardar()

            assertFalse(vm.estado.value.permitido)
        }
        coVerify(exactly = 0) { usuarios.crear(any(), any(), any(), any()) }
    }
}
