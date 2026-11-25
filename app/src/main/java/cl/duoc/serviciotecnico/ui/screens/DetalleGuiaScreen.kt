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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import cl.duoc.serviciotecnico.ui.components.BotonRedondo
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import cl.duoc.serviciotecnico.ui.components.DatoConIcono
import cl.duoc.serviciotecnico.ui.components.TituloDeSeccion
import androidx.compose.foundation.border
import androidx.compose.material3.CardDefaults
import cl.duoc.serviciotecnico.ui.theme.GrisAcentoClaro
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.serviciotecnico.ui.theme.VerdeProfundo
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(if (guia != null) "Guía N° ${guia.numero}" else "Guía de servicio") },
                navigationIcon = {
                    BotonRedondo(
                            icono = Icons.AutoMirrored.Filled.ArrowBack,
                            descripcion = "Volver",
                            onClick = onVolver
                        )
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
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TituloDeSeccion(Icons.Filled.Description, "Guía N° ${guia.numero}")
                        DatoConIcono(Icons.Filled.CalendarMonth, "Fecha", guia.fecha.format(formatoFechaGuia))
                        DatoConIcono(Icons.Filled.Build, "Técnico", guia.tecnico)
                        DatoConIcono(Icons.Filled.HowToReg, "Recibido por", guia.recibidoPor)
                        DatoConIcono(
                            icono = Icons.Filled.LocationOn,
                            etiqueta = "Dirección",
                            valor = guia.direccion
                        ) {
                            BotonRedondo(
                                icono = Icons.Filled.Map,
                                descripcion = "Abrir dirección en el mapa",
                                onClick = { Mapas.abrirDireccion(context, guia.direccion) }
                            )
                        }
                        val lat = guia.latitud
                        val lon = guia.longitud
                        if (lat != null && lon != null) {
                            DatoConIcono(
                                icono = Icons.Filled.MyLocation,
                                etiqueta = "Coordenadas",
                                valor = "%.5f, %.5f".format(lat, lon)
                            ) {
                                BotonRedondo(
                                    icono = Icons.Filled.Map,
                                    descripcion = "Ver coordenadas en el mapa",
                                    onClick = {
                                        Mapas.abrirCoordenadas(context, lat, lon, "Guía N° ${guia.numero}")
                                    }
                                )
                            }
                        }
                    }
                }

                estado.equipo?.let { equipo ->
                    Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TituloDeSeccion(Icons.Filled.Inventory2, "Equipo atendido")
                            DatoConIcono(Icons.Filled.Label, "Nombre", equipo.nombre)
                            DatoConIcono(Icons.Filled.Category, "Modelo", equipo.modelo)
                            DatoConIcono(Icons.Filled.QrCode2, "N° de serie", equipo.serie)
                            DatoConIcono(Icons.Filled.Shield, "Propiedad", equipo.propiedad.etiqueta)
                        }
                    }
                }

                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TituloDeSeccion(Icons.Filled.Handyman, "Trabajo")
                        DatoConIcono(Icons.Filled.Checklist, "Trabajo realizado", guia.trabajoRealizado)
                        HorizontalDivider()
                        DatoConIcono(
                            Icons.Filled.Settings,
                            "Repuestos",
                            guia.repuestos.ifBlank { "Sin repuestos" }
                        )
                        DatoConIcono(
                            Icons.Filled.Notes,
                            "Observaciones",
                            guia.observaciones.ifBlank { "Sin observaciones" }
                        )
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
