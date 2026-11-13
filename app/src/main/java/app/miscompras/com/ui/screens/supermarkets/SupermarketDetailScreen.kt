package app.miscompras.com.ui.screens.supermarkets

import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.ButtonStyle
import app.miscompras.com.ui.screens.history.lastPurchaseText
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.isLargeText
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import app.miscompras.com.nearby.OpeningHours
import app.miscompras.com.util.ListSharing
import app.miscompras.com.util.ShoppingListFormatter
import app.miscompras.com.util.StoreActions
import app.miscompras.com.util.StoreShortcuts
import androidx.compose.material.icons.filled.AddToHomeScreen
import java.time.LocalDateTime
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.AddProductSheet
import app.miscompras.com.ui.components.EditProductSheet
import app.miscompras.com.ui.components.EditSupermarketSheet
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.FloatingIconButton
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.util.ToastEffect
import app.miscompras.com.util.SupermarketApp
import app.miscompras.com.util.SupermarketApps
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SupermarketDetailScreen(
    onNavigateBack: () -> Unit,
    onLoyaltyCardClick: (Long) -> Unit = {},
    viewModel: SupermarketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val suggestions by viewModel.productNames.collectAsState()
    val hideOpenAppDialog by viewModel.hideOpenAppDialog.collectAsState()
    val showCounts by viewModel.showCounts.collectAsState()
    val locating by viewModel.locating.collectAsState()
    val lastPurchase by viewModel.lastPurchase.collectAsState()
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) viewModel.locateNearest()
    }
    val context = LocalContext.current
    var appToOpen by remember { mutableStateOf<SupermarketApp?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }
    var showEditSupermarket by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    ToastEffect(events = viewModel.events)

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
                    showCounts = showCounts,
                    locating = locating,
                    lastPurchase = lastPurchase,
                    onLoyaltyCard = { onLoyaltyCardClick(state.supermarket.id) },
                    onCreateShortcut = { StoreShortcuts.requestPin(context, state.supermarket) },
                    onShareList = {
                        val text = ShoppingListFormatter.format(listOf(state.supermarket.name to state.products))
                        if (text.isNotBlank()) ListSharing.share(context, text)
                    },
                    onCopyAndOpen = { app ->
                        ListSharing.copy(context, ShoppingListFormatter.format(listOf(state.supermarket.name to state.products)))
                        if (hideOpenAppDialog) SupermarketApps.open(context, app) else appToOpen = app
                    },
                    onDirections = {
                        StoreActions.openDirections(context, state.supermarket.name, state.supermarket.latitude, state.supermarket.longitude)
                    },
                    onLocate = {
                        locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                    },
                    onSortOrderChange = viewModel::setSortOrder,
                    onTogglePurchased = viewModel::toggleProductPurchased,
                    onDeleteProduct = { productToDelete = it },
                    onEditProduct = { productToEdit = it },
                    onAddProduct = { showAddProduct = true },
                    onNavigateBack = onNavigateBack,
                    onEditSupermarket = { showEditSupermarket = true },
                    onOpenApp = { app ->
                        if (hideOpenAppDialog) {
                            SupermarketApps.open(context, app)
                        } else {
                            appToOpen = app
                        }
                    }
                )

            }

            if (showAddProduct) {
                AddProductSheet(
                    supermarketName = state.supermarket.name,
                    suggestions = suggestions,
                    onDismiss = { showAddProduct = false },
                    onSave = { name, quantity, unit, price ->
                        viewModel.addProduct(name, quantity, unit, price)
                        showAddProduct = false
                    }
                )
            }

            if (showEditSupermarket) {
                EditSupermarketSheet(
                    supermarket = state.supermarket,
                    onDismiss = { showEditSupermarket = false },
                    onSave = { name, logoUri, passColor ->
                        viewModel.updateSupermarket(name, logoUri, passColor)
                        showEditSupermarket = false
                    }
                )
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
        EditProductSheet(
            product = product,
            onSave = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            },
            onDismiss = { productToEdit = null }
        )
    }

    appToOpen?.let { app ->
        OpenAppDialog(
            appName = app.appName,
            onConfirm = { dontShowAgain ->
                if (dontShowAgain) {
                    viewModel.setHideOpenAppDialog(true)
                }
                SupermarketApps.open(context, app)
                appToOpen = null
            },
            onDismiss = { appToOpen = null }
        )
    }
}

