package app.miscompras.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.PassPalette

/**
 * Selector del color de pase. Cada opción lleva muestra **y nombre** (el color nunca es la
 * única pista) y mide ≥ 56 dp. «Automático» deja que la app lo elija.
 * Se reparte en filas con [FlowRow], de modo que con texto grande baja de línea sin deformarse.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PassColorPicker(
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    automaticLabel: String = "Automático"
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PickerOption(
            label = automaticLabel,
            selected = selectedId == null,
            onClick = { onSelect(null) }
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        PassPalette.all.forEach { pass ->
            PickerOption(
                label = pass.name,
                selected = selectedId == pass.id,
                onClick = { onSelect(pass.id) },
                swatch = pass.light
            ) {
                if (selectedId == pass.id) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = pass.onLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatch: androidx.compose.ui.graphics.Color? = null,
    mark: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .defaultMinSize(minWidth = 76.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(swatch ?: MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { mark() }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Selector del icono de un artículo: cada opción con su nombre y ≥ 56 dp, en filas que bajan de línea. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemIconPicker(
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PickerOption(label = "Ninguno", selected = selectedKey == null, onClick = { onSelect(null) }) {}
        ItemIcons.all.forEach { icon ->
            PickerOption(
                label = icon.label,
                selected = selectedKey == icon.key,
                onClick = { onSelect(icon.key) }
            ) {
                Icon(
                    imageVector = icon.vector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
