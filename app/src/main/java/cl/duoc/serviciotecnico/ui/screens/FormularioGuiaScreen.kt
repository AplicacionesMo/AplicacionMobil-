
package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.serviciotecnico.ServicioTecnicoApp
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioGuiaViewModel

@Composable
fun FormularioGuiaScreen(
    ticketId: String,
    onVolver: () -> Unit
) {
    val app = LocalContext.current.applicationContext
            as ServicioTecnicoApp

    val vm: FormularioGuiaViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                FormularioGuiaViewModel(app.guiaRepository)
            }
        }
    )

    val ticket by remember(ticketId) {
        app.ticketRepository.observarTicket(ticketId)
    }.collectAsState(initial = null)

    val mensaje by vm.mensaje.collectAsState()
    val guardado by vm.guardado.collectAsState()

    var tecnico by remember { mutableStateOf("") }
    var trabajo by remember { mutableStateOf("") }
    var repuestos by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var recibidoPor by remember { mutableStateOf("") }

    var direccion by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    LaunchedEffect(ticket?.id) {
        if (direccion.isBlank()) {
            direccion = ticket?.direccion.orEmpty()
        }
    }

    LaunchedEffect(guardado) {
        if (guardado) {
            onVolver()
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje.isNotBlank()) {
            guardando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Nueva guía de servicio",
            style = MaterialTheme.typography.headlineMedium
        )

        if (ticket == null) {
            Text("Cargando datos del ticket...")
        } else {
            val actual = ticket!!

            Text("Ticket N° ${actual.numero}")
            Text("Cliente: ${actual.cliente}")

            if (!actual.editable) {
                Text("Este ticket ya está finalizado")
            } else {

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tecnico,
                    onValueChange = { tecnico = it },
                    label = { Text("Nombre del técnico") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = trabajo,
                    onValueChange = { trabajo = it },
                    label = { Text("Trabajo realizado") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repuestos,
                    onValueChange = { repuestos = it },
                    label = { Text("Repuestos utilizados") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recibidoPor,
                    onValueChange = { recibidoPor = it },
                    label = { Text("Recibido por") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        guardando = true
                        vm.guardarGuia(
                            ticketId = ticketId,
                            equipoId = actual.equipoId,
                            direccion = direccion,
                            tecnico = tecnico,
                            trabajoRealizado = trabajo,
                            repuestos = repuestos,
                            observaciones = observaciones,
                            recibidoPor = recibidoPor
                        )
                    },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (guardando) "Guardando..."
                        else "Guardar guía"
                    )
                }
            }
        }

        if (mensaje.isNotBlank()) {
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
