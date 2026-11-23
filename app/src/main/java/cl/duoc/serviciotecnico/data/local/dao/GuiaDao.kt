package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.serviciotecnico.data.local.entity.Guia
import kotlinx.coroutines.flow.Flow

@Dao
interface GuiaDao {
    @Query("SELECT * FROM guias WHERE ticketId = :ticketId ORDER BY numero DESC")
    fun observarPorTicket(ticketId: String): Flow<List<Guia>>

    @Query("SELECT * FROM guias WHERE id = :id")
    fun observarPorId(id: String): Flow<Guia?>

    @Query("SELECT * FROM guias WHERE id = :id")
    suspend fun obtenerPorId(id: String): Guia?

    @Query("SELECT COUNT(*) FROM guias WHERE ticketId = :ticketId")
    suspend fun contarPorTicket(ticketId: String): Int

    @Query("SELECT COUNT(*) FROM guias WHERE ticketId = :ticketId")
    fun observarCantidadPorTicket(ticketId: String): Flow<Int>

    @Upsert
    suspend fun upsert(guia: Guia)

    @Upsert
    suspend fun upsertTodas(guias: List<Guia>)

    @Query("DELETE FROM guias WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM guias WHERE ticketId = :ticketId AND id NOT IN (:ids)")
    suspend fun eliminarDeTicketExcepto(ticketId: String, ids: List<String>)

    @Query("DELETE FROM guias WHERE ticketId = :ticketId")
    suspend fun eliminarDeTicket(ticketId: String)
}
