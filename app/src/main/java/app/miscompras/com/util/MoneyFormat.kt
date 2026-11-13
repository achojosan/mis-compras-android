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
