package cl.duoc.serviciotecnico.data.repository

import cl.duoc.serviciotecnico.data.local.dao.FotoGuiaDao
import cl.duoc.serviciotecnico.data.local.dao.GuiaDao
import cl.duoc.serviciotecnico.data.local.entity.FotoGuia
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.remote.aEntidad
import cl.duoc.serviciotecnico.data.remote.api.GuiasApi
import cl.duoc.serviciotecnico.data.remote.dto.GuiaDto
import cl.duoc.serviciotecnico.data.remote.dto.GuiaRequestDto
import cl.duoc.serviciotecnico.data.remote.fotos
import kotlinx.coroutines.flow.Flow

/** Unica puerta a las guias de servicio: escribe en ms-guias y guarda la respuesta en Room. */
class GuiaRepository(
    private val api: GuiasApi,
    private val guiaDao: GuiaDao,
    private val fotoGuiaDao: FotoGuiaDao,
    private val sesion: SesionRepository
) {
    // ---------- Lectura (cache local) ----------

    fun observarGuiasDeTicket(ticketId: String): Flow<List<Guia>> =
        guiaDao.observarPorTicket(ticketId)

    fun observarGuia(id: String): Flow<Guia?> = guiaDao.observarPorId(id)

    fun observarCantidadDeGuias(ticketId: String): Flow<Int> =
        guiaDao.observarCantidadPorTicket(ticketId)

    fun observarFotos(guiaId: String): Flow<List<FotoGuia>> =
        fotoGuiaDao.observarPorGuia(guiaId)

    // ---------- Sincronizacion ----------

    suspend fun sincronizarGuiasDeTicket(ticketId: String) {
        val remotas = api.listarDeTicket(ticketId)
        remotas.forEach { guardarEnCache(it) }
        val ids = remotas.mapNotNull { it.id }
        if (ids.isEmpty()) guiaDao.eliminarDeTicket(ticketId) else guiaDao.eliminarDeTicketExcepto(ticketId, ids)
    }

    suspend fun sincronizarGuia(id: String) {
        guardarEnCache(api.obtener(id))
    }

    // ---------- Escritura ----------

    suspend fun crear(
        ticketId: String,
        equipoId: String,
        direccion: String,
        tecnico: String,
        trabajoRealizado: String,
        repuestos: String,
        observaciones: String,
        recibidoPor: String,
        latitud: Double? = null,
        longitud: Double? = null,
        urisDeFotos: List<String> = emptyList()
    ): Guia {
        val respuesta = api.crear(
            request = GuiaRequestDto(
                ticketId = ticketId,
                equipoId = equipoId,
                direccion = direccion,
                tecnico = tecnico,
                trabajoRealizado = trabajoRealizado,
                repuestos = repuestos,
                observaciones = observaciones,
                recibidoPor = recibidoPor,
                latitud = latitud,
                longitud = longitud,
                urisDeFotos = urisDeFotos
            ),
            rol = sesion.rolActual().name
        )
        return guardarEnCache(respuesta)
    }

    // ---------- Apoyo ----------

    private suspend fun guardarEnCache(dto: GuiaDto): Guia {
        val guia = dto.aEntidad()
        guiaDao.upsert(guia)
        fotoGuiaDao.eliminarDeGuia(guia.id)
        val fotos = dto.fotos()
        if (fotos.isNotEmpty()) fotoGuiaDao.insertarTodas(fotos)
        return guia
    }
}
