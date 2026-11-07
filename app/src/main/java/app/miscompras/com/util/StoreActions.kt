package app.miscompras.com.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/** Enlaces para llegar a una tienda. Funciones puras (probadas en tests JVM). */
object StoreLinks {
    private fun enc(text: String) = URLEncoder.encode(text, "UTF-8").replace("+", "%20")

    /** `geo:` abre la app de mapas que tenga el usuario; con coordenadas marca el punto exacto. */
    fun geoUri(name: String, lat: Double?, lon: Double?): String =
        if (lat != null && lon != null) "geo:$lat,$lon?q=$lat,$lon(${enc(name)})"
        else "geo:0,0?q=${enc("$name supermercado")}"

    /** Reserva si no hay ninguna app de mapas instalada. */
    fun webUri(name: String, lat: Double?, lon: Double?): String =
        "https://www.google.com/maps/search/?api=1&query=" +
            if (lat != null && lon != null) "$lat,$lon" else enc("$name supermercado")
}

object StoreActions {
    fun openDirections(context: Context, name: String, lat: Double?, lon: Double?) {
        for (uri in listOf(StoreLinks.geoUri(name, lat, lon), StoreLinks.webUri(name, lat, lon))) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return
            } catch (_: ActivityNotFoundException) {
                // prueba con el siguiente
            }
        }
    }
}
