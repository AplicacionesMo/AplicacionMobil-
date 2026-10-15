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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    tipoDeTeclado: KeyboardType = KeyboardType.Text,
    accionDelTeclado: ImeAction = ImeAction.Next,
    habilitado: Boolean = true,
    esClave: Boolean = false
) {
    val tieneError = error != null

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (tieneError) contentDescription = "$etiqueta, con error: $error"
            },
        enabled = habilitado,
        // En campos de contrasena se ocultan los caracteres
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        label = { Text(if (obligatorio) "$etiqueta *" else etiqueta) },
        isError = tieneError,
        singleLine = lineasMaximas == 1,
        maxLines = lineasMaximas,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoDeTeclado,
            imeAction = accionDelTeclado
        ),
        trailingIcon = {
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
