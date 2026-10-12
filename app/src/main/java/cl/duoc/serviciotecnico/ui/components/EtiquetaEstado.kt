package cl.duoc.serviciotecnico.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import cl.duoc.serviciotecnico.ui.theme.colorDeEstado

@Composable
fun EtiquetaEstado(
    estado: EstadoTicket,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = colorDeEstado(estado),
        animationSpec = tween(durationMillis = 400),
        label = "colorDelEstado"
    )

    AnimatedContent(
        targetState = estado,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
        },
        label = "etiquetaDelEstado",
        modifier = modifier
            .background(color = color, shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = "Estado del ticket: ${estado.etiqueta}" }
    ) { estadoActual ->
        Text(
            text = estadoActual.etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}
