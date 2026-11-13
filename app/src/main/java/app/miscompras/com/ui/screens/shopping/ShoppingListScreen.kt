package app.miscompras.com.ui.screens.shopping

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import app.miscompras.com.ui.components.BigFigure
import app.miscompras.com.ui.components.DashedDivider
import app.miscompras.com.ui.components.QuantityStepper
import app.miscompras.com.ui.components.TicketCard
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.PassPalette
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.ui.theme.isLargeText
import app.miscompras.com.ui.theme.pass
import app.miscompras.com.util.formatEuros
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
                val totalProductCount = state.shoppingItems.sumOf { it.products.size }
                val largeText = LocalAccessibilityConfig.current.isLargeText

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = AccessibilityDefaults.ItemSpacing,
                        end = AccessibilityDefaults.ItemSpacing,
                        top = AccessibilityDefaults.SectionSpacing,
                        bottom = AccessibilityDefaults.SectionSpacing
                    ),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.SectionSpacing)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                        ) {
                            BigFigure(
                                figure = formatEuros(grandTotal),
                                caption = "estimado en $totalProductCount producto${if (totalProductCount != 1) "s" else ""}",
                                figureStyle = if (largeText) MaterialTheme.typography.displaySmall
                                else MaterialTheme.typography.displayMedium,
                                modifier = Modifier.weight(1f)
                            )
                            androidx.compose.material3.IconButton(
                                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget),
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
                                    tint = MaterialTheme.colorScheme.primary
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
    val purchasedCount = shoppingItem.products.count { it.isPurchased }
    val totalCount = shoppingItem.products.size
    val pass = supermarket?.pass ?: PassPalette.defaultFor(0)

    TicketCard(
        pass = pass,
        modifier = modifier,
        header = { content ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
            ) {
                SupermarketLogoPlate(
                    supermarketName = supermarket?.name ?: "",
                    logoPath = supermarket?.logoPath,
                    height = 52.dp,
                    cornerRadius = 14.dp,
                    contentPadding = 8.dp,
                    shadowElevation = 0.dp,
                    showBorder = false,
                    containerColor = androidx.compose.ui.graphics.Color.White
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supermarket?.name ?: "Supermercado",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "$purchasedCount de $totalCount comprado${if (purchasedCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = content.copy(alpha = 0.88f)
                    )
                }
            }
        },
        body = {
            shoppingItem.products.forEachIndexed { index, product ->
                if (index > 0) DashedDivider()
                ShoppingProductItem(
                    product = product,
                    onToggle = { onToggleProduct(product) },
                    onQuantityChange = { onQuantityChange(product.id, it) }
                )
            }

            DashedDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AccessibilityDefaults.ItemSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = formatEuros(shoppingItem.total),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = TabularNumbers)
                )
            }
            Spacer(Modifier.height(AccessibilityDefaults.ItemSpacing))
            AccessibleButton(
                text = "Finalizar compra",
                onClick = onFinishPurchase,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

/**
 * Fila de un producto. Pulsar el nombre (o el círculo) lo marca como comprado: el objetivo táctil
 * es toda la fila de arriba, no una casilla de 24 dp. La cantidad va aparte, con su − / +.
 * Con texto grande el precio baja a la línea de la cantidad en lugar de apretar el nombre.
 */
@Composable
fun ShoppingProductItem(
    product: Product,
    onToggle: () -> Unit,
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPrice = product.price?.let { it * product.quantity }
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val largeText = LocalAccessibilityConfig.current.isLargeText

    Column(modifier = modifier.fillMaxWidth().padding(vertical = AccessibilityDefaults.ItemSpacing / 2)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AccessibilityDefaults.MinTouchTarget)
                .toggleable(
                    value = product.isPurchased,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            CheckCircle(checked = product.isPurchased)
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                color = if (product.isPurchased) dim else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!largeText && totalPrice != null) PriceText(totalPrice, product.isPurchased)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp + AccessibilityDefaults.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuantityStepper(
                quantity = product.quantity,
                unit = product.unit,
                onQuantityChange = onQuantityChange
            )
            if (largeText && totalPrice != null) PriceText(totalPrice, product.isPurchased)
        }
    }
}

@Composable
private fun PriceText(amount: Double, purchased: Boolean) {
    Text(
        text = formatEuros(amount),
        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = TabularNumbers),
        fontWeight = FontWeight.SemiBold,
        color = if (purchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    )
}

/** Círculo que se rellena al marcar el producto. La forma (círculo + marca), no solo el color, indica el estado. */
@Composable
private fun CheckCircle(checked: Boolean) {
    val fill by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(200),
        label = "checkFill"
    )
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar compra") },
        text = {
            Column {
                Text(
                    "¿Confirmas que has comprado $productCount producto" +
                        "${if (productCount != 1) "s" else ""} en $supermarketName " +
                        "por un total de ${formatEuros(total)}?"
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
