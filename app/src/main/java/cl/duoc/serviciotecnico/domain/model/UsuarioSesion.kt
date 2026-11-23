package cl.duoc.serviciotecnico.domain.model

data class UsuarioSesion(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: Rol
)
