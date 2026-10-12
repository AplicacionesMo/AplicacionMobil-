package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.local.dao.TicketDao
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.model.Prioridad
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TicketRepository(
    private val ticketDao: TicketDao,
    private val guiaDao: GuiaDao
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
}
