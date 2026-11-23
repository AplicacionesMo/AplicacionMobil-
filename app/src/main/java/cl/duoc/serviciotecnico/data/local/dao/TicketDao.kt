package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import kotlinx.coroutines.flow.Flow

/**
 * Room funciona como cache: la pantalla siempre lee de aqui (Flow) y el repositorio
 * lo rellena con lo que responde el microservicio.
 */
@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY numero DESC")
    fun observarTodos(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun observarPorId(id: String): Flow<Ticket?>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun obtenerPorId(id: String): Ticket?

    /** Inserta o actualiza. No usa REPLACE porque REPLACE borraria en cascada las guias. */
    @Upsert
    suspend fun upsert(ticket: Ticket)

    @Upsert
    suspend fun upsertTodos(tickets: List<Ticket>)

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    /** Quita de la cache los tickets que ya no existen en el servidor. */
    @Query("DELETE FROM tickets WHERE id NOT IN (:ids)")
    suspend fun eliminarExcepto(ids: List<String>)

    @Query("DELETE FROM tickets")
    suspend fun eliminarTodos()
}
