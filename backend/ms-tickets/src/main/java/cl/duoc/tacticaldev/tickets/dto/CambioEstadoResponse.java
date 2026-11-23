package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.CambioEstado;
import cl.duoc.tacticaldev.tickets.model.EstadoTicket;

import java.time.LocalDateTime;
import java.util.List;

public record CambioEstadoResponse(
        String id,
        String ticketId,
        EstadoTicket estadoAnterior,
        EstadoTicket estadoNuevo,
        String comentario,
        List<String> urisDeFotos,
        Double latitud,
        Double longitud,
        LocalDateTime fecha
) {
    public static CambioEstadoResponse desde(CambioEstado c) {
        return new CambioEstadoResponse(
                c.getId(), c.getTicket().getId(), c.getEstadoAnterior(), c.getEstadoNuevo(),
                c.getComentario(), List.copyOf(c.getUrisDeFotos()), c.getLatitud(), c.getLongitud(), c.getFecha()
        );
    }
}
