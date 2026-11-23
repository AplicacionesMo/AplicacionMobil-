package cl.duoc.serviciotecnico.domain.model

/** Perfil del usuario, entregado por ms-usuarios al iniciar sesion. */
enum class Rol(val etiqueta: String) {
    TECNICO("Técnico"),
    COORDINADOR("Coordinador");

    /** El tecnico avanza estados y cierra; solo el coordinador puede cancelar. */
    val estadosPermitidos: List<EstadoTicket>
        get() = when (this) {
            TECNICO -> EstadoTicket.intermedios + EstadoTicket.CERRADO
            COORDINADOR -> EstadoTicket.intermedios + EstadoTicket.CERRADO + EstadoTicket.CANCELADO
        }

    val puedeEliminarTickets: Boolean
        get() = this == COORDINADOR

    companion object {
        fun desde(texto: String?): Rol = entries.firstOrNull { it.name == texto } ?: TECNICO
    }
}
