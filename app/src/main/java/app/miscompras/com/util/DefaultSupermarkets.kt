package app.miscompras.com.util

import app.miscompras.com.R

data class DefaultSupermarket(
    val name: String,
    val logoResId: Int
)

object DefaultSupermarkets {
    val list = listOf(
        DefaultSupermarket("Mercadona", R.drawable.logo_mercadona),
        DefaultSupermarket("Carrefour", R.drawable.logo_carrefour),
        DefaultSupermarket("Lidl", R.drawable.logo_lidl),
        DefaultSupermarket("Aldi", R.drawable.logo_aldi),
        DefaultSupermarket("Dia", R.drawable.logo_dia),
        DefaultSupermarket("Consum", R.drawable.logo_consum),
        DefaultSupermarket("Eroski", R.drawable.logo_eroski),
        DefaultSupermarket("Hipercor", R.drawable.logo_hipercor),
        DefaultSupermarket("Coviran", R.drawable.logo_coviran)
    )

    fun getLogoResId(name: String): Int? {
        return list.find { it.name.equals(name, ignoreCase = true) }?.logoResId
    }
}
