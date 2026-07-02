package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SupermarketLogo
import app.miscompras.com.ui.theme.AccessibilityDefaults
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SupermarketDetailScreen(
    onNavigateBack: () -> Unit,
    onAddProductClick: (Long) -> Unit,
    onEditSupermarket: (Long) -> Unit,
    viewModel: SupermarketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    when (val state = uiState) {
        is SupermarketDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SupermarketDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is SupermarketDetailUiState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                SupermarketDetailContent(
                    supermarket = state.supermarket,
                    products = state.products,
                    sortOrder = sortOrder,
                    onSortOrderChange = viewModel::setSortOrder,
                    onTogglePurchased = viewModel::toggleProductPurchased,
                    onDeleteProduct = { productToDelete = it },
                    onEditProduct = { productToEdit = it },
                    onAddProduct = { onAddProductClick(state.supermarket.id) },
                    onNavigateBack = onNavigateBack,
                    onEditSupermarket = { onEditSupermarket(state.supermarket.id) }
                )

                FloatingActionButton(
                    onClick = { onAddProductClick(state.supermarket.id) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AccessibilityDefaults.SectionSpacing)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir producto",
                        modifier = Modifier.size(AccessibilityDefaults.IconSize)
                    )
                }
            }
        }
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que quieres eliminar ${product.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onSave = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            },
            onDismiss = { productToEdit = null }
        )
    }
}

@Composable
fun SupermarketDetailContent(
    supermarket: Supermarket,
    products: List<Product>,
    sortOrder: ProductSortOrder,
    onSortOrderChange: (ProductSortOrder) -> Unit,
    onTogglePurchased: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onAddProduct: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditSupermarket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purchasedCount = products.count { it.isPurchased }
    val pendingCount = products.size - purchasedCount
    val total = products.sumOf { (it.price ?: 0.0) * it.quantity }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AccessibilityDefaults.SectionSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }

                    Text(
                        text = supermarket.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onEditSupermarket,
                        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar tienda",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    SupermarketLogo(
                        supermarketName = supermarket.name,
                        customLogoPath = supermarket.logoPath,
                        size = 80.dp
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$pendingCount pendiente${if (pendingCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (purchasedCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$purchasedCount comprado${if (purchasedCount != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (total > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${String.format("%.2f", total)}€",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = AccessibilityDefaults.ItemSpacing / 2,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "No hay productos",
                    description = "Añade productos a este supermercado",
                    action = {
                        Button(onClick = onAddProduct) {
                            Text("Añadir producto")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AccessibilityDefaults.ItemSpacing,
                        vertical = AccessibilityDefaults.ItemSpacing
                    ),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Productos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${products.size} en total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                        ) {
                            ProductSortOrder.entries.forEach { order ->
                                val isSelected = sortOrder == order
                                Surface(
                                    onClick = { onSortOrderChange(order) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.heightIn(min = 32.dp)
                                ) {
                                    Text(
                                        text = when (order) {
                                            ProductSortOrder.NameAsc -> "A-Z"
                                            ProductSortOrder.NameDesc -> "Z-A"
                                            ProductSortOrder.PriceAsc -> "€ ↑"
                                            ProductSortOrder.PriceDesc -> "€ ↓"
                                            ProductSortOrder.Status -> "Estado"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onTogglePurchased = { onTogglePurchased(product) },
                            onDelete = { onDeleteProduct(product) },
                            onEdit = { onEditProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxWidth = 300f
    val threshold = 150f
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        label = "swipeAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AccessibilityDefaults.ItemSpacing / 4)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .padding(horizontal = AccessibilityDefaults.CardPadding)
                    .size(AccessibilityDefaults.IconSize)
            )
        }

        Card(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > threshold) {
                                onDelete()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-maxWidth, 0f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (product.isPurchased) Modifier.graphicsLayer { alpha = 0.6f }
                        else Modifier
                    )
                    .padding(
                        start = AccessibilityDefaults.ItemSpacing / 2,
                        end = AccessibilityDefaults.ItemSpacing,
                        top = AccessibilityDefaults.ItemSpacing / 4,
                        bottom = AccessibilityDefaults.ItemSpacing / 4
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
            ) {
                IconButton(
                    onClick = onTogglePurchased,
                    modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = if (product.isPurchased) Icons.Default.CheckCircle
                        else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (product.isPurchased) "Marcar como pendiente" else "Marcar como comprado",
                        tint = if (product.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AccessibilityDefaults.IconSize)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                        color = if (product.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${product.quantity.toBigDecimal().stripTrailingZeros().toPlainString()} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (product.price != null) {
                            val total = product.price * product.quantity
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "${String.format("%.2f", total)}€",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var unit by remember { mutableStateOf(product.unit) }
    var price by remember { mutableStateOf(product.price?.toString() ?: "") }
    var showUnitMenu by remember { mutableStateOf(false) }

    val commonUnits = listOf("unidades", "kg", "g", "L", "ml", "docena", "pack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar producto",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = showUnitMenu,
                        onExpandedChange = { showUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            commonUnits.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        unit = option
                                        showUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€) (opcional)") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: product.quantity
                    val p = price.toDoubleOrNull()
                    onSave(
                        product.copy(
                            name = name.trim().ifBlank { product.name },
                            quantity = qty,
                            unit = unit.ifBlank { product.unit },
                            price = p
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
