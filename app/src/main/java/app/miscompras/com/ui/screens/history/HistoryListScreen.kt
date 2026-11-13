package app.miscompras.com.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.miscompras.com.ui.components.BigFigure
import app.miscompras.com.ui.components.DashedDivider
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.ui.theme.isLargeText
import app.miscompras.com.util.formatEuros
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
                val largeText = LocalAccessibilityConfig.current.isLargeText

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AccessibilityDefaults.ItemSpacing,
                        vertical = AccessibilityDefaults.ItemSpacing
                    ),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    item {
                        MonthSummary(
                            currentMonthTotal = state.currentMonthTotal,
                            previousMonthTotal = state.previousMonthTotal,
                            recent = state.recentMonths,
                            totalSpent = state.totalSpent,
                            totalPurchases = state.totalPurchases,
                            largeText = largeText
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

/**
 * Lo primero que se ve: lo gastado este mes en cifra grande, la comparación con el mes anterior
 * y los últimos seis meses en un gráfico. Sin tarjeta: la cifra y el gráfico se apoyan en el fondo.
 */
@Composable
private fun MonthSummary(
    currentMonthTotal: Double,
    previousMonthTotal: Double,
    recent: List<MonthExpense>,
    totalSpent: Double,
    totalPurchases: Int,
    largeText: Boolean
) {
    val now = YearMonth.now()
    val comparison = comparisonText(currentMonthTotal, previousMonthTotal, now.minusMonths(1))
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        BigFigure(
            figure = formatEuros(currentMonthTotal),
            caption = "gastados en ${formatMonthName(now).lowercase()}",
            figureStyle = if (largeText) MaterialTheme.typography.displayMedium
            else MaterialTheme.typography.displayLarge
        )
        if (comparison != null) {
            Text(
                text = comparison,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        MonthBarChart(months = recent, current = now, largeText = largeText)
        Text(
            text = "Total acumulado: ${formatEuros(totalSpent)} · $totalPurchases compra${if (totalPurchases != 1) "s" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Gráfico de los últimos meses. Normal: barras verticales con el mes debajo y el importe del
 * mes actual encima. Texto grande: barras horizontales con «mes · importe» escrito encima de
 * cada una, para que ningún texto tenga que encogerse. El conjunto se lee como una sola frase.
 */
@Composable
private fun MonthBarChart(months: List<MonthExpense>, current: YearMonth, largeText: Boolean) {
    val max = (months.maxOfOrNull { it.total } ?: 0.0).coerceAtLeast(1.0)
    val past = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val now = MaterialTheme.colorScheme.primary
    val description = chartDescription(months)

    if (largeText) {
        Column(
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Con texto grande se omiten los meses sin gasto: el texto completo sigue en la descripción.
            months.reversed().filter { it.total > 0 || it.yearMonth == current }.forEach { m ->
                val isNow = m.yearMonth == current
                Column {
                    Text(
                        text = "${shortMonthName(m.yearMonth)} · ${formatEuros(m.total)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
                    )
                    if (m.total > 0) Box(
                        modifier = Modifier
                            .fillMaxWidth((m.total / max).toFloat().coerceAtLeast(0.02f))
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isNow) now else past)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clearAndSetSemantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            months.forEach { m ->
                val isNow = m.yearMonth == current
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (isNow && m.total > 0) {
                        Text(
                            text = formatEuros(m.total).replace(",00", ""),
                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = TabularNumbers),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(((m.total / max).toFloat() * 0.78f).coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (isNow) now else past)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = shortMonthName(m.yearMonth),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Fila de un mes: sin tarjeta, con separador; todo el ancho es el objetivo táctil. */
@Composable
private fun MonthExpenseCard(
    monthExpense: MonthExpense,
    isCurrentMonth: Boolean,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    val largeText = LocalAccessibilityConfig.current.isLargeText
    val count = "${monthExpense.purchaseCount} compra${if (monthExpense.purchaseCount != 1) "s" else ""}"
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .clip(AppShapes.small)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatMonthName(monthExpense.yearMonth),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (largeText) {
                    Text(
                        text = currencyFormat.format(monthExpense.total),
                        style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = TabularNumbers)
                    )
                }
            }
            if (!largeText) {
                Text(
                    text = currencyFormat.format(monthExpense.total),
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = TabularNumbers)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        DashedDivider(Modifier.padding(horizontal = 8.dp))
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
