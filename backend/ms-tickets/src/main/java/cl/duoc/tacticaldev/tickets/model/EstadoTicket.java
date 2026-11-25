package cl.duoc.tacticaldev.tickets.model;

/** Los mismos cuatro estados que usa la app movil. */
public enum EstadoTicket {
    ABIERTO,
    TRANSITO,
    FINALIZADO,
    CANCELADO;

    /** Finalizado y Cancelado son finales: el ticket ya no se modifica. */
    public boolean esFinal() {
        return this == FINALIZADO || this == CANCELADO;
    }
}
