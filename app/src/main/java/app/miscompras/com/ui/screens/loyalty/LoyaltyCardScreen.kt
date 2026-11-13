package app.miscompras.com.ui.screens.loyalty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.theme.PassColor
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.ui.theme.pass
import app.miscompras.com.ui.theme.resolvePass
import app.miscompras.com.util.groupCode
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.recognition.LoyaltyBarcode
import app.miscompras.com.recognition.ScanAnalyzer
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.AccessibleTextField
import app.miscompras.com.ui.components.ButtonStyle
import app.miscompras.com.ui.components.CameraPermissionGate
import app.miscompras.com.ui.components.CameraScanner
import app.miscompras.com.ui.components.FloatingTopBar
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun LoyaltyCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoyaltyCardViewModel = hiltViewModel()
) {
    val supermarket by viewModel.supermarket.collectAsState()
    val editing by viewModel.editing.collectAsState()
    val store = supermarket
    val hasCard = store?.loyaltyCode != null

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FloatingTopBar(
            title = "Tarjeta de cliente",
            backgroundFraction = 1f,
            onNavigateBack = onNavigateBack
        )
        if (store == null) return@Column
        if (hasCard && !editing) {
            CardView(
                storeName = store.name,
                pass = store.pass,
                code = store.loyaltyCode!!,
                format = store.loyaltyFormat,
                onChange = viewModel::startEditing,
                onDelete = viewModel::delete
            )
        } else {
            AddCardView(
                storeName = store.name,
                pass = store.pass,
                canCancel = hasCard,
                onCancel = viewModel::cancelEditing,
                onFormat = viewModel::onFormat,
                onScan = viewModel::onScanFrame,
                onTyped = viewModel::saveTyped
            )
        }
    }
}

/** Código a pantalla completa sobre blanco y con el brillo al máximo para que la caja lo lea. */
@Composable
private fun CardView(
    storeName: String,
    pass: PassColor,
    code: String,
    format: Int?,
    onChange: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val previous = window?.attributes?.screenBrightness
        window?.attributes = window?.attributes?.apply { screenBrightness = 1f }
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.attributes = window?.attributes?.apply {
                screenBrightness = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val bitmap = remember(code, format) { renderCode(code, format) }
    var confirmDelete by remember { mutableStateOf(false) }

    val resolved = resolvePass(pass)
    val grouped = remember(code) { groupCode(code) }
    var enlarged by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.SectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // La tarjeta: el color de la tienda por fuera y, dentro, el código sobre blanco con margen
        // (zona de silencio) y a todo el ancho: nunca se reduce por debajo de lo que lee la caja.
        Surface(
            shape = AppShapes.pass,
            color = resolved.container,
            contentColor = resolved.content,
            border = if (resolved.outlined) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(storeName, style = MaterialTheme.typography.headlineMedium)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Código de la tarjeta de $storeName",
                            contentScale = ContentScale.FillWidth,
                            filterQuality = FilterQuality.None,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        grouped,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = TabularNumbers),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Text(
            "Acerca la pantalla al lector de la caja. El brillo sube al máximo mientras la ves.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        AccessibleButton(text = "Ver número en grande", onClick = { enlarged = true }, modifier = Modifier.fillMaxWidth())
        AccessibleButton(text = "Cambiar tarjeta", onClick = onChange, style = ButtonStyle.Outlined, modifier = Modifier.fillMaxWidth())
        TextButton(
            onClick = { confirmDelete = true },
            modifier = Modifier.defaultMinSize(minHeight = AccessibilityDefaults.MinTouchTarget)
        ) { Text("Eliminar tarjeta", color = MaterialTheme.colorScheme.error) }
    }

    if (enlarged) {
        // Para quien no ve bien o para dictar el número en caja: cifras enormes sobre blanco, en bloques.
        Dialog(
            onDismissRequest = { enlarged = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(color = Color.White, modifier = Modifier.fillMaxSize(), onClick = { enlarged = false }) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(storeName, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    Spacer(Modifier.height(16.dp))
                    grouped.split(' ').forEach { block ->
                        Text(
                            block,
                            style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = TabularNumbers),
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Toca para cerrar", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF3B4F43))
                }
            }
        }
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar la tarjeta?") },
            text = { Text("Se borrará de este móvil. Podrás volver a guardarla cuando quieras.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun AddCardView(
    storeName: String,
    pass: PassColor,
    canCancel: Boolean,
    onCancel: () -> Unit,
    onFormat: (Int) -> Unit,
    onScan: (String?) -> Unit,
    onTyped: (String) -> Unit
) {
    var typed by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
    ) {
        // Cabecera con el pase de la tienda: la persona ve de qué tarjeta se trata antes de leer nada.
        val resolved = resolvePass(pass)
        Surface(
            shape = AppShapes.pass,
            color = resolved.container,
            contentColor = resolved.content,
            border = if (resolved.outlined) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(storeName, style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Apunta la cámara al código de barras o QR de tu tarjeta. Se guarda en este móvil y no se envía a ninguna parte.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().clip(AppShapes.pass)) {
            CameraPermissionGate {
                CameraScanner(
                    onFrame = { code, _ -> onScan(code) },
                    barcodeFormats = ScanAnalyzer.CARD_FORMATS,
                    onBarcodeFormat = onFormat
                )
            }
        }
        Text("O escribe el número a mano:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AccessibleTextField(value = typed, onValueChange = { typed = it }, label = "Número de la tarjeta")
        AccessibleButton(text = "Guardar número", onClick = { onTyped(typed) }, enabled = typed.isNotBlank(), modifier = Modifier.fillMaxWidth())
        if (canCancel) TextButton(onClick = onCancel) { Text("Cancelar") }
    }
}

private fun renderCode(code: String, format: Int?): Bitmap? {
    val matrix = LoyaltyBarcode.encode(code, format) ?: return null
    val pixels = IntArray(matrix.width * matrix.height) { i ->
        if (matrix[i % matrix.width, i / matrix.width]) AndroidColor.BLACK else AndroidColor.WHITE
    }
    return Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
