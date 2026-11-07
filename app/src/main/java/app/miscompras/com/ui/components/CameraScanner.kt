package app.miscompras.com.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import app.miscompras.com.recognition.ScanAnalyzer
import java.util.concurrent.Executors

/**
 * Pide el permiso de cámara (con una explicación clara) y, una vez concedido,
 * muestra [content]. Si se deniega, ofrece volver a pedirlo.
 */
@Composable
fun CameraPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    if (granted) {
        content()
    } else {
        EmptyState(
            icon = Icons.Default.CameraAlt,
            title = "Necesito la cámara",
            description = "La cámara se usa solo para reconocer los artículos. " +
                "Las imágenes no salen de tu teléfono.",
            modifier = Modifier.fillMaxSize()
        ) {
            AccessibleButton(
                text = "Permitir cámara",
                onClick = { launcher.launch(Manifest.permission.CAMERA) }
            )
        }
    }
}

/**
 * Vista previa de la cámara trasera con análisis de fotogramas ([ScanAnalyzer]) y un
 * marco guía cuadrado centrado. La cámara se libera al salir de la composición.
 */
@Composable
fun CameraScanner(
    onFrame: (barcode: String?, crop: Bitmap?) -> Unit,
    modifier: Modifier = Modifier,
    barcodeFormats: IntArray = ScanAnalyzer.PRODUCT_FORMATS,
    onBarcodeFormat: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val currentOnFrame by rememberUpdatedState(onFrame)
    val currentOnFormat by rememberUpdatedState(onBarcodeFormat)

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = ScanAnalyzer(
            formats = barcodeFormats,
            onFormat = { currentOnFormat?.invoke(it) }
        ) { code, crop -> currentOnFrame(code, crop) }
        var disposed = false
        // 4:3 fijo: con la caja 3:4 de abajo, el marco guía coincide con el recorte del analizador.
        val resolution = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            if (disposed) return@addListener
            val p = future.get()
            provider = p
            val preview = Preview.Builder().setResolutionSelector(resolution).build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }
            p.unbindAll()
            p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            provider?.unbindAll()
            executor.shutdown()
            analyzer.close()
        }
    }

    // Caja vertical 3:4 = fotograma completo (FILL_CENTER no recorta nada), de modo que
    // el marco de abajo (70 % del ancho) es exactamente la zona que recorta el analizador.
    Box(modifier = modifier.fillMaxWidth().aspectRatio(3f / 4f), contentAlignment = Alignment.Center) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Surface(
            modifier = Modifier
                .fillMaxWidth(ScanAnalyzer.GUIDE_FRACTION)
                .aspectRatio(1f)
                .padding(0.dp),
            color = androidx.compose.ui.graphics.Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
        ) {}
    }
}
