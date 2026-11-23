package cl.duoc.tacticaldev.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        String correo,

        @NotBlank(message = "La clave es obligatoria")
        String clave
) {
}
