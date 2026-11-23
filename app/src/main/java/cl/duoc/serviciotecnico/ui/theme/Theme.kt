package cl.duoc.serviciotecnico.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = BlancoTexto,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulTexto,
    secondary = NaranjaAcento,
    onSecondary = BlancoTexto,
    secondaryContainer = NaranjaClaro,
    error = RojoError,
    onError = BlancoTexto,
    errorContainer = RojoErrorClaro,
    onErrorContainer = RojoError,
    background = GrisFondo,
    onBackground = NegroTexto,
    surface = GrisFondo,
    onSurface = NegroTexto,
    surfaceVariant = GrisSuperficie
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulOscuro,
    onPrimary = AzulProfundo,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulClaro,
    secondary = NaranjaClaro,
    onSecondary = NaranjaAcento,
    error = RojoErrorClaro,
    onError = RojoError,
    background = NegroTexto,
    onBackground = BlancoTexto,
    surface = NegroTexto,
    onSurface = BlancoTexto
)

@Composable
fun ServicioTecnicoTheme(
    enOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (enOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
