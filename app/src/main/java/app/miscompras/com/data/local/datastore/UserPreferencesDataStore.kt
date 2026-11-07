package app.miscompras.com.data.local.datastore

import android.content.Context
import app.miscompras.com.ui.theme.ThemeColor
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val UPPERCASE_PRODUCT_NAMES = booleanPreferencesKey("uppercase_product_names")
        val HIDE_OPEN_APP_DIALOG = booleanPreferencesKey("hide_open_app_dialog")
        val SHOW_COUNTS = booleanPreferencesKey("show_counts")
        val UPDATE_CHECKS = booleanPreferencesKey("update_checks")
        val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
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

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    /** Guarda los nombres de los productos nuevos en mayúsculas. Desactivado por defecto. */
    val uppercaseProductNames: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UPPERCASE_PRODUCT_NAMES] ?: false
    }

    /** Si el usuario decidió no volver a ver el aviso al abrir la app de una tienda. */
    val hideOpenAppDialog: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIDE_OPEN_APP_DIALOG] ?: false
    }

    /** Muestra los recuentos (tiendas, productos, pendientes) en la pantalla principal. */
    val showCounts: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_COUNTS] ?: true
    }

    suspend fun setShowCounts(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COUNTS] = show
        }
    }

    /** Avisar de versiones nuevas (una consulta al día a GitHub). Activado por defecto. */
    val updateChecksEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UPDATE_CHECKS] ?: true
    }

    suspend fun setUpdateChecksEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.UPDATE_CHECKS] = enabled }
    }

    /** Marca de tiempo (ms) de la última comprobación que NO dejó un aviso pendiente. */
    val lastUpdateCheck: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_UPDATE_CHECK] ?: 0L
    }

    suspend fun setLastUpdateCheck(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_UPDATE_CHECK] = timestamp }
    }

    suspend fun setHideOpenAppDialog(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_OPEN_APP_DIALOG] = hide
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setUppercaseProductNames(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPPERCASE_PRODUCT_NAMES] = enabled
        }
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
