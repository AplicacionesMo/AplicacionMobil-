package cl.duoc.serviciotecnico.data

import cl.duoc.serviciotecnico.data.local.Converters
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** Room guarda fechas, enum y listas como texto: se prueba que ida y vuelta den lo mismo. */
class ConvertersTest {

    private val c = Converters()

    @Test
    fun `fecha ida y vuelta`() {
        val fecha = LocalDateTime.of(2026, 11, 23, 22, 40)
        assertEquals(fecha, c.textoAFecha(c.fechaATexto(fecha)))
        assertNull(c.fechaATexto(null))
        assertNull(c.textoAFecha(null))
    }

    @Test
    fun `enum ida y vuelta`() {
        assertEquals(EstadoTicket.TRANSITO, c.textoAEstado(c.estadoATexto(EstadoTicket.TRANSITO)))
        assertEquals(Prioridad.ALTA, c.textoAPrioridad(c.prioridadATexto(Prioridad.ALTA)))
        assertEquals(Propiedad.ARRENDADO, c.textoAPropiedad(c.propiedadATexto(Propiedad.ARRENDADO)))
    }

    @Test
    fun `lista de fotos ida y vuelta`() {
        val fotos = listOf("content://a", "content://b")
        assertEquals(fotos, c.textoALista(c.listaATexto(fotos)))
        assertTrue(c.textoALista("").isEmpty())
    }
}
