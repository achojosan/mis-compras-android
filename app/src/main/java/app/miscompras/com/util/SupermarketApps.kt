package app.miscompras.com.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.Normalizer
import java.util.Locale

/**
 * Enlace a la app oficial de una cadena de supermercados.
 *
 * @param packageName identificador Android de la app (Play Store).
 * @param appName nombre visible de la app, usado en el botón ("Abrir {appName}").
 */
data class SupermarketApp(
    val packageName: String,
    val appName: String
)

/**
 * Catálogo de apps oficiales de supermercados españoles.
 *
 * La app no se integra con estas aplicaciones (no pide credenciales ni usa APIs
 * privadas): solo las abre si están instaladas y, si no lo están, ofrece su ficha
 * en Play Store. Así el usuario puede mostrar su tarjeta de cliente o aplicar
 * cupones sin buscarlas manualmente.
 */
object SupermarketApps {

    private val byName: Map<String, SupermarketApp> = buildMap {
        fun register(app: SupermarketApp, vararg names: String) {
            names.forEach { put(normalize(it), app) }
        }

        register(
            SupermarketApp("es.mercadona.tienda", "Mercadona"),
            "Mercadona"
        )
        register(
            SupermarketApp("com.munrodev.crfmobile", "Mi Carrefour"),
            "Carrefour", "Mi Carrefour"
        )
        register(
            SupermarketApp("com.lidl.eci.lidlplus", "Lidl Plus"),
            "Lidl", "Lidl Plus"
        )
        register(
            SupermarketApp("es.aldi.android", "ALDI"),
            "Aldi", "ALDI"
        )
        register(
            SupermarketApp("es.dia", "Dia"),
            "Dia", "Día", "Dia Supermercado"
        )
        register(
            SupermarketApp("com.elcorteingles.app", "El Corte Inglés"),
            "Hipercor", "El Corte Inglés", "Supercor"
        )
        register(
            SupermarketApp("com.osp.alcampo", "Alcampo"),
            "Alcampo"
        )
        register(
            SupermarketApp("com.consum", "Mundo Consum"),
            "Consum"
        )
        register(
            SupermarketApp("com.tempos21.eroskiclub.pro", "Eroski"),
            "Eroski"
        )
        register(
            SupermarketApp("com.uvesco.bm", "BM Supermercados"),
            "BM", "BM Supermercados"
        )
    }

    /**
     * Devuelve la app oficial asociada a un supermercado, o `null` si no se conoce.
     *
     * Primero busca una coincidencia exacta del nombre normalizado y, si no la
     * encuentra, comprueba si algún alias aparece como palabra dentro del nombre
     * (por ejemplo "Lidl Alcobendas").
     */
    fun forName(supermarketName: String): SupermarketApp? {
        val normalized = normalize(supermarketName)
        if (normalized.isEmpty()) return null

        byName[normalized]?.let { return it }

        return byName.entries
            .filter { (alias, _) ->
                Regex("(^|\\s)${Regex.escape(alias)}(\\s|$)").containsMatchIn(normalized)
            }
            .maxByOrNull { it.key.length }
            ?.value
    }

    /**
     * Abre la app oficial si está instalada. Si no lo está, abre su ficha en
     * Play Store (con reserva a la versión web).
     */
    fun open(context: Context, app: SupermarketApp) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            runCatching { context.startActivity(launchIntent) }
                .onFailure { openStore(context, app.packageName) }
        } else {
            openStore(context, app.packageName)
        }
    }

    private fun openStore(context: Context, packageName: String) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(marketIntent) }.onFailure {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(webIntent) }
        }
    }

    /** Minúsculas, sin espacios sobrantes y sin acentos, para comparar nombres. */
    private fun normalize(value: String): String =
        Normalizer.normalize(
            value.trim().lowercase(Locale.ROOT),
            Normalizer.Form.NFD
        )
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ")
}
