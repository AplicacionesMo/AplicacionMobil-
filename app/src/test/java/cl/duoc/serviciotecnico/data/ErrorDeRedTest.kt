package cl.duoc.serviciotecnico.data

import cl.duoc.serviciotecnico.data.remote.ErrorDeRed
import cl.duoc.serviciotecnico.data.remote.capturarErrores
import cl.duoc.serviciotecnico.util.Datos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ErrorDeRedTest {

    @Test
    fun `usa el mensaje que envia el microservicio`() {
        val error = Datos.errorHttp(409, "Para cerrar el ticket debes emitir al menos una guia")

        assertEquals("Para cerrar el ticket debes emitir al menos una guia", ErrorDeRed.mensaje(error))
    }

    @Test
    fun `sin cuerpo usa un mensaje segun el codigo`() {
        assertEquals("No tienes permiso para esta acción", ErrorDeRed.mensaje(Datos.errorHttp(403)))
        assertEquals(ErrorDeRed.mensajePorCodigo(500), ErrorDeRed.mensaje(Datos.errorHttp(500)))
    }

    @Test
    fun `sin red informa que no hay conexion`() {
        assertEquals(ErrorDeRed.SIN_CONEXION, ErrorDeRed.mensaje(IOException("timeout")))
    }

    @Test
    fun `mensajes por codigo cubren los casos comunes`() {
        assertEquals("Los datos enviados no son válidos", ErrorDeRed.mensajePorCodigo(400))
        assertEquals("Correo o contraseña incorrectos", ErrorDeRed.mensajePorCodigo(401))
        assertEquals("No se encontró el recurso", ErrorDeRed.mensajePorCodigo(404))
        assertTrue(ErrorDeRed.mensajePorCodigo(418).contains("418"))
    }

    @Test
    fun `capturarErrores transforma excepciones en Result`() {
        val ok = capturarErrores { 5 }
        val falla = capturarErrores { throw IllegalStateException("boom") }

        assertEquals(5, ok.getOrNull())
        assertTrue(falla.isFailure)
        assertEquals("boom", ErrorDeRed.mensaje(falla.exceptionOrNull()!!))
    }
}
