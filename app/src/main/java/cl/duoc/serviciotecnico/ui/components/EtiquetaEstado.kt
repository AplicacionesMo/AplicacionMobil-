package cl.duoc.serviciotecnico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Text(
        text = estado.etiqueta,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        modifier = modifier
            .background(
                color = colorDeEstado(estado),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = "Estado del ticket: ${estado.etiqueta}" }
    )
}
