package app.miscompras.com.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SkeletonShape {
    Rectangle,
    Circle,
    RoundedRectangle
}

@Composable
fun SkeletonLoader(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    skeletonShape: SkeletonShape = SkeletonShape.RoundedRectangle,
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val baseColor = Color(0xFFE0E0E0)
    val highlightColor = Color(0xFFF5F5F5)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(shimmerTranslate.value - 500f, 0f),
        end = Offset(shimmerTranslate.value + 500f, 0f)
    )

    val shape: Shape = when (skeletonShape) {
        SkeletonShape.Rectangle -> RoundedCornerShape(0.dp)
        SkeletonShape.Circle -> CircleShape
        SkeletonShape.RoundedRectangle -> RoundedCornerShape(cornerRadius)
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(shimmerBrush)
    )
}
