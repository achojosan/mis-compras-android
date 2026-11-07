package app.miscompras.com.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

/**
 * Muestra el logo de un supermercado.
 *
 * Los logos no tienen todos la misma forma: los hay tipo logotipo (letras, muy
 * anchos) y los hay cuadrados. Por eso la imagen siempre se dibuja con
 * [contentScale] `Fit` (nunca se recorta) y se puede consultar su proporción real
 * mediante [onAspectRatio], para que el contenedor que la envuelva se adapte.
 *
 * @param size tamaño cuadrado del logo. Si es `null`, el logo ocupa el espacio que
 *   le da [modifier] (útil para rellenar un contenedor con forma propia).
 * @param onAspectRatio devuelve la relación ancho/alto real de la imagen cargada.
 */
@Composable
fun SupermarketLogo(
    supermarketName: String,
    customLogoPath: String?,
    modifier: Modifier = Modifier,
    size: Dp? = AccessibilityDefaults.LargeIconSize,
    contentScale: ContentScale = ContentScale.Fit,
    onAspectRatio: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val sizingModifier = if (size != null) modifier.size(size) else modifier

    // Prioridad del logo:
    //  1. Un logo de asset guardado (p. ej. "logos/lidl.svg"), que ya no depende del nombre.
    //  2. El logo de la tienda predeterminada resuelto por nombre (datos antiguos).
    //  3. Un logo personalizado del usuario (URI de imagen).
    val assetPath = when {
        DefaultSupermarkets.isLogoAssetPath(customLogoPath) -> customLogoPath
        customLogoPath == null -> DefaultSupermarkets.getLogoAssetPath(supermarketName)
        else -> null
    }

    when {
        assetPath != null -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$assetPath")
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "Logo de $supermarketName",
                contentScale = contentScale,
                modifier = sizingModifier,
                onSuccess = { state -> reportAspectRatio(state.result.drawable, onAspectRatio) }
            )
        }
        customLogoPath != null -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(customLogoPath)
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "Logo de $supermarketName",
                contentScale = contentScale,
                modifier = sizingModifier,
                onSuccess = { state -> reportAspectRatio(state.result.drawable, onAspectRatio) }
            )
        }
        else -> {
            LaunchedEffect(supermarketName) { onAspectRatio?.invoke(1f) }
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                modifier = sizingModifier,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun reportAspectRatio(drawable: Drawable?, onAspectRatio: ((Float) -> Unit)?) {
    if (onAspectRatio == null || drawable == null) return
    val width = drawable.intrinsicWidth
    val height = drawable.intrinsicHeight
    if (width > 0 && height > 0) {
        onAspectRatio(width.toFloat() / height.toFloat())
    }
}
