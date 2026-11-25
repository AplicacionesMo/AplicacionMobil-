package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.domain.model.Usuario
import cl.duoc.serviciotecnico.ui.viewmodel.UsuariosViewModel

/** Pantalla del administrador: lista de usuarios, eliminar y boton para crear. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    onVolver: () -> Unit,
    onCrearUsuario: () -> Unit,
    vm: UsuariosViewModel = viewModel(factory = UsuariosViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Usuario?>(null) }

    // Se recarga cada vez que se entra, incluso al volver de "crear usuario"
    LaunchedEffect(Unit) { vm.cargar() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    porEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar a ${usuario.nombre} (${usuario.correo})? Ya no podrá iniciar sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    porEliminar = null
                    vm.eliminar(usuario)
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = vm::cargar, enabled = !estado.cargando) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (estado.permitido) {
                FloatingActionButton(onClick = onCrearUsuario) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Crear usuario")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        if (!estado.permitido) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("Solo el administrador puede gestionar usuarios")
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(relleno)) {
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.usuarios, key = { it.id }) { usuario ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    usuario.correo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AssistChip(onClick = {}, label = { Text(usuario.rol.etiqueta) })
                            }
                            IconButton(
                                onClick = { porEliminar = usuario },
                                enabled = estado.eliminandoId != usuario.id
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${usuario.nombre}")
                            }
                        }
                    }
                }
            }
        }
    }
}
