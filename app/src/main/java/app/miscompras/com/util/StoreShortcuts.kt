package app.miscompras.com.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.miscompras.com.MainActivity
import app.miscompras.com.R
import app.miscompras.com.domain.model.Supermarket

/** Acceso directo en la pantalla de inicio que abre directamente la ficha de una tienda. */
object StoreShortcuts {
    fun uriFor(supermarketId: Long): String = "miscompras://supermarket/$supermarketId"

    fun requestPin(context: Context, supermarket: Supermarket) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Tu pantalla de inicio no permite crear accesos directos", Toast.LENGTH_LONG).show()
            return
        }
        // ACTION_VIEW + URI: sin acción el sistema rechaza el acceso directo. CLEAR_TASK evita
        // que la app ya abierta ignore el destino.
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(uriFor(supermarket.id))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val info = ShortcutInfoCompat.Builder(context, "store_${supermarket.id}")
            .setShortLabel(supermarket.name)
            .setLongLabel("Lista de ${supermarket.name}")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }
}
