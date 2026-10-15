package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.viewmodel.CampoLogin
import cl.duoc.serviciotecnico.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onIngresado: () -> Unit,
    vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    // Al ingresar con exito, se pasa al listado de tickets
    LaunchedEffect(estado.ingresado) {
        if (estado.ingresado) onIngresado()
    }

    Scaffold { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Filled.AcUnit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Servicio Técnico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Ingresa con tu cuenta de técnico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            CampoTextoValidado(
                valor = estado.correo,
                onValorCambia = vm::cambiarCorreo,
                etiqueta = "Correo",
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Email,
                error = estado.errorVisible(CampoLogin.CORREO),
                habilitado = !estado.ingresando
            )
            Spacer(Modifier.height(8.dp))
            CampoTextoValidado(
                valor = estado.clave,
                onValorCambia = vm::cambiarClave,
                etiqueta = "Contraseña",
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Password,
                accionDelTeclado = ImeAction.Done,
                esClave = true,
                error = estado.errorVisible(CampoLogin.CLAVE),
                habilitado = !estado.ingresando
            )

            // Mensaje de credenciales incorrectas, animado
            AnimatedVisibility(
                visible = estado.mensaje != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = estado.mensaje.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = vm::ingresar,
                enabled = !estado.ingresando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (estado.ingresando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresando")
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresar")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Ayuda para el prototipo: credenciales del usuario de prueba
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Usuario de prueba",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "${SesionRepository.USUARIO_DEMO}\n${SesionRepository.CLAVE_DEMO}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}