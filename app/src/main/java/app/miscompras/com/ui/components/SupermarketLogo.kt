package app.miscompras.com.ui.components

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun SupermarketLogo(
    supermarketName: String,
    customLogoPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = AccessibilityDefaults.LargeIconSize
) {
    val context = LocalContext.current
    val defaultAssetPath = DefaultSupermarkets.getLogoAssetPath(supermarketName)

    when {
        customLogoPath != null -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(customLogoPath)
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "Logo de $supermarketName",
                modifier = modifier.size(size)
            )
        }
        defaultAssetPath != null -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$defaultAssetPath")
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "Logo de $supermarketName",
                modifier = modifier.size(size)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                modifier = modifier.size(size),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
