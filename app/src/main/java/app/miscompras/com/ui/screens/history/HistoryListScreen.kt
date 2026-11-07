package app.miscompras.com.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import app.miscompras.com.ui.theme.AppShapes
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@Composable
fun HistoryListScreen(
    onMonthClick: (String) -> Unit,
    viewModel: HistoryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HistoryListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HistoryListUiState.Empty -> {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No hay historial",
                    description = "Cuando finalices una compra, aparecerá aquí"
                )
            }
            is HistoryListUiState.Success -> {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                val currentMonth = YearMonth.now()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AccessibilityDefaults.ItemSpacing,
                        vertical = AccessibilityDefaults.ItemSpacing
                    ),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    item {
                        CurrentMonthCard(
                            currentMonthTotal = state.currentMonthTotal,
                            totalSpent = state.totalSpent,
                            totalPurchases = state.totalPurchases,
                            currencyFormat = currencyFormat
                        )
                    }

                    if (state.availableYears.isNotEmpty()) {
                        item {
                            YearSelector(
                                years = state.availableYears,
                                selectedYear = state.selectedYear,
                                onSelect = viewModel::selectYear
                            )
                        }
                    }

                    item {
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
                                text = "Por meses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${state.selectedYear} · " +
                                    currencyFormat.format(state.yearTotal),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(state.months, key = { it.yearMonth.toString() }) { month ->
                        MonthExpenseCard(
                            monthExpense = month,
                            isCurrentMonth = month.yearMonth == currentMonth,
                            currencyFormat = currencyFormat,
                            onClick = { onMonthClick(month.yearMonth.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentMonthCard(
    currentMonthTotal: Double,
    totalSpent: Double,
    totalPurchases: Int,
    currencyFormat: NumberFormat
) {
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
                    text = "Gasto del mes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Total acumulado: ${currencyFormat.format(totalSpent)} · " +
                        "$totalPurchases compra${if (totalPurchases != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = currencyFormat.format(currentMonthTotal),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MonthExpenseCard(
    monthExpense: MonthExpense,
    isCurrentMonth: Boolean,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = formatMonthName(monthExpense.yearMonth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrentMonth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${monthExpense.purchaseCount} compra${if (monthExpense.purchaseCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currencyFormat.format(monthExpense.total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun YearSelector(
    years: List<Int>,
    selectedYear: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        years.forEach { year ->
            YearChip(
                year = year,
                selected = year == selectedYear,
                onClick = { onSelect(year) }
            )
        }
    }
}

@Composable
private fun YearChip(
    year: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}
