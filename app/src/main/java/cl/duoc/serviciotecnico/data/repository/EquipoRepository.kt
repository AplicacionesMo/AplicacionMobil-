package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.EquipoDao
import cl.duoc.serviciotecnico.data.local.entity.Equipo
import cl.duoc.serviciotecnico.domain.model.Propiedad
import kotlinx.coroutines.flow.Flow

class EquipoRepository(
    private val equipoDao: EquipoDao
) {
    fun observarEquipo(id: String): Flow<Equipo?> = equipoDao.observarPorId(id)

    suspend fun obtenerEquipo(id: String): Equipo? = equipoDao.obtenerPorId(id)

    suspend fun buscarPorSerie(serie: String): Equipo? = equipoDao.buscarPorSerie(serie)

    suspend fun crear(
        nombre: String,
        modelo: String,
        serie: String,
        propiedad: Propiedad
    ): Equipo {
        val equipo = Equipo(
            nombre = nombre,
            modelo = modelo,
            serie = serie,
            propiedad = propiedad
        )
        equipoDao.insertar(equipo)
        return equipo
    }

    suspend fun actualizar(equipo: Equipo) {
        equipoDao.actualizar(equipo)
    }
}
