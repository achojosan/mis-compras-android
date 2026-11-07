package app.miscompras.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.AccessibilityDefaults

/**
 * Barra superior flotante. Los botones se apoyan sobre el contenido con su propio
 * fondo circular y, al hacer scroll, aparece un fondo sólido detrás de ellos junto
 * al título. Es el efecto usado en la ficha de tienda, reutilizado en el resto de
 * pantallas para mantener una apariencia coherente.
 *
 * @param backgroundFraction 0 = transparente (arriba), 1 = fondo sólido y título visibles.
 */
@Composable
fun FloatingTopBar(
    title: String,
    backgroundFraction: Float,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // El título solo aparece cuando el fondo está lo bastante sólido como para
    // garantizar su legibilidad (evita texto translúcido sobre el contenido).
    val titleAlpha = ((backgroundFraction - 0.5f) / 0.5f).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = backgroundFraction }
                .background(MaterialTheme.colorScheme.surface)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AccessibilityDefaults.ItemSpacing,
                    vertical = AccessibilityDefaults.ItemSpacing
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                onClick = onNavigateBack
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer { alpha = titleAlpha }
                )
            }

            actions()
        }
    }
}

/** Botón circular elevado para las barras superiores flotantes. */
@Composable
fun FloatingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(AccessibilityDefaults.SmallIconSize)
            )
        }
    }
}

/** Fracción de 0 a 1 para una lista perezosa, según cuánto se ha desplazado. */
@Composable
fun rememberTopBarFraction(
    listState: LazyListState,
    threshold: Dp = 140.dp
): Float {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val fraction by remember(listState, thresholdPx) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / thresholdPx).coerceIn(0f, 1f)
            }
        }
    }
    return fraction
}

/** Fracción de 0 a 1 para un scroll normal (Column con `verticalScroll`). */
@Composable
fun rememberTopBarFraction(
    scrollState: ScrollState,
    threshold: Dp = 120.dp
): Float {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val fraction by remember(scrollState, thresholdPx) {
        derivedStateOf { (scrollState.value / thresholdPx).coerceIn(0f, 1f) }
    }
    return fraction
}
