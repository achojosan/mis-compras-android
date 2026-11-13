package app.miscompras.com.ui.screens.recognition

import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.material3.Icon
import app.miscompras.com.recognition.commonPart
import app.miscompras.com.recognition.distinctiveWord
import app.miscompras.com.ui.components.ItemIcons
import app.miscompras.com.ui.theme.PassPalette
import app.miscompras.com.ui.theme.resolvePass
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
    // Cada artículo tiene su color: «el azul es el pollo». Se distingue antes de leer.
    val resolved = resolvePass(PassPalette.byId(data?.item?.color, data?.item?.id ?: 0L))
    val content = resolved.content

    Column(modifier = Modifier.fillMaxSize().background(resolved.container)) {
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
        val word = distinctiveWord(current.item.name)
        val common = commonPart(current.item.name)
        val icon = ItemIcons.byKey(current.item.icon)
        // Palabra enorme: se parte con guion si es larga, nunca se corta ni se sale de la pantalla.
        val wordStyle = MaterialTheme.typography.displayLarge.copy(
            fontSize = 60.sp,
            lineHeight = 64.sp,
            hyphens = Hyphens.Auto,
            lineBreak = LineBreak.Heading
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.SectionSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon?.vector ?: Icons.Default.Check,
                    contentDescription = icon?.label,
                    tint = if (resolved.outlined) Color.Black else resolved.container,
                    modifier = Modifier.size(52.dp)
                )
            }
            Column {
                if (common != null) {
                    Text(
                        text = common,
                        style = MaterialTheme.typography.headlineMedium,
                        color = content,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = word,
                    style = wordStyle,
                    color = content,
                    modifier = Modifier.fillMaxWidth().semantics { heading() }
                )
            }
            if (current.item.description.isNotBlank()) {
                Text(
                    text = current.item.description,
                    style = MaterialTheme.typography.headlineSmall,
                    color = content
                )
            }
            current.item.barcode?.let {
                Text(
                    text = "Código de barras: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = content.copy(alpha = 0.9f)
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
            // Blanco sobre el color del artículo: contraste garantizado con cualquier pase.
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = AccessibilityDefaults.MinTouchTarget),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF13201A))
            ) {
                Text("Escanear otro", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
