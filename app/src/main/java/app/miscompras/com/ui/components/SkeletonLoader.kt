package app.miscompras.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val baseColor = Color(0xFFE0E0E0)

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
            .background(baseColor)
    )
}
