package cl.duoc.tacticaldev.usuarios.dto;

import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.model.Usuario;

/** Lo que sale hacia la app: nunca incluye la clave. */
public record UsuarioResponse(Long id, String nombre, String correo, Rol rol, boolean activo) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.isActivo());
    }
}
