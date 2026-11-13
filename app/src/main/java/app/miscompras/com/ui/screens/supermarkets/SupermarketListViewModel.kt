package app.miscompras.com.ui.screens.supermarkets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.util.DefaultSupermarkets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupermarketListViewModel @Inject constructor(
    private val repository: SupermarketRepository,
    private val productRepository: ProductRepository,
    dataStore: UserPreferencesDataStore
) : ViewModel() {

    /** Preferencia "Mostrar recuento" (Ajustes > Apariencia). */
    val showCounts: StateFlow<Boolean> = dataStore.showCounts
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _uiState = MutableStateFlow<SupermarketListUiState>(SupermarketListUiState.Loading)
    val uiState: StateFlow<SupermarketListUiState> = _uiState.asStateFlow()

    init {
        loadSupermarkets()
    }

    private fun loadSupermarkets() {
        viewModelScope.launch {
            combine(
                repository.getAllSupermarkets(),
                productRepository.getAllProducts()
            ) { supermarkets, products ->
                val items = supermarkets.map { supermarket ->
                    val supermarketProducts = products.filter { it.supermarketId == supermarket.id }
                    SupermarketListItem(
                        supermarket = supermarket,
                        productCount = supermarketProducts.size,
                        pendingCount = supermarketProducts.count { !it.isPurchased }
                    )
                }

                if (items.isEmpty()) {
                    SupermarketListUiState.Empty
                } else {
                    SupermarketListUiState.Success(items)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteSupermarket(supermarket: Supermarket) {
        viewModelScope.launch {
            repository.deleteSupermarket(supermarket)
        }
    }

    fun addSupermarket(name: String, logoUri: String?, passColor: Int? = null) {
        viewModelScope.launch {
            repository.insertSupermarket(
                Supermarket(
                    name = name.trim(),
                    // Guardamos la ruta del logo (asset) para que no dependa del nombre:
                    // así, si más adelante se renombra la tienda, el logo se conserva.
                    logoPath = logoUri ?: DefaultSupermarkets.getLogoAssetPath(name),
                    isDefault = DefaultSupermarkets.getLogoAssetPath(name) != null,
                    passColor = passColor
                )
            )
        }
    }
}

data class SupermarketListItem(
    val supermarket: Supermarket,
    val productCount: Int,
    val pendingCount: Int
)

sealed class SupermarketListUiState {
    data object Loading : SupermarketListUiState()
    data object Empty : SupermarketListUiState()
    data class Success(val items: List<SupermarketListItem>) : SupermarketListUiState()
}
