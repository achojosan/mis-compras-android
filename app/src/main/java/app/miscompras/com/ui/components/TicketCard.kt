package app.miscompras.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.PassColor
import app.miscompras.com.ui.theme.TicketEdgeShape
import app.miscompras.com.ui.theme.resolvePass

/**
 * Ticket de una tienda: cabecera con el color de su pase, cuerpo de papel y borde inferior
 * dentado. En alto contraste la cabecera pasa a superficie con borde de 2 dp.
 * Sin sombras: la profundidad la da el contraste entre cabecera y papel.
 */
@Composable
fun TicketCard(
    pass: PassColor,
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.(contentColor: Color) -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    val resolved = resolvePass(pass)
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = resolved.container,
            contentColor = resolved.content,
            border = if (resolved.outlined) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) { header(resolved.content) }
        }
        Surface(
            shape = TicketEdgeShape(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 22.dp)
            ) { body() }
        }
    }
}

/** Línea discontinua de separación entre filas del ticket. */
@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))
        )
    }
}
