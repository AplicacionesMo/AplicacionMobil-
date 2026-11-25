package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cambio de estado de un ticket.
 * Solo el estado nuevo es obligatorio: el detalle del trabajo (fotos, ubicacion y
 * descripcion) se registra en la guia de servicio, no aqui.
 */
public record CambioEstadoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoTicket estadoNuevo,

        String comentario,

        @Size(max = 5, message = "Maximo 5 fotos por cambio de estado")
        List<String> urisDeFotos,

        Double latitud,

        Double longitud
) {
}
