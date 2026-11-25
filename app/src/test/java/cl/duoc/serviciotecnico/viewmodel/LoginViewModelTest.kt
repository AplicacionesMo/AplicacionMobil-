package cl.duoc.serviciotecnico.viewmodel

import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.UsuarioSesion
import cl.duoc.serviciotecnico.ui.viewmodel.CampoLogin
import cl.duoc.serviciotecnico.ui.viewmodel.LoginViewModel
import cl.duoc.serviciotecnico.util.Datos
import cl.duoc.serviciotecnico.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val sesion: SesionRepository = mockk()

    private fun vmCon(correo: String, clave: String) = LoginViewModel(sesion).apply {
        cambiarCorreo(correo)
        cambiarClave(clave)
    }

    @Test
    fun `formulario invalido no llama al microservicio`() {
        val vm = vmCon("sin-arroba", "123")

        vm.ingresar()

        assertNotNull(vm.estado.value.errorVisible(CampoLogin.CORREO))
        assertNotNull(vm.estado.value.errorVisible(CampoLogin.CLAVE))
        coVerify(exactly = 0) { sesion.iniciarSesion(any(), any()) }
    }

    @Test
    fun `los errores no se muestran antes de tocar el campo`() {
        val vm = LoginViewModel(sesion)

        assertNull(vm.estado.value.errorVisible(CampoLogin.CORREO))
        assertFalse(vm.estado.value.formularioValido)
    }

    @Test
    fun `login correcto marca ingresado`() {
        coEvery { sesion.iniciarSesion("tecnico@tacticaldev.cl", "tecnico123") } returns
            UsuarioSesion(1L, "Tecnico", "tecnico@tacticaldev.cl", Rol.TECNICO)
        val vm = vmCon("tecnico@tacticaldev.cl", "tecnico123")

        vm.ingresar()

        assertTrue(vm.estado.value.ingresado)
        assertFalse(vm.estado.value.ingresando)
    }

    @Test
    fun `credenciales incorrectas muestran el mensaje del servidor`() {
        coEvery { sesion.iniciarSesion(any(), any()) } throws Datos.errorHttp(401, "Correo o contraseña incorrectos")
        val vm = vmCon("tecnico@tacticaldev.cl", "malaclave")

        vm.ingresar()

        assertFalse(vm.estado.value.ingresado)
        assertEquals("Correo o contraseña incorrectos", vm.estado.value.mensaje)
    }

    @Test
    fun `sin servidor avisa que no hay conexion`() {
        coEvery { sesion.iniciarSesion(any(), any()) } throws IOException("Connection refused")
        val vm = vmCon("tecnico@tacticaldev.cl", "tecnico123")

        vm.ingresar()

        assertEquals(ErrorDeRed.SIN_CONEXION, vm.estado.value.mensaje)
    }

    @Test
    fun `tocar una cuenta de prueba reemplaza lo escrito y no inicia sesion`() {
        val vm = vmCon("otro@correo.cl", "otraclave")
        val tecnico = SesionRepository.CUENTAS_DE_PRUEBA.first { it.rol == Rol.TECNICO }

        vm.usarCuentaDePrueba(tecnico)

        assertEquals(SesionRepository.USUARIO_TECNICO, vm.estado.value.correo)
        assertEquals(SesionRepository.CLAVE_TECNICO, vm.estado.value.clave)
        // El usuario todavia tiene que apretar Ingresar
        assertFalse(vm.estado.value.ingresado)
        coVerify(exactly = 0) { sesion.iniciarSesion(any(), any()) }
    }

    @Test
    fun `las tres cuentas de prueba quedan validas al tocarlas`() {
        SesionRepository.CUENTAS_DE_PRUEBA.forEach { cuenta ->
            val vm = LoginViewModel(sesion)

            vm.usarCuentaDePrueba(cuenta)

            assertTrue("${cuenta.rol} deberia quedar valida", vm.estado.value.formularioValido)
            assertNull(vm.estado.value.errorVisible(CampoLogin.CORREO))
            assertNull(vm.estado.value.errorVisible(CampoLogin.CLAVE))
        }
    }
}