/**
 * Aviso previo a abrir la app oficial de la tienda. Permite cancelar y ocultar
 * el aviso en adelante.
 */
@Composable
private fun OpenAppDialog(
    appName: String,
    onConfirm: (dontShowAgain: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abrir $appName") },
        text = {
            Column {
                Text(
                    text = "Se va a abrir $appName para que puedas mostrar tu tarjeta " +
                        "de cliente y tus cupones."
                )
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Text(
                        text = "No volver a mostrar este aviso",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(dontShowAgain) }) {
                Text("Abrir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
    onOpenApp: (SupermarketApp) -> Unit,
    modifier: Modifier = Modifier,
    showCounts: Boolean = true,
    locating: Boolean = false,
    onDirections: () -> Unit = {},
    onLocate: () -> Unit = {},
    onLoyaltyCard: () -> Unit = {},
    onShareList: () -> Unit = {},
    onCopyAndOpen: (SupermarketApp) -> Unit = {},
    onCreateShortcut: () -> Unit = {},
    lastPurchase: PurchaseHistory? = null
) {
    val purchasedCount = products.count { it.isPurchased }
    val pendingCount = products.size - purchasedCount
    val total = products.sumOf { (it.price ?: 0.0) * it.quantity }

    val listState = rememberLazyListState()
    val barFraction = rememberTopBarFraction(listState)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AccessibilityDefaults.ItemSpacing,
                end = AccessibilityDefaults.ItemSpacing,
                top = AccessibilityDefaults.ItemSpacing,
                bottom = AccessibilityDefaults.SectionSpacing
            ),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            item(key = "hero") {
                StoreHero(
                    supermarket = supermarket,
                    pendingCount = pendingCount,
                    purchasedCount = purchasedCount,
                    total = total,
                    showCounts = showCounts,
                    onDirections = onDirections
                )
            }

            item(key = "actions") {
                val storeApp = SupermarketApps.forName(supermarket.name)
                val actions = buildList {
                    add(
                        DetailAction(
                            icon = Icons.Default.CreditCard,
                            label = if (supermarket.loyaltyCode != null) "Mi tarjeta" else "Añadir tarjeta",
                            onClick = onLoyaltyCard
                        )
                    )
                    if (pendingCount > 0) {
                        add(DetailAction(Icons.Default.Share, "Compartir", "Compartir la lista de ${supermarket.name}", onClick = onShareList))
                        if (storeApp != null) {
                            add(DetailAction(Icons.Default.ContentCopy, "Copiar y abrir", "Copiar la lista y abrir ${storeApp.appName}", onClick = { onCopyAndOpen(storeApp) }))
                        }
                    }
                    if (storeApp != null) {
                        add(DetailAction(Icons.Default.OpenInNew, "Abrir app", "Abrir ${storeApp.appName}", onClick = { onOpenApp(storeApp) }))
                    }
                    if (supermarket.latitude == null) {
                        add(
                            DetailAction(
                                Icons.Default.MyLocation,
                                if (locating) "Buscando…" else "Buscar sucursal",
                                enabled = !locating,
                                onClick = onLocate
                            )
                        )
                    }
                }
                ActionTiles(actions)
            }

            if (products.isEmpty()) {
                item(key = "empty") {
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
                }
            } else {
                item(key = "products") {
                    ProductsTicket(
                        products = products,
                        showCounts = showCounts,
                        sortOrder = sortOrder,
                        onSortOrderChange = onSortOrderChange,
                        onToggle = onTogglePurchased,
                        onEdit = onEditProduct,
                        onDelete = onDeleteProduct
                    )
                }

                // Al final de la lista, no flotando encima: así nunca tapa un producto.
                item(key = "add") {
                    AccessibleButton(
                        text = "Añadir producto",
                        onClick = onAddProduct,
                        style = ButtonStyle.Outlined,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (lastPurchase != null) {
                item(key = "last_purchase") {
                    Text(
                        text = lastPurchaseText(lastPurchase.purchasedAt, lastPurchase.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
            }
        }

        FloatingTopBar(
            title = supermarket.name,
            backgroundFraction = barFraction,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            FloatingIconButton(
                icon = Icons.Default.AddToHomeScreen,
                contentDescription = "Crear acceso directo en la pantalla de inicio",
                onClick = onCreateShortcut
            )
            Spacer(modifier = Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
            FloatingIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "Editar tienda",
                onClick = onEditSupermarket
            )
        }
    }
}
