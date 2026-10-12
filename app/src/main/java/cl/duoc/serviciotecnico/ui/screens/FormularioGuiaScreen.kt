package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.runtime.Composable
import cl.duoc.serviciotecnico.ui.components.PantallaProvisional

@Composable
fun FormularioGuiaScreen(
    ticketId: String,
    onVolver: () -> Unit
) {
    PantallaProvisional(
        titulo = "Nueva guia de servicio",
        detalle = "Guia del ticket $ticketId: trabajo realizado, repuestos, " +
            "observaciones, quien recibe, fotografias y ubicacion.",
        responsable = "Persona 2 - tarjeta G-07 (con G-09 de la persona 3)",
        onVolver = onVolver
    )
}
