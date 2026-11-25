package cl.duoc.tacticaldev.usuarios.model;

/**
 * Perfiles de la app:
 *  - TECNICO: trabaja en terreno (cambia estados, emite guias, cierra).
 *  - COORDINADOR: ademas crea, cancela y elimina tickets.
 *  - ADMIN: todo lo anterior y ademas crea y elimina usuarios.
 */
public enum Rol {
    TECNICO,
    COORDINADOR,
    ADMIN
}
