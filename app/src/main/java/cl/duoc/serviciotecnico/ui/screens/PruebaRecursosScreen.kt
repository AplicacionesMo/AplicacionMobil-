package cl.duoc.serviciotecnico.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.domain.validation.Validaciones
import cl.duoc.serviciotecnico.ui.components.BotonDeFoto
import cl.duoc.serviciotecnico.ui.components.BotonDeUbicacion
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.components.GaleriaDeFotos
import cl.duoc.serviciotecnico.util.ArchivosDeFoto
import cl.duoc.serviciotecnico.util.Mapas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebaRecursosScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val fotos = remember { mutableStateListOf<Uri>() }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var textoDePrueba by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var indiceDeEstado by remember { mutableStateOf(0) }

    val errorDeTexto = Validaciones.largoMinimo(textoDePrueba, 5, "El texto de prueba")
    val estado = EstadoTicket.entries[indiceDeEstado]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prueba de recursos nativos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Camara", style = MaterialTheme.typography.titleMedium)
            BotonDeFoto(onFotoTomada = { uri -> fotos.add(uri) })
            GaleriaDeFotos(
                fotos = fotos,
                onQuitarFoto = { uri ->
                    fotos.remove(uri)
                    ArchivosDeFoto.eliminar(context, uri)
                }
            )

            HorizontalDivider()

            Text("Ubicacion", style = MaterialTheme.typography.titleMedium)
            BotonDeUbicacion(
                onUbicacionObtenida = { lat, lon ->
                    latitud = lat
                    longitud = lon
                }
            )
            Text(
                text = if (latitud != null && longitud != null) {
                    "Latitud: $latitud\nLongitud: $longitud"
                } else {
                    "Sin coordenadas registradas"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (latitud != null && longitud != null) {
                OutlinedButton(
                    onClick = {
                        Mapas.abrirCoordenadas(
                            context = context,
                            latitud = latitud!!,
                            longitud = longitud!!,
                            etiqueta = "Lugar del servicio"
                        )
                    }
                ) {
                    Text("Abrir en el mapa")
                }
            }

            HorizontalDivider()

            Text("Animaciones", style = MaterialTheme.typography.titleMedium)
            EtiquetaEstado(estado = estado)
            OutlinedButton(
                onClick = {
                    indiceDeEstado = (indiceDeEstado + 1) % EstadoTicket.entries.size
                }
            ) {
                Text("Cambiar estado")
            }
            CampoTextoValidado(
                valor = textoDePrueba,
                onValorCambia = { textoDePrueba = it },
                etiqueta = "Texto de prueba",
                obligatorio = true,
                error = errorDeTexto
            )
            BotonGuardar(
                onClick = { guardando = true },
                texto = "Simular guardado",
                habilitado = errorDeTexto == null,
                guardando = guardando
            )

            LaunchedEffect(guardando) {
                if (guardando) {
                    kotlinx.coroutines.delay(1500)
                    guardando = false
                }
            }
        }
    }
}
