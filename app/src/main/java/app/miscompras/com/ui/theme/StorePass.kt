package app.miscompras.com.ui.theme

import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.util.DefaultSupermarkets

/** Pase de la tienda: el elegido, el de su cadena conocida o uno estable según su id. */
val Supermarket.pass: PassColor
    get() = PassPalette.byId(
        id = passColor ?: DefaultSupermarkets.passColorFor(name),
        fallbackKey = id
    )
