package cl.duoc.serviciotecnico

import android.app.Application
import cl.duoc.serviciotecnico.data.local.AppDatabase
import cl.duoc.serviciotecnico.data.remote.RetrofitService
import cl.duoc.serviciotecnico.data.repository.ClimaRepository
import cl.duoc.serviciotecnico.data.repository.EquipoRepository
import cl.duoc.serviciotecnico.data.repository.GuiaRepository
import cl.duoc.serviciotecnico.data.repository.PreferenciasSesion
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.data.repository.TicketRepository
import cl.duoc.serviciotecnico.data.repository.UsuarioRepository

/**
 * Contenedor de dependencias: aqui se arman una sola vez los repositorios,
 * cada uno con su API (Retrofit) y sus DAO (Room). Los ViewModel los piden por su factory.
 */
class ServicioTecnicoApp : Application() {
    private val baseDeDatos by lazy { AppDatabase.obtener(this) }

    val ticketRepository by lazy {
        TicketRepository(
            api = RetrofitService.ticketsApi,
            ticketDao = baseDeDatos.ticketDao(),
            equipoDao = baseDeDatos.equipoDao(),
            guiaDao = baseDeDatos.guiaDao(),
            cambioEstadoDao = baseDeDatos.cambioEstadoDao()
        )
    }

    val equipoRepository by lazy {
        EquipoRepository(baseDeDatos.equipoDao())
    }

    val guiaRepository by lazy {
        GuiaRepository(RetrofitService.guiasApi, baseDeDatos.guiaDao(), baseDeDatos.fotoGuiaDao())
    }

    val sesionRepository by lazy {
        SesionRepository(RetrofitService.usuariosApi, PreferenciasSesion(this))
    }

    val usuarioRepository by lazy {
        UsuarioRepository(RetrofitService.usuariosApi, sesionRepository)
    }

    val climaRepository by lazy {
        ClimaRepository(RetrofitService.climaApi)
    }
}
