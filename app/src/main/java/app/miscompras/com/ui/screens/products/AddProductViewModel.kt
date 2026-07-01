package app.miscompras.com.ui.screens.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    private val _commonUnits = MutableStateFlow(
        listOf("unidades", "kg", "g", "L", "ml", "docena", "pack")
    )
    val commonUnits: StateFlow<List<String>> = _commonUnits

    init {
        loadProductNames()
    }

    private fun loadProductNames() {
        viewModelScope.launch {
            productRepository.getAllProductNames().collect { names ->
                _suggestions.value = names
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun onQuantityChange(quantity: String) {
        _uiState.value = _uiState.value.copy(quantity = quantity, quantityError = null)
    }

    fun onUnitChange(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(price = price, priceError = null)
    }

    fun saveProduct(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "El nombre es obligatorio")
            return
        }

        val quantity = state.quantity.toDoubleOrNull()
        if (quantity == null || quantity <= 0) {
            _uiState.value = state.copy(quantityError = "Introduce una cantidad válida")
            return
        }

        val price = state.price.toDoubleOrNull()
        if (price == null || price < 0) {
            _uiState.value = state.copy(priceError = "Introduce un precio válido")
            return
        }

        viewModelScope.launch {
            val product = Product(
                name = state.name.trim(),
                quantity = quantity,
                unit = state.unit,
                price = price,
                supermarketId = supermarketId
            )
            productRepository.insertProduct(product)
            onSuccess()
        }
    }
}

data class AddProductUiState(
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "unidades",
    val price: String = "",
    val nameError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null
)
