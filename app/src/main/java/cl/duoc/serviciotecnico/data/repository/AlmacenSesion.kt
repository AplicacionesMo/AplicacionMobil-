package cl.duoc.serviciotecnico.data.repository

import android.content.Context
import cl.duoc.serviciotecnico.domain.model.Rol
import cl.duoc.serviciotecnico.domain.model.UsuarioSesion

/**
 * Donde se guarda la sesion. Es una interfaz para que en las pruebas unitarias
 * se reemplace por un mock, sin necesitar el SharedPreferences real de Android.
 */
interface AlmacenSesion {
    fun guardar(usuario: UsuarioSesion)
    fun leer(): UsuarioSesion?
    fun borrar()
}

class PreferenciasSesion(context: Context) : AlmacenSesion {

    private val preferencias = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override fun guardar(usuario: UsuarioSesion) {
        preferencias.edit()
            .putLong(ID, usuario.id)
            .putString(NOMBRE, usuario.nombre)
            .putString(CORREO, usuario.correo)
            .putString(ROL, usuario.rol.name)
            .apply()
    }

    override fun leer(): UsuarioSesion? {
        val correo = preferencias.getString(CORREO, null) ?: return null
        return UsuarioSesion(
            id = preferencias.getLong(ID, 0L),
            nombre = preferencias.getString(NOMBRE, null).orEmpty(),
            correo = correo,
            rol = Rol.desde(preferencias.getString(ROL, null))
        )
    }

    override fun borrar() {
        preferencias.edit().clear().apply()
    }

    private companion object {
        const val ARCHIVO = "sesion"
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val CORREO = "correo"
        const val ROL = "rol"
    }
}
