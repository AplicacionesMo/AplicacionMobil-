package cl.duoc.serviciotecnico.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.theme.colorDeEstado

/**
 * Pildora del estado: icono + texto sobre un fondo del mismo color pero suave.
 * El color fuerte se reserva para el texto, asi se lee bien sobre el blanco.
 */
@Composable
fun EtiquetaEstado(
    estado: EstadoTicket,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = estado,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
        },
        label = "etiquetaDelEstado",
        modifier = modifier
    ) { estadoActual ->
        val color = colorDeEstado(estadoActual)
        Row(
            modifier = Modifier
                .background(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = iconoDeEstado(estadoActual),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = estadoActual.etiqueta,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

private fun iconoDeEstado(estado: EstadoTicket): ImageVector = when (estado) {
    EstadoTicket.ABIERTO -> Icons.Filled.Schedule
    EstadoTicket.TRANSITO -> Icons.Filled.LocalShipping
    EstadoTicket.FINALIZADO -> Icons.Filled.CheckCircle
    EstadoTicket.CANCELADO -> Icons.Filled.Cancel
}
