package app.miscompras.com.ui.screens.loyalty

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.recognition.LoyaltyBarcode
import app.miscompras.com.recognition.RecognitionSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoyaltyCardViewModel @Inject constructor(
    private val repository: SupermarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supermarketId: Long = savedStateHandle.get<Long>("supermarketId") ?: -1L

    val supermarket: StateFlow<Supermarket?> = repository.getSupermarketById(supermarketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _editing = MutableStateFlow(false)
    /** `true` mientras se escanea o escribe una tarjeta nueva. */
    val editing: StateFlow<Boolean> = _editing.asStateFlow()

    // Dos lecturas iguales seguidas antes de fiarse de un código escaneado.
    private val reader = RecognitionSession()
    private var lastFormat: Int? = null

    fun startEditing() {
        _editing.value = true
    }

    fun cancelEditing() {
        _editing.value = false
    }

    fun onFormat(format: Int) {
        lastFormat = format
    }

    fun onScanFrame(code: String?) {
        val confirmed = reader.onBarcodeRead(code) ?: return
        save(confirmed, lastFormat)
    }

    /** Guarda el número escrito a mano; el formato se deduce de su forma. */
    fun saveTyped(code: String) {
        val clean = code.trim()
        if (clean.isNotEmpty()) save(clean, LoyaltyBarcode.guessFormat(clean))
    }

    fun delete() {
        val current = supermarket.value ?: return
        viewModelScope.launch {
            repository.updateSupermarket(current.copy(loyaltyCode = null, loyaltyFormat = null))
            _editing.value = false
        }
    }

    private fun save(code: String, format: Int?) {
        val current = supermarket.value ?: return
        viewModelScope.launch {
            repository.updateSupermarket(current.copy(loyaltyCode = code, loyaltyFormat = format))
            _editing.value = false
        }
    }
}
