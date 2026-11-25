package cl.duoc.serviciotecnico.domain

import cl.duoc.serviciotecnico.domain.model.ClimaActual
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.util.Datos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasDeDominioTest {

    // ---------- Estados ----------

    @Test
    fun `solo finalizado y cancelado son finales`() {
        val finales = EstadoTicket.entries.filter { it.esFinal }
        assertEquals(listOf(EstadoTicket.FINALIZADO, EstadoTicket.CANCELADO), finales)
        assertEquals(listOf(EstadoTicket.ABIERTO, EstadoTicket.TRANSITO), EstadoTicket.intermedios)
    }

    @Test
    fun `solo finalizado exige una guia`() {
        assertTrue(EstadoTicket.FINALIZADO.exigeGuia)
        assertFalse(EstadoTicket.ABIERTO.exigeGuia)
        assertFalse(EstadoTicket.TRANSITO.exigeGuia)
        assertFalse(EstadoTicket.CANCELADO.exigeGuia)
    }

    @Test
    fun `un ticket final no es editable`() {
        assertTrue(Datos.ticket(EstadoTicket.TRANSITO).editable)
        assertFalse(Datos.ticket(EstadoTicket.FINALIZADO).editable)
        assertFalse(Datos.ticket(EstadoTicket.CANCELADO).editable)
    }

    // ---------- Roles ----------

    @Test
    fun `el tecnico puede finalizar pero no crear, cancelar ni eliminar`() {
        assertTrue(EstadoTicket.FINALIZADO in Rol.TECNICO.estadosPermitidos)
        assertFalse(EstadoTicket.CANCELADO in Rol.TECNICO.estadosPermitidos)
        assertFalse(Rol.TECNICO.puedeCrearTickets)
        assertFalse(Rol.TECNICO.puedeEliminarTickets)
        assertFalse(Rol.TECNICO.puedeGestionarUsuarios)
    }

    @Test
    fun `el coordinador crea, cancela y elimina tickets pero no gestiona usuarios`() {
        assertTrue(EstadoTicket.CANCELADO in Rol.COORDINADOR.estadosPermitidos)
        assertTrue(Rol.COORDINADOR.puedeCrearTickets)
        assertTrue(Rol.COORDINADOR.puedeEliminarTickets)
        assertFalse(Rol.COORDINADOR.puedeGestionarUsuarios)
    }

    @Test
    fun `el admin puede todo`() {
        assertTrue(EstadoTicket.CANCELADO in Rol.ADMIN.estadosPermitidos)
        assertTrue(Rol.ADMIN.puedeCrearTickets)
        assertTrue(Rol.ADMIN.puedeEliminarTickets)
        assertTrue(Rol.ADMIN.puedeGestionarUsuarios)
    }

    @Test
    fun `un rol desconocido se trata como tecnico`() {
        assertEquals(Rol.COORDINADOR, Rol.desde("COORDINADOR"))
        assertEquals(Rol.ADMIN, Rol.desde("ADMIN"))
        assertEquals(Rol.TECNICO, Rol.desde("SUPERUSUARIO"))
        assertEquals(Rol.TECNICO, Rol.desde(null))
    }

    // ---------- Clima (API externa) ----------

    @Test
    fun `codigos WMO se traducen a texto`() {
        assertEquals("Despejado", ClimaActual.describir(0))
        assertEquals("Nublado", ClimaActual.describir(3))
        assertEquals("Lluvia", ClimaActual.describir(63))
        assertEquals("Lluvia", ClimaActual.describir(81))
        assertEquals("Tormenta", ClimaActual.describir(95))
        assertEquals("Sin datos", ClimaActual.describir(-1))
    }

    @Test
    fun `la recomendacion depende de la temperatura`() {
        assertTrue(ClimaActual.recomendar(31.0).startsWith("Calor"))
        assertTrue(ClimaActual.recomendar(28.0).startsWith("Calor"))
        assertTrue(ClimaActual.recomendar(5.0).startsWith("Frío"))
        assertTrue(ClimaActual.recomendar(18.0).startsWith("Templado"))
    }

    @Test
    fun `clima actual expone descripcion y recomendacion`() {
        val clima = ClimaActual(temperatura = 30.0, humedad = 20, viento = 10.0, codigo = 0)
        assertEquals("Despejado", clima.descripcion)
        assertTrue(clima.recomendacion.contains("aire acondicionado"))
    }

    @Test
    fun `el resumen del clima no revienta por el signo de porcentaje`() {
        // Armar "Humedad 45% ..." dentro de String.format lanzaba
        // UnknownFormatConversionException y cerraba la app al entrar al listado
        val clima = ClimaActual(temperatura = 21.5, humedad = 45, viento = 12.4, codigo = 3)

        assertTrue(clima.resumen.startsWith("Humedad 45%"))
        assertTrue(clima.resumen.endsWith("12 km/h"))
        assertTrue(clima.titulo.contains("Nublado"))
    }
}
