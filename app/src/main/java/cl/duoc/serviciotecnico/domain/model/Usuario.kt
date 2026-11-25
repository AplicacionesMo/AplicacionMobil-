package cl.duoc.serviciotecnico.domain.model

/** Un usuario del sistema, como lo lista el administrador. */
data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean
)
