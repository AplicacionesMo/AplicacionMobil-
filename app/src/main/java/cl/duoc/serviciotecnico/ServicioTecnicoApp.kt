package cl.duoc.serviciotecnico

import android.app.Application
import cl.duoc.serviciotecnico.data.local.AppDatabase
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository

class ServicioTecnicoApp : Application() {
    private val baseDeDatos by lazy { AppDatabase.obtener(this) }

    val ticketRepository by lazy {
        TicketRepository(baseDeDatos.ticketDao(), baseDeDatos.guiaDao())
    }

    val equipoRepository by lazy {
        EquipoRepository(baseDeDatos.equipoDao())
    }

    val guiaRepository by lazy {
        GuiaRepository(baseDeDatos.guiaDao(), baseDeDatos.fotoGuiaDao())
    }
}
