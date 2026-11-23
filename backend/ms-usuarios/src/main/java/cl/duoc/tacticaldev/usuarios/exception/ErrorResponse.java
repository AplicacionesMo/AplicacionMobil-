package cl.duoc.tacticaldev.usuarios.exception;

/** Cuerpo de todos los errores: la app muestra "mensaje" directamente al usuario. */
public record ErrorResponse(int estado, String mensaje) {
}
