package co.edu.mipuente.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = OnTealPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = NavySecondary,
    onSecondary = OnNavySecondary,
    secondaryContainer = NavyContainer,
    onSecondaryContainer = OnNavyContainer,
    tertiary = CoralTertiary,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = OnCoralContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline
)

private val DarkColors = darkColorScheme(
    primary = TealContainer,
    onPrimary = OnTealContainer,
    primaryContainer = TealPrimary,
    onPrimaryContainer = OnTealPrimary,
    secondary = NavyContainer,
    onSecondary = OnNavyContainer,
    tertiary = CoralContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant
)

@Composable
fun MiPuenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
