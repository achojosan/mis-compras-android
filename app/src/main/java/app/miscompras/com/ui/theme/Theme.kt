package app.miscompras.com.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Green800,
    onPrimary = White,
    primaryContainer = Green50,
    onPrimaryContainer = Green700,
    secondary = Orange700,
    onSecondary = White,
    secondaryContainer = Orange50,
    onSecondaryContainer = Orange800,
    tertiary = Amber600,
    onTertiary = White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber700,
    background = WarmWhite,
    onBackground = WarmGray900,
    surface = White,
    onSurface = WarmGray900,
    surfaceVariant = WarmGray100,
    onSurfaceVariant = WarmGray700,
    error = Red700,
    onError = White,
    errorContainer = Red50,
    onErrorContainer = Red800,
    outline = WarmGray300,
    outlineVariant = WarmGray200
)

private val DarkColorScheme = darkColorScheme(
    primary = Green300,
    onPrimary = WarmGray900,
    primaryContainer = Green700,
    onPrimaryContainer = Green200,
    secondary = Orange300,
    onSecondary = WarmGray900,
    secondaryContainer = Orange700,
    onSecondaryContainer = Orange200,
    tertiary = Amber300,
    onTertiary = WarmGray900,
    tertiaryContainer = Amber700,
    onTertiaryContainer = Amber200,
    background = DarkWarmBg,
    onBackground = WarmGray100,
    surface = DarkWarmSurface,
    onSurface = WarmGray100,
    surfaceVariant = WarmGray700,
    onSurfaceVariant = WarmGray300,
    error = Red600,
    onError = WarmGray900,
    errorContainer = Red800,
    onErrorContainer = Red50,
    outline = WarmGray500,
    outlineVariant = WarmGray700
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Green800,
    onPrimary = White,
    primaryContainer = Green700,
    onPrimaryContainer = White,
    secondary = Orange800,
    onSecondary = White,
    secondaryContainer = Orange700,
    onSecondaryContainer = White,
    tertiary = Amber700,
    onTertiary = White,
    tertiaryContainer = Amber600,
    onTertiaryContainer = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = WarmGray100,
    onSurfaceVariant = Black,
    error = Red800,
    onError = White,
    errorContainer = Red700,
    onErrorContainer = White,
    outline = Black,
    outlineVariant = WarmGray700
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Green200,
    onPrimary = Black,
    primaryContainer = Green300,
    onPrimaryContainer = Black,
    secondary = Orange200,
    onSecondary = Black,
    secondaryContainer = Orange300,
    onSecondaryContainer = Black,
    tertiary = Amber200,
    onTertiary = Black,
    tertiaryContainer = Amber300,
    onTertiaryContainer = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = WarmGray700,
    onSurfaceVariant = White,
    error = Red50,
    onError = Black,
    errorContainer = Red600,
    onErrorContainer = Black,
    outline = White,
    outlineVariant = WarmGray300
)

@Composable
fun MisComprasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast -> HighContrastLightColorScheme
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
