package cl.duoc.serviciotecnico.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun GaleriaDeFotos(
    fotos: List<Uri>,
    modifier: Modifier = Modifier,
    onQuitarFoto: ((Uri) -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (fotos.isEmpty()) {
                "Sin fotografias"
            } else {
                "Fotografias (${fotos.size})"
            },
            style = MaterialTheme.typography.titleMedium
        )

        AnimatedVisibility(
            visible = fotos.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = fotos, key = { it.toString() }) { foto ->
                    Box {
                        AsyncImage(
                            model = foto,
                            contentDescription = "Fotografia del trabajo realizado",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(104.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        if (onQuitarFoto != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Quitar fotografia",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
