package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.viewmodel.EstadoClima
import cl.duoc.serviciotecnico.ui.viewmodel.ListadoTicketsViewModel
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoTicketsScreen(
    onCrearTicket: () -> Unit,
    onAbrirTicket: (String) -> Unit,
    onSesionCerrada: () -> Unit = {},
    vm: ListadoTicketsViewModel = viewModel(factory = ListadoTicketsViewModel.Factory)
) {
    val tickets by vm.tickets.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val sincronizando by vm.sincronizando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val clima by vm.clima.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tickets")
                        vm.usuario?.let {
                            Text(
                                "${it.nombre} · ${it.rol.etiqueta}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = vm::refrescar, enabled = !sincronizando) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar desde el servidor")
                    }
                    IconButton(onClick = {
                        vm.cerrarSesion()
                        onSesionCerrada()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesion")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearTicket) {
                Icon(Icons.Filled.Add, contentDescription = "Crear ticket")
            }
        }
    ) { relleno ->
        Column(modifier = Modifier.fillMaxSize().padding(relleno)) {

            if (sincronizando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            error?.let { AvisoSinConexion(it) }

            TarjetaClima(estado = clima, onReintentar = vm::cargarClima)

            FiltroDeEstados(
                seleccionado = filtro,
                onSeleccionar = vm::cambiarFiltro
            )

            if (tickets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filtro == null) "No hay tickets" else "No hay tickets en este estado",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tickets, key = { it.id }) { ticket ->
                        TarjetaTicket(
                            ticket = ticket,
                            onClick = { onAbrirTicket(ticket.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroDeEstados(
    seleccionado: EstadoTicket?,
    onSeleccionar: (EstadoTicket?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = seleccionado == null,
                onClick = { onSeleccionar(null) },
                label = { Text("Todos") }
            )
        }
        items(EstadoTicket.entries) { estado ->
            FilterChip(
                selected = seleccionado == estado,
                onClick = { onSeleccionar(estado) },
                label = { Text(estado.etiqueta) }
            )
        }
    }
}

@Composable
private fun TarjetaTicket(
    ticket: Ticket,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "N° ${ticket.numero}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                EtiquetaEstado(estado = ticket.estado)
            }
            Spacer(Modifier.height(8.dp))
            Text(ticket.titulo, style = MaterialTheme.typography.titleMedium)
            Text(ticket.cliente, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = ticket.fechaCreacion.format(formatoFecha),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Aviso cuando el microservicio no responde: la lista sigue mostrando la cache de Room. */
@Composable
private fun AvisoSinConexion(texto: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/** API externa Open-Meteo: clima actual en Santiago y su efecto en la demanda. */
@Composable
private fun TarjetaClima(estado: EstadoClima, onReintentar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Thermostat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            when (estado) {
                EstadoClima.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Consultando el clima…", style = MaterialTheme.typography.bodyMedium)
                }

                is EstadoClima.Listo -> Column(Modifier.weight(1f)) {
                    Text(
                        "Santiago ahora: %.1f °C · %s".format(estado.clima.temperatura, estado.clima.descripcion),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Humedad ${estado.clima.humedad}% · Viento %.0f km/h".format(estado.clima.viento),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        estado.clima.recomendacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                is EstadoClima.Error -> {
                    Text(
                        estado.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onReintentar) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reintentar clima")
                    }
                }
            }
        }
    }
}
