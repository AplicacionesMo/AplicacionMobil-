package cl.duoc.serviciotecnico.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.ui.components.GaleriaDeFotos
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleGuiaViewModel
import cl.duoc.serviciotecnico.util.Mapas
import java.time.format.DateTimeFormatter

private val formatoFechaGuia = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGuiaScreen(
    guiaId: String,
    onVolver: () -> Unit,
    vm: DetalleGuiaViewModel = viewModel(factory = DetalleGuiaViewModel.factory(guiaId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    val guia = estado.guia

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (guia != null) "Guía N° ${guia.numero}" else "Guía de servicio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        when {
            estado.cargando -> Box(
                Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            guia == null -> Box(
                Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { Text("No se encontró la guía") }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DatoGuia("Fecha", guia.fecha.format(formatoFechaGuia))
                        DatoGuia("Técnico", guia.tecnico)
                        DatoGuia("Recibido por", guia.recibidoPor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DatoGuia("Dirección", guia.direccion, Modifier.weight(1f))
                            IconButton(onClick = { Mapas.abrirDireccion(context, guia.direccion) }) {
                                Icon(Icons.Filled.Map, contentDescription = "Abrir dirección en el mapa")
                            }
                        }
                        val lat = guia.latitud
                        val lon = guia.longitud
                        if (lat != null && lon != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DatoGuia("Coordenadas", "%.5f, %.5f".format(lat, lon), Modifier.weight(1f))
                                IconButton(onClick = {
                                    Mapas.abrirCoordenadas(context, lat, lon, "Guía N° ${guia.numero}")
                                }) {
                                    Icon(Icons.Filled.Map, contentDescription = "Ver coordenadas en el mapa")
                                }
                            }
                        }
                    }
                }

                estado.equipo?.let { equipo ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Equipo atendido", style = MaterialTheme.typography.titleMedium)
                            DatoGuia("Nombre", equipo.nombre)
                            DatoGuia("Modelo", equipo.modelo)
                            DatoGuia("N° de serie", equipo.serie)
                            DatoGuia("Propiedad", equipo.propiedad.etiqueta)
                        }
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Trabajo", style = MaterialTheme.typography.titleMedium)
                        DatoGuia("Trabajo realizado", guia.trabajoRealizado)
                        HorizontalDivider()
                        DatoGuia("Repuestos", guia.repuestos.ifBlank { "Sin repuestos" })
                        DatoGuia("Observaciones", guia.observaciones.ifBlank { "Sin observaciones" })
                    }
                }

                val fotos = remember(estado.fotos) { estado.fotos.map { Uri.parse(it.uri) } }
                GaleriaDeFotos(fotos = fotos)
            }
        }
    }
}

@Composable
private fun DatoGuia(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
