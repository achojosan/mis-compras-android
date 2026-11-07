package app.miscompras.com.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.data.remote.osm.OverpassClient
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.location.LocationProvider
import app.miscompras.com.nearby.NearbyChain
import app.miscompras.com.nearby.NearbyChains
import app.miscompras.com.util.DefaultSupermarkets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Pasos del onboarding inicial. */
enum class OnboardingStep { Welcome, Tour, Nearby, Done }

/** Estado de la búsqueda de supermercados cercanos. */
sealed interface NearbyUi {
    data object Intro : NearbyUi
    data object Loading : NearbyUi
    data object Denied : NearbyUi
    data object Empty : NearbyUi
    data class Error(val message: String) : NearbyUi
    data class Results(val chains: List<NearbyChain>, val selected: Set<String>) : NearbyUi
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val locationProvider: LocationProvider,
    private val overpass: OverpassClient,
    private val supermarkets: SupermarketRepository
) : ViewModel() {

    private val _nearby = MutableStateFlow<NearbyUi>(NearbyUi.Intro)
    val nearby: StateFlow<NearbyUi> = _nearby.asStateFlow()

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Empieza el tour explicativo. */
    fun onGetStarted() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.Tour)
    }

    /** El usuario decide saltarse el tour. */
    fun onSkipTour() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.Nearby)
    }

    /** El usuario termina el tour. */
    fun onTourCompleted() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.Nearby)
    }

    /** Sin permiso de ubicación: se sigue adelante sin bloquear el onboarding. */
    fun onLocationDenied() {
        _nearby.value = NearbyUi.Denied
    }

    /** Busca las cadenas conocidas más cercanas y las deja marcadas para añadir. */
    fun searchNearby() {
        _nearby.value = NearbyUi.Loading
        viewModelScope.launch {
            val here = locationProvider.currentLocation()
            if (here == null) {
                _nearby.value = NearbyUi.Error(
                    "No he podido saber dónde estás. Comprueba que la ubicación del móvil esté activada."
                )
                return@launch
            }
            _nearby.value = try {
                val found = NearbyChains.pick(
                    overpass.nearbyShops(here.latitude, here.longitude), here.latitude, here.longitude
                )
                if (found.isEmpty()) NearbyUi.Empty
                else NearbyUi.Results(found, selected = found.map { it.chainName }.toSet())
            } catch (e: Exception) {
                NearbyUi.Error("No he podido consultar el mapa. Puede que no tengas conexión o que el servicio de OpenStreetMap esté saturado: inténtalo de nuevo en unos minutos, o añade tus tiendas a mano.")
            }
        }
    }

    fun toggleChain(name: String) {
        val current = _nearby.value as? NearbyUi.Results ?: return
        _nearby.value = current.copy(
            selected = if (name in current.selected) current.selected - name else current.selected + name
        )
    }

    /** Añade las tiendas marcadas (sin duplicar las que ya existan) y pasa al final. */
    fun addSelectedAndContinue() {
        val current = _nearby.value as? NearbyUi.Results
        viewModelScope.launch {
            current?.chains?.filter { it.chainName in current.selected }?.forEach { chain ->
                if (!supermarkets.supermarketExists(chain.chainName)) {
                    supermarkets.insertSupermarket(
                        Supermarket(
                            name = chain.chainName,
                            isDefault = DefaultSupermarkets.getLogoAssetPath(chain.chainName) != null,
                            latitude = chain.latitude,
                            longitude = chain.longitude,
                            openingHours = chain.openingHours
                        )
                    )
                }
            }
            continueToDone()
        }
    }

    fun continueToDone() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.Done)
    }

    /** Marca el onboarding como completado y da paso a la app. */
    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.setOnboardingCompleted(true)
        }
    }
}
