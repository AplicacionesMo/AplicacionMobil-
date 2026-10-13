package cl.duoc.serviciotecnico.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGuiaScreen(
    guiaId: String,
    onVolver: () -> Unit,
    vm: DetalleGuiaViewModel = viewModel(factory = DetalleGuiaViewModel.Factory(guiaId))
) {
    val context = LocalContext.current
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.guia?.let { "Guia N° ${it.numero}" } ?: "Guia de servicio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        val guia = estado.guia

        if (estado.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (guia == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) {
                Text("La guia no existe", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Campo("Fecha de la visita", guia.fecha.format(formatoFecha))
            Campo("Tecnico", guia.tecnico)
            Campo("Direccion del servicio", guia.direccion)

            if (guia.latitud != null && guia.longitud != null) {
                Campo("Coordenadas", "${guia.latitud}, ${guia.longitud}")
                OutlinedButton(
                    onClick = {
                        Mapas.abrirCoordenadas(
                            context = context,
                            latitud = guia.latitud,
                            longitud = guia.longitud,
                            etiqueta = "Guia N° ${guia.numero}"
                        )
                    }
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Text("  Abrir en el mapa")
                }
            } else {
                Campo("Coordenadas", "No se registraron")
                OutlinedButton(onClick = { Mapas.abrirDireccion(context, guia.direccion) }) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Text("  Buscar la direccion en el mapa")
                }
            }

            HorizontalDivider()

            Text("Equipo atendido", style = MaterialTheme.typography.titleMedium)
            val equipo = estado.equipo
            if (equipo == null) {
                Text("Sin equipo registrado", style = MaterialTheme.typography.bodyMedium)
            } else {
                Campo("Nombre", equipo.nombre)
                Campo("Modelo", equipo.modelo)
                Campo("Serie", equipo.serie)
                Campo("Propiedad", equipo.propiedad.etiqueta)
            }

            HorizontalDivider()

            Campo("Trabajo realizado", guia.trabajoRealizado)
            Campo("Repuestos utilizados", guia.repuestos)
            Campo("Observaciones", guia.observaciones)
            Campo("Recibido por", guia.recibidoPor)

            HorizontalDivider()

            GaleriaDeFotos(
                fotos = estado.fotos.map { Uri.parse(it.uri) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Campo(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = valor.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge)
    }
}
