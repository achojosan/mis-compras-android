package app.miscompras.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.util.formatQuantity
import app.miscompras.com.util.toDoubleOrNullLocalized

/** Los botones miden 56 dp: se pueden pulsar con una mano, de pie y sin precisión. */
private val StepperButton = 56.dp

/**
 * Cantidad con − / +. Sustituye al campo de texto de 60 dp, que se cortaba con texto grande.
 * Una pulsación larga sobre la cifra abre un diálogo para teclear decimales («1,5 kg»).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuantityStepper(
    quantity: Double,
    unit: String,
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    val label = "${formatQuantity(quantity)} $unit".trim()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepButton(
            icon = Icons.Default.Remove,
            description = "Quitar una unidad de $label",
            enabled = quantity > 1.0,
            onClick = { onQuantityChange((quantity - 1.0).coerceAtLeast(1.0)) }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TabularNumbers),
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = StepperButton)
                .combinedClickable(
                    onClick = { editing = true },
                    onLongClick = { editing = true },
                    role = Role.Button
                )
                .semantics {
                    contentDescription = "Cantidad: $label. Toca para escribirla"
                    customActions = listOf(
                        CustomAccessibilityAction("Escribir cantidad") { editing = true; true }
                    )
                }
                .padding(horizontal = 6.dp, vertical = 14.dp)
        )
        StepButton(
            icon = Icons.Default.Add,
            description = "Añadir una unidad de $label",
            enabled = true,
            onClick = { onQuantityChange(quantity + 1.0) }
        )
    }

    if (editing) {
        var text by remember { mutableStateOf(formatQuantity(quantity)) }
        val parsed = text.toDoubleOrNullLocalized()?.takeIf { it > 0 }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Cantidad") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("Cantidad en $unit") },
                    isError = parsed == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = parsed != null,
                    onClick = { parsed?.let(onQuantityChange); editing = false }
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            2.dp,
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .size(StepperButton)
            .clip(CircleShape)
            .semantics { contentDescription = description },
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(14.dp)
        )
    }
}
