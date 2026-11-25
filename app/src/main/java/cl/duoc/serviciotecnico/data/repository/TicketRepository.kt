package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.CambioEstadoDao
import cl.duoc.serviciotecnico.data.local.dao.EquipoDao
import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.local.dao.TicketDao
import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.data.remote.aEntidad
import cl.duoc.serviciotecnico.data.remote.aRequest
import cl.duoc.serviciotecnico.data.remote.api.TicketsApi
import cl.duoc.serviciotecnico.data.remote.dto.CambioEstadoRequestDto
import cl.duoc.serviciotecnico.data.remote.dto.TicketDto
import cl.duoc.serviciotecnico.domain.model.DatosTicket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Rol
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/**
 * Unica puerta a los tickets.
 *  - Escribir: siempre contra el microservicio ms-tickets (la fuente de verdad).
 *  - Leer: la pantalla observa Room (Flow). Cada respuesta del servidor se guarda en Room,
 *    y Room avisa a la pantalla. Si no hay red se siguen viendo los ultimos datos.
 */
class TicketRepository(
    private val api: TicketsApi,
    private val ticketDao: TicketDao,
    private val equipoDao: EquipoDao,
    private val guiaDao: GuiaDao,
    private val cambioEstadoDao: CambioEstadoDao
) {
    // ---------- Lectura (cache local) ----------

    fun observarTickets(): Flow<List<Ticket>> = ticketDao.observarTodos()

    fun observarTicket(id: String): Flow<Ticket?> = ticketDao.observarPorId(id)

    suspend fun obtenerTicket(id: String): Ticket? = ticketDao.obtenerPorId(id)

    fun observarHistorial(ticketId: String): Flow<List<CambioEstado>> =
        cambioEstadoDao.observarPorTicket(ticketId)

    suspend fun tieneGuiasEmitidas(ticketId: String): Boolean =
        guiaDao.contarPorTicket(ticketId) > 0

    // ---------- Sincronizacion (GET) ----------

    /** Descarga todos los tickets y deja la cache igual al servidor. */
    suspend fun sincronizar() {
        val remotos = api.listar()
        remotos.forEach { guardarEnCache(it) }
        val ids = remotos.mapNotNull { it.id }
        if (ids.isEmpty()) ticketDao.eliminarTodos() else ticketDao.eliminarExcepto(ids)
    }

    /** Refresca un ticket y su historial. Si el servidor ya no lo tiene (404), se borra local. */
    suspend fun sincronizarTicket(id: String) {
        try {
            guardarEnCache(api.obtener(id))
            sincronizarHistorial(id)
        } catch (e: HttpException) {
            if (e.code() == 404) ticketDao.eliminarPorId(id) else throw e
        }
    }

    suspend fun sincronizarHistorial(ticketId: String) {
        val cambios = api.historial(ticketId).map { it.aEntidad() }
        cambioEstadoDao.eliminarDeTicket(ticketId)
        cambioEstadoDao.insertarTodos(cambios)
    }

    // ---------- Escritura (POST, PUT, PATCH, DELETE) ----------

    suspend fun crear(datos: DatosTicket, rol: Rol): Ticket {
        val respuesta = api.crear(datos.aRequest(), rol.name)
        return guardarEnCache(respuesta)
    }

    suspend fun actualizar(id: String, datos: DatosTicket): Ticket {
        val respuesta = api.actualizar(id, datos.aRequest())
        return guardarEnCache(respuesta)
    }

    /**
     * Aplica un cambio de estado en el servidor. Si es Finalizado, el comentario queda como
     * solucion; si es Cancelado, como motivo. Luego se refresca el historial.
     * El comentario es opcional: si va vacio, ms-tickets deja una glosa automatica.
     */
    suspend fun registrarCambioDeEstado(
        ticketId: String,
        nuevoEstado: EstadoTicket,
        rol: Rol,
        comentario: String = "",
        urisDeFotos: List<String> = emptyList(),
        latitud: Double? = null,
        longitud: Double? = null
    ): Ticket {
        val respuesta = api.cambiarEstado(
            id = ticketId,
            request = CambioEstadoRequestDto(
                estadoNuevo = nuevoEstado.name,
                comentario = comentario,
                urisDeFotos = urisDeFotos,
                latitud = latitud,
                longitud = longitud
            ),
            rol = rol.name
        )
        val ticket = guardarEnCache(respuesta)
        sincronizarHistorial(ticketId)
        return ticket
    }

    suspend fun eliminar(ticketId: String, rol: Rol) {
        val respuesta = api.eliminar(ticketId, rol.name)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        // Room borra en cascada sus guias e historial locales
        ticketDao.eliminarPorId(ticketId)
    }

    // ---------- Apoyo ----------

    /** Primero el equipo (el ticket tiene llave foranea hacia el) y luego el ticket. */
    private suspend fun guardarEnCache(dto: TicketDto): Ticket {
        val equipo = requireNotNull(dto.equipo) { "El ticket llego sin equipo" }.aEntidad()
        equipoDao.upsert(equipo)
        val ticket = dto.aEntidad()
        ticketDao.upsert(ticket)
        return ticket
    }
}
