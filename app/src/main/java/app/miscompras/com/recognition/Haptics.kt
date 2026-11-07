package app.miscompras.com.recognition

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Haptics @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator by lazy {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    }

    /** Vibración corta: artículo reconocido. */
    fun recognized() {
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }

    /** Doble vibración: no se ha reconocido nada. */
    fun notRecognized() {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 90, 80), -1))
    }
}
