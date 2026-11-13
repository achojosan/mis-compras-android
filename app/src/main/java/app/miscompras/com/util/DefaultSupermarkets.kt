package app.miscompras.com.util

data class DefaultSupermarket(
    val name: String,
    val logoAssetPath: String
)

object DefaultSupermarkets {
    private const val LOGO_ASSET_FOLDER = "logos/"

    val list = listOf(
        DefaultSupermarket("Mercadona", "logos/mercadona.svg"),
        DefaultSupermarket("Carrefour", "logos/carrefour.svg"),
        DefaultSupermarket("Lidl", "logos/lidl.svg"),
        DefaultSupermarket("Aldi", "logos/aldi.svg"),
        DefaultSupermarket("Dia", "logos/dia.svg"),
        DefaultSupermarket("Hipercor", "logos/hipercor.svg")
    )

    /** Color de pase propio de cada cadena conocida (ids de PassPalette). */
    private val chainPassColors = mapOf(
        "mercadona" to 0, "carrefour" to 1, "lidl" to 3,
        "aldi" to 4, "dia" to 2, "hipercor" to 0
    )

    fun passColorFor(name: String): Int? = chainPassColors[name.trim().lowercase()]

    fun getLogoAssetPath(name: String): String? {
        return list.find { it.name.equals(name, ignoreCase = true) }?.logoAssetPath
    }

    /** Indica si una ruta guardada corresponde a un logo de asset (p. ej. "logos/lidl.svg"). */
    fun isLogoAssetPath(path: String?): Boolean =
        path != null && path.startsWith(LOGO_ASSET_FOLDER)
}
