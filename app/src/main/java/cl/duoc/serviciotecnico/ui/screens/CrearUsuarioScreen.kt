package cl.duoc.serviciotecnico.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.ui.components.BotonGuardar
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.viewmodel.CampoUsuario
import cl.duoc.serviciotecnico.ui.viewmodel.CrearUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioScreen(
    onVolver: () -> Unit,
    vm: CrearUsuarioViewModel = viewModel(factory = CrearUsuarioViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
            // Tras crear el usuario se vuelve al listado
            if (estado.creado) onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo usuario") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        if (!estado.permitido) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("Solo el administrador puede crear usuarios")
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
            CampoTextoValidado(
                valor = estado.nombre,
                onValorCambia = { vm.cambiarCampo(CampoUsuario.NOMBRE, it) },
                etiqueta = "Nombre completo",
                error = estado.errorVisible(CampoUsuario.NOMBRE),
                obligatorio = true
            )
            CampoTextoValidado(
                valor = estado.correo,
                onValorCambia = { vm.cambiarCampo(CampoUsuario.CORREO, it) },
                etiqueta = "Correo",
                error = estado.errorVisible(CampoUsuario.CORREO),
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Email
            )
            CampoTextoValidado(
                valor = estado.clave,
                onValorCambia = { vm.cambiarCampo(CampoUsuario.CLAVE, it) },
                etiqueta = "Contraseña",
                error = estado.errorVisible(CampoUsuario.CLAVE),
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Password,
                esClave = true
            )
            CampoTextoValidado(
                valor = estado.confirmacion,
                onValorCambia = { vm.cambiarCampo(CampoUsuario.CONFIRMACION, it) },
                etiqueta = "Repetir contraseña",
                error = estado.errorVisible(CampoUsuario.CONFIRMACION),
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Password,
                accionDelTeclado = ImeAction.Done,
                esClave = true
            )

            Text("Rol", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Rol.entries.forEach { rol ->
                    FilterChip(
                        selected = estado.rol == rol,
                        onClick = { vm.cambiarRol(rol) },
                        label = { Text(rol.etiqueta) }
                    )
                }
            }

            BotonGuardar(
                onClick = vm::guardar,
                modifier = Modifier.fillMaxWidth(),
                texto = "Crear usuario",
                guardando = estado.guardando
            )
        }
    }
}
