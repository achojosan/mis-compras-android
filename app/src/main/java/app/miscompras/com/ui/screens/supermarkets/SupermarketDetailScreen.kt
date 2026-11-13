package app.miscompras.com.ui.screens.supermarkets

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

                if (state.products.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { showAddProduct = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = AppShapes.button,
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
    onCreateShortcut: () -> Unit = {}
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
                bottom = AccessibilityDefaults.SectionSpacing + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            item(key = "hero") {
                DetailHero(
                    supermarket = supermarket,
                    pendingCount = pendingCount,
                    purchasedCount = purchasedCount,
                    total = total,
                    showCounts = showCounts,
                    onOpenApp = onOpenApp
                )
            }

            item(key = "store_info") {
                StoreInfoCard(
                    supermarket = supermarket,
                    locating = locating,
                    onDirections = onDirections,
                    onLocate = onLocate,
                    onLoyaltyCard = onLoyaltyCard,
                    hasPending = pendingCount > 0,
                    onShareList = onShareList,
                    onCopyAndOpen = onCopyAndOpen
                )
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
                item(key = "products_header") {
                    ProductsHeader(
                        productCount = products.size,
                        showCounts = showCounts,
                        sortOrder = sortOrder,
                        onSortOrderChange = onSortOrderChange
                    )
                }

                items(products, key = { it.id }) { product ->
                    ProductItem(
                        product = product,
                        onTogglePurchased = { onTogglePurchased(product) },
                        onDelete = { onDeleteProduct(product) },
                        onEdit = { onEditProduct(product) },
                        modifier = Modifier.animateItem()
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

@Composable
private fun DetailHero(
    supermarket: Supermarket,
    pendingCount: Int,
    purchasedCount: Int,
    total: Double,
    showCounts: Boolean,
    onOpenApp: (SupermarketApp) -> Unit
) {
    val app = SupermarketApps.forName(supermarket.name)
    val largeText = LocalAccessibilityConfig.current.isLargeText
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(28.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(
            lerp(primary, Color.Black, 0.18f),
            primary,
            lerp(primary, MaterialTheme.colorScheme.tertiary, 0.45f)
        )
    )
    val itemCount = pendingCount + purchasedCount

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = primary, spotColor = primary)
            .clip(shape)
            .background(gradient)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.width * 0.55f, center = Offset(size.width * 0.95f, size.height * 0.05f))
                drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.35f, center = Offset(size.width * 0.05f, size.height * 0.95f))
            }
    ) {
        val contentPadding = AccessibilityDefaults.scaledDp(20f)
        val availableWidth = maxWidth
        val plateHeight = when {
            availableWidth < 340.dp -> 84.dp
            availableWidth < 420.dp -> 100.dp
            else -> 116.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = contentPadding,
                    end = contentPadding,
                    // Hueco para la barra superior flotante.
                    top = AccessibilityDefaults.MinTouchTarget + AccessibilityDefaults.ItemSpacing,
                    bottom = contentPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SupermarketLogoPlate(
                supermarketName = supermarket.name,
                logoPath = supermarket.logoPath,
                height = plateHeight,
                maxWidth = availableWidth - contentPadding * 2,
                adaptWidth = true,
                cornerRadius = 28.dp,
                contentPadding = AccessibilityDefaults.ItemSpacing,
                shadowElevation = 8.dp
            )

            Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

            Text(
                text = supermarket.name,
                style = if (largeText) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = onPrimary,
                textAlign = TextAlign.Center
            )

            if (showCounts && itemCount > 0) {
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
                Text(
                    text = "$purchasedCount de $itemCount comprados",
                    style = MaterialTheme.typography.labelLarge,
                    color = onPrimary.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { purchasedCount.toFloat() / itemCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = onPrimary,
                    trackColor = onPrimary.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }

            Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing * 1.5f))

            val totalText = formatCurrency(total)
            if (!showCounts) {
                // Sin recuentos: solo el importe total (no es un contador).
                if (total > 0.0) StatRow("Total", totalText)
            } else if (largeText) {
                // Texto grande: una fila por dato (etiqueta a la izquierda, valor a la derecha).
                Column(verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)) {
                    StatRow("Pendientes", "$pendingCount", highlight = pendingCount > 0)
                    StatRow("Comprados", "$purchasedCount")
                    StatRow("Total", totalText)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    StatTile("$pendingCount", "PENDIENTES", Modifier.weight(1f), highlight = pendingCount > 0)
                    StatTile("$purchasedCount", "COMPRADOS", Modifier.weight(1f))
                    StatTile(totalText, "TOTAL", Modifier.weight(1f))
                }
            }

            if (app != null) {
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing + 4.dp))
                OpenSupermarketAppButton(app = app, onClick = { onOpenApp(app) })
            }
        }
    }
}

