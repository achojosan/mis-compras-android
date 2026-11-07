package app.miscompras.com.ui.util

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow

/**
 * Recoge los eventos de un solo uso emitidos por un ViewModel y los muestra
 * como Toast. Se usa junto a un `Channel` expuesto como `Flow` para que el
 * aviso se muestre una única vez, cuando la operación ya se ha completado.
 */
@Composable
fun ToastEffect(events: Flow<String>) {
    val context = LocalContext.current
    LaunchedEffect(events) {
        events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
