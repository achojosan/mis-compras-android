package app.miscompras.com.ui.screens.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFinishDialog by remember { mutableStateOf<ShoppingItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ShoppingListUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ShoppingListUiState.Empty -> {
                EmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "Lista de compra vacía",
                    description = "Añade productos a tus supermercados para verlos aquí"
                )
            }
            is ShoppingListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(AccessibilityDefaults.ItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    items(state.shoppingItems, key = { it.supermarket?.id ?: 0 }) { shoppingItem ->
                        SupermarketShoppingCard(
                            shoppingItem = shoppingItem,
                            onToggleProduct = viewModel::toggleProductPurchased,
                            onFinishPurchase = { showFinishDialog = shoppingItem }
                        )
                    }
                }
            }
        }
    }

    showFinishDialog?.let { shoppingItem ->
        FinishPurchaseDialog(
            supermarketName = shoppingItem.supermarket?.name ?: "",
            total = shoppingItem.total,
            onConfirm = {
                viewModel.finishPurchase(shoppingItem)
                showFinishDialog = null
            },
            onDismiss = { showFinishDialog = null }
        )
    }
}

@Composable
fun SupermarketShoppingCard(
    shoppingItem: ShoppingItem,
    onToggleProduct: (Product) -> Unit,
    onFinishPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val supermarket = shoppingItem.supermarket
    val defaultLogoRes = supermarket?.let { DefaultSupermarkets.getLogoResId(it.name) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    Card(
        modifier = modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AccessibilityDefaults.ItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
            ) {
                if (supermarket?.logoPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(supermarket.logoPath)
                            .build(),
                        contentDescription = "Logo de ${supermarket.name}",
                        modifier = Modifier.size(AccessibilityDefaults.LargeIconSize)
                    )
                } else if (defaultLogoRes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(defaultLogoRes)
                            .build(),
                        contentDescription = "Logo de ${supermarket.name}",
                        modifier = Modifier.size(AccessibilityDefaults.LargeIconSize)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(AccessibilityDefaults.LargeIconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supermarket?.name ?: "Supermercado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${shoppingItem.products.size} productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            shoppingItem.products.forEach { product ->
                ShoppingProductItem(
                    product = product,
                    onToggle = { onToggleProduct(product) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AccessibilityDefaults.ItemSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${currencyFormat.format(shoppingItem.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                AccessibleButton(
                    text = "Finalizar",
                    onClick = onFinishPurchase,
                    modifier = Modifier.padding(start = AccessibilityDefaults.ItemSpacing)
                )
            }
        }
    }
}

@Composable
fun ShoppingProductItem(
    product: Product,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AccessibilityDefaults.ItemSpacing / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        Checkbox(
            checked = product.isPurchased,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null
            )
            Text(
                text = "${product.quantity} ${product.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${product.price}€",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FinishPurchaseDialog(
    supermarketName: String,
    total: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar compra") },
        text = {
            Text("¿Confirmas que has completado la compra en $supermarketName por un total de ${currencyFormat.format(total)}?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
