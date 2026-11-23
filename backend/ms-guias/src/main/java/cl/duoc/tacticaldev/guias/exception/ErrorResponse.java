package cl.duoc.tacticaldev.guias.exception;

/** Cuerpo de todos los errores: la app muestra "mensaje" directamente al usuario. */
public record ErrorResponse(int estado, String mensaje) {
}
