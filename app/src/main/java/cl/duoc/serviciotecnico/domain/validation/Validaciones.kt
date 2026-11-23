package cl.duoc.serviciotecnico.domain.validation

import java.time.LocalDateTime

object Validaciones {
    fun noVacio(valor: String, nombreDelCampo: String): String? =
        if (valor.isBlank()) "$nombreDelCampo es obligatorio" else null

    fun largoMinimo(valor: String, minimo: Int, nombreDelCampo: String): String? = when {
        valor.isBlank() -> "$nombreDelCampo es obligatorio"
        valor.trim().length < minimo -> "$nombreDelCampo debe tener al menos $minimo caracteres"
        else -> null
    }

    fun fechaNoFutura(valor: LocalDateTime, nombreDelCampo: String): String? =
        if (valor.isAfter(LocalDateTime.now())) "$nombreDelCampo no puede ser futura" else null

    fun serieValida(serie: String): String? = noVacio(serie, "El numero de serie")

    fun correoValido(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !correo.contains("@") -> "El correo debe tener @"
        else -> null
    }

    fun todoCorrecto(vararg errores: String?): Boolean = errores.all { it == null }
}