package cl.duoc.serviciotecnico.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Esqueletos de carga: bloques grises con la forma del contenido que viene en camino.
 *
 * Se usan en lugar de una rueda girando porque la pantalla ya sabe que va a mostrar,
 * y sobre todo porque antes, mientras cargaba, el listado decia "No hay tickets":
 * el mismo texto que cuando de verdad no hay ninguno.
 */

/** Degradado que recorre el bloque de izquierda a derecha para que se note que esta vivo. */
@Composable
private fun brochaDeEsqueleto(): Brush {
    val base = MaterialTheme.colorScheme.onSurfaceVariant
    val colores = listOf(
        base.copy(alpha = 0.10f),
        base.copy(alpha = 0.22f),
        base.copy(alpha = 0.10f)
    )
    val transicion = rememberInfiniteTransition(label = "esqueleto")
    val desplazamiento by transicion.animateFloat(
        initialValue = -ANCHO_DEL_BRILLO,
        targetValue = ANCHO_DEL_BRILLO * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brillo"
    )
    return Brush.linearGradient(
        colors = colores,
        start = Offset(desplazamiento, 0f),
        end = Offset(desplazamiento + ANCHO_DEL_BRILLO, 0f)
    )
}

/** Un bloque gris. [porcentajeDeAncho] simula renglones de distinto largo. */
@Composable
fun BloqueEsqueleto(
    modifier: Modifier = Modifier,
    alto: Dp = 14.dp,
    porcentajeDeAncho: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxWidth(porcentajeDeAncho)
            .height(alto)
            .clip(RoundedCornerShape(6.dp))
            .background(brochaDeEsqueleto())
    )
}

/** Mismo alto y disposicion que TarjetaTicket, para que no salte nada al llegar los datos. */
@Composable
fun TarjetaTicketEsqueleto(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BloqueEsqueleto(modifier = Modifier.width(56.dp), alto = 14.dp)
                BloqueEsqueleto(modifier = Modifier.width(88.dp), alto = 24.dp)
            }
            BloqueEsqueleto(modifier = Modifier.padding(top = 12.dp), alto = 18.dp, porcentajeDeAncho = 0.75f)
            BloqueEsqueleto(modifier = Modifier.padding(top = 8.dp), alto = 14.dp, porcentajeDeAncho = 0.45f)
            BloqueEsqueleto(modifier = Modifier.padding(top = 10.dp), alto = 12.dp, porcentajeDeAncho = 0.35f)
        }
    }
}

/**
 * Varias tarjetas falsas. Para un lector de pantalla es un solo aviso de "Cargando":
 * leer cinco bloques grises no le sirve a nadie.
 */
@Composable
fun ListaDeTicketsEsqueleto(
    modifier: Modifier = Modifier,
    cuantas: Int = 4
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clearAndSetSemantics { contentDescription = "Cargando los tickets" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(cuantas) { TarjetaTicketEsqueleto() }
    }
}

/** Esqueleto de una ficha con titulo y varias lineas de datos (detalle de ticket o guia). */
@Composable
fun FichaEsqueleto(
    modifier: Modifier = Modifier,
    lineas: Int = 4,
    descripcion: String = "Cargando"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = descripcion }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BloqueEsqueleto(alto = 20.dp, porcentajeDeAncho = 0.6f)
            repeat(lineas) { indice ->
                BloqueEsqueleto(
                    alto = 14.dp,
                    // Renglones desparejos: un bloque perfecto no parece texto
                    porcentajeDeAncho = if (indice % 2 == 0) 0.9f else 0.65f
                )
            }
        }
    }
}

private const val ANCHO_DEL_BRILLO = 420f
