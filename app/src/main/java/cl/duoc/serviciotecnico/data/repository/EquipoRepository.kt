package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.EquipoDao
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import kotlinx.coroutines.flow.Flow

/**
 * Lectura de equipos desde la cache. Los equipos se crean y editan en el servidor
 * junto con su ticket (ver TicketRepository), que los deja guardados aqui.
 */
class EquipoRepository(
    private val equipoDao: EquipoDao
) {
    fun observarEquipo(id: String): Flow<Equipo?> = equipoDao.observarPorId(id)

    suspend fun obtenerEquipo(id: String): Equipo? = equipoDao.obtenerPorId(id)
}
