package cl.duoc.serviciotecnico.domain

import cl.duoc.serviciotecnico.domain.validation.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** Reglas de validacion en Kotlin puro: no necesitan emulador ni mocks. */
class ValidacionesTest {

    @Test
    fun `noVacio devuelve error con texto en blanco`() {
        assertEquals("El cliente es obligatorio", Validaciones.noVacio("   ", "El cliente"))
        assertNull(Validaciones.noVacio("Banco Uno", "El cliente"))
    }

    @Test
    fun `largoMinimo distingue vacio, corto y correcto`() {
        assertEquals("El titulo es obligatorio", Validaciones.largoMinimo("", 5, "El titulo"))
        assertEquals("El titulo debe tener al menos 5 caracteres", Validaciones.largoMinimo(" abc ", 5, "El titulo"))
        assertNull(Validaciones.largoMinimo("No enfria", 5, "El titulo"))
    }

    @Test
    fun `fechaNoFutura rechaza fechas futuras`() {
        assertEquals("La fecha no puede ser futura",
            Validaciones.fechaNoFutura(LocalDateTime.now().plusDays(1), "La fecha"))
        assertNull(Validaciones.fechaNoFutura(LocalDateTime.now().minusMinutes(1), "La fecha"))
    }

    @Test
    fun `correoValido exige arroba`() {
        assertEquals("El correo es obligatorio", Validaciones.correoValido(""))
        assertEquals("El correo debe tener @", Validaciones.correoValido("tecnico.cl"))
        assertNull(Validaciones.correoValido("tecnico@tacticaldev.cl"))
    }

    @Test
    fun `serieValida no acepta vacio`() {
        assertEquals("El numero de serie es obligatorio", Validaciones.serieValida(""))
        assertNull(Validaciones.serieValida("ABC123"))
    }

    @Test
    fun `todoCorrecto solo si no hay errores`() {
        assertTrue(Validaciones.todoCorrecto(null, null))
        assertFalse(Validaciones.todoCorrecto(null, "error"))
    }
}
