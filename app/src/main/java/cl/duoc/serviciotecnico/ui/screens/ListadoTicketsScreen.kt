package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.runtime.Composable
import cl.duoc.serviciotecnico.ui.components.PantallaProvisional

@Composable
fun ListadoTicketsScreen(
    onCrearTicket: () -> Unit,
    onAbrirTicket: (String) -> Unit,
    onProbarRecursos: () -> Unit = {}
) {
    PantallaProvisional(
        titulo = "Tickets",
        detalle = "Aqui va el listado de tickets con filtro por estado y el boton de crear.",
        responsable = "Persona 1 - tarjetas T-01 y T-02",
        acciones = listOf(
            "Crear ticket" to onCrearTicket,
            "Abrir detalle de ejemplo" to { onAbrirTicket("demo") },
            "Probar camara y ubicacion" to onProbarRecursos
        )
    )
}
