package app.miscompras.com.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

data class AccessibilityConfig(
    /** Escala efectiva de texto: la del sistema por la elegida en la app, con tope. */
    val fontScale: Float = 1.0f,
    val spacingScale: Float = 1.0f,
    val highContrast: Boolean = false
) {
    companion object {
        val Default = AccessibilityConfig()
    }
}

val LocalAccessibilityConfig = staticCompositionLocalOf { AccessibilityConfig.Default }

/** Tope de la escala efectiva (sistema × app). Android 14 llega a 2,0 por sí solo. */
const val MAX_EFFECTIVE_FONT_SCALE = 2.0f

/** A partir de esta escala las pantallas pasan a diseños apilados. Un único umbral para toda la app. */
const val LARGE_TEXT_THRESHOLD = 1.3f

/**
 * Combina la escala del sistema con la elegida en la app. Antes la de la app sustituía
 * a la del sistema, de modo que quien agrandaba el texto en Android no lo veía agrandado.
 */
fun effectiveFontScale(systemScale: Float, appScale: Float): Float =
    (systemScale * appScale).coerceIn(0.85f, MAX_EFFECTIVE_FONT_SCALE)

@Composable
fun ProvideAccessibilityConfig(
    config: AccessibilityConfig,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val effective = effectiveFontScale(density.fontScale, config.fontScale)
    val newDensity = Density(
        density = density.density,
        fontScale = effective
    )

    CompositionLocalProvider(
        LocalAccessibilityConfig provides config.copy(fontScale = effective),
        LocalDensity provides newDensity,
        content = content
    )
}

/** Texto grande (≥ 1,3 efectivo): las pantallas pasan a diseños apilados. */
val AccessibilityConfig.isLargeText: Boolean
    get() = fontScale >= LARGE_TEXT_THRESHOLD
