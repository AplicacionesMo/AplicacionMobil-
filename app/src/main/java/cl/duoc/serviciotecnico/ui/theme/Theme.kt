package cl.duoc.serviciotecnico.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = VerdeBoton,
    onPrimary = BlancoTexto,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeTexto,
    secondary = GrisAcento,
    onSecondary = BlancoTexto,
    secondaryContainer = GrisAcentoClaro,
    onSecondaryContainer = VerdeTexto,
    error = RojoError,
    onError = BlancoTexto,
    errorContainer = RojoErrorClaro,
    onErrorContainer = RojoError,
    background = BlancoFondo,
    onBackground = NegroTexto,
    surface = BlancoFondo,
    onSurface = NegroTexto,
    surfaceVariant = GrisSuperficie,
    onSurfaceVariant = GrisAcento,
    // Sin estos, Material rellena con su paleta base morada y las tarjetas
    // salen gris lavanda en vez de blancas
    surfaceContainerLowest = BlancoFondo,
    surfaceContainerLow = BlancoFondo,
    surfaceContainer = BlancoFondo,
    surfaceContainerHigh = BlancoFondo,
    surfaceContainerHighest = BlancoFondo,
    surfaceTint = VerdeBoton,
    inverseSurface = NegroTexto,
    inverseOnSurface = BlancoFondo,
    outline = GrisAcento,
    outlineVariant = GrisAcentoClaro,
    scrim = NegroTexto
)

/**
 * La app usa siempre el esquema claro: el blanco es el color principal y no debe
 * cambiar porque el telefono este en modo oscuro. El unico fondo oscuro es el del
 * login, que lo pone su propia imagen.
 */
@Composable
fun ServicioTecnicoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
