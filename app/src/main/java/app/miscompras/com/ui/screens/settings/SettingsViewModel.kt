package app.miscompras.com.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.ThemeMode
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.ui.theme.AccessibilityConfig
import app.miscompras.com.ui.theme.ThemeColor
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
                combine(dataStore.fontScale, dataStore.spacingScale, dataStore.highContrast) { a, b, c ->
                    Triple(a, b, c)
                },
                combine(dataStore.deletePurchased, dataStore.themeMode, dataStore.themeColor) { a, b, c ->
                    Triple(a, b, c)
                },
                combine(dataStore.uppercaseProductNames, dataStore.showCounts) { a, b -> a to b }
            ) { (fontScale, spacingScale, highContrast), (deletePurchased, themeMode, themeColor), (uppercaseProductNames, showCounts) ->
                SettingsUiState(
                    fontScale = fontScale,
                    spacingScale = spacingScale,
                    highContrast = highContrast,
                    deletePurchased = deletePurchased,
                    themeMode = themeMode,
                    themeColor = themeColor,
                    uppercaseProductNames = uppercaseProductNames,
                    showCounts = showCounts
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

    fun setDeletePurchased(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setDeletePurchased(enabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    fun setThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            dataStore.setThemeColor(color)
        }
    }

    fun setShowCounts(show: Boolean) {
        viewModelScope.launch {
            dataStore.setShowCounts(show)
        }
    }

    fun setUppercaseProductNames(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setUppercaseProductNames(enabled)
        }
    }
}

data class SettingsUiState(
    val fontScale: Float = 1.0f,
    val spacingScale: Float = 1.0f,
    val highContrast: Boolean = false,
    val deletePurchased: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val themeColor: ThemeColor = ThemeColor.Green,
    val uppercaseProductNames: Boolean = false,
    val showCounts: Boolean = true
) {
    val accessibilityConfig = AccessibilityConfig(
        fontScale = fontScale,
        spacingScale = spacingScale,
        highContrast = highContrast
    )
}
