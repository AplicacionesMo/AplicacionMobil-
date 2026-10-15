package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.CambioEstadoDao
import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.local.dao.TicketDao
import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TicketRepository(
    private val ticketDao: TicketDao,
    private val guiaDao: GuiaDao,
    private val cambioEstadoDao: CambioEstadoDao
) {
    fun observarTickets(): Flow<List<Ticket>> = ticketDao.observarTodos()

    fun observarTicket(id: String): Flow<Ticket?> = ticketDao.observarPorId(id)

    suspend fun obtenerTicket(id: String): Ticket? = ticketDao.obtenerPorId(id)

    suspend fun crear(
        titulo: String,
        descripcion: String,
        cliente: String,
        direccion: String,
        prioridad: Prioridad,
        equipoId: String
    ): Ticket {
        val ticket = Ticket(
            numero = (ticketDao.ultimoNumero() ?: 0) + 1,
            titulo = titulo,
            descripcion = descripcion,
            cliente = cliente,
            direccion = direccion,
            prioridad = prioridad,
            equipoId = equipoId
        )
        ticketDao.insertar(ticket)
        return ticket
    }

    suspend fun actualizar(ticket: Ticket) {
        ticketDao.actualizar(ticket)
    }

    suspend fun cambiarEstado(ticketId: String, nuevoEstado: EstadoTicket) {
        val ticket = ticketDao.obtenerPorId(ticketId) ?: return
        ticketDao.actualizar(ticket.copy(estado = nuevoEstado))
    }

    suspend fun tieneGuiasEmitidas(ticketId: String): Boolean =
        guiaDao.contarPorTicket(ticketId) > 0

    suspend fun cerrar(ticketId: String, solucion: String) {
        val ticket = ticketDao.obtenerPorId(ticketId) ?: return
        ticketDao.actualizar(
            ticket.copy(
                estado = EstadoTicket.CERRADO,
                fechaCierre = LocalDateTime.now(),
                solucion = solucion
            )
        )
    }

    suspend fun cancelar(ticketId: String, motivo: String) {
        val ticket = ticketDao.obtenerPorId(ticketId) ?: return
        ticketDao.actualizar(
            ticket.copy(
                estado = EstadoTicket.CANCELADO,
                motivoCancelacion = motivo
            )
        )
    }

    fun observarHistorial(ticketId: String): Flow<List<CambioEstado>> =
        cambioEstadoDao.observarPorTicket(ticketId)

    /**
     * Aplica un cambio de estado y deja registro con el comentario y las fotos.
     * Si el nuevo estado es Cerrado, el comentario queda como la solucion.
     */
    suspend fun registrarCambioDeEstado(
        ticketId: String,
        nuevoEstado: EstadoTicket,
        comentario: String,
        urisDeFotos: List<String>,
        latitud: Double?,
        longitud: Double?
    ) {
        val ticket = ticketDao.obtenerPorId(ticketId) ?: return
        when (nuevoEstado) {
            EstadoTicket.CERRADO -> cerrar(ticketId, comentario)
            EstadoTicket.CANCELADO -> cancelar(ticketId, comentario)
            else -> cambiarEstado(ticketId, nuevoEstado)
        }
        cambioEstadoDao.insertar(
            CambioEstado(
                ticketId = ticketId,
                estadoAnterior = ticket.estado,
                estadoNuevo = nuevoEstado,
                comentario = comentario,
                urisDeFotos = urisDeFotos,
                latitud = latitud,
                longitud = longitud
            )
        )
    }
}
