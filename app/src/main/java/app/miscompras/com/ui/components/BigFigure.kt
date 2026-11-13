package app.miscompras.com.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.TabularNumbers

/**
 * Cifra grande con su leyenda («10» / «cosas por comprar en 3 tiendas»).
 * Sin tarjeta ni fondo: el número manda. TalkBack lo lee como una sola frase.
 * La cifra usa el rol `displayLarge` (se agranda con el texto del sistema) y la leyenda
 * puede ocupar varias líneas, de modo que nada se corta.
 */
@Composable
fun BigFigure(
    figure: String,
    caption: String,
    modifier: Modifier = Modifier,
    figureStyle: TextStyle = MaterialTheme.typography.displayLarge,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    figureColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$figure $caption" }
    ) {
        Text(
            text = figure,
            style = figureStyle.copy(fontFeatureSettings = TabularNumbers),
            color = figureColor
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}
