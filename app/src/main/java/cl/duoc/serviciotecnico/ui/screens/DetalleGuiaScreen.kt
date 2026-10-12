package cl.duoc.serviciotecnico.ui.screens

import androidx.compose.runtime.Composable
import cl.duoc.serviciotecnico.ui.components.PantallaProvisional

@Composable
fun DetalleGuiaScreen(
    guiaId: String,
    onVolver: () -> Unit
) {
    PantallaProvisional(
        titulo = "Guia de servicio",
        detalle = "Detalle de la guia $guiaId, con direccion, coordenadas, " +
            "equipo atendido, trabajo realizado y fotografias.",
        responsable = "Persona 2 - tarjeta G-08",
        onVolver = onVolver
    )
}
