package cl.duoc.serviciotecnico.domain.model

/**
 * Cuenta de demostracion que ms-usuarios crea la primera vez que arranca.
 * En la pantalla de login se toca una y el formulario queda listo para ingresar.
 */
data class CuentaDePrueba(
    val rol: Rol,
    val correo: String,
    val clave: String
)
