package app.miscompras.com.ui.screens

import app.miscompras.com.ui.screens.history.MonthExpense
import app.miscompras.com.ui.screens.history.chartDescription
import app.miscompras.com.ui.screens.history.comparisonText
import app.miscompras.com.ui.screens.history.recentMonths
import app.miscompras.com.ui.screens.history.shortMonthName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class HistoryLogicTest {
    private val nov = YearMonth.of(2026, 11)
    private fun String.plain() = replace(' ', ' ')

    @Test fun rellenaLosMesesSinCompras() {
        val all = listOf(MonthExpense(nov, 67.45, 1), MonthExpense(YearMonth.of(2026, 9), 54.30, 1))
        val r = recentMonths(all, nov, 4)
        assertEquals(listOf(8, 9, 10, 11), r.map { it.yearMonth.monthValue })
        assertEquals(listOf(0.0, 54.30, 0.0, 67.45), r.map { it.total })
    }

    @Test fun cruzaElCambioDeAnio() {
        val r = recentMonths(emptyList(), YearMonth.of(2026, 2), 4)
        assertEquals(listOf(YearMonth.of(2025, 11), YearMonth.of(2025, 12), YearMonth.of(2026, 1), YearMonth.of(2026, 2)), r.map { it.yearMonth })
    }

    @Test fun comparaConElMesAnterior() {
        val oct = YearMonth.of(2026, 10)
        assertEquals("6,50 € menos que octubre", comparisonText(67.45, 73.95, oct)!!.plain())
        assertEquals("10,00 € más que octubre", comparisonText(83.95, 73.95, oct)!!.plain())
        assertEquals("Igual que octubre", comparisonText(73.95, 73.95, oct))
        assertNull(comparisonText(10.0, 0.0, oct))
    }

    @Test fun nombreCortoSinPunto() {
        assertEquals("nov", shortMonthName(nov))
    }

    @Test fun descripcionIncluyeTodosLosValores() {
        val d = chartDescription(recentMonths(listOf(MonthExpense(nov, 67.45, 1)), nov, 2)).plain()
        assertEquals("Gasto de los últimos meses. octubre: 0,00 €. noviembre: 67,45 €", d)
    }
}
