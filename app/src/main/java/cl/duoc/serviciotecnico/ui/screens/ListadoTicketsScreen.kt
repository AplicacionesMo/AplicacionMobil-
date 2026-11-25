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
import androidx.compose.material.icons.filled.ManageAccounts
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
import androidx.compose.foundation.border
import cl.duoc.serviciotecnico.ui.theme.GrisAcentoClaro
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
import cl.duoc.serviciotecnico.ui.components.BotonRedondo
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.components.ListaDeTicketsEsqueleto
import cl.duoc.serviciotecnico.ui.viewmodel.EstadoClima
import cl.duoc.serviciotecnico.ui.viewmodel.ListadoTicketsViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import cl.duoc.serviciotecnico.R
import cl.duoc.serviciotecnico.ui.theme.BlancoTexto
import cl.duoc.serviciotecnico.ui.theme.NegroTexto
import cl.duoc.serviciotecnico.ui.theme.VerdeMarca
import cl.duoc.serviciotecnico.ui.theme.VerdeProfundo
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoTicketsScreen(
    onCrearTicket: () -> Unit,
    onAbrirTicket: (String) -> Unit,
    onGestionarUsuarios: () -> Unit = {},
    onSesionCerrada: () -> Unit = {},
    vm: ListadoTicketsViewModel = viewModel(factory = ListadoTicketsViewModel.Factory)
) {
    val tickets by vm.tickets.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val sincronizando by vm.sincronizando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val clima by vm.clima.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_servicio),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "SERVICIO TÉCNICO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            vm.usuario?.let {
                                Text(
                                    "${it.nombre} · ${it.rol.etiqueta}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    BotonRedondo(
                        icono = Icons.Filled.Refresh,
                        descripcion = "Actualizar desde el servidor",
                        habilitado = !sincronizando,
                        onClick = vm::refrescar
                    )
                    // Solo el administrador ve el boton de usuarios
                    if (vm.usuario?.rol?.puedeGestionarUsuarios == true) {
                        Spacer(Modifier.width(8.dp))
                        BotonRedondo(
                            icono = Icons.Filled.ManageAccounts,
                            descripcion = "Gestionar usuarios",
                            onClick = onGestionarUsuarios
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    BotonRedondo(
                        icono = Icons.AutoMirrored.Filled.Logout,
                        descripcion = "Cerrar sesion",
                        onClick = {
                            vm.cerrarSesion()
                            onSesionCerrada()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            // El tecnico no crea tickets: solo coordinador y administrador ven el boton
            if (vm.usuario?.rol?.puedeCrearTickets == true) {
                FloatingActionButton(
                    onClick = onCrearTicket,
                    containerColor = VerdeProfundo,
                    contentColor = BlancoTexto,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear ticket")
                }
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

            // Mientras llegan los datos se muestra el esqueleto. Antes decia "No hay
            // tickets", que es justo lo contrario de lo que estaba pasando.
            if (tickets.isEmpty() && sincronizando) {
                ListaDeTicketsEsqueleto()
            } else if (tickets.isEmpty()) {
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
                label = { Text("Todos") },
                shape = RoundedCornerShape(50),
                colors = coloresDeChip()
            )
        }
        items(EstadoTicket.entries) { estado ->
            FilterChip(
                selected = seleccionado == estado,
                onClick = { onSeleccionar(estado) },
                label = { Text(estado.etiqueta) },
                shape = RoundedCornerShape(50),
                colors = coloresDeChip()
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoTexto),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "N° ${ticket.numero}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VerdeProfundo
                    )
                    EtiquetaEstado(estado = ticket.estado)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    ticket.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    ticket.cliente,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = ticket.fechaCreacion.format(formatoFecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeMarca.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = VerdeProfundo
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
                        estado.clima.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NegroTexto
                    )
                    Text(
                        estado.clima.resumen,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        estado.clima.recomendacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

/** Chip seleccionado en verde oscuro relleno; sin seleccionar, blanco con borde. */
@Composable
private fun coloresDeChip() = FilterChipDefaults.filterChipColors(
    containerColor = BlancoTexto,
    labelColor = NegroTexto,
    selectedContainerColor = VerdeProfundo,
    selectedLabelColor = BlancoTexto
)
