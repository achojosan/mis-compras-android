package app.miscompras.com.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

data class AccessibilityConfig(
    val fontScale: Float = 1.0f,
    val spacingScale: Float = 1.0f,
    val highContrast: Boolean = false
) {
    companion object {
        val Default = AccessibilityConfig()
    }
}

val LocalAccessibilityConfig = staticCompositionLocalOf { AccessibilityConfig.Default }

@Composable
fun ProvideAccessibilityConfig(
    config: AccessibilityConfig,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val newDensity = Density(
        density = density.density,
        fontScale = config.fontScale
    )
    
    CompositionLocalProvider(
        LocalAccessibilityConfig provides config,
        LocalDensity provides newDensity,
        content = content
    )
}

/** Texto "Grande" (1,2) o "Muy grande" (1,4): las pantallas pasan a diseños apilados. */
val AccessibilityConfig.isLargeText: Boolean
    get() = fontScale >= 1.2f
