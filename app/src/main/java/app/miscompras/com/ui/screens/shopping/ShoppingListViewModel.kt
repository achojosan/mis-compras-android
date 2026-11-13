package app.miscompras.com.ui.screens.shopping

import app.miscompras.com.util.formatEuros
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.PurchaseHistoryRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.domain.model.Supermarket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val supermarketRepository: SupermarketRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    // Eventos de un solo uso para mostrar avisos (Toast) al usuario.
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadShoppingList()
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                supermarketRepository.getAllSupermarkets()
            ) { products, supermarkets ->
                val groupedBySupermarket = products.groupBy { it.supermarketId }
                val shoppingItems = groupedBySupermarket.map { (supermarketId, products) ->
                    val supermarket = supermarkets.find { it.id == supermarketId }
                    val fullTotal = products.sumOf { (it.price ?: 0.0) * it.quantity }
                    ShoppingItem(
                        supermarket = supermarket,
                        products = products,
                        total = fullTotal
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
            val purchased = !product.isPurchased
            productRepository.updatePurchaseStatus(product.id, purchased)
            _events.send(if (purchased) "Marcado como comprado" else "Marcado como pendiente")
        }
    }

    fun updateProductQuantity(productId: Long, quantity: Double) {
        viewModelScope.launch {
            productRepository.getProductById(productId).first()?.let { product ->
                productRepository.updateProduct(product.copy(quantity = quantity))
            }
        }
    }

    fun finishPurchase(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            val supermarket = shoppingItem.supermarket ?: run {
                _events.send("No se encontró el supermercado")
                return@launch
            }

            // Se registran los productos marcados; si el usuario no marcó
            // ninguno, se registra la lista completa (comportamiento esperado
            // al pulsar «Finalizar»).
            val productsToRecord = shoppingItem.productsToRecord
            if (productsToRecord.isEmpty()) {
                _events.send("No hay productos para registrar")
                return@launch
            }

            val deletePurchased = dataStore.deletePurchased.first()

            val totalAmount = shoppingItem.totalToRecord

            val productsJson = Json.encodeToString(
                productsToRecord.map {
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
                supermarketName = supermarket.name,
                totalAmount = totalAmount,
                productsJson = productsJson
            )

            purchaseHistoryRepository.insertHistory(history)

            if (deletePurchased) {
                productsToRecord.forEach { product ->
                    productRepository.deleteProduct(product)
                }
            } else {
                productsToRecord.forEach { product ->
                    productRepository.updatePurchaseStatus(product.id, true)
                }
            }

            _events.send("Compra registrada: ${formatEuros(totalAmount)}")
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
    // Total de la compra (todos los productos, marcados o no).
    val total: Double
) {
    val purchasedProducts: List<Product>
        get() = products.filter { it.isPurchased }

    val pendingProducts: List<Product>
        get() = products.filter { !it.isPurchased }

    // Total real de lo marcado como comprado.
    val purchasedTotal: Double
        get() = purchasedProducts.sumOf { (it.price ?: 0.0) * it.quantity }

    // Lo que se registra al finalizar: los marcados o, si no hay ninguno, toda
    // la lista.
    val productsToRecord: List<Product>
        get() = purchasedProducts.ifEmpty { products }

    val totalToRecord: Double
        get() = productsToRecord.sumOf { (it.price ?: 0.0) * it.quantity }
}

@Serializable
data class PurchasedProductData(
    val name: String,
    val quantity: Double,
    val unit: String,
    val price: Double?
)
