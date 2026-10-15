package cl.duoc.serviciotecnico.data.repository

import android.content.Context

/**
 * Sesion del tecnico.
 *
 * Prototipo: no hay servidor, asi que se valida contra un usuario de prueba.
 * La sesion queda guardada en SharedPreferences, de modo que el tecnico no
 * tiene que volver a ingresar cada vez que abre la app.
 */
class SesionRepository(context: Context) {

    private val preferencias =
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun haySesionActiva(): Boolean = usuarioActual() != null

    fun usuarioActual(): String? = preferencias.getString(CLAVE_USUARIO, null)

    /** Devuelve true si las credenciales son correctas y deja la sesion iniciada. */
    fun iniciarSesion(correo: String, clave: String): Boolean {
        val correoLimpio = correo.trim().lowercase()
        val correcto = correoLimpio == USUARIO_DEMO && clave == CLAVE_DEMO
        if (correcto) {
            preferencias.edit().putString(CLAVE_USUARIO, correoLimpio).apply()
        }
        return correcto
    }

    fun cerrarSesion() {
        preferencias.edit().remove(CLAVE_USUARIO).apply()
    }

    companion object {
        /** Usuario de prueba del prototipo. */
        const val USUARIO_DEMO = "tecnico@tacticaldev.cl"
        const val CLAVE_DEMO = "tecnico123"

        private const val ARCHIVO = "sesion"
        private const val CLAVE_USUARIO = "usuario"
    }
}