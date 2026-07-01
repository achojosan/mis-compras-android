package app.miscompras.com.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val SPACING_SCALE = floatPreferencesKey("spacing_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
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
}
