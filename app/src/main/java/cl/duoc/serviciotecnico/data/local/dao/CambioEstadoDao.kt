package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import kotlinx.coroutines.flow.Flow

@Dao
interface CambioEstadoDao {
    @Query("SELECT * FROM cambios_estado WHERE ticketId = :ticketId ORDER BY fecha DESC")
    fun observarPorTicket(ticketId: String): Flow<List<CambioEstado>>

    @Insert
    suspend fun insertarTodos(cambios: List<CambioEstado>)

    @Query("DELETE FROM cambios_estado WHERE ticketId = :ticketId")
    suspend fun eliminarDeTicket(ticketId: String)
}
