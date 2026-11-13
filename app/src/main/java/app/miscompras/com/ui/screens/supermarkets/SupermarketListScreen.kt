package app.miscompras.com.ui.screens.supermarkets

import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.ButtonStyle
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import app.miscompras.com.ui.components.BigFigure
import app.miscompras.com.ui.components.StorePass
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
import app.miscompras.com.ui.theme.isLargeText
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
    val largeText = LocalAccessibilityConfig.current.isLargeText
    var showAddSupermarket by remember { mutableStateOf(false) }
    var supermarketToDelete by remember { mutableStateOf<Supermarket?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    // Primero lo que falta por comprar; las tiendas al día se apartan al final.
                    val ordered = remember(state.items) {
                        state.items.sortedBy { if (it.productCount > 0 && it.pendingCount == 0) 1 else 0 }
                    }
                    // Los pases se solapan unos dp solo con texto normal; con texto grande se apilan sin tocarse.
                    val overlap = if (largeText) 0.dp else 14.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = AccessibilityDefaults.ItemSpacing,
                            end = AccessibilityDefaults.ItemSpacing,
                            top = AccessibilityDefaults.ItemSpacing,
                            bottom = AccessibilityDefaults.SectionSpacing
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            if (largeText) AccessibilityDefaults.ItemSpacing else -overlap
                        )
                    ) {
                        item(key = "header") {
                            WalletHeader(
                                storeCount = state.items.size,
                                productCount = productTotal,
                                pendingCount = pendingTotal,
                                showCounts = showCounts,
                                modifier = Modifier.padding(
                                    start = 8.dp, end = 8.dp,
                                    bottom = AccessibilityDefaults.SectionSpacing
                                )
                            )
                        }

                        itemsIndexed(ordered, key = { _, item -> item.supermarket.id }) { index, item ->
                            SwipeToDeletePass(
                                onDelete = { supermarketToDelete = item.supermarket },
                                modifier = Modifier.animateItem()
                            ) {
                                StorePass(
                                    supermarket = item.supermarket,
                                    productCount = item.productCount,
                                    pendingCount = item.pendingCount,
                                    showCounts = showCounts,
                                    stacked = largeText,
                                    onClick = { onSupermarketClick(item.supermarket.id) },
                                    overlapPadding = if (index < ordered.lastIndex) overlap else 0.dp
                                )
                            }
                        }

                        // Un botón al final de la lista, no un botón flotante: así nunca tapa una tienda.
                        item(key = "add") {
                            AccessibleButton(
                                text = "Añadir tienda",
                                onClick = { showAddSupermarket = true },
                                style = ButtonStyle.Outlined,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = AccessibilityDefaults.SectionSpacing + overlap)
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
            onSave = { name, logoUri, passColor ->
                viewModel.addSupermarket(name, logoUri, passColor)
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

/** Cabecera sin tarjeta: la cifra de lo que falta por comprar, en grande. */
@Composable
private fun WalletHeader(
    storeCount: Int,
    productCount: Int,
    pendingCount: Int,
    showCounts: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showCounts) {
        Text(
            text = "Tus tiendas",
            style = MaterialTheme.typography.displaySmall,
            modifier = modifier
        )
        return
    }
    val stores = "$storeCount tienda${plural(storeCount)}"
    when {
        productCount == 0 -> BigFigure(
            figure = "0",
            caption = "cosas por comprar en $stores",
            modifier = modifier
        )
        pendingCount == 0 -> BigFigure(
            figure = "¡Hecho!",
            caption = "Todo comprado en $stores",
            figureStyle = MaterialTheme.typography.displayMedium,
            modifier = modifier
        )
        else -> BigFigure(
            figure = pendingCount.toString(),
            caption = "cosa${plural(pendingCount)} por comprar en $stores",
            modifier = modifier
        )
    }
}

/** Arrastrar un pase a la izquierda lo elimina (con confirmación); también hay una acción de accesibilidad. */
@Composable
private fun SwipeToDeletePass(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipeAnimation")

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(AppShapes.pass)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .padding(horizontal = AccessibilityDefaults.CardPadding)
                    .size(AccessibilityDefaults.IconSize)
            )
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction("Eliminar tienda") { onDelete(); true }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > 150f) onDelete()
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                        }
                    )
                }
        ) { content() }
    }
}

private fun plural(count: Int): String = if (count == 1) "" else "s"
