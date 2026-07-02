package app.miscompras.com.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.data.local.datastore.ThemeMode
import app.miscompras.com.ui.theme.ThemeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apariencia", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SettingsSection(title = "COLOR DEL TEMA") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeColor.entries.forEach { colorOption ->
                        ColorOption(
                            color = colorOption.previewColor,
                            isSelected = uiState.themeColor == colorOption,
                            label = colorOption.displayName,
                            onClick = { viewModel.setThemeColor(colorOption) }
                        )
                    }
                }
            }

            SettingsSection(title = "MODO") {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.System -> "Sistema"
                        ThemeMode.Light -> "Claro"
                        ThemeMode.Dark -> "Oscuro"
                    }
                    RadioButtonRow(
                        label = label,
                        isSelected = uiState.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) }
                    )
                }
            }

            SettingsSection(title = "TAMAÑO DEL TEXTO") {
                listOf(
                    0.85f to "Pequeño",
                    1.0f to "Normal",
                    1.2f to "Grande",
                    1.4f to "Muy grande"
                ).forEach { (scale, label) ->
                    RadioButtonRow(
                        label = label,
                        isSelected = uiState.fontScale == scale,
                        onClick = { viewModel.setFontScale(scale) }
                    )
                }
            }

            SettingsSection(title = "ESPACIADO") {
                listOf(
                    0.8f to "Compacto",
                    1.0f to "Normal",
                    1.3f to "Amplio"
                ).forEach { (scale, label) ->
                    RadioButtonRow(
                        label = label,
                        isSelected = uiState.spacingScale == scale,
                        onClick = { viewModel.setSpacingScale(scale) }
                    )
                }
            }

            SettingsSection(title = "ALTO CONTRASTE") {
                SwitchRow(
                    label = "Activar alto contraste",
                    description = "Mejora la visibilidad con colores más contrastados",
                    checked = uiState.highContrast,
                    onCheckedChange = viewModel::setHighContrast
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            content()
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(if (isSelected) 4.dp else 1.dp, CircleShape)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun RadioButtonRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
