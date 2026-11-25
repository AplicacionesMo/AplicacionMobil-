package cl.duoc.serviciotecnico.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Esquinas de toda la app. Definirlas aqui es lo que da coherencia sin repetir
 * el mismo RoundedCornerShape en cada pantalla: las tarjetas toman "medium" y
 * los botones "small" por su cuenta.
 */
val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
