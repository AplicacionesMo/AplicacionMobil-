package cl.duoc.serviciotecnico.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.data.local.entity.CambioEstado
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.data.local.entity.Ticket
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.components.BotonDeFoto
import cl.duoc.serviciotecnico.ui.components.BotonDeUbicacion
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.components.GaleriaDeFotos
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketUiState
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketViewModel
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioCambioEstado
import cl.duoc.serviciotecnico.ui.viewmodel.MAX_FOTOS_CAMBIO
import cl.duoc.serviciotecnico.ui.viewmodel.estadosParaTecnico
import cl.duoc.serviciotecnico.util.ArchivosDeFoto
import cl.duoc.serviciotecnico.util.Mapas
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTicketScreen(
    ticketId: String,
    onVolver: () -> Unit,
    onEditarTicket: () -> Unit,
    onNuevaGuia: () -> Unit,
    onAbrirGuia: (String) -> Unit,
    vm: DetalleTicketViewModel = viewModel(factory = DetalleTicketViewModel.factory(ticketId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    val ticket = estado.ticket

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ticket != null) "Ticket N° ${ticket.numero}" else "Ticket") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Si el ticket esta cerrado o cancelado, editar queda deshabilitado
                    IconButton(onClick = onEditarTicket, enabled = estado.editable) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar ticket")
                    }
                }
            )
        },
        floatingActionButton = {
            if (estado.editable) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaGuia,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva guía") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        when {
            estado.cargando -> Box(
                Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            ticket == null -> Box(
                Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { Text("No se encontró el ticket") }

            else -> ContenidoDetalle(
                estado = estado,
                ticket = ticket,
                relleno = relleno,
                vm = vm,
                onAbrirGuia = onAbrirGuia
            )
        }
    }
}

