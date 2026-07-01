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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.theme.AccessibilityDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AccessibilityDefaults.ItemSpacing),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.SectionSpacing)
        ) {
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

            Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))
        }
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
