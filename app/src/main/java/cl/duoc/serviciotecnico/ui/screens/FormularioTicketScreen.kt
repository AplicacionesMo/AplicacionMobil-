package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.domain.model.Prioridad
import cl.duoc.serviciotecnico.domain.model.Propiedad
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.viewmodel.CampoTicket
import cl.duoc.serviciotecnico.ui.viewmodel.FormularioTicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioTicketScreen(
    ticketId: String?,
    onVolver: () -> Unit,
    vm: FormularioTicketViewModel = viewModel(factory = FormularioTicketViewModel.factory(ticketId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Al guardar con exito, se vuelve a la pantalla anterior
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    val titulo = when {
        !estado.esEdicion -> "Nuevo ticket"
        estado.numero != null -> "Editar ticket N° ${estado.numero}"
        else -> "Editar ticket"
    }
    val habilitado = !estado.soloLectura

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->

        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---------- Formulario 1: Ticket ----------
            Text("Datos del ticket", style = MaterialTheme.typography.titleMedium)

            CampoTextoValidado(
                valor = estado.titulo,
                onValorCambia = { vm.cambiarCampo(CampoTicket.TITULO, it) },
                etiqueta = "Titulo",
                obligatorio = true,
                error = estado.errorVisible(CampoTicket.TITULO),
                habilitado = habilitado
            )
            CampoTextoValidado(
                valor = estado.descripcion,
                onValorCambia = { vm.cambiarCampo(CampoTicket.DESCRIPCION, it) },
                etiqueta = "Descripcion del problema",
                obligatorio = true,
                lineasMaximas = 4,
                error = estado.errorVisible(CampoTicket.DESCRIPCION),
                habilitado = habilitado
            )
            CampoTextoValidado(
                valor = estado.cliente,
                onValorCambia = { vm.cambiarCampo(CampoTicket.CLIENTE, it) },
                etiqueta = "Cliente",
                obligatorio = true,
                error = estado.errorVisible(CampoTicket.CLIENTE),
                habilitado = habilitado
            )
            CampoTextoValidado(
                valor = estado.direccion,
                onValorCambia = { vm.cambiarCampo(CampoTicket.DIRECCION, it) },
                etiqueta = "Direccion",
                obligatorio = true,
                error = estado.errorVisible(CampoTicket.DIRECCION),
                habilitado = habilitado
            )

            Text("Prioridad", style = MaterialTheme.typography.labelLarge)
            SelectorDeOpciones(
                opciones = Prioridad.entries,
                seleccionada = estado.prioridad,
                etiqueta = { it.etiqueta },
                onSeleccionar = vm::cambiarPrioridad,
                habilitado = habilitado
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ---------- Formulario 2: Equipo ----------
            Text("Equipo atendido", style = MaterialTheme.typography.titleMedium)

            CampoTextoValidado(
                valor = estado.nombreEquipo,
                onValorCambia = { vm.cambiarCampo(CampoTicket.NOMBRE_EQUIPO, it) },
                etiqueta = "Nombre del equipo",
                obligatorio = true,
                error = estado.errorVisible(CampoTicket.NOMBRE_EQUIPO),
                habilitado = habilitado
            )
            CampoTextoValidado(
                valor = estado.modelo,
                onValorCambia = { vm.cambiarCampo(CampoTicket.MODELO, it) },
                etiqueta = "Modelo",
                obligatorio = true,
                error = estado.errorVisible(CampoTicket.MODELO),
                habilitado = habilitado
            )
            CampoTextoValidado(
                valor = estado.serie,
                onValorCambia = { vm.cambiarCampo(CampoTicket.SERIE, it) },
                etiqueta = "Numero de serie",
                obligatorio = true,
                accionDelTeclado = ImeAction.Done,
                error = estado.errorVisible(CampoTicket.SERIE),
                habilitado = habilitado
            )

            Text("Propiedad del equipo", style = MaterialTheme.typography.labelLarge)
            SelectorDeOpciones(
                opciones = Propiedad.entries,
                seleccionada = estado.propiedad,
                etiqueta = { it.etiqueta },
                onSeleccionar = vm::cambiarPropiedad,
                habilitado = habilitado
            )

            if (habilitado) {
                BotonGuardar(
                    onClick = vm::guardar,
                    texto = if (estado.esEdicion) "Guardar cambios" else "Crear ticket",
                    guardando = estado.guardando,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/** Fila de chips para elegir una opcion de un enum (prioridad, propiedad). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SelectorDeOpciones(
    opciones: List<T>,
    seleccionada: T,
    etiqueta: (T) -> String,
    onSeleccionar: (T) -> Unit,
    habilitado: Boolean
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                label = { Text(etiqueta(opcion)) },
                enabled = habilitado
            )
        }
    }
}