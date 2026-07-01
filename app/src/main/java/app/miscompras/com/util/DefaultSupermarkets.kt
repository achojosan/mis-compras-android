package app.miscompras.com.util

import app.miscompras.com.R

data class DefaultSupermarket(
    val name: String,
    val logoResId: Int
)

object DefaultSupermarkets {
    val list = listOf(
        DefaultSupermarket("Mercadona", R.drawable.logos.logo_mercadona),
        DefaultSupermarket("Carrefour", R.drawable.logos.logo_carrefour),
        DefaultSupermarket("Lidl", R.drawable.logos.logo_lidl),
        DefaultSupermarket("Aldi", R.drawable.logos.logo_aldi),
        DefaultSupermarket("Dia", R.drawable.logos.logo_dia),
        DefaultSupermarket("Consum", R.drawable.logos.logo_consum),
        DefaultSupermarket("Eroski", R.drawable.logos.logo_eroski),
        DefaultSupermarket("Hipercor", R.drawable.logos.logo_hipercor),
        DefaultSupermarket("Coviran", R.drawable.logos.logo_coviran)
    )

    fun getLogoResId(name: String): Int? {
        return list.find { it.name.equals(name, ignoreCase = true) }?.logoResId
    }
}
