package cl.duoc.serviciotecnico.data.remote

import cl.duoc.serviciotecnico.BuildConfig
import cl.duoc.serviciotecnico.data.remote.api.ClimaApi
import cl.duoc.serviciotecnico.data.remote.api.GuiasApi
import cl.duoc.serviciotecnico.data.remote.api.TicketsApi
import cl.duoc.serviciotecnico.data.remote.api.UsuariosApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Punto unico donde se construyen los clientes HTTP.
 *  - Un solo OkHttpClient compartido (reutiliza conexiones).
 *  - Un Retrofit por URL base: tres microservicios propios + una API externa.
 *  - Gson convierte JSON <-> data class.
 *  - El interceptor imprime cada peticion en el Logcat (filtro "okhttp").
 */
object RetrofitService {

    private val registro = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(registro)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ---------- Microservicios propios (Spring Boot) ----------
    val usuariosApi: UsuariosApi by lazy { crear(BuildConfig.URL_USUARIOS).create(UsuariosApi::class.java) }
    val ticketsApi: TicketsApi by lazy { crear(BuildConfig.URL_TICKETS).create(TicketsApi::class.java) }
    val guiasApi: GuiasApi by lazy { crear(BuildConfig.URL_GUIAS).create(GuiasApi::class.java) }

    // ---------- API externa ----------
    val climaApi: ClimaApi by lazy { crear(BuildConfig.URL_CLIMA).create(ClimaApi::class.java) }
}
