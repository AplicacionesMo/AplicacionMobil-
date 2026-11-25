package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.serviciotecnico.R
import cl.duoc.serviciotecnico.data.repository.SesionRepository
import cl.duoc.serviciotecnico.domain.model.CuentaDePrueba
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.ui.components.CampoTextoValidado
import cl.duoc.serviciotecnico.ui.theme.BlancoTexto
import cl.duoc.serviciotecnico.ui.theme.NegroCampo
import cl.duoc.serviciotecnico.ui.theme.VerdeBoton
import cl.duoc.serviciotecnico.ui.theme.VerdeMarca
import cl.duoc.serviciotecnico.ui.viewmodel.CampoLogin
import cl.duoc.serviciotecnico.ui.viewmodel.LoginViewModel

/**
 * Unica pantalla oscura de la app: el fondo con las ondas verdes es parte de la marca.
 * De la sesion para adentro todo es blanco.
 */
@Composable
fun LoginScreen(
    onIngresado: () -> Unit,
    vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.ingresado) {
        if (estado.ingresado) onIngresado()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondo_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.logo_servicio),
                contentDescription = "Logo de TacticalDEV",
                modifier = Modifier.size(160.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "SERVICIO TÉCNICO",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BlancoTexto
            )
            Text(
                text = "T A C T I C A L D E V",
                style = MaterialTheme.typography.bodyMedium,
                color = BlancoTexto.copy(alpha = 0.75f),
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            CampoTextoValidado(
                valor = estado.correo,
                onValorCambia = vm::cambiarCorreo,
                etiqueta = "Correo",
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Email,
                error = estado.errorVisible(CampoLogin.CORREO),
                habilitado = !estado.ingresando,
                icono = Icons.Filled.Email,
                etiquetaFlotante = false,
                colores = coloresDeCampoOscuro()
            )
            Spacer(Modifier.height(12.dp))
            CampoTextoValidado(
                valor = estado.clave,
                onValorCambia = vm::cambiarClave,
                etiqueta = "Contraseña",
                obligatorio = true,
                tipoDeTeclado = KeyboardType.Password,
                accionDelTeclado = ImeAction.Done,
                esClave = true,
                error = estado.errorVisible(CampoLogin.CLAVE),
                habilitado = !estado.ingresando,
                icono = Icons.Filled.Lock,
                etiquetaFlotante = false,
                colores = coloresDeCampoOscuro()
            )

            AnimatedVisibility(
                visible = estado.mensaje != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = estado.mensaje.orEmpty(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = vm::ingresar,
                enabled = !estado.ingresando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeBoton,
                    contentColor = BlancoTexto
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (estado.ingresando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = BlancoTexto
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresando", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Se toca una cuenta y el formulario queda cargado; ingresar sigue siendo manual
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SesionRepository.CUENTAS_DE_PRUEBA.forEach { cuenta ->
                    TarjetaDeCuenta(
                        cuenta = cuenta,
                        habilitada = !estado.ingresando,
                        onClick = { vm.usarCuentaDePrueba(cuenta) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Campos oscuros translucidos, como en el mockup. */
@Composable
private fun coloresDeCampoOscuro() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BlancoTexto,
    unfocusedTextColor = BlancoTexto,
    disabledTextColor = BlancoTexto.copy(alpha = 0.6f),
    focusedContainerColor = NegroCampo.copy(alpha = 0.75f),
    unfocusedContainerColor = NegroCampo.copy(alpha = 0.55f),
    disabledContainerColor = NegroCampo.copy(alpha = 0.4f),
    focusedBorderColor = VerdeMarca,
    unfocusedBorderColor = BlancoTexto.copy(alpha = 0.25f),
    focusedLabelColor = VerdeMarca,
    unfocusedLabelColor = BlancoTexto.copy(alpha = 0.7f),
    focusedLeadingIconColor = VerdeMarca,
    unfocusedLeadingIconColor = BlancoTexto.copy(alpha = 0.8f),
    focusedTrailingIconColor = BlancoTexto,
    unfocusedTrailingIconColor = BlancoTexto.copy(alpha = 0.8f),
    cursorColor = VerdeMarca
)

/** Cuenta de prueba tocable: rellena correo y contrasena de una vez. */
@Composable
private fun TarjetaDeCuenta(
    cuenta: CuentaDePrueba,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NegroCampo.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = habilitada, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Usar la cuenta de ${cuenta.rol.etiqueta}" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconoDe(cuenta.rol),
                contentDescription = null,
                tint = VerdeMarca
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = cuenta.rol.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BlancoTexto
                )
                Text(
                    text = cuenta.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = BlancoTexto.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun iconoDe(rol: Rol): ImageVector = when (rol) {
    Rol.TECNICO -> Icons.Filled.Build
    Rol.COORDINADOR -> Icons.Filled.SupervisorAccount
    Rol.ADMIN -> Icons.Filled.AdminPanelSettings
}
