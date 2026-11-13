package app.miscompras.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.nearby.OpeningHours
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.ui.theme.pass
import app.miscompras.com.ui.theme.resolvePass
import java.time.LocalDateTime

/**
 * Pase de una tienda en la cartera: su color, su logo sobre una placa blanca, su nombre en
 * grande, si está abierta y cuántos productos quedan por comprar.
 *
 * - [stacked] (texto grande): logo y cifra arriba, nombre y datos debajo, a todo el ancho;
 *   nada se encoge ni se corta.
 * - [done]: la tienda sin pendientes se calma: superficie neutra con una marca de «hecho».
 * - En alto contraste el color desaparece (superficie + borde de 2 dp) y quedan forma y texto.
 */
@Composable
fun StorePass(
    supermarket: Supermarket,
    productCount: Int,
    pendingCount: Int,
    showCounts: Boolean,
    stacked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Espacio extra al pie que cubrirá el pase siguiente al solaparse. */
    overlapPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val done = productCount > 0 && pendingCount == 0
    val resolved = resolvePass(supermarket.pass)
    val container = if (done) MaterialTheme.colorScheme.surface else resolved.container
    val content = if (done) MaterialTheme.colorScheme.onSurface else resolved.content
    val outlined = resolved.outlined || done
    val secondary = content.copy(alpha = 0.88f)

    val status = remember(supermarket.openingHours) {
        OpeningHours.parse(supermarket.openingHours)?.statusText(LocalDateTime.now())
    }
    val summary = passSummary(productCount, pendingCount)

    Surface(
        onClick = onClick,
        shape = AppShapes.pass,
        color = container,
        contentColor = content,
        border = if (outlined) {
            BorderStroke(
                if (resolved.outlined) 2.dp else 1.dp,
                if (resolved.outlined) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
            )
        } else null,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp, end = 16.dp, top = 16.dp,
                bottom = 16.dp + overlapPadding
            )
        ) {
            if (stacked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Logo(supermarket)
                    if (showCounts) PendingFigure(pendingCount, done, content, secondary)
                }
                Spacer(Modifier.height(12.dp))
                Text(supermarket.name, style = MaterialTheme.typography.headlineMedium)
                PassDetails(status, summary, showCounts, secondary)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Logo(supermarket)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            supermarket.name,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2
                        )
                        PassDetails(status, summary, showCounts, secondary)
                    }
                    if (showCounts) PendingFigure(pendingCount, done, content, secondary)
                }
            }

            if (showCounts && productCount > 0 && !done) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (productCount - pendingCount).toFloat() / productCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = content,
                    trackColor = content.copy(alpha = 0.25f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun Logo(supermarket: Supermarket) {
    SupermarketLogoPlate(
        supermarketName = supermarket.name,
        logoPath = supermarket.logoPath,
        height = 56.dp,
        cornerRadius = 16.dp,
        contentPadding = 8.dp,
        shadowElevation = 0.dp,
        showBorder = false,
        containerColor = Color.White
    )
}

@Composable
private fun PassDetails(status: String?, summary: String, showCounts: Boolean, color: Color) {
    if (status != null) {
        Text(status, style = MaterialTheme.typography.bodyMedium, color = color)
    }
    if (showCounts) {
        Text(summary, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

/** Número de pendientes en grande, o una marca de «hecho» si no queda nada. */
@Composable
private fun PendingFigure(pending: Int, done: Boolean, content: Color, secondary: Color) {
    if (done) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Todo comprado",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AccessibilityDefaults.IconSize)
        )
        return
    }
    if (pending == 0) return
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = pending.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = TabularNumbers),
            color = content,
            maxLines = 1,
            textAlign = TextAlign.End
        )
        Text(
            text = if (pending == 1) "pendiente" else "pendientes",
            style = MaterialTheme.typography.labelMedium,
            color = secondary,
            maxLines = 1
        )
    }
}

internal fun passSummary(productCount: Int, pendingCount: Int): String = when {
    productCount == 0 -> "Sin productos todavía"
    pendingCount == 0 -> "$productCount producto${if (productCount == 1) "" else "s"} · todo comprado"
    else -> "$productCount producto${if (productCount == 1) "" else "s"}"
}
