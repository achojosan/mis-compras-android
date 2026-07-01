package app.miscompras.com.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.theme.AccessibilityDefaults
import kotlinx.serialization.json.Json
import app.miscompras.com.ui.screens.shopping.PurchasedProductData
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryListScreen(
    onHistoryItemClick: (Long) -> Unit,
    viewModel: HistoryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is HistoryListUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HistoryListUiState.Empty -> {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No hay historial",
                    description = "Cuando finalices una compra, aparecerá aquí"
                )
            }
            is HistoryListUiState.Success -> {
                val monthlyTotal = calculateMonthlyTotal(state.historyItems)
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AccessibilityDefaults.ItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

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
                                        text = "Gasto del mes",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = currencyFormat.format(monthlyTotal),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))
                    }

                    items(state.historyItems, key = { it.history.id }) { historyItem ->
                        TimelineHistoryItem(
                            historyItem = historyItem,
                            onClick = { onHistoryItemClick(historyItem.history.id) },
                            onRepeatPurchase = { viewModel.repeatPurchase(historyItem) },
                            isFirst = historyItem == state.historyItems.first(),
                            isLast = historyItem == state.historyItems.last()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineHistoryItem(
    historyItem: HistoryItem,
    onClick: () -> Unit,
    onRepeatPurchase: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AccessibilityDefaults.CardPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    Text(
                        text = historyItem.supermarket?.name ?: "Supermercado",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    val productCount = try {
                        Json.decodeFromString<List<PurchasedProductData>>(historyItem.history.productsJson).size
                    } catch (e: Exception) {
                        0
                    }
                    Text(
                        text = "$productCount productos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getRelativeTimeString(historyItem.history.purchasedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                    Text(
                        text = currencyFormat.format(historyItem.history.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))

                TextButton(
                    onClick = onRepeatPurchase,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Repetir compra")
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(
    historyItem: HistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TimelineHistoryItem(
        historyItem = historyItem,
        onClick = onClick,
        onRepeatPurchase = {},
        modifier = modifier
    )
}

private fun getRelativeTimeString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minute = 60 * 1000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    return when {
        diff < minute -> "Ahora mismo"
        diff < hour -> "Hace ${diff / minute} min"
        diff < day -> "Hace ${diff / hour} h"
        diff < week -> {
            val days = diff / day
            if (days == 1L) "Hace 1 día" else "Hace $days días"
        }
        diff < month -> {
            val weeks = diff / week
            if (weeks == 1L) "La semana pasada" else "Hace $weeks semanas"
        }
        diff < year -> {
            val months = diff / month
            if (months == 1L) "El mes pasado" else "Hace $months meses"
        }
        else -> {
            val years = diff / year
            if (years == 1L) "Hace 1 año" else "Hace $years años"
        }
    }
}

private fun calculateMonthlyTotal(historyItems: List<HistoryItem>): Double {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    return historyItems
        .filter {
            val itemCalendar = Calendar.getInstance().apply { timeInMillis = it.history.purchasedAt }
            itemCalendar.get(Calendar.MONTH) == currentMonth &&
                itemCalendar.get(Calendar.YEAR) == currentYear
        }
        .sumOf { it.history.totalAmount }
}
