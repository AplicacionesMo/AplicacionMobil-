package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.Propiedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EquipoRequest(
        @NotBlank(message = "El nombre del equipo es obligatorio")
        String nombre,

        @NotBlank(message = "El modelo es obligatorio")
        String modelo,

        @NotBlank(message = "El numero de serie es obligatorio")
        String serie,

        @NotNull(message = "La propiedad del equipo es obligatoria")
        Propiedad propiedad
) {
}
