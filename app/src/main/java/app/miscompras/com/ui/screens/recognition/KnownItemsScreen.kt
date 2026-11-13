package app.miscompras.com.ui.screens.recognition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import app.miscompras.com.recognition.commonPart
import app.miscompras.com.recognition.distinctiveWord
import app.miscompras.com.ui.components.ItemIcons
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.theme.PassPalette
import app.miscompras.com.ui.theme.resolvePass
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.KnownItemWithRefs
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.FloatingIconButton
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun KnownItemsScreen(
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: KnownItemsViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val listState = rememberLazyListState()
    var toDelete by remember { mutableStateOf<KnownItemWithRefs?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FloatingTopBar(
            title = "Mis artículos",
            backgroundFraction = rememberTopBarFraction(listState),
            onNavigateBack = onNavigateBack
        )
        val list = items
        when {
            list == null -> Unit
            list.isEmpty() -> EmptyState(
                icon = Icons.Default.CameraAlt,
                title = "Aún no hay artículos",
                description = "Registra un artículo con la cámara para que la app lo reconozca.",
                modifier = Modifier.weight(1f)
            ) { AccessibleButton(text = "Registrar artículo", onClick = onRegisterClick) }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    items(list, key = { it.item.id }) { entry ->
                        KnownItemRow(
                            entry = entry,
                            onClick = { onItemClick(entry.item.id) },
                            onDelete = { toDelete = entry }
                        )
                    }
                }
                Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    AccessibleButton(text = "Registrar artículo", onClick = onRegisterClick)
                }
            }
        }
    }

    toDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar ${entry.item.name}?") },
            text = { Text("Se borrarán también sus fotos. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(entry.item.id); toDelete = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

/**
 * Fila de un artículo: a la izquierda su baldosa (color + icono propios, la misma que verá al
 * reconocerlo), después el nombre con la palabra distintiva en grande y los datos. Con texto grande
 * el nombre ocupa todo el ancho y los datos bajan; el botón de eliminar mide 56 dp.
 */
@Composable
private fun KnownItemRow(entry: KnownItemWithRefs, onClick: () -> Unit, onDelete: () -> Unit) {
    val item = entry.item
    val resolved = resolvePass(PassPalette.byId(item.color, item.id))
    val icon = ItemIcons.byKey(item.icon)
    val common = commonPart(item.name)
    val details = "${entry.references.size} foto${if (entry.references.size == 1) "" else "s"}" +
        (item.barcode?.let { " · $it" } ?: "")

    Surface(
        shape = AppShapes.pass,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().clip(AppShapes.pass).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = AppShapes.small,
                color = resolved.container,
                border = if (resolved.outlined) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon?.vector ?: Icons.Default.CameraAlt,
                        contentDescription = icon?.label,
                        tint = resolved.content,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                if (common != null) {
                    Text(common, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(distinctiveWord(item.name), style = MaterialTheme.typography.headlineSmall)
                Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FloatingIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "Eliminar ${item.name}",
                onClick = onDelete
            )
        }
    }
}
