package app.miscompras.com.ui.screens.supermarkets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Supermarket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupermarketListViewModel @Inject constructor(
    private val repository: SupermarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SupermarketListUiState>(SupermarketListUiState.Loading)
    val uiState: StateFlow<SupermarketListUiState> = _uiState.asStateFlow()

    init {
        loadSupermarkets()
    }

    private fun loadSupermarkets() {
        viewModelScope.launch {
            repository.getAllSupermarkets().collect { supermarkets ->
                _uiState.value = if (supermarkets.isEmpty()) {
                    SupermarketListUiState.Empty
                } else {
                    SupermarketListUiState.Success(supermarkets)
                }
            }
        }
    }

    fun deleteSupermarket(supermarket: Supermarket) {
        viewModelScope.launch {
            repository.deleteSupermarket(supermarket)
        }
    }
}

sealed class SupermarketListUiState {
    data object Loading : SupermarketListUiState()
    data object Empty : SupermarketListUiState()
    data class Success(val supermarkets: List<Supermarket>) : SupermarketListUiState()
}
