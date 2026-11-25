package cl.duoc.serviciotecnico.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import cl.duoc.serviciotecnico.ui.theme.VerdeMarca

/**
 * Fondo blanco con dos ondas verdes muy tenues abajo, como en el mockup.
 *
 * Se dibuja con Canvas en vez de una imagen: pesa cero en el APK y se adapta a
 * cualquier pantalla sin deformarse.
 */
@Composable
fun FondoConOndas(
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ancho = size.width
            val alto = size.height

            // Onda de atras, mas clara y mas arriba
            val atras = Path().apply {
                moveTo(0f, alto * 0.80f)
                cubicTo(
                    ancho * 0.30f, alto * 0.74f,
                    ancho * 0.62f, alto * 0.90f,
                    ancho, alto * 0.82f
                )
                lineTo(ancho, alto)
                lineTo(0f, alto)
                close()
            }
            drawPath(path = atras, color = VerdeMarca.copy(alpha = 0.10f))

            // Onda de adelante, un poco mas intensa
            val adelante = Path().apply {
                moveTo(0f, alto * 0.92f)
                cubicTo(
                    ancho * 0.28f, alto * 0.86f,
                    ancho * 0.70f, alto * 1.00f,
                    ancho, alto * 0.93f
                )
                lineTo(ancho, alto)
                lineTo(0f, alto)
                close()
            }
            drawPath(path = adelante, color = VerdeMarca.copy(alpha = 0.16f))
        }

        contenido()
    }
}
