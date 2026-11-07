package app.miscompras.com

import app.miscompras.com.domain.model.Product
import app.miscompras.com.util.ShoppingListFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListFormatterTest {
    private fun p(name: String, q: Double, unit: String, bought: Boolean = false) =
        Product(name = name, quantity = q, unit = unit, supermarketId = 1, isPurchased = bought)

    @Test fun groupsByStoreAndSkipsPurchased() {
        val text = ShoppingListFormatter.format(
            listOf(
                "Mercadona" to listOf(p("Leche", 2.0, "ud"), p("Pan", 1.0, "ud", bought = true)),
                "Lidl" to listOf(p("Queso", 0.5, "kg"))
            )
        )
        assertEquals("Lista de la compra\n\nMercadona\n• Leche — 2 ud\n\nLidl\n• Queso — 0.5 kg", text)
    }

    @Test fun storesWithNothingPendingAreOmitted() {
        val text = ShoppingListFormatter.format(
            listOf("Aldi" to listOf(p("Agua", 6.0, "ud", bought = true)), "Dia" to listOf(p("Sal", 1.0, "ud")))
        )
        assertEquals("Lista de la compra\n\nDia\n• Sal — 1 ud", text)
    }

    @Test fun emptyWhenNothingPending() {
        assertEquals("", ShoppingListFormatter.format(listOf("Aldi" to listOf(p("Agua", 1.0, "ud", true)))))
        assertEquals("", ShoppingListFormatter.format(emptyList()))
    }
}
