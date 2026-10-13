package cl.duoc.serviciotecnico.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.data.local.entity.Guia
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.components.EtiquetaEstado
import cl.duoc.serviciotecnico.ui.viewmodel.DetalleTicketViewModel
import cl.duoc.serviciotecnico.ui.viewmodel.DialogoFinalizacion
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioFinalizacionState
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
    vm: DetalleTicketViewModel = viewModel(factory = DetalleTicketViewModel.Factory(ticketId))
) {
    val context = LocalContext.current
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val dialogo by vm.dialogoVisible.collectAsStateWithLifecycle()
    val cierre by vm.cierre.collectAsStateWithLifecycle()
    val cancelacion by vm.cancelacion.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.ticket?.let { "Ticket N° ${it.numero}" } ?: "Ticket") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (estado.editable) {
                        IconButton(onClick = onEditarTicket) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar ticket")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        val ticket = estado.ticket

        if (estado.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (ticket == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) {
                Text("El ticket no existe", style = MaterialTheme.typography.bodyLarge)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.titulo, style = MaterialTheme.typography.titleLarge)
                EtiquetaEstado(estado = ticket.estado)
            }

            if (!estado.editable) {
                Text(
                    text = "Este ticket esta ${ticket.estado.etiqueta.lowercase()} y solo se puede consultar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Dato("Cliente", ticket.cliente)
            Dato("Direccion", ticket.direccion)
            Dato("Prioridad", ticket.prioridad.etiqueta)
            Dato("Descripcion", ticket.descripcion)
            Dato("Creado", ticket.fechaCreacion.format(formatoFecha))
            ticket.fechaCierre?.let { Dato("Cerrado", it.format(formatoFecha)) }
            ticket.solucion?.let { Dato("Solucion", it) }
            ticket.motivoCancelacion?.let { Dato("Motivo de cancelacion", it) }

            OutlinedButton(onClick = { Mapas.abrirDireccion(context, ticket.direccion) }) {
                Icon(Icons.Filled.Map, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Ver en el mapa")
            }

            HorizontalDivider()

            Text("Equipo atendido", style = MaterialTheme.typography.titleMedium)
            val equipo = estado.equipo
            if (equipo == null) {
                Text("Sin equipo registrado", style = MaterialTheme.typography.bodyMedium)
            } else {
                Dato("Nombre", equipo.nombre)
                Dato("Modelo", equipo.modelo)
                Dato("Serie", equipo.serie)
                Dato("Propiedad", equipo.propiedad.etiqueta)
            }

            HorizontalDivider()

            AnimatedVisibility(visible = estado.editable) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cambiar estado", style = MaterialTheme.typography.titleMedium)
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(EstadoTicket.intermedios) { opcion ->
                            FilterChip(
                                selected = opcion == ticket.estado,
                                onClick = { vm.cambiarEstado(opcion) },
                                label = { Text(opcion.etiqueta) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Guias de servicio", style = MaterialTheme.typography.titleMedium)
                Text("${estado.guias.size}", style = MaterialTheme.typography.labelLarge)
            }

            if (estado.guias.isEmpty()) {
                Text("Este ticket no tiene guias emitidas", style = MaterialTheme.typography.bodyMedium)
            } else {
                estado.guias.forEach { guia ->
                    TarjetaGuia(guia = guia, onClick = { onAbrirGuia(guia.id) })
                }
            }

            if (estado.editable) {
                OutlinedButton(
                    onClick = onNuevaGuia,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.NoteAdd, contentDescription = null)
                    Text("  Nueva guia de servicio")
                }

                HorizontalDivider()

                Button(
                    onClick = { vm.abrirDialogo(DialogoFinalizacion.CIERRE) },
                    enabled = estado.puedeCerrarse,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Text("  Cerrar ticket")
                }

                if (!estado.puedeCerrarse) {
                    Text(
                        text = "Para cerrar el ticket debe existir al menos una guia emitida",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = { vm.abrirDialogo(DialogoFinalizacion.CANCELACION) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null)
                    Text("  Cancelar ticket")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    when (dialogo) {
        DialogoFinalizacion.CIERRE -> DialogoFinalizacionTicket(
            titulo = "Cerrar ticket",
            explicacion = "Registra el comentario de solucion. Esta accion no se puede deshacer.",
            etiquetaCampo = "Comentario de solucion",
            textoConfirmar = "Cerrar ticket",
            formulario = cierre,
            onTextoCambia = vm::cambiarSolucion,
            onConfirmar = vm::confirmarCierre,
            onCerrar = vm::cerrarDialogo
        )

        DialogoFinalizacion.CANCELACION -> DialogoFinalizacionTicket(
            titulo = "Cancelar ticket",
            explicacion = "Indica el motivo de la cancelacion. Esta accion no se puede deshacer.",
            etiquetaCampo = "Motivo de cancelacion",
            textoConfirmar = "Cancelar ticket",
            formulario = cancelacion,
            onTextoCambia = vm::cambiarMotivo,
            onConfirmar = vm::confirmarCancelacion,
            onCerrar = vm::cerrarDialogo
        )

        DialogoFinalizacion.NINGUNO -> Unit
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = valor.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TarjetaGuia(guia: Guia, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Guia N° ${guia.numero}", style = MaterialTheme.typography.labelLarge)
            Text(guia.fecha.format(formatoFecha), style = MaterialTheme.typography.bodySmall)
            Text(
                text = guia.trabajoRealizado,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DialogoFinalizacionTicket(
    titulo: String,
    explicacion: String,
    etiquetaCampo: String,
    textoConfirmar: String,
    formulario: FormularioFinalizacionState,
    onTextoCambia: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(explicacion, style = MaterialTheme.typography.bodyMedium)
                CampoTextoValidado(
                    valor = formulario.texto,
                    onValorCambia = onTextoCambia,
                    etiqueta = etiquetaCampo,
                    obligatorio = true,
                    error = formulario.errorVisible,
                    lineasMaximas = 3,
                    accionDelTeclado = ImeAction.Done,
                    habilitado = !formulario.guardando
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                enabled = formulario.esValido && !formulario.guardando
            ) {
                if (formulario.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(textoConfirmar)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !formulario.guardando) {
                Text("Volver")
            }
        }
    )
}
