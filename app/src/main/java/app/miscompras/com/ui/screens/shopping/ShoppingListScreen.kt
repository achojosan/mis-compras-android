package app.miscompras.com.ui.screens.shopping

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.util.ToastEffect
import app.miscompras.com.util.toDoubleOrNullLocalized
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showFinishDialog by remember { mutableStateOf<ShoppingItem?>(null) }

    ToastEffect(events = viewModel.events)

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
                            shape = AppShapes.card,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total estimado",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "$totalProductCount producto${if (totalProductCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Text(
                                    text = currencyFormat.format(grandTotal),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )

                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        val text = app.miscompras.com.util.ShoppingListFormatter.format(
                                            state.shoppingItems.map { (it.supermarket?.name ?: "Sin tienda") to it.products }
                                        )
                                        if (text.isBlank()) {
                                            android.widget.Toast.makeText(context, "No hay nada pendiente que compartir", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            app.miscompras.com.util.ListSharing.share(context, text)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir lista",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
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
            total = shoppingItem.totalToRecord,
            productCount = shoppingItem.productsToRecord.size,
            noneMarked = shoppingItem.purchasedProducts.isEmpty(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AccessibilityDefaults.ItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
            ) {
                SupermarketLogoPlate(
                    supermarketName = supermarket?.name ?: "",
                    logoPath = supermarket?.logoPath,
                    height = 56.dp,
                    cornerRadius = 18.dp,
                    contentPadding = 10.dp,
                    shadowElevation = 2.dp
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
                        value.toDoubleOrNullLocalized()
                            ?.takeIf { it > 0 }
                            ?.let { onQuantityChange(it) }
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
    productCount: Int,
    noneMarked: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar compra") },
        text = {
            Column {
                Text(
                    "¿Confirmas que has comprado $productCount producto" +
                        "${if (productCount != 1) "s" else ""} en $supermarketName " +
                        "por un total de ${currencyFormat.format(total)}?"
                )
                if (noneMarked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No has marcado ningún producto, así que se registrarán todos los de la lista.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
