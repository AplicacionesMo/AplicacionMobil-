package cl.duoc.tacticaldev.usuarios.dto;

import cl.duoc.tacticaldev.usuarios.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Al actualizar, si la clave viene vacia se conserva la anterior. */
public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        String correo,

        String clave,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
