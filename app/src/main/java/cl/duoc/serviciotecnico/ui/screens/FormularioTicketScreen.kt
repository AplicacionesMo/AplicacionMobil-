package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.runtime.Composable
import cl.duoc.serviciotecnico.ui.components.PantallaProvisional

@Composable
fun FormularioTicketScreen(
    ticketId: String?,
    onVolver: () -> Unit
) {
    PantallaProvisional(
        titulo = if (ticketId == null) "Nuevo ticket" else "Editar ticket",
        detalle = "Aqui van los campos del ticket y del equipo, con validacion por campo.",
        responsable = "Persona 1 - tarjetas T-03 a T-06",
        onVolver = onVolver
    )
}
