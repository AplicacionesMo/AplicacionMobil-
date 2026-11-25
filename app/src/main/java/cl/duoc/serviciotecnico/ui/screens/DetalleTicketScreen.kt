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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import cl.duoc.serviciotecnico.ui.components.BotonRedondo
import cl.duoc.serviciotecnico.ui.theme.VerdeProfundo
import androidx.compose.foundation.border
import androidx.compose.material3.CardDefaults
import cl.duoc.serviciotecnico.ui.theme.GrisAcentoClaro
import androidx.compose.material3.TextButton
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
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.components.FichaEsqueleto
import cl.duoc.serviciotecnico.ui.components.GaleriaDeFotos
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketUiState
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketViewModel
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioCambioEstado
import cl.duoc.serviciotecnico.util.Mapas
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Build
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.ui.components.CajaDeAviso
import cl.duoc.serviciotecnico.ui.components.DatoConIcono
import cl.duoc.serviciotecnico.ui.components.DatoConPildora
import cl.duoc.serviciotecnico.ui.components.TituloDeSeccion
import cl.duoc.serviciotecnico.ui.theme.EstadoCancelado
import cl.duoc.serviciotecnico.ui.theme.EstadoFinalizado
import cl.duoc.serviciotecnico.ui.theme.EstadoTransito
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
    onEliminado: () -> Unit = onVolver,
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

    // Al eliminar con exito se vuelve al listado
    LaunchedEffect(estado.eliminado) {
        if (estado.eliminado) onEliminado()
    }

    // Al guardar el cambio de estado la pantalla se cierra: el ticket ya quedo actualizado
    LaunchedEffect(estado.cambioAplicado) {
        if (estado.cambioAplicado) onVolver()
    }

    var confirmarEliminar by remember { mutableStateOf(false) }
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar ticket") },
            text = { Text("Se eliminará el ticket y su historial. Solo es posible si no tiene guías emitidas.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    vm.eliminarTicket()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    val ticket = estado.ticket

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(if (ticket != null) "Ticket N° ${ticket.numero}" else "Ticket") },
                navigationIcon = {
                    BotonRedondo(
                            icono = Icons.AutoMirrored.Filled.ArrowBack,
                            descripcion = "Volver",
                            onClick = onVolver
                        )
                },
                actions = {
                    IconButton(onClick = vm::sincronizar, enabled = !estado.sincronizando) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                    // Solo el coordinador ve el boton de eliminar
                    if (estado.puedeEliminar) {
                        IconButton(onClick = { confirmarEliminar = true }, enabled = !estado.eliminando) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar ticket")
                        }
                    }
                    // Si el ticket esta cerrado o cancelado, editar queda deshabilitado
                    IconButton(onClick = onEditarTicket, enabled = estado.editable) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar ticket")
                    }
                }
            )
        },
        floatingActionButton = {
            // La guia la emite el tecnico en terreno, no el coordinador
            if (estado.editable && estado.puedeCrearGuias) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaGuia,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva guía") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        if (estado.sincronizando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(relleno))
        }
        when {
            // Esqueleto con la forma del detalle: ficha del ticket, equipo y guias
            estado.cargando -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FichaEsqueleto(lineas = 5, descripcion = "Cargando el ticket")
                FichaEsqueleto(lineas = 3, descripcion = "Cargando el equipo")
            }

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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
                    DatoConIcono(Icons.Filled.Sell, "Cliente", ticket.cliente)
                    DatoConPildora(
                        icono = Icons.Filled.Flag,
                        etiqueta = "Prioridad",
                        valor = ticket.prioridad.etiqueta,
                        color = colorDePrioridad(ticket.prioridad)
                    )
                    DatoConIcono(
                        Icons.Filled.Build,
                        "Técnico asignado",
                        ticket.tecnicoNombre ?: "Sin asignar"
                    )
                    DatoConIcono(
                        Icons.Filled.CalendarMonth,
                        "Creado",
                        ticket.fechaCreacion.format(formatoFecha)
                    )
                    DatoConIcono(
                        icono = Icons.Filled.LocationOn,
                        etiqueta = "Dirección",
                        valor = ticket.direccion
                    ) {
                        BotonRedondo(
                            icono = Icons.Filled.Map,
                            descripcion = "Abrir en el mapa",
                            onClick = { Mapas.abrirDireccion(context, ticket.direccion) }
                        )
                    }
                    HorizontalDivider()
                    Text(ticket.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---------- Equipo ----------
        estado.equipo?.let { equipo ->
            item {
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TituloDeSeccion(Icons.Filled.Inventory2, "Equipo")
                        DatoConIcono(Icons.Filled.Label, "Nombre", equipo.nombre)
                        DatoConIcono(Icons.Filled.Category, "Modelo", equipo.modelo)
                        DatoConIcono(Icons.Filled.QrCode2, "N° de serie", equipo.serie)
                        DatoConIcono(Icons.Filled.Shield, "Propiedad", equipo.propiedad.etiqueta)
                    }
                }
            }
        }

        // ---------- Cambio de estado, o reapertura si el ticket ya esta cerrado ----------
        if (estado.editable || estado.puedeReabrir) {
            item {
                FormularioDeCambio(
                    actual = ticket.estado,
                    opciones = estado.estadosDisponibles,
                    formulario = estado.formulario,
                    tieneGuias = estado.guias.isNotEmpty(),
                    reabriendo = estado.reabriendo,
                    exigeMotivo = estado.exigeMotivo,
                    onSeleccionarEstado = vm::seleccionarEstado,
                    onMotivoCambia = vm::cambiarMotivo,
                    onEnviar = vm::enviarCambioDeEstado
                )
            }
        }
        if (!estado.editable) {
            item {
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TituloDeSeccion(
                            Icons.Filled.Lock,
                            "Ticket ${ticket.estado.etiqueta.lowercase()}"
                        )
                        ticket.solucion?.let {
                            DatoConIcono(Icons.Filled.CheckCircle, "Solución", it)
                        }
                        ticket.motivoCancelacion?.let {
                            DatoConIcono(Icons.Filled.Cancel, "Motivo", it)
                        }
                        ticket.fechaCierre?.let {
                            DatoConIcono(Icons.Filled.EventAvailable, "Fecha de cierre", it.format(formatoFecha))
                        }
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
    opciones: List<EstadoTicket>,
    formulario: FormularioCambioEstado,
    tieneGuias: Boolean,
    reabriendo: Boolean,
    exigeMotivo: Boolean,
    onSeleccionarEstado: (EstadoTicket) -> Unit,
    onMotivoCambia: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TituloDeSeccion(
                if (reabriendo) Icons.Filled.LockOpen else Icons.Filled.SwapHoriz,
                if (reabriendo) "Reabrir ticket" else "Cambiar estado"
            )
            Text(
                "Estado actual: ${actual.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SelectorDeEstado(
                seleccionado = formulario.estadoSeleccionado,
                opciones = opciones,
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

            // Al cancelar y al reabrir hay que explicar por que
            AnimatedVisibility(visible = exigeMotivo) {
                CampoTextoValidado(
                    valor = formulario.motivo,
                    onValorCambia = onMotivoCambia,
                    etiqueta = if (reabriendo) "Motivo de la reapertura" else "Motivo de la cancelación",
                    obligatorio = true,
                    lineasMinimas = 3,
                    lineasMaximas = 6,
                    error = formulario.errorMotivo,
                    habilitado = !formulario.enviando
                )
            }

            // El detalle del trabajo va en la guia, no aqui
            if (reabriendo) {
                CajaDeAviso(
                    "Al reabrirlo volverá a Abierto y se borrará su fecha de cierre. " +
                        "El motivo queda en el historial."
                )
            } else if (!tieneGuias) {
                CajaDeAviso("Para finalizar el ticket primero debes crear una guía de servicio.")
            }

            BotonGuardar(
                onClick = onEnviar,
                texto = if (reabriendo) "Reabrir ticket" else "Enviar cambio de estado",
                guardando = formulario.enviando
            )
        }
    }
}

@Composable
private fun SelectorDeEstado(
    seleccionado: EstadoTicket?,
    opciones: List<EstadoTicket>,
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
            opciones.forEach { opcion ->
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
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeProfundo,
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GrisAcentoClaro, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Guía N° ${guia.numero}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VerdeProfundo
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
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = VerdeProfundo,
        modifier = Modifier.padding(top = 8.dp)
    )
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
            text = etiqueta.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

/** La prioridad se lee de un vistazo por el color de su pildora. */
@Composable
private fun colorDePrioridad(prioridad: Prioridad): Color = when (prioridad) {
    Prioridad.BAJA -> EstadoFinalizado
    Prioridad.MEDIA -> EstadoTransito
    Prioridad.ALTA -> EstadoCancelado
}
