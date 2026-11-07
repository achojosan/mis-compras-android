package app.miscompras.com.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.miscompras.com.domain.model.Product

/** Texto de la lista de la compra para compartir o pegar. Función pura (probada en tests JVM). */
object ShoppingListFormatter {
    /** Solo los productos pendientes, agrupados por tienda. Vacío si no hay nada que comprar. */
    fun format(groups: List<Pair<String, List<Product>>>): String {
        val blocks = groups.mapNotNull { (store, products) ->
            val pending = products.filter { !it.isPurchased }
            if (pending.isEmpty()) null
            else buildString {
                append(store)
                pending.forEach { append("\n• ").append(line(it)) }
            }
        }
        return if (blocks.isEmpty()) "" else "Lista de la compra\n\n" + blocks.joinToString("\n\n")
    }

    private fun line(p: Product): String {
        val qty = p.quantity.toBigDecimal().stripTrailingZeros().toPlainString()
        return "${p.name} — $qty ${p.unit}".trim()
    }
}

object ListSharing {
    fun share(context: Context, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Compartir lista").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun copy(context: Context, text: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("Lista de la compra", text))
        Toast.makeText(context, "Lista copiada", Toast.LENGTH_SHORT).show()
    }
}
