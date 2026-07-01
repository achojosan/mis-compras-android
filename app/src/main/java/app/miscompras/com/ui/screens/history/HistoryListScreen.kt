package app.miscompras.com.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        .padding(AccessibilityDefaults.ItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    item {
                        Text(
                            text = "Gastado este mes: ${currencyFormat.format(monthlyTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = AccessibilityDefaults.ItemSpacing)
                        )
                    }

                    items(state.historyItems, key = { it.history.id }) { historyItem ->
                        TimelineHistoryItem(
                            historyItem = historyItem,
                            onClick = { onHistoryItemClick(historyItem.history.id) }
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(24.dp)
                .padding(top = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
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
                Text(
                    text = historyItem.supermarket?.name ?: "Supermercado",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = getRelativeTimeString(historyItem.history.purchasedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AccessibilityDefaults.ItemSpacing / 2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                        Text(
                            text = currencyFormat.format(historyItem.history.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
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
