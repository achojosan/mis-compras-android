package app.miscompras.com.ui.screens.supermarkets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupermarketDetailViewModel @Inject constructor(
    private val supermarketRepository: SupermarketRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    private val _uiState = MutableStateFlow<SupermarketDetailUiState>(SupermarketDetailUiState.Loading)
    val uiState: StateFlow<SupermarketDetailUiState> = _uiState.asStateFlow()

    init {
        loadSupermarket()
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
                val currentState = _uiState.value
                if (currentState is SupermarketDetailUiState.Success) {
                    _uiState.value = currentState.copy(products = products)
                }
            }
        }
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            productRepository.updatePurchaseStatus(product.id, !product.isPurchased)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
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
