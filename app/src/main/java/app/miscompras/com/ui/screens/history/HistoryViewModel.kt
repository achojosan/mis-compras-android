package app.miscompras.com.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.PurchaseHistoryRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.screens.shopping.PurchasedProductData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val supermarketRepository: SupermarketRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryListUiState>(HistoryListUiState.Loading)
    val uiState: StateFlow<HistoryListUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                purchaseHistoryRepository.getAllHistory(),
                supermarketRepository.getAllSupermarkets()
            ) { historyList, supermarkets ->
                historyList.map { history ->
                    val supermarket = supermarkets.find { it.id == history.supermarketId }
                    HistoryItem(
                        history = history,
                        supermarket = supermarket
                    )
                }
            }.collect { historyItems ->
                _uiState.value = if (historyItems.isEmpty()) {
                    HistoryListUiState.Empty
                } else {
                    HistoryListUiState.Success(historyItems)
                }
            }
        }
    }

    fun repeatPurchase(historyItem: HistoryItem) {
        viewModelScope.launch {
            val supermarketId = historyItem.history.supermarketId
            val products = try {
                Json.decodeFromString<List<PurchasedProductData>>(historyItem.history.productsJson)
            } catch (e: Exception) {
                emptyList()
            }
            products.forEach { data ->
                productRepository.insertProduct(
                    Product(
                        name = data.name,
                        quantity = data.quantity,
                        unit = data.unit,
                        price = data.price,
                        supermarketId = supermarketId
                    )
                )
            }
        }
    }
}

sealed class HistoryListUiState {
    data object Loading : HistoryListUiState()
    data object Empty : HistoryListUiState()
    data class Success(val historyItems: List<HistoryItem>) : HistoryListUiState()
}

data class HistoryItem(
    val history: PurchaseHistory,
    val supermarket: Supermarket?
)

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val supermarketRepository: SupermarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val historyId: Long = savedStateHandle.get<Long>("historyId") ?: -1L

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadHistoryDetail()
    }

    private fun loadHistoryDetail() {
        viewModelScope.launch {
            combine(
                purchaseHistoryRepository.getHistoryById(historyId),
                supermarketRepository.getAllSupermarkets()
            ) { history, supermarkets ->
                if (history != null) {
                    val supermarket = supermarkets.find { it.id == history.supermarketId }
                    val products = try {
                        Json.decodeFromString<List<PurchasedProductData>>(history.productsJson)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    HistoryDetailUiState.Success(
                        history = history,
                        supermarket = supermarket,
                        products = products
                    )
                } else {
                    HistoryDetailUiState.Error("Compra no encontrada")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed class HistoryDetailUiState {
    data object Loading : HistoryDetailUiState()
    data class Error(val message: String) : HistoryDetailUiState()
    data class Success(
        val history: PurchaseHistory,
        val supermarket: Supermarket?,
        val products: List<PurchasedProductData>
    ) : HistoryDetailUiState()
}
