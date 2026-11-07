package app.miscompras.com.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.data.local.datastore.ThemeMode
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.ThemeColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()
    val barFraction = rememberTopBarFraction(scrollState)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = AccessibilityDefaults.MinTouchTarget + AccessibilityDefaults.ItemSpacing
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsGroup(title = "Color del tema") {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeColor.entries.forEach { option ->
                        ThemeColorSwatch(
                            color = option.previewColor,
                            label = option.displayName,
                            selected = uiState.themeColor == option,
                            onClick = { viewModel.setThemeColor(option) }
                        )
                    }
                }
            }

            SettingsGroup(title = "Modo") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SegmentedOptions(
                        options = ThemeMode.entries,
                        selected = uiState.themeMode,
                        onSelect = viewModel::setThemeMode,
                        label = { mode ->
                            when (mode) {
                                ThemeMode.System -> "Sistema"
                                ThemeMode.Light -> "Claro"
                                ThemeMode.Dark -> "Oscuro"
                            }
                        }
                    )
                }
            }

            SettingsGroup(title = "Tamaño del texto") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            0.85f to "Pequeño",
                            1.0f to "Normal",
                            1.2f to "Grande",
                            1.4f to "Muy grande"
                        ).forEach { (scale, label) ->
                            SelectableChip(
                                text = label,
                                selected = uiState.fontScale == scale,
                                onClick = { viewModel.setFontScale(scale) }
                            )
                        }
                    }
                }
            }

            SettingsGroup(title = "Espaciado") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            0.8f to "Compacto",
                            1.0f to "Normal",
                            1.3f to "Amplio"
                        ).forEach { (scale, label) ->
                            SelectableChip(
                                text = label,
                                selected = uiState.spacingScale == scale,
                                onClick = { viewModel.setSpacingScale(scale) }
                            )
                        }
                    }
                }
            }

            SettingsGroup(title = "Accesibilidad") {
                SettingsSwitchRow(
                    icon = Icons.Default.Contrast,
                    title = "Alto contraste",
                    subtitle = "Colores más marcados para mejorar la visibilidad",
                    checked = uiState.highContrast,
                    onCheckedChange = viewModel::setHighContrast
                )
            }

            SettingsGroup(title = "Pantalla principal") {
                SettingsSwitchRow(
                    icon = Icons.Default.Tag,
                    title = "Mostrar recuento",
                    subtitle = "Número de tiendas, productos y pendientes en la pantalla principal",
                    checked = uiState.showCounts,
                    onCheckedChange = viewModel::setShowCounts
                )
            }

            SettingsGroup(title = "Productos") {
                SettingsSwitchRow(
                    icon = Icons.Default.TextFields,
                    title = "Nombres en mayúsculas",
                    subtitle = "Al añadir un producto, su nombre se guarda en mayúsculas para verlo mejor",
                    checked = uiState.uppercaseProductNames,
                    onCheckedChange = viewModel::setUppercaseProductNames
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        FloatingTopBar(
            title = "Apariencia",
            backgroundFraction = barFraction,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
