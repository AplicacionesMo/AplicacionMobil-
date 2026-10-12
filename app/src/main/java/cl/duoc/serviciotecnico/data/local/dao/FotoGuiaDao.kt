package cl.duoc.serviciotecnico.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.serviciotecnico.data.local.entity.FotoGuia
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoGuiaDao {
    @Query("SELECT * FROM fotos_guia WHERE guiaId = :guiaId ORDER BY fechaCaptura")
    fun observarPorGuia(guiaId: String): Flow<List<FotoGuia>>

    @Query("SELECT * FROM fotos_guia WHERE guiaId = :guiaId ORDER BY fechaCaptura")
    suspend fun obtenerPorGuia(guiaId: String): List<FotoGuia>

    @Insert
    suspend fun insertar(foto: FotoGuia)

    @Insert
    suspend fun insertarTodas(fotos: List<FotoGuia>)

    @Delete
    suspend fun eliminar(foto: FotoGuia)
}
