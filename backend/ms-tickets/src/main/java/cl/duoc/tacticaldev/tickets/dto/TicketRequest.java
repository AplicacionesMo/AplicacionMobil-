package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.Prioridad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Lo que envia la app al crear o editar un ticket. El equipo viene dentro:
 * si ya existe uno con esa serie se reutiliza, si no se crea.
 * Las mismas reglas que valida la app se vuelven a validar aqui: nunca se confia en el cliente.
 */
public record TicketRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(min = 5, message = "El titulo debe tener al menos 5 caracteres")
        String titulo,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(min = 10, message = "La descripcion debe tener al menos 10 caracteres")
        String descripcion,

        @NotBlank(message = "El cliente es obligatorio")
        String cliente,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(min = 5, message = "La direccion debe tener al menos 5 caracteres")
        String direccion,

        @NotNull(message = "La prioridad es obligatoria")
        Prioridad prioridad,

        @NotNull(message = "El equipo es obligatorio")
        @Valid
        EquipoRequest equipo,

        /** Tecnico asignado. Opcional: un ticket puede quedar sin asignar. */
        Long tecnicoId,

        String tecnicoNombre
) {
}
