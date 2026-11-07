package app.miscompras.com.ui.screens.recognition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.ButtonStyle
import app.miscompras.com.ui.components.FloatingIconButton
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults
import coil.compose.AsyncImage
import java.io.File

/** Ficha del artículo reconocido: nombre grande, descripción, código y fotos. */
@Composable
fun ItemDetailScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val data by viewModel.item.collectAsState(initial = null)
    val scroll = rememberScrollState()
    val fraction = rememberTopBarFraction(scroll)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FloatingTopBar(
            title = data?.item?.name ?: "",
            backgroundFraction = fraction,
            onNavigateBack = onNavigateBack,
            actions = {
                data?.let {
                    FloatingIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Editar artículo",
                        onClick = { onEditClick(it.item.id) }
                    )
                }
            }
        )
        val current = data ?: return@Column
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.SectionSpacing)
        ) {
            Text(
                text = current.item.name,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            if (current.item.description.isNotBlank()) {
                Text(
                    text = current.item.description,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            current.item.barcode?.let {
                Text(
                    text = "Código de barras: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                current.references.take(4).forEach { ref ->
                    AsyncImage(
                        model = File(ref.imagePath),
                        contentDescription = "Foto de ${current.item.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(84.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            AccessibleButton(text = "Escanear otro", onClick = onNavigateBack)
        }
    }
}
