package app.miscompras.com.data.local.datastore

import android.content.Context
import app.miscompras.com.ui.theme.ThemeColor
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val value: Int) {
    System(0),
    Light(1),
    Dark(2);

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: System
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val SPACING_SCALE = floatPreferencesKey("spacing_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val DELETE_PURCHASED = booleanPreferencesKey("delete_purchased")
        val THEME_MODE = intPreferencesKey("theme_mode")
        val THEME_COLOR = intPreferencesKey("theme_color")
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }

    val spacingScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SPACING_SCALE] ?: 1.0f
    }

    val highContrast: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGH_CONTRAST] ?: false
    }

    val deletePurchased: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DELETE_PURCHASED] ?: true
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.fromInt(preferences[PreferencesKeys.THEME_MODE] ?: 0)
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }

    suspend fun setSpacingScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPACING_SCALE] = scale
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST] = enabled
        }
    }

    suspend fun setDeletePurchased(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELETE_PURCHASED] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.value
        }
    }

    val themeColor: Flow<ThemeColor> = context.dataStore.data.map { preferences ->
        ThemeColor.fromInt(preferences[PreferencesKeys.THEME_COLOR] ?: 0)
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_COLOR] = color.value
        }
    }
}
