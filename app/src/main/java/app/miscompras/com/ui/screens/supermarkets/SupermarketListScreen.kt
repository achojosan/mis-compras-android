package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.AddSupermarketSheet
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SkeletonLoader
import app.miscompras.com.ui.components.SkeletonShape
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.AppShapes
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SupermarketListScreen(
    onSupermarketClick: (Long) -> Unit,
    viewModel: SupermarketListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCounts by viewModel.showCounts.collectAsState()
    // Con texto grande o muy grande se pasa a un diseño apilado que no deforma las tarjetas.
    val largeText = LocalAccessibilityConfig.current.fontScale >= LARGE_TEXT_SCALE
    var showAddSupermarket by remember { mutableStateOf(false) }
    var supermarketToDelete by remember { mutableStateOf<Supermarket?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState !is SupermarketListUiState.Empty) {
                if (largeText) {
                    // El botón extendido se desbordaría con texto grande: solo icono.
                    FloatingActionButton(
                        onClick = { showAddSupermarket = true },
                        shape = AppShapes.button,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir tienda",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { showAddSupermarket = true },
                        shape = AppShapes.button,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(AccessibilityDefaults.SmallIconSize)
                            )
                        },
                        text = {
                            Text(text = "Añadir tienda", style = MaterialTheme.typography.labelLarge)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SupermarketListUiState.Loading -> {
                    LoadingContent()
                }

                is SupermarketListUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.Store,
                        title = "No hay supermercados",
                        description = "Añade tu primer supermercado para empezar",
                        action = {
                            Button(onClick = { showAddSupermarket = true }) {
                                Text("Añadir supermercado")
                            }
                        }
                    )
                }

                is SupermarketListUiState.Success -> {
                    val productTotal = state.items.sumOf { it.productCount }
                    val pendingTotal = state.items.sumOf { it.pendingCount }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = AccessibilityDefaults.ItemSpacing,
                            end = AccessibilityDefaults.ItemSpacing,
                            top = 0.dp,
                            bottom = AccessibilityDefaults.SectionSpacing + 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        item(key = "header") {
                            ListHeader(
                                supermarketCount = state.items.size,
                                productCount = productTotal,
                                pendingCount = pendingTotal,
                                showCounts = showCounts,
                                largeText = largeText
                            )
                        }

                        itemsIndexed(state.items, key = { _, item -> item.supermarket.id }) { _, item ->
                            SupermarketItem(
                                item = item,
                                onClick = { onSupermarketClick(item.supermarket.id) },
                                onDelete = { supermarketToDelete = item.supermarket },
                                showCounts = showCounts,
                                largeText = largeText,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSupermarket) {
        AddSupermarketSheet(
            onDismiss = { showAddSupermarket = false },
            onSave = { name, logoUri ->
                viewModel.addSupermarket(name, logoUri)
                showAddSupermarket = false
            }
        )
    }

    supermarketToDelete?.let { supermarket ->
        AlertDialog(
            onDismissRequest = { supermarketToDelete = null },
            title = { Text("Eliminar supermercado") },
            text = { Text("¿Estás seguro de que quieres eliminar ${supermarket.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSupermarket(supermarket)
                        supermarketToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { supermarketToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ListHeader(
    supermarketCount: Int,
    productCount: Int,
    pendingCount: Int,
    showCounts: Boolean,
    largeText: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val gradient = Brush.linearGradient(
        colors = listOf(
            lerp(primary, Color.Black, 0.18f),
            primary,
            lerp(primary, MaterialTheme.colorScheme.tertiary, 0.45f)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AccessibilityDefaults.SectionSpacing)
            .shadow(14.dp, RoundedCornerShape(28.dp), ambientColor = primary, spotColor = primary)
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            // Círculos translúcidos decorativos que dan profundidad al degradado.
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.9f, center = Offset(size.width * 0.95f, -size.height * 0.1f))
                drawCircle(Color.White.copy(alpha = 0.07f), radius = size.height * 0.55f, center = Offset(size.width * 0.72f, size.height * 1.05f))
            }
            .padding(
                horizontal = AccessibilityDefaults.CardPadding * 1.4f,
                vertical = AccessibilityDefaults.CardPadding * 1.5f
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MIS COMPRAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = onPrimary.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tus tiendas",
                // displaySmall × texto muy grande no cabe en una línea: se baja un escalón.
                style = if (largeText) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = onPrimary
            )
            if (showCounts) {
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing * 1.5f))
                val stats = listOf(
                    Triple(supermarketCount, "tienda${plural(supermarketCount)}", false),
                    Triple(productCount, "producto${plural(productCount)}", false),
                    Triple(pendingCount, "pendiente${plural(pendingCount)}", pendingCount > 0)
                )
                if (largeText) {
                    // Texto grande: una estadística por fila, número a la izquierda y etiqueta a la derecha.
                    Column(verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)) {
                        stats.forEach { (value, label, highlight) ->
                            HeroStat(value, label, Modifier.fillMaxWidth(), highlight, horizontal = true)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        stats.forEach { (value, label, highlight) ->
                            HeroStat(value, label, Modifier.weight(1f), highlight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    horizontal: Boolean = false
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (highlight) onPrimary.copy(alpha = 0.26f) else onPrimary.copy(alpha = 0.14f),
        contentColor = onPrimary
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = onPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = onPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = AccessibilityDefaults.ItemSpacing,
                vertical = AccessibilityDefaults.SectionSpacing
            ),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        Column(modifier = Modifier.padding(start = 4.dp, bottom = AccessibilityDefaults.ItemSpacing)) {
            SkeletonLoader(width = 200.dp, height = 30.dp)
            Spacer(modifier = Modifier.height(10.dp))
            SkeletonLoader(width = 260.dp, height = 16.dp)
        }
        repeat(4) {
            SupermarketItemSkeleton()
        }
    }
}

@Composable
fun SupermarketItem(
    item: SupermarketListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showCounts: Boolean = true,
    largeText: Boolean = false
) {
    val supermarket = item.supermarket
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
            onClick = onClick,
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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(AccessibilityDefaults.CardPadding)) {
                // Fila principal: logo + nombre (+ resumen/insignia solo en texto normal) + flecha.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    SupermarketLogoPlate(
                        supermarketName = supermarket.name,
                        logoPath = supermarket.logoPath,
                        height = 52.dp,
                        cornerRadius = 16.dp,
                        contentPadding = 8.dp,
                        shadowElevation = 2.dp,
                        showBorder = false
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = supermarket.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            // El nombre puede ocupar varias líneas en vez de cortarse o desbordar.
                            maxLines = if (largeText) 3 else 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (showCounts && !largeText) {
                            Spacer(modifier = Modifier.height(3.dp))
                            ItemSummary(item = item, stacked = false)
                        }
                    }

                    if (showCounts && !largeText && item.pendingCount > 0) {
                        PendingBadge(item.pendingCount)
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Texto grande: el resumen pasa debajo, a todo el ancho, para que nada se deforme.
                if (showCounts && largeText) {
                    Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ItemSummary(item = item, stacked = true)
                        }
                        if (item.pendingCount > 0) PendingBadge(item.pendingCount)
                    }
                }

                if (showCounts && item.productCount > 0) {
                    Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
                    LinearProgressIndicator(
                        progress = { (item.productCount - item.pendingCount).toFloat() / item.productCount },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1
        )
    }
}

/** Resumen "N productos · M pendientes". Con [stacked] cada dato va en su propia línea. */
@Composable
private fun ItemSummary(item: SupermarketListItem, stacked: Boolean) {
    if (item.productCount == 0) {
        Text(
            text = "Sin productos todavía",
            style = if (stacked) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val small = if (stacked) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
    val products = "${item.productCount} producto${plural(item.productCount)}"
    val pending = if (item.pendingCount > 0) "${item.pendingCount} pendiente${plural(item.pendingCount)}" else "todo comprado"
    val pendingColor = if (item.pendingCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
    val pendingWeight = if (item.pendingCount > 0) FontWeight.Medium else null

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(products, style = small, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(pending, style = small, fontWeight = pendingWeight, color = pendingColor)
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(products, style = small, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text("·", style = small, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text(pending, style = small, fontWeight = pendingWeight, color = pendingColor, maxLines = 1)
        }
    }
}

@Composable
private fun SupermarketItemSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AccessibilityDefaults.ItemSpacing / 4),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            SkeletonLoader(width = 44.dp, height = 44.dp, skeletonShape = SkeletonShape.Circle)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonLoader(width = 150.dp, height = 18.dp)
                SkeletonLoader(width = 110.dp, height = 13.dp)
            }
        }
    }
}

private fun headerSummary(supermarkets: Int, products: Int, pending: Int): String {
    val parts = mutableListOf("$supermarkets tienda${plural(supermarkets)}")
    if (products > 0) {
        parts += "$products producto${plural(products)}"
    }
    if (pending > 0) {
        parts += "$pending pendiente${plural(pending)}"
    }
    return parts.joinToString(" · ")
}

/** Desde esta escala de texto se usa el diseño apilado (Grande = 1,2; Muy grande = 1,4). */
private const val LARGE_TEXT_SCALE = 1.2f

private fun plural(count: Int): String = if (count == 1) "" else "s"
