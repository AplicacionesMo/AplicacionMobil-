package cl.duoc.serviciotecnico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.serviciotecnico.ui.theme.VerdeMarca
import cl.duoc.serviciotecnico.ui.theme.VerdeProfundo

/** Icono dentro de un circulo verde claro. Es el boton de las barras superiores. */
@Composable
fun BotonRedondo(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier
            .size(42.dp)
            .background(color = VerdeMarca.copy(alpha = 0.12f), shape = CircleShape)
    ) {
        Icon(icono, contentDescription = descripcion, tint = VerdeProfundo)
    }
}
