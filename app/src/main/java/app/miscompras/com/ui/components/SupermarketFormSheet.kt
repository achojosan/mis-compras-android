package app.miscompras.com.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Hoja de alta de tienda.
 */
@Composable
fun AddSupermarketSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, logoUri: String?, passColor: Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var passColor by remember { mutableStateOf<Int?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    SupermarketFormBody(
        title = "Nueva tienda",
        subtitle = "Añade un supermercado a tu lista",
        name = name,
        onNameChange = {
            name = it
            nameError = null
        },
        logoUri = logoUri,
        onLogoUriChange = { logoUri = it },
        passColor = passColor,
        onPassColorChange = { passColor = it },
        nameError = nameError,
        saveLabel = "Crear tienda",
        onSave = {
            nameError = if (name.isBlank()) "El nombre es obligatorio" else null
            if (name.isNotBlank()) {
                onSave(name.trim(), logoUri, passColor)
            }
        },
        onDismiss = onDismiss
    )
}

/**
 * Hoja de edición de tienda, precargada con los datos actuales.
 */
@Composable
fun EditSupermarketSheet(
    supermarket: Supermarket,
    onDismiss: () -> Unit,
    onSave: (name: String, logoUri: String?, passColor: Int?) -> Unit
) {
    var name by remember { mutableStateOf(supermarket.name) }
    var logoUri by remember {
        mutableStateOf(supermarket.logoPath ?: DefaultSupermarkets.getLogoAssetPath(supermarket.name))
    }
    var passColor by remember { mutableStateOf(supermarket.passColor) }
    var nameError by remember { mutableStateOf<String?>(null) }

    SupermarketFormBody(
        title = "Editar tienda",
        subtitle = supermarket.name,
        name = name,
        onNameChange = {
            name = it
            nameError = null
        },
        logoUri = logoUri,
        onLogoUriChange = { logoUri = it },
        passColor = passColor,
        onPassColorChange = { passColor = it },
        nameError = nameError,
        saveLabel = "Guardar cambios",
        onSave = {
            nameError = if (name.isBlank()) "El nombre es obligatorio" else null
            if (name.isNotBlank()) {
                onSave(name.trim(), logoUri, passColor)
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun SupermarketFormBody(
    title: String,
    subtitle: String?,
    name: String,
    onNameChange: (String) -> Unit,
    logoUri: String?,
    onLogoUriChange: (String?) -> Unit,
    passColor: Int?,
    onPassColorChange: (Int?) -> Unit,
    nameError: String?,
    saveLabel: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onLogoUriChange(uri.toString())
        }
    }

    FormSheetScaffold(
        title = title,
        subtitle = subtitle,
        onDismiss = onDismiss
    ) {
        FormTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Nombre del supermercado",
            isError = nameError != null,
            errorMessage = nameError
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri == null || DefaultSupermarkets.isLogoAssetPath(logoUri)) {
                            SupermarketLogo(
                                supermarketName = name,
                                customLogoPath = logoUri,
                                size = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(logoUri)
                                    .build(),
                                contentDescription = "Logo seleccionado",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            logoUri == null -> "Usa el logo de la tienda o elige una imagen"
                            DefaultSupermarkets.isLogoAssetPath(logoUri) -> "Logo oficial de la tienda"
                            else -> "Imagen personalizada"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(if (logoUri == null) "Elegir" else "Cambiar")
                }
            }
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing + 4.dp))

        Text(
            text = "Supermercados predeterminados",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
        ) {
            DefaultSupermarkets.list.forEach { defaultSupermarket ->
                val defaultAsset = DefaultSupermarkets.getLogoAssetPath(defaultSupermarket.name)
                val selected = name.equals(defaultSupermarket.name, ignoreCase = true) &&
                    (logoUri == null || logoUri == defaultAsset)
                Surface(
                    onClick = {
                        onNameChange(defaultSupermarket.name)
                        onLogoUriChange(defaultAsset)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    border = if (selected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                    modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupermarketLogo(
                            supermarketName = defaultSupermarket.name,
                            customLogoPath = null,
                            size = 28.dp
                        )
                        Text(
                            text = defaultSupermarket.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Text(
            text = "Color de la tienda",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))
        PassColorPicker(
            selectedId = passColor,
            onSelect = onPassColorChange
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing + 8.dp))

        PrimarySheetButton(
            text = saveLabel,
            onClick = onSave,
            enabled = name.isNotBlank()
        )
    }
}
