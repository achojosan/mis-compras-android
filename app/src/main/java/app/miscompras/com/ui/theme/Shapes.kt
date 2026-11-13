package app.miscompras.com.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Formas unificadas de la app para mantener la coherencia visual.
 *
 * [card] es la forma estándar de todas las tarjetas (de contenido y destacadas).
 */
object AppShapes {
    /** Tarjetas de contenido y tarjetas destacadas. */
    val card: Shape = RoundedCornerShape(20.dp)

    /** Contenedores pequeños (iconos, chips, segmentos). */
    val small: Shape = RoundedCornerShape(14.dp)

    /** Pases de tienda y tarjetas grandes de la cartera. */
    val pass: Shape = RoundedCornerShape(24.dp)

    /** Botones. */
    val button: Shape = RoundedCornerShape(16.dp)
}

/**
 * Borde inferior dentado de un ticket (el pie de la lista de una tienda).
 * [tooth] es el ancho de cada diente en dp.
 */
class TicketEdgeShape(private val tooth: Float = 10f, private val depth: Float = 6f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val toothPx = with(density) { tooth.dp.toPx() }
        val depthPx = with(density) { depth.dp.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - depthPx)
            var x = size.width
            var down = false
            while (x > 0f) {
                x = (x - toothPx / 2f).coerceAtLeast(0f)
                lineTo(x, size.height - if (down) depthPx else 0f)
                down = !down
            }
            lineTo(0f, size.height - depthPx)
            close()
        }
        return Outline.Generic(path)
    }
}
