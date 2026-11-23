package cl.duoc.serviciotecnico.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BotonGuardar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String = "Guardar",
    habilitado: Boolean = true,
    guardando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !guardando,
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = guardando,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "estadoDelBotonGuardar"
        ) { estaGuardando ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (estaGuardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando")
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(texto)
                }
            }
        }
    }
}
