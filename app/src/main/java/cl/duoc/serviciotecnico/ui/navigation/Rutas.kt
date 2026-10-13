package cl.duoc.serviciotecnico.ui.navigation

object Rutas {

    const val LISTADO_TICKETS = "tickets"

    const val FORMULARIO_TICKET = "tickets/formulario?ticketId={ticketId}"
    fun formularioTicket(ticketId: String? = null): String =
        "tickets/formulario?ticketId=${ticketId ?: ""}"

    const val DETALLE_TICKET = "tickets/{ticketId}"
    fun detalleTicket(ticketId: String): String = "tickets/$ticketId"

    const val FORMULARIO_GUIA = "tickets/{ticketId}/guias/formulario"
    fun formularioGuia(ticketId: String): String = "tickets/$ticketId/guias/formulario"

    const val DETALLE_GUIA = "guias/{guiaId}"
    fun detalleGuia(guiaId: String): String = "guias/$guiaId"

    const val ARG_TICKET_ID = "ticketId"
    const val ARG_GUIA_ID = "guiaId"
}
