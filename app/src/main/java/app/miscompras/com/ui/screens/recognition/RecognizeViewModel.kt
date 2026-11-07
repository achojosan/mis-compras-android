package app.miscompras.com.ui.screens.recognition

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.recognition.BarcodeResolution
import app.miscompras.com.recognition.Haptics
import app.miscompras.com.recognition.ItemRecognizer
import app.miscompras.com.recognition.RecognitionConfig
import app.miscompras.com.recognition.RecognitionSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecognizeUiState(
    val status: String = "Apunta a la etiqueta del artículo",
    val notRecognized: Boolean = false
)

@HiltViewModel
class RecognizeViewModel @Inject constructor(
    private val recognizer: ItemRecognizer,
    private val haptics: Haptics
) : ViewModel() {

    private val session = RecognitionSession()
    private var finished = false
    private var processing = false
    private var timeoutJob: Job? = null

    private val _state = MutableStateFlow(RecognizeUiState())
    val state: StateFlow<RecognizeUiState> = _state.asStateFlow()

    private val _recognized = Channel<Long>(Channel.BUFFERED)
    /** Se emite UNA vez por artículo reconocido (la pantalla navega a su ficha). */
    val recognized = _recognized.receiveAsFlow()

    /** Reinicia el escaneo (al abrir la pantalla o volver de una ficha). */
    fun restart() {
        session.reset()
        finished = false
        processing = false
        _state.value = RecognizeUiState()
        viewModelScope.launch { recognizer.loadReferences() }
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(RecognitionConfig.TIMEOUT_MS)
            if (!finished) {
                haptics.notRecognized()
                _state.value = RecognizeUiState(
                    status = "No lo reconozco. Prueba a acercarte o a registrarlo.",
                    notRecognized = true
                )
            }
        }
    }

    /** Detiene el escaneo y el temporizador al salir de la pantalla. */
    fun pause() {
        finished = true
        timeoutJob?.cancel()
    }

    fun onFrame(barcode: String?, crop: Bitmap?) {
        if (finished) return
        viewModelScope.launch {
            if (finished) return@launch
            val confirmed = session.onBarcodeRead(barcode)
            if (confirmed != null && session.restriction == null) {
                when (val r = session.onBarcodeCandidates(recognizer.itemsForBarcode(confirmed))) {
                    is BarcodeResolution.Single -> return@launch found(r.itemId)
                    is BarcodeResolution.Ambiguous ->
                        _state.value = _state.value.copy(status = "Hay varios iguales. Acerca la pegatina…")
                    BarcodeResolution.None -> Unit
                }
            }
            if (crop != null && !processing) {
                processing = true
                try {
                    val id = session.onVisualRanking(recognizer.rank(crop))
                    if (id != null) found(id)
                } finally {
                    processing = false
                }
            }
        }
    }

    private fun found(itemId: Long) {
        if (finished) return
        finished = true
        timeoutJob?.cancel()
        haptics.recognized()
        _recognized.trySend(itemId)
    }
}
