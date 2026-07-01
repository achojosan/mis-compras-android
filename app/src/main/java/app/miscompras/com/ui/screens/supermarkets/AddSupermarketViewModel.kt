package app.miscompras.com.ui.screens.supermarkets

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.util.DefaultSupermarkets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSupermarketViewModel @Inject constructor(
    private val repository: SupermarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    private val _uiState = MutableStateFlow(AddSupermarketUiState())
    val uiState: StateFlow<AddSupermarketUiState> = _uiState.asStateFlow()

    private val _defaultSupermarkets = MutableStateFlow(DefaultSupermarkets.list)
    val defaultSupermarkets: StateFlow<List<app.miscompras.com.util.DefaultSupermarket>> = _defaultSupermarkets

    init {
        if (supermarketId != -1L) {
            loadSupermarket()
        }
    }

    private fun loadSupermarket() {
        viewModelScope.launch {
            repository.getSupermarketById(supermarketId).collect { supermarket ->
                supermarket?.let {
                    _uiState.value = AddSupermarketUiState(
                        name = it.name,
                        logoUri = it.logoPath,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onLogoUriChange(uri: String?) {
        _uiState.value = _uiState.value.copy(logoUri = uri)
    }

    fun selectDefaultSupermarket(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            logoUri = null
        )
    }

    fun saveSupermarket(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "El nombre es obligatorio")
            return
        }

        viewModelScope.launch {
            val supermarket = Supermarket(
                id = if (supermarketId != -1L) supermarketId else 0,
                name = state.name.trim(),
                logoPath = state.logoUri,
                isDefault = DefaultSupermarkets.getLogoResId(state.name) != null
            )

            if (state.isEditing) {
                repository.updateSupermarket(supermarket)
            } else {
                repository.insertSupermarket(supermarket)
            }
            onSuccess()
        }
    }
}

data class AddSupermarketUiState(
    val name: String = "",
    val logoUri: String? = null,
    val nameError: String? = null,
    val isEditing: Boolean = false
)
