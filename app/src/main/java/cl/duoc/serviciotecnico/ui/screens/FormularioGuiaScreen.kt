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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.ui.components.BotonDeFoto
import cl.duoc.serviciotecnico.ui.components.BotonDeUbicacion
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.components.FichaEsqueleto
import cl.duoc.serviciotecnico.ui.components.GaleriaDeFotos
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioGuiaViewModel
import cl.duoc.serviciotecnico.util.ArchivosDeFoto

/** Alto de los campos donde se espera un parrafo, no una linea. */
private const val LINEAS_DE_PARRAFO = 5
private const val LINEAS_MAXIMAS_DE_PARRAFO = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioGuiaScreen(
    ticketId: String,
    onVolver: () -> Unit,
    vm: FormularioGuiaViewModel = viewModel(factory = FormularioGuiaViewModel.Factory(ticketId))
) {
    val context = LocalContext.current
    val ticket by vm.ticket.collectAsStateWithLifecycle()
    val estado by vm.estado.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva guia de servicio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        val actual = ticket

        if (actual == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FichaEsqueleto(lineas = 3, descripcion = "Cargando el ticket")
                FichaEsqueleto(lineas = 4, descripcion = "Cargando el formulario")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ticket N° ${actual.numero} - ${actual.cliente}",
                style = MaterialTheme.typography.titleMedium
            )

            // La direccion y el tecnico no se escriben: los trae el ticket y la sesion activa
            DatoHeredado("Direccion del servicio", actual.direccion)
            DatoHeredado("Tecnico", vm.tecnico)

            HorizontalDivider()

            CampoTextoValidado(
                valor = estado.trabajoRealizado,
                onValorCambia = vm::cambiarTrabajo,
                etiqueta = "Trabajo realizado",
                obligatorio = true,
                lineasMinimas = LINEAS_DE_PARRAFO,
                lineasMaximas = LINEAS_MAXIMAS_DE_PARRAFO,
                error = estado.errorVisible(estado.errorTrabajo)
            )

            CampoTextoValidado(
                valor = estado.repuestos,
                onValorCambia = vm::cambiarRepuestos,
                etiqueta = "Repuestos utilizados",
                lineasMinimas = LINEAS_DE_PARRAFO,
                lineasMaximas = LINEAS_MAXIMAS_DE_PARRAFO
            )

            CampoTextoValidado(
                valor = estado.observaciones,
                onValorCambia = vm::cambiarObservaciones,
                etiqueta = "Observaciones",
                lineasMinimas = LINEAS_DE_PARRAFO,
                lineasMaximas = LINEAS_MAXIMAS_DE_PARRAFO
            )

            CampoTextoValidado(
                valor = estado.recibidoPor,
                onValorCambia = vm::cambiarRecibidoPor,
                etiqueta = "Recibido por",
                obligatorio = true,
                accionDelTeclado = ImeAction.Done,
                error = estado.errorVisible(estado.errorRecibidoPor)
            )

            HorizontalDivider()

            Text("Fotografias del trabajo", style = MaterialTheme.typography.titleMedium)
            BotonDeFoto(
                onFotoTomada = { uri -> vm.agregarFoto(uri.toString()) },
                modifier = Modifier.fillMaxWidth()
            )
            GaleriaDeFotos(
                fotos = estado.fotos.map { Uri.parse(it) },
                onQuitarFoto = { uri ->
                    vm.quitarFoto(uri.toString())
                    ArchivosDeFoto.eliminar(context, uri)
                }
            )

            HorizontalDivider()

            Text("Ubicacion del servicio", style = MaterialTheme.typography.titleMedium)
            BotonDeUbicacion(
                onUbicacionObtenida = vm::registrarUbicacion,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (estado.tieneUbicacion) {
                    "Latitud ${estado.latitud}, longitud ${estado.longitud}"
                } else {
                    "Sin coordenadas registradas. La guia se puede guardar igual."
                },
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            BotonGuardar(
                onClick = vm::guardar,
                texto = "Guardar guia",
                habilitado = estado.esValido,
                guardando = estado.guardando
            )
        }
    }
}

/** Dato que la guia toma del ticket o de la sesion: se muestra, no se escribe. */
@Composable
private fun DatoHeredado(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor.ifBlank { "Sin dato" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
