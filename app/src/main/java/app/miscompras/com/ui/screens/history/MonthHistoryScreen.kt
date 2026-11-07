package app.miscompras.com.ui.screens.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.components.rememberTopBarFraction
import app.miscompras.com.ui.screens.shopping.PurchasedProductData
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.util.ToastEffect
import kotlinx.serialization.json.Json
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val purchaseTimeFormat = SimpleDateFormat("HH:mm", Locale("es", "ES"))
private val dayLabelFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))

@Composable
fun MonthHistoryScreen(
    onNavigateBack: () -> Unit,
    onHistoryItemClick: (Long) -> Unit,
    viewModel: MonthHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ToastEffect(events = viewModel.events)

    val title = when (val state = uiState) {
        is MonthHistoryUiState.Success -> formatMonthLabel(state.yearMonth)
        else -> "Mes"
    }

    val listState = rememberLazyListState()
    val barFraction = rememberTopBarFraction(listState)

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MonthHistoryUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is MonthHistoryUiState.Empty -> {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Sin compras",
                    description = "No hay compras registradas en este mes"
                )
            }
            is MonthHistoryUiState.Success -> {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                val groups = remember(state.items) { state.items.groupedByDay() }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
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
                    item(key = "hero") {
                        MonthSummaryHero(state = state, currencyFormat = currencyFormat)
                    }

                    groups.forEach { (day, purchases) ->
                        item(key = "day_header_$day") {
                            DayHeader(day = day, purchases = purchases, currencyFormat = currencyFormat)
                        }
                        item(key = "day_group_$day") {
                            DayPurchasesCard(
                                purchases = purchases,
                                currencyFormat = currencyFormat,
                                onItemClick = onHistoryItemClick,
                                onRepeat = viewModel::repeatPurchase
                            )
                        }
                    }
                }
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
private fun MonthSummaryHero(
    state: MonthHistoryUiState.Success,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gasto del mes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(state.total),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TrendBadge(
                    current = state.total,
                    previous = state.previousMonthTotal
                )
            }

            Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

            Text(
                text = "${state.items.size} compra${if (state.items.size != 1) "s" else ""} · " +
                    "ticket medio ${currencyFormat.format(state.averageTicket)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.favouriteSupermarketName?.let { favourite ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Súper habitual: $favourite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrendBadge(current: Double, previous: Double) {
    if (previous <= 0.0) return

    val percentage = ((current - previous) / previous) * 100
    val spendingMore = percentage > 0
    val container = if (spendingMore) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val content = if (spendingMore) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (spendingMore) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "${abs(percentage).roundToInt()} %",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DayHeader(
    day: LocalDate,
    purchases: List<HistoryItem>,
    currencyFormat: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 4.dp,
                end = 4.dp,
                top = AccessibilityDefaults.ItemSpacing / 2
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDayLabel(day).uppercase(Locale("es", "ES")),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = currencyFormat.format(purchases.sumOf { it.history.totalAmount }),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayPurchasesCard(
    purchases: List<HistoryItem>,
    currencyFormat: NumberFormat,
    onItemClick: (Long) -> Unit,
    onRepeat: (HistoryItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column {
            purchases.forEachIndexed { index, item ->
                PurchaseRow(
                    item = item,
                    currencyFormat = currencyFormat,
                    onClick = { onItemClick(item.history.id) },
                    onRepeat = { onRepeat(item) }
                )
                if (index < purchases.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = AccessibilityDefaults.CardPadding + 52.dp
                        ),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseRow(
    item: HistoryItem,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onRepeat: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxWidth = 220f
    val threshold = 110f
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        label = "repeatSwipe"
    )

    val productCount = remember(item.history.productsJson) {
        try {
            Json.decodeFromString<List<PurchasedProductData>>(item.history.productsJson).size
        } catch (e: Exception) {
            0
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier.padding(end = AccessibilityDefaults.CardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Repetir",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .clickable(onClick = onClick)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > threshold) {
                                onRepeat()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxWidth, 0f)
                        }
                    )
                }
                .padding(
                    horizontal = AccessibilityDefaults.CardPadding,
                    vertical = AccessibilityDefaults.ItemSpacing
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            SupermarketLogoPlate(
                supermarketName = item.supermarketName,
                logoPath = item.supermarket?.logoPath,
                height = 40.dp,
                cornerRadius = 12.dp,
                contentPadding = 6.dp,
                shadowElevation = 0.dp,
                showBorder = false
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.supermarketName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${purchaseTimeFormat.format(Date(item.history.purchasedAt))} · " +
                        "$productCount producto${if (productCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currencyFormat.format(item.history.totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun List<HistoryItem>.groupedByDay(): List<Pair<LocalDate, List<HistoryItem>>> {
    val zone = ZoneId.systemDefault()
    return groupBy { Instant.ofEpochMilli(it.history.purchasedAt).atZone(zone).toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (day, items) -> day to items.sortedByDescending { it.history.purchasedAt } }
}

private fun formatDayLabel(day: LocalDate): String {
    val today = LocalDate.now()
    return when (day) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> {
            val formatted = day.format(dayLabelFormatter)
            formatted.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale("es", "ES")) else char.toString()
            }
        }
    }
}
