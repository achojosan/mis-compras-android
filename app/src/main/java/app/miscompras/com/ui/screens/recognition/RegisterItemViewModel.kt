package app.miscompras.com.ui.screens.recognition

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.remote.off.OpenFoodFactsClient
import app.miscompras.com.data.repository.KnownItemRepository
import app.miscompras.com.domain.model.KnownItem
import app.miscompras.com.recognition.EmbeddingMath
import app.miscompras.com.recognition.ItemRecognizer
import app.miscompras.com.recognition.RecognitionConfig
import app.miscompras.com.recognition.RecognitionSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class StagedPhoto(val bitmap: Bitmap, val embedding: FloatArray)

data class RegisterUiState(
    val name: String = "",
    val description: String = "",
    val barcode: String = "",
    val color: Int? = null,
    val icon: String? = null,
    val photos: List<StagedPhoto> = emptyList(),
    val existingPhotos: Int = 0,
    val isEditing: Boolean = false,
    val error: String? = null,
    val saving: Boolean = false,
    /** Aviso cuando se han rellenado nombre/notas desde Open Food Facts. */
    val suggestion: String? = null
)

@HiltViewModel
class RegisterItemViewModel @Inject constructor(
    private val repository: KnownItemRepository,
    private val recognizer: ItemRecognizer,
    private val openFoodFacts: OpenFoodFactsClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: -1L
    private var original: KnownItem? = null
    private var latestCrop: Bitmap? = null
    private val barcodeReader = RecognitionSession()
    private val lookedUp = mutableSetOf<String>()

    private val _state = MutableStateFlow(RegisterUiState(isEditing = itemId > 0))
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    init {
        if (itemId > 0) viewModelScope.launch {
            repository.getById(itemId)?.let { data ->
                original = data.item
                _state.update {
                    it.copy(
                        name = data.item.name,
                        description = data.item.description,
                        barcode = data.item.barcode.orEmpty(),
                        color = data.item.color,
                        icon = data.item.icon,
                        existingPhotos = data.references.size
                    )
                }
            }
        }
    }

    fun onColor(id: Int?) = _state.update { it.copy(color = id) }
    fun onIcon(key: String?) = _state.update { it.copy(icon = key) }

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onBarcode(v: String) {
        val digits = v.filter(Char::isDigit)
        _state.update { it.copy(barcode = digits) }
        if (digits.length == 8 || digits.length == 12 || digits.length == 13) lookupProduct(digits)
    }

    /**
     * Rellena nombre y notas con Open Food Facts si siguen vacíos. Es la única llamada de red de este
     * flujo y solo envía el código de barras. Sin conexión o sin datos, no pasa nada.
     */
    private fun lookupProduct(code: String) {
        if (!lookedUp.add(code)) return
        viewModelScope.launch {
            val product = runCatching { openFoodFacts.lookup(code) }.getOrNull() ?: return@launch
            _state.update {
                if (it.name.isNotBlank() || it.barcode != code) it
                else it.copy(
                    name = product.name,
                    description = it.description.ifBlank { product.notes() },
                    suggestion = "Nombre y notas sugeridos por Open Food Facts. Revísalos antes de guardar."
                )
            }
        }
    }

    fun onFrame(barcode: String?, crop: Bitmap?) {
        if (crop != null) latestCrop = crop
        val confirmed = barcodeReader.onBarcodeRead(barcode)
        if (confirmed != null && _state.value.barcode != confirmed) {
            _state.update { it.copy(barcode = confirmed) }
            lookupProduct(confirmed)
        }
    }

    /** Usa el último recorte del flujo de cámara (mismo preprocesado que el reconocimiento). */
    fun takePhoto() {
        val crop = latestCrop ?: return
        if (_state.value.photos.size >= MAX_PHOTOS) return
        viewModelScope.launch {
            val embedding = recognizer.embed(crop)
            _state.update { it.copy(photos = it.photos + StagedPhoto(crop, embedding), error = null) }
        }
    }

    fun removePhoto(index: Int) =
        _state.update { it.copy(photos = it.photos.filterIndexed { i, _ -> i != index }) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Escribe un nombre") }
            return
        }
        if (s.photos.isEmpty() && s.barcode.isBlank() && s.existingPhotos == 0) {
            _state.update { it.copy(error = "Haz al menos una foto o escanea el código de barras") }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val base = original ?: KnownItem(name = s.name.trim())
            val id = repository.saveItem(
                base.copy(
                    name = s.name.trim(),
                    description = s.description.trim(),
                    barcode = s.barcode.ifBlank { null },
                    color = s.color,
                    icon = s.icon
                )
            )
            s.photos.forEach { repository.addReference(id, it.bitmap, EmbeddingMath.toBytes(it.embedding)) }
            onDone()
        }
    }

    private companion object {
        const val MAX_PHOTOS = 8
    }
}
