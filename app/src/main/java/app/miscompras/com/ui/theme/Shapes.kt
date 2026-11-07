package app.miscompras.com.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
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

    /** Botones. */
    val button: Shape = RoundedCornerShape(16.dp)
}
