package cl.duoc.serviciotecnico.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object Mapas {

    fun abrirCoordenadas(context: Context, latitud: Double, longitud: Double, etiqueta: String) {
        val destino = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud(${Uri.encode(etiqueta)})")
        abrir(context, destino)
    }

    fun abrirDireccion(context: Context, direccion: String) {
        abrir(context, Uri.parse("geo:0,0?q=${Uri.encode(direccion)}"))
    }

    private fun abrir(context: Context, destino: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, destino)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