/** Horario, estado (abierto/cerrado) y "cómo llegar" de la tienda. */
@Composable
private fun StoreInfoCard(
    supermarket: Supermarket,
    locating: Boolean,
    onDirections: () -> Unit,
    onLocate: () -> Unit,
    onLoyaltyCard: () -> Unit,
    hasPending: Boolean,
    onShareList: () -> Unit,
    onCopyAndOpen: (SupermarketApp) -> Unit
) {
    val raw = supermarket.openingHours
    val storeApp = SupermarketApps.forName(supermarket.name)
    val schedule = remember(raw) { OpeningHours.parse(raw) }
    val now = remember { LocalDateTime.now() }
    val open = schedule?.isOpen(now)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(AccessibilityDefaults.CardPadding),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            if (schedule != null && open != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    )
                    Column {
                        Text(
                            text = if (open) "Abierto ahora" else "Cerrado ahora",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hoy: " + (schedule.hoursText(now.dayOfWeek) ?: "cerrado"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!raw.isNullOrBlank()) {
                // Formato que no sé interpretar con seguridad: se muestra tal cual.
                Text(text = "Horario: $raw", style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = onDirections,
                shape = AppShapes.button,
                modifier = Modifier.fillMaxWidth().heightIn(min = AccessibilityDefaults.MinTouchTarget)
            ) {
                Icon(Icons.Default.Directions, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                Spacer(Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
                Text("Cómo llegar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }

            // Acciones secundarias: fluyen en filas para no empujar los productos fuera de pantalla.
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onLoyaltyCard,
                    shape = AppShapes.button,
                    modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                ) {
                    Icon(Icons.Default.CreditCard, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                    Spacer(Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
                    Text(
                        if (supermarket.loyaltyCode != null) "Mi tarjeta" else "Añadir tarjeta",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                if (hasPending) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onShareList,
                        shape = AppShapes.button,
                        modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                        Spacer(Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
                        Text("Compartir", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                    if (storeApp != null) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onCopyAndOpen(storeApp) },
                            shape = AppShapes.button,
                            modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                            Spacer(Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
                            Text("Copiar y abrir app", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                        }
                    }
                }

                if (supermarket.latitude == null) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onLocate,
                        enabled = !locating,
                        shape = AppShapes.button,
                        modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(Icons.Default.MyLocation, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                        Spacer(Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
                        Text(
                            if (locating) "Buscando…" else "Buscar sucursal",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = onPrimary.copy(alpha = if (highlight) 0.26f else 0.14f),
        contentColor = onPrimary
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = onPrimary.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, highlight: Boolean = false) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = onPrimary.copy(alpha = if (highlight) 0.26f else 0.14f),
        contentColor = onPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = onPrimary.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductsHeader(
    productCount: Int,
    showCounts: Boolean,
    sortOrder: ProductSortOrder,
    onSortOrderChange: (ProductSortOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AccessibilityDefaults.ItemSpacing)
    ) {
        // FlowRow: con texto grande el contador baja de línea en vez de comprimirse.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Productos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (showCounts) {
                Text(
                    text = "$productCount en total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

        // Los filtros se reparten en varias líneas: así ninguno queda oculto fuera de pantalla.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
        ) {
            ProductSortOrder.entries.forEach { order ->
                SortChip(
                    label = order.label(),
                    selected = sortOrder == order,
                    onClick = { onSortOrderChange(order) }
                )
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.heightIn(min = 44.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private fun ProductSortOrder.label(): String = when (this) {
    ProductSortOrder.NameAsc -> "A-Z"
    ProductSortOrder.NameDesc -> "Z-A"
    ProductSortOrder.PriceAsc -> "€ ↑"
    ProductSortOrder.PriceDesc -> "€ ↓"
    ProductSortOrder.Status -> "Estado"
}

@OptIn(ExperimentalLayoutApi::class)
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
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AccessibilityDefaults.ItemSpacing / 4)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(AppShapes.card)
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
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(
                // Un comprado se tiñe levemente del color del tema para distinguirlo de un vistazo.
                containerColor = if (product.isPurchased) lerp(surface, primary, 0.07f) else surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (product.isPurchased) 1.dp else 5.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (product.isPurchased) Modifier.graphicsLayer { alpha = 0.6f }
                        else Modifier
                    )
                    .padding(
                        start = AccessibilityDefaults.ItemSpacing / 4,
                        end = AccessibilityDefaults.CardPadding,
                        top = AccessibilityDefaults.ItemSpacing / 2,
                        bottom = AccessibilityDefaults.ItemSpacing / 2
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
            ) {
                IconButton(
                    onClick = onTogglePurchased,
                    modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = if (product.isPurchased) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = if (product.isPurchased) {
                            "Marcar como pendiente"
                        } else {
                            "Marcar como comprado"
                        },
                        tint = if (product.isPurchased) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AccessibilityDefaults.IconSize + 4.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        // El nombre se parte en varias líneas; nunca se recorta con texto grande.
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // FlowRow: cantidad y precio van juntos si caben y en líneas separadas si no.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${product.quantity.toBigDecimal().stripTrailingZeros().toPlainString()} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (product.price != null) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = formatCurrency(product.price * product.quantity),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenSupermarketAppButton(app: SupermarketApp, onClick: () -> Unit) {
    // Botón claro sobre el degradado: máximo contraste con el fondo de color.
    Button(
        onClick = onClick,
        shape = AppShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AccessibilityDefaults.MinTouchTarget)
    ) {
        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(AccessibilityDefaults.SmallIconSize)
        )
        Spacer(modifier = Modifier.width(AccessibilityDefaults.ItemSpacing / 2))
        Text(
            text = "Abrir ${app.appName}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCurrency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
