package app.miscompras.com.ui.screens.recognition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.AccessibleTextField
import app.miscompras.com.ui.components.ButtonStyle
import app.miscompras.com.ui.components.CameraPermissionGate
import app.miscompras.com.ui.components.CameraScanner
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun RegisterItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegisterItemViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FloatingTopBar(
            title = if (state.isEditing) "Editar artículo" else "Registrar artículo",
            backgroundFraction = rememberTopBarFraction(scroll),
            onNavigateBack = onNavigateBack
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            Text(
                text = "Centra en el marco la parte que distingue este artículo (por ejemplo, la pegatina del sabor) y pulsa \"Tomar foto\". Haz 3 o 4 fotos desde distintos ángulos. Si tiene código de barras, muéstralo a la cámara.",
                style = MaterialTheme.typography.bodyLarge
            )
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))) {
                CameraPermissionGate {
                    CameraScanner(onFrame = viewModel::onFrame)
                }
            }
            AccessibleButton(
                text = "Tomar foto (${state.photos.size + state.existingPhotos})",
                onClick = viewModel::takePhoto
            )
            if (state.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.photos) { index, photo ->
                        Image(
                            bitmap = photo.bitmap.asImageBitmap(),
                            contentDescription = "Foto ${index + 1}. Toca para quitarla",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.removePhoto(index) }
                        )
                    }
                }
            }
            AccessibleTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = "Nombre (p. ej. Lata gato - Salmón)"
            )
            AccessibleTextField(
                value = state.description,
                onValueChange = viewModel::onDescription,
                label = "Notas (sabor, uso, alergias…)",
                singleLine = false
            )
            AccessibleTextField(
                value = state.barcode,
                onValueChange = viewModel::onBarcode,
                label = "Código de barras (opcional)",
                keyboardType = KeyboardType.Number
            )
            state.suggestion?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            AccessibleButton(
                text = "Guardar",
                enabled = !state.saving,
                onClick = { viewModel.save(onDone = onNavigateBack) }
            )
        }
    }
}
