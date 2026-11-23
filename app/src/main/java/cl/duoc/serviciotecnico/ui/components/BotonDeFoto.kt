package cl.duoc.serviciotecnico.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.serviciotecnico.util.ArchivosDeFoto
import cl.duoc.serviciotecnico.util.Permisos

@Composable
fun BotonDeFoto(
    onFotoTomada: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Agregar fotografia"
) {
    val context = LocalContext.current
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }

    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = uriPendiente
        uriPendiente = null
        if (exito && uri != null) {
            onFotoTomada(uri)
        } else if (uri != null) {
            ArchivosDeFoto.eliminar(context, uri)
        }
    }

    fun abrirCamara() {
        val archivo = ArchivosDeFoto.crearArchivo(context)
        val uri = ArchivosDeFoto.uriDeArchivo(context, archivo)
        uriPendiente = uri
        camara.launch(uri)
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else permisoDenegado = true
    }

    OutlinedButton(
        onClick = {
            if (Permisos.concedido(context, Manifest.permission.CAMERA)) {
                abrirCamara()
            } else {
                solicitarPermiso.launch(Manifest.permission.CAMERA)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(etiqueta)
    }

    if (permisoDenegado) {
        DialogoPermisoDenegado(
            titulo = "Permiso de camara",
            explicacion = "La camara se usa para adjuntar fotografias del trabajo " +
                "realizado a la guia de servicio. Puedes guardar la guia sin fotografias.",
            onCerrar = { permisoDenegado = false }
        )
    }
}
