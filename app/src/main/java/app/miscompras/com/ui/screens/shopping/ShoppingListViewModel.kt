package app.miscompras.com.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.PurchaseHistoryRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.domain.model.Supermarket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val supermarketRepository: SupermarketRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadShoppingList()
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            combine(
                productRepository.getAllPendingProducts(),
                supermarketRepository.getAllSupermarkets()
            ) { products, supermarkets ->
                val groupedBySupermarket = products.groupBy { it.supermarketId }
                val shoppingItems = groupedBySupermarket.map { (supermarketId, products) ->
                    val supermarket = supermarkets.find { it.id == supermarketId }
                    ShoppingItem(
                        supermarket = supermarket,
                        products = products,
                        total = products.sumOf { it.price * it.quantity }
                    )
                }.sortedBy { it.supermarket?.name ?: "" }
                shoppingItems
            }.collect { shoppingItems ->
                _uiState.value = if (shoppingItems.isEmpty()) {
                    ShoppingListUiState.Empty
                } else {
                    ShoppingListUiState.Success(shoppingItems)
                }
            }
        }
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            productRepository.updatePurchaseStatus(product.id, !product.isPurchased)
        }
    }

    fun finishPurchase(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            val supermarket = shoppingItem.supermarket ?: return@launch
            val products = shoppingItem.products

            val productsJson = Json.encodeToString(
                products.map {
                    PurchasedProductData(
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        price = it.price
                    )
                }
            )

            val history = PurchaseHistory(
                supermarketId = supermarket.id,
                totalAmount = shoppingItem.total,
                productsJson = productsJson
            )

            purchaseHistoryRepository.insertHistory(history)

            products.forEach { product ->
                productRepository.deleteProduct(product)
            }
        }
    }
}

sealed class ShoppingListUiState {
    data object Loading : ShoppingListUiState()
    data object Empty : ShoppingListUiState()
    data class Success(val shoppingItems: List<ShoppingItem>) : ShoppingListUiState()
}

data class ShoppingItem(
    val supermarket: Supermarket?,
    val products: List<Product>,
    val total: Double
)

data class PurchasedProductData(
    val name: String,
    val quantity: Double,
    val unit: String,
    val price: Double
)
