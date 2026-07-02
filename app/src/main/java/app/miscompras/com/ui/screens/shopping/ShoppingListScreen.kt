package app.miscompras.com.ui.screens.shopping

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SupermarketLogo
import app.miscompras.com.ui.theme.AccessibilityDefaults
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFinishDialog by remember { mutableStateOf<ShoppingItem?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                val grandTotal = state.shoppingItems.sumOf { it.total }
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                val totalProductCount = state.shoppingItems.sumOf { it.products.size }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(AccessibilityDefaults.ItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AccessibilityDefaults.CardPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total estimado",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = currencyFormat.format(grandTotal),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$totalProductCount prod.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    items(state.shoppingItems, key = { it.supermarket?.id ?: 0 }) { shoppingItem ->
                        SupermarketShoppingCard(
                            shoppingItem = shoppingItem,
                            onToggleProduct = viewModel::toggleProductPurchased,
                            onQuantityChange = viewModel::updateProductQuantity,
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
    onQuantityChange: (Long, Double) -> Unit,
    onFinishPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supermarket = shoppingItem.supermarket
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val purchasedCount = shoppingItem.products.count { it.isPurchased }
    val totalCount = shoppingItem.products.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                SupermarketLogo(
                    supermarketName = supermarket?.name ?: "",
                    customLogoPath = supermarket?.logoPath,
                    size = AccessibilityDefaults.LargeIconSize
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supermarket?.name ?: "Supermercado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "$purchasedCount de $totalCount productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            shoppingItem.products.forEach { product ->
                ShoppingProductItem(
                    product = product,
                    onToggle = { onToggleProduct(product) },
                    onQuantityChange = { onQuantityChange(product.id, it) }
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
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPrice = product.price?.let { it * product.quantity }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (product.isPurchased) Modifier.graphicsLayer { alpha = 0.6f }
                else Modifier
            )
            .padding(vertical = AccessibilityDefaults.ItemSpacing / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
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
                textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                color = if (product.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = product.quantity.toBigDecimal().stripTrailingZeros().toPlainString(),
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { onQuantityChange(it) }
                    },
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(60.dp).heightIn(min = 36.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = product.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = if (totalPrice != null) "${String.format("%.2f", totalPrice)}€" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (product.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
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
