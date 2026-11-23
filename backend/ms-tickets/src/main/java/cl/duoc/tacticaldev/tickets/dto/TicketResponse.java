package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.model.Prioridad;
import cl.duoc.tacticaldev.tickets.model.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        String id,
        int numero,
        String titulo,
        String descripcion,
        String cliente,
        String direccion,
        Prioridad prioridad,
        EstadoTicket estado,
        EquipoResponse equipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCierre,
        String solucion,
        String motivoCancelacion
) {
    public static TicketResponse desde(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getNumero(), t.getTitulo(), t.getDescripcion(), t.getCliente(),
                t.getDireccion(), t.getPrioridad(), t.getEstado(), EquipoResponse.desde(t.getEquipo()),
                t.getFechaCreacion(), t.getFechaCierre(), t.getSolucion(), t.getMotivoCancelacion()
        );
    }
}
