package cl.duoc.serviciotecnico.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

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
    habilitado: Boolean = true
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
        label = { Text(if (obligatorio) "$etiqueta *" else etiqueta) },
        isError = tieneError,
        singleLine = lineasMaximas == 1,
        maxLines = lineasMaximas,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoDeTeclado,
            imeAction = accionDelTeclado
        ),
        trailingIcon = {
            if (tieneError) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Campo con error"
                )
            }
        },
        supportingText = {
            if (tieneError) {
                Text(text = error)
            }
        }
    )
}
