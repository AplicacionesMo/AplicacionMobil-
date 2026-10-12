package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.runtime.Composable
import cl.duoc.serviciotecnico.ui.components.PantallaProvisional

@Composable
fun DetalleTicketScreen(
    ticketId: String,
    onVolver: () -> Unit,
    onEditarTicket: () -> Unit,
    onNuevaGuia: () -> Unit,
    onAbrirGuia: (String) -> Unit
) {
    PantallaProvisional(
        titulo = "Ticket",
        detalle = "Detalle del ticket $ticketId, con su estado, sus acciones y sus guias.",
        responsable = "Persona 2 - tarjetas G-01 a G-06",
        onVolver = onVolver,
        acciones = listOf(
            "Editar ticket" to onEditarTicket,
            "Nueva guia de servicio" to onNuevaGuia,
            "Abrir guia de ejemplo" to { onAbrirGuia("demo") }
        )
    )
}
