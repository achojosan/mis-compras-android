package app.miscompras.com.ui.screens.supermarkets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.data.remote.osm.OverpassClient
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.location.LocationProvider
import app.miscompras.com.nearby.Chains
import app.miscompras.com.nearby.NearbyChains
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.util.DefaultSupermarkets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductSortOrder {
    NameAsc,
    NameDesc,
    PriceAsc,
    PriceDesc,
    Status
}

@HiltViewModel
class SupermarketDetailViewModel @Inject constructor(
    private val supermarketRepository: SupermarketRepository,
    private val productRepository: ProductRepository,
    private val dataStore: UserPreferencesDataStore,
    private val locationProvider: LocationProvider,
    private val overpass: OverpassClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    private val _uiState = MutableStateFlow<SupermarketDetailUiState>(SupermarketDetailUiState.Loading)
    val uiState: StateFlow<SupermarketDetailUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.NameAsc)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    // Nombres ya usados, para sugerir al escribir uno nuevo.
    private val _productNames = MutableStateFlow<List<String>>(emptyList())
    val productNames: StateFlow<List<String>> = _productNames.asStateFlow()

    // Si el usuario decidió no volver a ver el aviso al abrir la app de una tienda.
    /** Preferencia "Mostrar recuento" (Ajustes > Apariencia). */
    val showCounts: StateFlow<Boolean> = dataStore.showCounts
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _hideOpenAppDialog = MutableStateFlow(false)
    val hideOpenAppDialog: StateFlow<Boolean> = _hideOpenAppDialog.asStateFlow()

    // Eventos de un solo uso para mostrar avisos (Toast) al usuario.
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _locating = MutableStateFlow(false)
    val locating: StateFlow<Boolean> = _locating.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init {
        loadSupermarket()
        loadProductNames()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            dataStore.hideOpenAppDialog.collect { hidden ->
                _hideOpenAppDialog.value = hidden
            }
        }
    }

    private fun loadProductNames() {
        viewModelScope.launch {
            productRepository.getAllProductNames().collect { names ->
                _productNames.value = names
            }
        }
    }

    private fun loadSupermarket() {
        viewModelScope.launch {
            supermarketRepository.getSupermarketById(supermarketId).collect { supermarket ->
                if (supermarket != null) {
                    _uiState.value = SupermarketDetailUiState.Success(
                        supermarket = supermarket,
                        products = emptyList()
                    )
                    loadProducts()
                } else {
                    _uiState.value = SupermarketDetailUiState.Error("Supermercado no encontrado")
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProductsBySupermarket(supermarketId).collect { products ->
                allProducts = products
                applySort()
            }
        }
    }

    fun setSortOrder(order: ProductSortOrder) {
        _sortOrder.value = order
        applySort()
    }

    private fun applySort() {
        val sorted = when (_sortOrder.value) {
            ProductSortOrder.NameAsc -> allProducts.sortedBy { it.name }
            ProductSortOrder.NameDesc -> allProducts.sortedByDescending { it.name }
            ProductSortOrder.PriceAsc -> allProducts.sortedBy { it.price ?: Double.MAX_VALUE }
            ProductSortOrder.PriceDesc -> allProducts.sortedByDescending { it.price ?: 0.0 }
            ProductSortOrder.Status -> allProducts.sortedBy { it.isPurchased }
        }
        val currentState = _uiState.value
        if (currentState is SupermarketDetailUiState.Success) {
            _uiState.value = currentState.copy(products = sorted)
        }
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            val purchased = !product.isPurchased
            productRepository.updatePurchaseStatus(product.id, purchased)
            _events.send(if (purchased) "Marcado como comprado" else "Marcado como pendiente")
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
            _events.send("Producto actualizado")
        }
    }

    fun addProduct(name: String, quantity: Double, unit: String, price: Double?) {
        viewModelScope.launch {
            val uppercaseNames = dataStore.uppercaseProductNames.first()
            val finalName = if (uppercaseNames) name.trim().uppercase() else name.trim()
            productRepository.insertProduct(
                Product(
                    name = finalName,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    supermarketId = supermarketId
                )
            )
            _events.send("Producto añadido")
        }
    }

    fun setHideOpenAppDialog(hide: Boolean) {
        viewModelScope.launch { dataStore.setHideOpenAppDialog(hide) }
    }

    fun updateSupermarket(name: String, logoUri: String?) {
        val current = (_uiState.value as? SupermarketDetailUiState.Success)?.supermarket ?: return
        viewModelScope.launch {
            supermarketRepository.updateSupermarket(
                current.copy(
                    name = name.trim(),
                    // Conserva el logo aunque cambie el nombre: si no se eligió uno nuevo,
                    // mantiene el actual o el de la tienda predeterminada original.
                    logoPath = logoUri
                        ?: current.logoPath
                        ?: DefaultSupermarkets.getLogoAssetPath(current.name),
                    isDefault = DefaultSupermarkets.getLogoAssetPath(name) != null
                )
            )
            _events.send("Tienda actualizada")
        }
    }

    /**
     * Busca la sucursal más cercana de esta cadena y guarda sus coordenadas y su horario.
     * Sirve para tiendas añadidas a mano o antes de existir esta función.
     */
    fun locateNearest() {
        val current = (_uiState.value as? SupermarketDetailUiState.Success)?.supermarket ?: return
        if (_locating.value) return
        _locating.value = true
        viewModelScope.launch {
            try {
                val chain = Chains.match(current.name)
                if (chain == null) {
                    _events.send("Esta tienda no es una cadena conocida, no puedo localizarla")
                    return@launch
                }
                val here = locationProvider.currentLocation()
                if (here == null) {
                    _events.send("No he podido saber dónde estás")
                    return@launch
                }
                val branch = NearbyChains.pick(overpass.nearbyShops(here.latitude, here.longitude), here.latitude, here.longitude)
                    .firstOrNull { it.chainName == chain.name }
                if (branch == null) {
                    _events.send("No hay ninguna sucursal de ${current.name} a menos de 10 km")
                    return@launch
                }
                supermarketRepository.updateSupermarket(
                    current.copy(latitude = branch.latitude, longitude = branch.longitude, openingHours = branch.openingHours)
                )
                _events.send("Sucursal más cercana a ${NearbyChains.formatDistance(branch.distanceMeters)}")
            } catch (e: Exception) {
                _events.send("No he podido consultar las tiendas. Comprueba tu conexión")
            } finally {
                _locating.value = false
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
            _events.send("Producto eliminado")
        }
    }
}

sealed class SupermarketDetailUiState {
    data object Loading : SupermarketDetailUiState()
    data class Error(val message: String) : SupermarketDetailUiState()
    data class Success(
        val supermarket: Supermarket,
        val products: List<Product>
    ) : SupermarketDetailUiState()
}
