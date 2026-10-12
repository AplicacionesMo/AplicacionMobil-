package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos WHERE id = :id")
    fun observarPorId(id: String): Flow<Equipo?>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun obtenerPorId(id: String): Equipo?

    @Query("SELECT * FROM equipos WHERE serie = :serie LIMIT 1")
    suspend fun buscarPorSerie(serie: String): Equipo?

    @Insert
    suspend fun insertar(equipo: Equipo)

    @Update
    suspend fun actualizar(equipo: Equipo)
}
