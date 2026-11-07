package app.miscompras.com.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UpdaterEntryPoint {
    fun updater(): AppUpdater
}

/**
 * Recibe el resultado de la sesión de instalación. NO lanza la pantalla de confirmación del sistema:
 * desde segundo plano Android 14+ lo bloquea sin avisar, así que se guarda en [AppUpdater] y la
 * pantalla principal la abre cuando la app está visible.
 */
class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val updater = EntryPointAccessors.fromApplication(context.applicationContext, UpdaterEntryPoint::class.java).updater()
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val confirm: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }
        updater.onInstallStatus(status, message, confirm)
    }
}
