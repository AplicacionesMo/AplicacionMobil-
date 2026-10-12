package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY numero DESC")
    fun observarTodos(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun observarPorId(id: String): Flow<Ticket?>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun obtenerPorId(id: String): Ticket?

    @Query("SELECT MAX(numero) FROM tickets")
    suspend fun ultimoNumero(): Int?

    @Insert
    suspend fun insertar(ticket: Ticket)

    @Update
    suspend fun actualizar(ticket: Ticket)
}
