package cl.duoc.serviciotecnico.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CampoTextoValidado(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    obligatorio: Boolean = false,
    lineasMaximas: Int = 1,
    // Alto minimo del cuadro. Subelo en los campos donde se espera un parrafo.
    lineasMinimas: Int = 1,
    tipoDeTeclado: KeyboardType = KeyboardType.Text,
    accionDelTeclado: ImeAction = ImeAction.Next,
    habilitado: Boolean = true,
    esClave: Boolean = false,
    // Icono a la izquierda del campo (lo usa el login)
    icono: ImageVector? = null,
    // false = la etiqueta se usa de marca de agua y desaparece al escribir
    etiquetaFlotante: Boolean = true,
    colores: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val tieneError = error != null
    var claveVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                // Sin etiqueta flotante el lector de pantalla igual debe saber que campo es
                contentDescription = when {
                    tieneError -> "$etiqueta, con error: $error"
                    else -> etiqueta
                }
            },
        enabled = habilitado,
        // En campos de contrasena se ocultan los caracteres
        visualTransformation = if (esClave && !claveVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        label = if (etiquetaFlotante) {
            { Text(if (obligatorio) "$etiqueta *" else etiqueta) }
        } else {
            null
        },
        placeholder = if (etiquetaFlotante) null else {
            { Text(etiqueta) }
        },
        colors = colores,
        leadingIcon = icono?.let { { Icon(imageVector = it, contentDescription = null) } },
        isError = tieneError,
        singleLine = lineasMaximas == 1,
        maxLines = lineasMaximas,
        minLines = lineasMinimas.coerceAtMost(lineasMaximas),
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoDeTeclado,
            imeAction = accionDelTeclado
        ),
        trailingIcon = {
            // En una contrasena el espacio lo ocupa el ojo; el error igual se ve
            // en el borde rojo y en el texto de abajo
            if (esClave) {
                IconButton(onClick = { claveVisible = !claveVisible }) {
                    Icon(
                        imageVector = if (claveVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (claveVisible) {
                            "Ocultar la contrasena"
                        } else {
                            "Mostrar la contrasena"
                        }
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = tieneError,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Campo con error"
                    )
                }
            }
        },
        supportingText = {
            AnimatedVisibility(
                visible = tieneError,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(text = error.orEmpty())
            }
        }
    )
}
