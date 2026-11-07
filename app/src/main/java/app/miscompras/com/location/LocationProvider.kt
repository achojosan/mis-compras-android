package app.miscompras.com.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class GeoPoint(val latitude: Double, val longitude: Double)

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** `true` si hay ubicación precisa o aproximada concedida (en Android 12+ basta la aproximada). */
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Posición actual. En un móvil recién estrenado `lastLocation` suele ser nulo, así que se pide una
     * lectura nueva: primero con precisión equilibrada (red/Wi‑Fi, rápida y de bajo consumo) y, si no
     * llega a tiempo, con GPS. Solo entonces se recurre a la última posición conocida.
     */
    @Suppress("MissingPermission")
    suspend fun currentLocation(): GeoPoint? {
        if (!hasPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = fresh(client, Priority.PRIORITY_BALANCED_POWER_ACCURACY, 8_000)
            ?: fresh(client, Priority.PRIORITY_HIGH_ACCURACY, 15_000)
            ?: runCatching { client.lastLocation.await() }.getOrNull()
        return location?.let { GeoPoint(it.latitude, it.longitude) }
    }

    @Suppress("MissingPermission")
    private suspend fun fresh(client: FusedLocationProviderClient, priority: Int, timeoutMs: Long): Location? =
        runCatching {
            val token = CancellationTokenSource()
            withTimeoutOrNull(timeoutMs) {
                client.getCurrentLocation(priority, token.token).await()
            }.also { token.cancel() }
        }.getOrNull()
}
