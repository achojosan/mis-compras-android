package app.miscompras.com.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun PurchaseSettingsScreen(
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

            SettingsGroup(title = "Al finalizar la compra") {
                SettingsSwitchRow(
                    icon = Icons.Default.DeleteSweep,
                    title = "Eliminar productos comprados",
                    subtitle = if (uiState.deletePurchased) {
                        "Los productos se eliminan automáticamente al completar la compra"
                    } else {
                        "Los productos se mantienen en la lista para la próxima compra"
                    },
                    checked = uiState.deletePurchased,
                    onCheckedChange = viewModel::setDeletePurchased
                )
            }

            SettingsGroup(title = "Cómo funciona") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    SettingsInfoBullet(
                        icon = Icons.Default.CheckCircle,
                        text = "Marca con la casilla los productos que ya has comprado."
                    )
                    SettingsInfoBullet(
                        icon = Icons.Default.ShoppingCart,
                        text = "Pulsa «Finalizar» en el supermercado para registrar la compra."
                    )
                    SettingsInfoBullet(
                        icon = Icons.Default.History,
                        text = "El gasto aparecerá en la pestaña «Gastos»."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        FloatingTopBar(
            title = "Gestión de compra",
            backgroundFraction = barFraction,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
