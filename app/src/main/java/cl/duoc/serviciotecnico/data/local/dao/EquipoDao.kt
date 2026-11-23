package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos WHERE id = :id")
    fun observarPorId(id: String): Flow<Equipo?>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun obtenerPorId(id: String): Equipo?

    @Upsert
    suspend fun upsert(equipo: Equipo)

    @Upsert
    suspend fun upsertTodos(equipos: List<Equipo>)
}
