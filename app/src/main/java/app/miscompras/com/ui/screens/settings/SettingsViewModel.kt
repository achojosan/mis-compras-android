package app.miscompras.com.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.ui.theme.AccessibilityConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dataStore.fontScale,
                dataStore.spacingScale,
                dataStore.highContrast
            ) { fontScale, spacingScale, highContrast ->
                SettingsUiState(
                    fontScale = fontScale,
                    spacingScale = spacingScale,
                    highContrast = highContrast
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            dataStore.setFontScale(scale)
        }
    }

    fun setSpacingScale(scale: Float) {
        viewModelScope.launch {
            dataStore.setSpacingScale(scale)
        }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setHighContrast(enabled)
        }
    }
}

data class SettingsUiState(
    val fontScale: Float = 1.0f,
    val spacingScale: Float = 1.0f,
    val highContrast: Boolean = false
) {
    val accessibilityConfig = AccessibilityConfig(
        fontScale = fontScale,
        spacingScale = spacingScale,
        highContrast = highContrast
    )
}
