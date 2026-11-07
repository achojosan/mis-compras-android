package app.miscompras.com.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.screens.shopping.PurchasedProductData
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val barFraction = rememberTopBarFraction(listState)

    val title = (uiState as? HistoryDetailUiState.Success)?.let { state ->
        state.supermarket?.name ?: state.history.supermarketName
    } ?: "Compra"

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HistoryDetailUiState.Loading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            is HistoryDetailUiState.Error -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HistoryDetailUiState.Success -> {
                HistoryDetailContent(
                    supermarket = state.supermarket,
                    supermarketName = state.history.supermarketName,
                    products = state.products,
                    totalAmount = state.history.totalAmount,
                    purchasedAt = state.history.purchasedAt,
                    listState = listState
                )
            }
        }

        FloatingTopBar(
            title = title,
            backgroundFraction = barFraction,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun HistoryDetailContent(
    supermarket: Supermarket?,
    supermarketName: String?,
    products: List<PurchasedProductData>,
    totalAmount: Double,
    purchasedAt: Long,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AccessibilityDefaults.ItemSpacing,
            end = AccessibilityDefaults.ItemSpacing,
            top = AccessibilityDefaults.MinTouchTarget +
                AccessibilityDefaults.SectionSpacing +
                AccessibilityDefaults.ItemSpacing,
            bottom = AccessibilityDefaults.ItemSpacing
        ),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AccessibilityDefaults.CardPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        SupermarketLogoPlate(
                            supermarketName = supermarket?.name ?: supermarketName ?: "",
                            logoPath = supermarket?.logoPath,
                            height = 76.dp,
                            cornerRadius = 22.dp,
                            contentPadding = 12.dp,
                            shadowElevation = 3.dp
                        )

                        Column {
                            Text(
                                text = supermarket?.name ?: supermarketName ?: "Supermercado",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = dateFormat.format(Date(purchasedAt)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = AccessibilityDefaults.ItemSpacing)
                    )

                    Text(
                        text = currencyFormat.format(totalAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text(
                text = "Productos comprados",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing)
            )
        }

        items(products) { product ->
            HistoryProductItem(product = product)
        }
    }
}

@Composable
fun HistoryProductItem(
    product: PurchasedProductData,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${product.quantity} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val price = product.price
            if (price != null) {
                Text(
                    text = currencyFormat.format(price * product.quantity),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
