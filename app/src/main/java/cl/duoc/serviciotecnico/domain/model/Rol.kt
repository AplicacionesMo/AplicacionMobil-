package cl.duoc.serviciotecnico.domain.model

/**
 * Perfil del usuario, entregado por ms-usuarios al iniciar sesion.
 *  - TECNICO: trabaja en terreno. Avanza estados, emite guias y cierra. No crea tickets.
 *  - COORDINADOR: ademas crea, cancela y elimina tickets.
 *  - ADMIN: todo lo del coordinador y ademas crea y elimina usuarios.
 */
enum class Rol(val etiqueta: String) {
    TECNICO("Técnico"),
    COORDINADOR("Coordinador"),
    ADMIN("Administrador");

    /** El tecnico avanza estados y finaliza; coordinador y admin tambien pueden cancelar. */
    val estadosPermitidos: List<EstadoTicket>
        get() = when (this) {
            TECNICO -> EstadoTicket.intermedios + EstadoTicket.FINALIZADO
            COORDINADOR, ADMIN -> EstadoTicket.intermedios + EstadoTicket.FINALIZADO + EstadoTicket.CANCELADO
        }

    val puedeCrearTickets: Boolean
        get() = this != TECNICO

    /** La guia la emite quien fue a terreno: solo el tecnico. */
    val puedeCrearGuias: Boolean
        get() = this == TECNICO

    val puedeEliminarTickets: Boolean
        get() = this != TECNICO

    /** Reabrir un ticket cerrado es excepcional: solo coordinador y administrador. */
    val puedeReabrirTickets: Boolean
        get() = this != TECNICO

    val puedeGestionarUsuarios: Boolean
        get() = this == ADMIN

    companion object {
        fun desde(texto: String?): Rol = entries.firstOrNull { it.name == texto } ?: TECNICO
    }
}
