package cl.duoc.serviciotecnico.ui.theme

import androidx.compose.ui.graphics.Color
import cl.duoc.serviciotecnico.domain.model.EstadoTicket

fun colorDeEstado(estado: EstadoTicket): Color = when (estado) {
    EstadoTicket.ABIERTO -> EstadoAbierto
    EstadoTicket.COORDINACION_EN_PROCESO -> EstadoCoordinando
    EstadoTicket.COORDINADO -> EstadoCoordinado
    EstadoTicket.TRANSITO -> EstadoTransito
    EstadoTicket.TRABAJO_EN_PROCESO -> EstadoTrabajando
    EstadoTicket.PENDIENTE_POR_CLIENTE -> EstadoPendienteCliente
    EstadoTicket.PENDIENTE_POR_TACTICALDEV -> EstadoPendienteEmpresa
    EstadoTicket.CERRADO -> EstadoCerrado
    EstadoTicket.CANCELADO -> EstadoCancelado
}
