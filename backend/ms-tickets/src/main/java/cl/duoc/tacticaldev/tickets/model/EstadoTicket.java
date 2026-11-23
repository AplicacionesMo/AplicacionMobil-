package cl.duoc.tacticaldev.tickets.model;

/** Los mismos nueve estados que usa la app movil. */
public enum EstadoTicket {
    ABIERTO,
    COORDINACION_EN_PROCESO,
    COORDINADO,
    TRANSITO,
    TRABAJO_EN_PROCESO,
    PENDIENTE_POR_CLIENTE,
    PENDIENTE_POR_TACTICALDEV,
    CERRADO,
    CANCELADO;

    /** Cerrado y Cancelado son finales: el ticket ya no se modifica. */
    public boolean esFinal() {
        return this == CERRADO || this == CANCELADO;
    }
}
