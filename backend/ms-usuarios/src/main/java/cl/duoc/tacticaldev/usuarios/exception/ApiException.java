package cl.duoc.tacticaldev.usuarios.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con su codigo HTTP. El manejador global lo transforma en JSON. */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
