package cl.duoc.serviciotecnico.ui.theme

import androidx.compose.ui.graphics.Color
import cl.duoc.serviciotecnico.domain.model.EstadoTicket

fun colorDeEstado(estado: EstadoTicket): Color = when (estado) {
    EstadoTicket.ABIERTO -> EstadoAbierto
    EstadoTicket.TRANSITO -> EstadoTransito
    EstadoTicket.FINALIZADO -> EstadoCerrado
    EstadoTicket.CANCELADO -> EstadoCancelado
}
