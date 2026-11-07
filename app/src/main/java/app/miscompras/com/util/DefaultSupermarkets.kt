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

    fun getLogoAssetPath(name: String): String? {
        return list.find { it.name.equals(name, ignoreCase = true) }?.logoAssetPath
    }

    /** Indica si una ruta guardada corresponde a un logo de asset (p. ej. "logos/lidl.svg"). */
    fun isLogoAssetPath(path: String?): Boolean =
        path != null && path.startsWith(LOGO_ASSET_FOLDER)
}
