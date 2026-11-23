package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CambioEstadoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoTicket estadoNuevo,

        @NotBlank(message = "El comentario es obligatorio")
        @Size(min = 5, message = "El comentario debe tener al menos 5 caracteres")
        String comentario,

        @Size(max = 5, message = "Maximo 5 fotos por cambio de estado")
        List<String> urisDeFotos,

        @NotNull(message = "Debes registrar tu ubicacion para cambiar el estado")
        Double latitud,

        @NotNull(message = "Debes registrar tu ubicacion para cambiar el estado")
        Double longitud
) {
}
