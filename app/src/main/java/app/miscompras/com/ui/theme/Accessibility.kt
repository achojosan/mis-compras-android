package app.miscompras.com.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AccessibilityDefaults {
    val MinTouchTarget = 56.dp
    val BodyLineHeight = 1.5f
    val TitleLineHeight = 1.3f
    val TextFieldMinHeight = 64.dp
    val SwitchMinWidth = 80.dp

    @Composable
    fun scaledDp(baseValue: Float): Dp {
        val config = LocalAccessibilityConfig.current
        return (baseValue * config.spacingScale).dp
    }

    val CardPadding: Dp
        @Composable get() = scaledDp(16f)

    val ItemSpacing: Dp
        @Composable get() = scaledDp(12f)

    val SectionSpacing: Dp
        @Composable get() = scaledDp(24f)

    val ButtonPadding: Dp
        @Composable get() = scaledDp(16f)

    val IconSize: Dp
        @Composable get() = scaledDp(32f)

    val SmallIconSize: Dp
        @Composable get() = scaledDp(24f)

    val LargeIconSize: Dp
        @Composable get() = scaledDp(48f)
}
