package app.miscompras.com.ui.screens.nearby

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.remote.osm.OverpassApiService
import app.miscompras.com.data.remote.osm.OverpassQueryBuilder
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.NearbyPlace
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.util.DefaultSupermarkets
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val overpassApiService: OverpassApiService,
    private val supermarketRepository: SupermarketRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _uiState = MutableStateFlow<NearbyUiState>(NearbyUiState.Idle)
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    private val _selectedPlaceIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPlaceIds: StateFlow<Set<Long>> = _selectedPlaceIds.asStateFlow()

    private val _addResult = MutableStateFlow<AddResult?>(null)
    val addResult: StateFlow<AddResult?> = _addResult.asStateFlow()

    fun checkPermissionAndLoad() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            loadNearbyPlaces()
        } else {
            _uiState.value = NearbyUiState.PermissionRequired
        }
    }

    fun loadNearbyPlaces() {
        viewModelScope.launch {
            _uiState.value = NearbyUiState.Loading
            _selectedPlaceIds.value = emptySet()

            try {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    _uiState.value = NearbyUiState.PermissionRequired
                    return@launch
                }

                val location = fusedLocationClient.lastLocation.await()
                if (location == null) {
                    _uiState.value = NearbyUiState.Error("No se pudo obtener la ubicación")
                    return@launch
                }

                val query = OverpassQueryBuilder.buildNearbySupermarketsQuery(
                    lat = location.latitude,
                    lon = location.longitude
                )

                val response = overpassApiService.getNearbySupermarkets(query)

                val places = response.elements.mapNotNull { element ->
                    val lat = element.lat ?: return@mapNotNull null
                    val lon = element.lon ?: return@mapNotNull null
                    val name = element.name ?: element.brand ?: element.operator ?: return@mapNotNull null

                    val distance = calculateDistance(
                        location.latitude, location.longitude,
                        lat, lon
                    )

                    NearbyPlace(
                        id = element.id,
                        name = name,
                        brand = element.brand,
                        latitude = lat,
                        longitude = lon,
                        openingHours = element.openingHours,
                        phone = element.phone,
                        website = element.website,
                        distanceMeters = distance,
                        isOpenNow = parseOpeningHours(element.openingHours)
                    )
                }.sortedBy { it.distanceMeters }

                _uiState.value = if (places.isEmpty()) {
                    NearbyUiState.Empty
                } else {
                    NearbyUiState.Success(places)
                }
            } catch (e: Exception) {
                _uiState.value = NearbyUiState.Error("Error al cargar comercios: ${e.message}")
            }
        }
    }

    fun toggleSelection(placeId: Long) {
        _selectedPlaceIds.value = if (_selectedPlaceIds.value.contains(placeId)) {
            _selectedPlaceIds.value - placeId
        } else {
            _selectedPlaceIds.value + placeId
        }
    }

    fun selectAll() {
        val currentState = _uiState.value
        if (currentState is NearbyUiState.Success) {
            _selectedPlaceIds.value = currentState.places.map { it.id }.toSet()
        }
    }

    fun deselectAll() {
        _selectedPlaceIds.value = emptySet()
    }

    fun addSelectedSupermarkets() {
        val currentState = _uiState.value
        if (currentState !is NearbyUiState.Success) return

        val selectedPlaces = currentState.places.filter { it.id in _selectedPlaceIds.value }
        if (selectedPlaces.isEmpty()) return

        viewModelScope.launch {
            try {
                selectedPlaces.forEach { place ->
                    val isDefault = DefaultSupermarkets.getLogoAssetPath(place.name) != null
                    val supermarket = Supermarket(
                        name = place.name,
                        logoPath = null,
                        isDefault = isDefault,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        openingHours = place.openingHours
                    )
                    supermarketRepository.insertSupermarket(supermarket)
                }
                _selectedPlaceIds.value = emptySet()
                _addResult.value = AddResult.Success(selectedPlaces.size)
            } catch (e: Exception) {
                _addResult.value = AddResult.Error("Error al añadir supermercados: ${e.message}")
            }
        }
    }

    fun clearAddResult() {
        _addResult.value = null
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun parseOpeningHours(openingHours: String?): Boolean? {
        if (openingHours.isNullOrBlank()) return null

        return try {
            val now = Calendar.getInstance()
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val currentTime = currentHour * 60 + currentMinute

            val dayMap = mapOf(
                Calendar.MONDAY to "Mo",
                Calendar.TUESDAY to "Tu",
                Calendar.WEDNESDAY to "We",
                Calendar.THURSDAY to "Th",
                Calendar.FRIDAY to "Fr",
                Calendar.SATURDAY to "Sa",
                Calendar.SUNDAY to "Su"
            )

            val todayAbbr = dayMap[dayOfWeek] ?: return null

            val parts = openingHours.split(";")
            for (part in parts) {
                val trimmed = part.trim()
                if (trimmed.contains(todayAbbr) || trimmed.contains("Mo-Su") || trimmed.contains("Mo-Fr")) {
                    val timeMatch = Regex("(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})").find(trimmed)
                    if (timeMatch != null) {
                        val (openHour, openMin, closeHour, closeMin) = timeMatch.destructured
                        val openTime = openHour.toInt() * 60 + openMin.toInt()
                        val closeTime = closeHour.toInt() * 60 + closeMin.toInt()
                        return currentTime in openTime..closeTime
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

sealed class NearbyUiState {
    data object Idle : NearbyUiState()
    data object PermissionRequired : NearbyUiState()
    data object Loading : NearbyUiState()
    data object Empty : NearbyUiState()
    data class Error(val message: String) : NearbyUiState()
    data class Success(val places: List<NearbyPlace>) : NearbyUiState()
}

sealed class AddResult {
    data class Success(val count: Int) : AddResult()
    data class Error(val message: String) : AddResult()
}
