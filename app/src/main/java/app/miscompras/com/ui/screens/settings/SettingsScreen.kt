package app.miscompras.com.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.foundation.clickable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AccessibilityDefaults.ItemSpacing),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
            Text(
                text = "APARIENCIA",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(
                    top = AccessibilityDefaults.ItemSpacing,
                    bottom = AccessibilityDefaults.ItemSpacing / 2
                )
            )

            FontScaleSection(
                currentScale = uiState.fontScale,
                onScaleSelected = viewModel::setFontScale
            )

            SpacingScaleSection(
                currentScale = uiState.spacingScale,
                onScaleSelected = viewModel::setSpacingScale
            )

            HighContrastSection(
                enabled = uiState.highContrast,
                onEnabledChange = viewModel::setHighContrast
            )

            ThemeSection(
                currentMode = uiState.themeMode,
                onModeSelected = viewModel::setThemeMode
            )

            Text(
                text = "GESTIÓN DE COMPRA",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(
                    top = AccessibilityDefaults.ItemSpacing,
                    bottom = AccessibilityDefaults.ItemSpacing / 2
                )
            )

            PurchaseSection(
                deletePurchased = uiState.deletePurchased,
                onDeletePurchasedChange = viewModel::setDeletePurchased
            )

            Text(
                text = "INFORMACIÓN",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(
                    top = AccessibilityDefaults.ItemSpacing,
                    bottom = AccessibilityDefaults.ItemSpacing / 2
                )
            )

            val context = LocalContext.current
            val versionName = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (e: Exception) { "" }

            Text(
                text = "Mis Compras v$versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Desarrollado por ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Josan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:achojosan@proton.me")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
        }
}

@Composable
fun FontScaleSection(
    currentScale: Float,
    onScaleSelected: (Float) -> Unit
) {
    SettingsSection(title = "Tamaño del texto") {
        FontScaleOption(
            label = "Pequeño",
            scale = 0.85f,
            isSelected = currentScale == 0.85f,
            onClick = { onScaleSelected(0.85f) }
        )
        FontScaleOption(
            label = "Normal",
            scale = 1.0f,
            isSelected = currentScale == 1.0f,
            onClick = { onScaleSelected(1.0f) }
        )
        FontScaleOption(
            label = "Grande",
            scale = 1.2f,
            isSelected = currentScale == 1.2f,
            onClick = { onScaleSelected(1.2f) }
        )
        FontScaleOption(
            label = "Muy grande",
            scale = 1.4f,
            isSelected = currentScale == 1.4f,
            onClick = { onScaleSelected(1.4f) }
        )
    }
}

@Composable
fun FontScaleOption(
    label: String,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SpacingScaleSection(
    currentScale: Float,
    onScaleSelected: (Float) -> Unit
) {
    SettingsSection(title = "Espaciado") {
        SpacingScaleOption(
            label = "Compacto",
            scale = 0.8f,
            isSelected = currentScale == 0.8f,
            onClick = { onScaleSelected(0.8f) }
        )
        SpacingScaleOption(
            label = "Normal",
            scale = 1.0f,
            isSelected = currentScale == 1.0f,
            onClick = { onScaleSelected(1.0f) }
        )
        SpacingScaleOption(
            label = "Amplio",
            scale = 1.3f,
            isSelected = currentScale == 1.3f,
            onClick = { onScaleSelected(1.3f) }
        )
    }
}

@Composable
fun SpacingScaleOption(
    label: String,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun HighContrastSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    SettingsSection(title = "Alto contraste") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Activar alto contraste",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Mejora la visibilidad con colores más contrastados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            )
        }
    }
}

@Composable
fun PurchaseSection(
    deletePurchased: Boolean,
    onDeletePurchasedChange: (Boolean) -> Unit
) {
    SettingsSection(title = "Gestión de compra") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Eliminar productos al finalizar",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (deletePurchased) "Los productos se eliminan automáticamente al completar la compra"
                    else "Los productos permanecen en la lista para la próxima compra",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = deletePurchased,
                onCheckedChange = onDeletePurchasedChange,
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            )
        }
    }
}

@Composable
fun ThemeSection(
    currentMode: app.miscompras.com.data.local.datastore.ThemeMode,
    onModeSelected: (app.miscompras.com.data.local.datastore.ThemeMode) -> Unit
) {
    SettingsSection(title = "Apariencia") {
        app.miscompras.com.data.local.datastore.ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
            ) {
                RadioButton(
                    selected = currentMode == mode,
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                )
                Text(
                    text = when (mode) {
                        app.miscompras.com.data.local.datastore.ThemeMode.System -> "Sistema"
                        app.miscompras.com.data.local.datastore.ThemeMode.Light -> "Claro"
                        app.miscompras.com.data.local.datastore.ThemeMode.Dark -> "Oscuro"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AccessibilityDefaults.CardPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = AccessibilityDefaults.ItemSpacing)
            )
            content()
        }
    }
}
