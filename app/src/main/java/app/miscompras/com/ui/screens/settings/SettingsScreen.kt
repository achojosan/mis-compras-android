package app.miscompras.com.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.BuildConfig
import app.miscompras.com.update.UpdateState
import app.miscompras.com.update.UpdateViewModel
import app.miscompras.com.ui.util.ToastEffect

@Composable
fun SettingsScreen(
    onKnownItemsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onInfoClick: () -> Unit,
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val updatesEnabled by updateViewModel.enabled.collectAsState()
    val updateState by updateViewModel.state.collectAsState()
    ToastEffect(events = updateViewModel.messages)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Personaliza la app a tu manera",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsGroup(title = "Preferencias") {
                SettingsRow(
                    icon = Icons.Default.Palette,
                    title = "Apariencia",
                    subtitle = "Color, modo, tamaño y contraste",
                    onClick = onAppearanceClick
                )
                SettingsRow(
                    icon = Icons.Default.ShoppingCart,
                    title = "Gestión de compra",
                    subtitle = "Qué ocurre al finalizar una compra",
                    onClick = onPurchaseClick,
                    showDivider = true
                )
            }

            SettingsGroup(title = "Reconocimiento") {
                SettingsRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Mis artículos",
                    subtitle = "Artículos que la app sabe reconocer",
                    onClick = onKnownItemsClick
                )
            }

            SettingsGroup(title = "Actualizaciones") {
                SettingsSwitchRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "Avisarme de nuevas versiones",
                    subtitle = "Una consulta al día a GitHub al abrir la app. Puedes desactivarla.",
                    checked = updatesEnabled,
                    onCheckedChange = updateViewModel::setEnabled
                )
                SettingsRow(
                    icon = Icons.Default.Refresh,
                    title = "Buscar actualizaciones",
                    subtitle = if (updateState is UpdateState.Checking) "Buscando…" else "Versión instalada: ${BuildConfig.VERSION_NAME}",
                    onClick = updateViewModel::checkNow,
                    showDivider = true
                )
            }

            SettingsGroup(title = "Aplicación") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "Información",
                    subtitle = "Versión, contacto y créditos",
                    onClick = onInfoClick
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mis Compras",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
