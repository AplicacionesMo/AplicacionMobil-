package cl.duoc.tacticaldev.guias.dto;

import cl.duoc.tacticaldev.guias.model.Guia;

import java.time.LocalDateTime;
import java.util.List;

public record GuiaResponse(
        String id,
        int numero,
        String ticketId,
        String equipoId,
        String direccion,
        Double latitud,
        Double longitud,
        LocalDateTime fecha,
        String tecnico,
        String trabajoRealizado,
        String repuestos,
        String observaciones,
        String recibidoPor,
        List<String> urisDeFotos
) {
    public static GuiaResponse desde(Guia g) {
        return new GuiaResponse(
                g.getId(), g.getNumero(), g.getTicketId(), g.getEquipoId(), g.getDireccion(),
                g.getLatitud(), g.getLongitud(), g.getFecha(), g.getTecnico(), g.getTrabajoRealizado(),
                g.getRepuestos(), g.getObservaciones(), g.getRecibidoPor(), List.copyOf(g.getUrisDeFotos())
        );
    }
}
