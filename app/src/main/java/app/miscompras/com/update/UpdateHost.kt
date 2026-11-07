package app.miscompras.com.update

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.miscompras.com.BuildConfig

/**
 * Avisos de actualización: busca una vez al día al abrir la app (si está activado) y muestra los
 * diálogos de «hay versión nueva», permiso, descarga y errores. También abre la confirmación del
 * sistema cuando la app está visible.
 */
@Composable
fun UpdateHost(viewModel: UpdateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.checkOnLaunch() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (val s = state) {
        is UpdateState.ReadyToConfirm -> LaunchedEffect(s) {
            runCatching { context.startActivity(s.confirmIntent) }
            viewModel.confirmLaunched()
        }

        is UpdateState.Available -> AlertDialog(
            onDismissRequest = viewModel::later,
            title = { Text("Hay una versión nueva") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Mis Compras ${s.release.version} está disponible (tienes la ${BuildConfig.VERSION_NAME}).")
                    val news = UpdateLogic.highlights(s.release.notes)
                    if (news.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Novedades:")
                        news.forEach { Text("• $it") }
                    }
                    Spacer(Modifier.height(12.dp))
                    val size = UpdateLogic.formatSize(s.release.apkSize)
                    Text(
                        (if (size.isNotEmpty()) "Pesa unos $size: mejor con Wi‑Fi. " else "") +
                            "Al instalarla, la app se cerrará y tendrás que volver a abrirla."
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.startUpdate(s.release) }) { Text("Actualizar ahora") } },
            dismissButton = { TextButton(onClick = viewModel::later) { Text("Más tarde") } }
        )

        is UpdateState.NeedsPermission -> AlertDialog(
            onDismissRequest = viewModel::later,
            title = { Text("Un permiso, una sola vez") },
            text = {
                Text(
                    "Para instalar la actualización, Android necesita que permitas a Mis Compras instalar " +
                        "aplicaciones. Pulsa «Abrir ajustes», activa «Permitir de esta fuente» (o «Permitir siempre») y vuelve atrás."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                    )
                }) { Text("Abrir ajustes") }
            },
            dismissButton = { TextButton(onClick = viewModel::later) { Text("Cancelar") } }
        )

        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Descargando la actualización") },
            text = {
                Column {
                    LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text("${(s.progress * 100).toInt()} %")
                    Spacer(Modifier.height(10.dp))
                    // Google Play Protect no conoce la app y suele preguntar al final.
                    Text("Al terminar, si Google Play Protect pregunta, elige «Instalar sin analizar» (o «Analizar app»).")
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = viewModel::cancel) { Text("Cancelar") } }
        )

        is UpdateState.Failed -> AlertDialog(
            onDismissRequest = viewModel::dismissFailure,
            title = { Text("No se pudo actualizar") },
            text = { Text(s.message + "\n\nPuedes descargarla a mano desde la página de versiones.") },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UpdateConfig.RELEASES_PAGE))) }
                    viewModel.dismissFailure()
                }) { Text("Descargar a mano") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissFailure) { Text("Cerrar") } }
        )

        else -> Unit
    }
}
