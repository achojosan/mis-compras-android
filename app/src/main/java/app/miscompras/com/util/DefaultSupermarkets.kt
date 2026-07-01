package app.miscompras.com.util

data class DefaultSupermarket(
    val name: String,
    val logoAssetPath: String
)

object DefaultSupermarkets {
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
}
