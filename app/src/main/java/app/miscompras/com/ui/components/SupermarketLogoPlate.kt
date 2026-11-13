package app.miscompras.com.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placa del logo del supermercado: contenedor redondeado, con borde fino y sombra,
 * que encuadra el logo sin recortarlo (`Fit`). Se adapta a la proporción real de la
 * imagen, de modo que tanto los logos cuadrados como los de letras quedan bien.
 *
 * Es el efecto usado en la ficha de tienda, reutilizado en el resto de apariciones.
 *
 * @param adaptWidth si es `true`, el ancho crece según la proporción del logo
 *   (útil para un encabezado grande). Si es `false`, la placa es cuadrada.
 */
@Composable
fun SupermarketLogoPlate(
    supermarketName: String,
    logoPath: String?,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    maxWidth: Dp = Dp.Unspecified,
    adaptWidth: Boolean = false,
    cornerRadius: Dp = 18.dp,
    contentPadding: Dp = 10.dp,
    shadowElevation: Dp = 3.dp,
    showBorder: Boolean = true,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface
) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val targetWidth = if (adaptWidth) {
        val adaptive = height * aspectRatio.coerceIn(0.8f, 2.4f)
        if (maxWidth == Dp.Unspecified) adaptive else adaptive.coerceAtMost(maxWidth)
    } else {
        height
    }
    val animatedWidth by animateDpAsState(targetValue = targetWidth, label = "logoPlateWidth")

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = if (showBorder) {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        } else {
            null
        },
        shadowElevation = shadowElevation,
        modifier = modifier.size(width = animatedWidth, height = height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            SupermarketLogo(
                supermarketName = supermarketName,
                customLogoPath = logoPath,
                size = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onAspectRatio = { aspectRatio = it }
            )
        }
    }
}
