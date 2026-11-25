package cl.duoc.serviciotecnico.domain.model

enum class EstadoTicket(val etiqueta: String) {
    ABIERTO("Abierto"),
    TRANSITO("En transito"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    val esFinal: Boolean
        get() = this == FINALIZADO || this == CANCELADO

    /**
     * Un ticket solo se finaliza con el trabajo documentado en una guia de servicio.
     * Abierto, En transito y Cancelado se alcanzan sin guia.
     */
    val exigeGuia: Boolean
        get() = this == FINALIZADO

    companion object {
        val intermedios: List<EstadoTicket>
            get() = entries.filterNot { it.esFinal }
    }
}
