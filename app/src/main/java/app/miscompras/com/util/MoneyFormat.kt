package app.miscompras.com.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private val spanish = Locale("es", "ES")

/** Importe en euros al estilo español: «10,68 €». */
fun formatEuros(amount: Double): String =
    NumberFormat.getCurrencyInstance(spanish).apply {
        currency = Currency.getInstance("EUR")
    }.format(amount)

/** Cantidad sin ceros sobrantes: 12.0 → «12», 1.5 → «1,5». */
fun formatQuantity(quantity: Double): String =
    NumberFormat.getNumberInstance(spanish).apply {
        maximumFractionDigits = 3
        isGroupingUsed = false
    }.format(quantity)

/** «12 ud», «1 unidad», «2,5 kg»: la unidad en singular cuando la cantidad es 1. */
fun quantityLabel(quantity: Double, unit: String): String {
    val u = if (quantity == 1.0 && unit.equals("unidades", ignoreCase = true)) "unidad" else unit
    return "${formatQuantity(quantity)} $u".trim()
}
