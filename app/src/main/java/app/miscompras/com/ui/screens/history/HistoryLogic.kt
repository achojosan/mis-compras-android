package app.miscompras.com.ui.screens.history

import app.miscompras.com.util.formatEuros
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private val es = Locale("es", "ES")

/**
 * Los [count] meses que terminan en [anchor], en orden cronológico; los meses sin compras
 * salen con total 0 para que el gráfico tenga siempre la misma anchura.
 */
fun recentMonths(all: List<MonthExpense>, anchor: YearMonth, count: Int = 6): List<MonthExpense> {
    val byMonth = all.associateBy { it.yearMonth }
    return (count - 1 downTo 0).map { back ->
        val ym = anchor.minusMonths(back.toLong())
        byMonth[ym] ?: MonthExpense(ym, 0.0, 0)
    }
}

/** «6,50 € menos que octubre», «igual que octubre», o `null` si el mes anterior no tuvo gasto. */
fun comparisonText(current: Double, previous: Double, previousMonth: YearMonth): String? {
    if (previous <= 0.0) return null
    val name = previousMonth.month.getDisplayName(TextStyle.FULL, es)
    val diff = current - previous
    return when {
        abs(diff) < 0.005 -> "Igual que $name"
        diff < 0 -> "${formatEuros(-diff)} menos que $name"
        else -> "${formatEuros(diff)} más que $name"
    }
}

/** Nombre corto del mes para el eje del gráfico: «nov». */
fun shortMonthName(ym: YearMonth): String =
    ym.month.getDisplayName(TextStyle.SHORT, es).trimEnd('.')

/** Texto equivalente del gráfico para lectores de pantalla: todos los valores, mes a mes. */
fun chartDescription(months: List<MonthExpense>): String =
    "Gasto de los últimos meses. " + months.joinToString(". ") {
        "${it.yearMonth.month.getDisplayName(TextStyle.FULL, es)}: ${formatEuros(it.total)}"
    }

/** «Última compra: 3 nov · 67,45 €». */
fun lastPurchaseText(purchasedAtMillis: Long, amount: Double, zone: ZoneId = ZoneId.systemDefault()): String {
    val date = Instant.ofEpochMilli(purchasedAtMillis).atZone(zone).toLocalDate()
    return "Última compra: ${date.dayOfMonth} ${shortMonthName(YearMonth.from(date))} · ${formatEuros(amount)}"
}
