package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Query("SELECT MAX(numero) FROM guias")
    suspend fun ultimoNumero(): Int?

    @Insert
    suspend fun insertar(guia: Guia)
}