@Composable
private fun ContenidoDetalle(
    estado: DetalleTicketUiState,
    ticket: Ticket,
    relleno: PaddingValues,
    vm: DetalleTicketViewModel,
    onAbrirGuia: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(relleno),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- Datos del ticket ----------
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            ticket.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        EtiquetaEstado(estado = ticket.estado)
                    }
                    Dato("Cliente", ticket.cliente)
                    Dato("Prioridad", ticket.prioridad.etiqueta)
                    Dato("Creado", ticket.fechaCreacion.format(formatoFecha))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dato("Dirección", ticket.direccion, Modifier.weight(1f))
                        IconButton(onClick = { Mapas.abrirDireccion(context, ticket.direccion) }) {
                            Icon(Icons.Filled.Map, contentDescription = "Abrir en el mapa")
                        }
                    }
                    HorizontalDivider()
                    Text(ticket.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---------- Equipo ----------
        estado.equipo?.let { equipo ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Equipo", style = MaterialTheme.typography.titleMedium)
                        Dato("Nombre", equipo.nombre)
                        Dato("Modelo", equipo.modelo)
                        Dato("N° de serie", equipo.serie)
                        Dato("Propiedad", equipo.propiedad.etiqueta)
                    }
                }
            }
        }

        // ---------- Cambio de estado (solo si se puede editar) ----------
        if (estado.editable) {
            item {
                FormularioDeCambio(
                    actual = ticket.estado,
                    formulario = estado.formulario,
                    onSeleccionarEstado = vm::seleccionarEstado,
                    onComentarioCambia = vm::cambiarComentario,
                    onFotoTomada = { uri -> vm.agregarFoto(uri.toString()) },
                    onQuitarFoto = { uri ->
                        vm.quitarFoto(uri.toString())
                        ArchivosDeFoto.eliminar(context, uri)
                    },
                    onUbicacionObtenida = vm::ubicacionObtenida,
                    onVerEnMapa = { lat, lon ->
                        Mapas.abrirCoordenadas(context, lat, lon, "Ubicación del cambio de estado")
                    },
                    onEnviar = vm::enviarCambioDeEstado
                )
            }
        } else {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ticket ${ticket.estado.etiqueta.lowercase()}", style = MaterialTheme.typography.titleMedium)
                        ticket.solucion?.let { Dato("Solución", it) }
                        ticket.motivoCancelacion?.let { Dato("Motivo", it) }
                        ticket.fechaCierre?.let { Dato("Fecha de cierre", it.format(formatoFecha)) }
                        Text(
                            "Este ticket es de solo lectura.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- Guias de servicio ----------
        item {
            Titulo("Guías de servicio (${estado.guias.size})")
        }
        if (estado.guias.isEmpty()) {
            item { TextoSecundario("Aún no hay guías. Para cerrar el ticket necesitas al menos una.") }
        } else {
            items(estado.guias, key = { it.id }) { guia ->
                TarjetaGuia(guia = guia, onClick = { onAbrirGuia(guia.id) }, modifier = Modifier.animateItem())
            }
        }

        // ---------- Historial de estados ----------
        item {
            Titulo("Historial de estados (${estado.historial.size})")
        }
        if (estado.historial.isEmpty()) {
            item { TextoSecundario("Todavía no hay cambios de estado registrados.") }
        } else {
            items(estado.historial, key = { it.id }) { cambio ->
                TarjetaCambio(cambio = cambio, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun FormularioDeCambio(
    actual: EstadoTicket,
    formulario: FormularioCambioEstado,
    onSeleccionarEstado: (EstadoTicket) -> Unit,
    onComentarioCambia: (String) -> Unit,
    onFotoTomada: (Uri) -> Unit,
    onQuitarFoto: (Uri) -> Unit,
    onUbicacionObtenida: (Double, Double) -> Unit,
    onVerEnMapa: (Double, Double) -> Unit,
    onEnviar: () -> Unit
) {
    val fotos = remember(formulario.fotos) { formulario.fotos.map(Uri::parse) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cambiar estado", style = MaterialTheme.typography.titleMedium)
            Text(
                "Estado actual: ${actual.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Nuevo estado
            SelectorDeEstado(
                seleccionado = formulario.estadoSeleccionado,
                actual = actual,
                habilitado = !formulario.enviando,
                onSeleccionar = onSeleccionarEstado
            )
            AnimatedVisibility(visible = formulario.errorEstado != null) {
                Text(
                    formulario.errorEstado.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 2. Comentario
            CampoTextoValidado(
                valor = formulario.comentario,
                onValorCambia = onComentarioCambia,
                etiqueta = "Comentario",
                obligatorio = true,
                lineasMaximas = 4,
                error = formulario.errorComentario,
                habilitado = !formulario.enviando
            )

            // 3. Fotos (hasta 5)
            Text(
                "Fotos (${formulario.fotos.size} de $MAX_FOTOS_CAMBIO)",
                style = MaterialTheme.typography.labelLarge
            )
            if (formulario.puedeAgregarFotos && !formulario.enviando) {
                BotonDeFoto(onFotoTomada = onFotoTomada, etiqueta = "Agregar foto")
            }
            if (fotos.isNotEmpty()) {
                GaleriaDeFotos(fotos = fotos, onQuitarFoto = onQuitarFoto)
            }

            // 4. Ubicacion (obligatoria)
            Text("Ubicación *", style = MaterialTheme.typography.labelLarge)
            if (!formulario.enviando) {
                BotonDeUbicacion(
                    onUbicacionObtenida = onUbicacionObtenida,
                    etiqueta = if (formulario.tieneUbicacion) "Actualizar ubicación" else "Registrar mi ubicación"
                )
            }
            val lat = formulario.latitud
            val lon = formulario.longitud
            if (lat != null && lon != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lat: %.5f · Lon: %.5f".format(lat, lon),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onVerEnMapa(lat, lon) }) {
                        Icon(Icons.Filled.Map, contentDescription = "Ver en el mapa")
                    }
                }
            }
            AnimatedVisibility(visible = formulario.errorUbicacion != null) {
                Text(
                    formulario.errorUbicacion.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 5. Enviar
            BotonGuardar(
                onClick = onEnviar,
                texto = "Enviar cambio de estado",
                guardando = formulario.enviando
            )
        }
    }
}

@Composable
private fun SelectorDeEstado(
    seleccionado: EstadoTicket?,
    actual: EstadoTicket,
    habilitado: Boolean,
    onSeleccionar: (EstadoTicket) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { abierto = true },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                seleccionado?.let { "Nuevo estado: ${it.etiqueta}" } ?: "Selecciona el nuevo estado",
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            estadosParaTecnico.filter { it != actual }.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion.etiqueta,
                            fontWeight = if (opcion == seleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        abierto = false
                        onSeleccionar(opcion)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaCambio(cambio: CambioEstado, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fotos = remember(cambio.urisDeFotos) { cambio.urisDeFotos.map(Uri::parse) }

    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${cambio.estadoAnterior.etiqueta} → ${cambio.estadoNuevo.etiqueta}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    cambio.fecha.format(formatoFecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(cambio.comentario, style = MaterialTheme.typography.bodyMedium)
            val lat = cambio.latitud
            val lon = cambio.longitud
            if (lat != null && lon != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Ubicación: %.5f, %.5f".format(lat, lon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        Mapas.abrirCoordenadas(context, lat, lon, "Cambio a ${cambio.estadoNuevo.etiqueta}")
                    }) {
                        Icon(Icons.Filled.Map, contentDescription = "Ver en el mapa")
                    }
                }
            }
            if (fotos.isNotEmpty()) {
                // Sin onQuitarFoto: la galeria es de solo lectura
                GaleriaDeFotos(fotos = fotos)
            }
        }
    }
}

@Composable
private fun TarjetaGuia(guia: Guia, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Guía N° ${guia.numero}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    guia.fecha.format(formatoFecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Técnico: ${guia.tecnico}", style = MaterialTheme.typography.bodyMedium)
            Text(
                guia.trabajoRealizado,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun TextoSecundario(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}