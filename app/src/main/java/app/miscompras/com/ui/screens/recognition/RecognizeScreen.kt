package app.miscompras.com.ui.screens.recognition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.CameraPermissionGate
import app.miscompras.com.ui.components.CameraScanner
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun RecognizeScreen(
    onItemRecognized: (Long) -> Unit,
    onRegisterClick: () -> Unit,
    onKnownItemsClick: () -> Unit,
    viewModel: RecognizeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reinicia el escaneo cada vez que la pantalla vuelve a primer plano.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.restart()
                Lifecycle.Event.ON_PAUSE -> viewModel.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { viewModel.recognized.collect { onItemRecognized(it) } }

    CameraPermissionGate {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CameraScanner(
                onFrame = viewModel::onFrame,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = state.status,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(AccessibilityDefaults.CardPadding)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .padding(AccessibilityDefaults.CardPadding)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
            ) {
                if (state.notRecognized) {
                    AccessibleButton(text = "Registrar este artículo", onClick = onRegisterClick)
                }
                // Fondo propio: un botón transparente sobre la imagen de la cámara no se lee.
                androidx.compose.material3.Surface(
                    shape = app.miscompras.com.ui.theme.AppShapes.button,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    AccessibleButton(
                        text = "Mis artículos",
                        onClick = onKnownItemsClick,
                        style = app.miscompras.com.ui.components.ButtonStyle.Outlined
                    )
                }
            }
        }
    }
}
