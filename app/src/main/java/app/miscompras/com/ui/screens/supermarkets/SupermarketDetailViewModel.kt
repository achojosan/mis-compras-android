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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    private val _uiState = MutableStateFlow<SupermarketDetailUiState>(SupermarketDetailUiState.Loading)
    val uiState: StateFlow<SupermarketDetailUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.NameAsc)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    private var allProducts: List<Product> = emptyList()

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
            productRepository.updatePurchaseStatus(product.id, !product.isPurchased)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
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
