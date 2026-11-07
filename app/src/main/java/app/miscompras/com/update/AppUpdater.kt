package app.miscompras.com.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import app.miscompras.com.BuildConfig
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val release: ReleaseInfo) : UpdateState
    /** Falta el permiso «instalar apps desconocidas» para Mis Compras. */
    data class NeedsPermission(val release: ReleaseInfo) : UpdateState
    data class Downloading(val release: ReleaseInfo, val progress: Float) : UpdateState
    /** El sistema pide confirmar la instalación; la pantalla lanza [confirmIntent] cuando la app está visible. */
    data class ReadyToConfirm(val confirmIntent: Intent) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/**
 * Comprueba si hay una release nueva en GitHub y la instala con [PackageInstaller].
 *
 * - Solo se aceptan APK de las releases de este repositorio y, si la release publica su SHA-256, se
 *   verifica antes de instalar. Además Android exige que la firma coincida con la de la app instalada.
 * - El primer update siempre pide confirmación; los siguientes pueden hacerse sin ella en Android 12+
 *   cuando esta app es el instalador de la anterior (no se promete).
 */
@Singleton
class AppUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
    private val json: Json,
    private val dataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var activeSessionId: Int? = null

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /** Mensajes de una sola vez para las comprobaciones manuales (las automáticas nunca molestan). */
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Al abrir la app: como mucho una vez cada 24 h y solo si el usuario no lo ha desactivado. */
    fun checkOnLaunch() {
        scope.launch {
            if (!dataStore.updateChecksEnabled.first()) return@launch
            val last = dataStore.lastUpdateCheck.first()
            if (System.currentTimeMillis() - last < CHECK_INTERVAL_MS) return@launch
            check(manual = false)
        }
    }

    fun checkNow() {
        scope.launch { check(manual = true) }
    }

    private suspend fun check(manual: Boolean) {
        if (job?.isActive == true || _state.value !is UpdateState.Idle && !manual) return
        if (manual) _state.value = UpdateState.Checking
        val release = try {
            fetchLatest()
        } catch (e: Exception) {
            // 403/429 (límite de GitHub), sin red, etc.: en automático, en silencio.
            if (manual) {
                _messages.tryEmit("No se pudo comprobar ahora. Revisa tu conexión e inténtalo más tarde.")
            }
            if (_state.value is UpdateState.Checking) _state.value = UpdateState.Idle
            return
        }
        if (release != null && UpdateLogic.isNewer(BuildConfig.VERSION_NAME, release.version)) {
            _state.value = UpdateState.Available(release)
        } else {
            dataStore.setLastUpdateCheck(System.currentTimeMillis())
            _state.value = UpdateState.Idle
            if (manual) _messages.tryEmit("Ya tienes la última versión (${BuildConfig.VERSION_NAME}).")
        }
    }

    private fun fetchLatest(): ReleaseInfo? {
        val request = Request.Builder().url(UpdateConfig.LATEST_API)
            .header("Accept", "application/vnd.github+json").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("GitHub HTTP ${response.code}")
            return UpdateLogic.parseRelease(response.body?.string() ?: throw IOException("vacío"), json)
        }
    }

    /** «Más tarde»: se olvida el aviso y no se vuelve a preguntar hasta dentro de 24 h. */
    fun later() {
        scope.launch { dataStore.setLastUpdateCheck(System.currentTimeMillis()) }
        _state.value = UpdateState.Idle
    }

    fun startUpdate(release: ReleaseInfo) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            _state.value = UpdateState.NeedsPermission(release)
            return
        }
        if (job?.isActive == true) return
        job = scope.launch { downloadAndInstall(release) }
    }

    /** Al volver a la app: si el usuario acaba de conceder el permiso, se continúa sola. */
    fun onAppResumed() {
        val s = _state.value
        if (s is UpdateState.NeedsPermission && context.packageManager.canRequestPackageInstalls()) startUpdate(s.release)
    }

    fun cancel() {
        job?.cancel()
        abandonActiveSession()
        _state.value = UpdateState.Idle
    }

    fun dismissFailure() {
        if (_state.value is UpdateState.Failed) _state.value = UpdateState.Idle
    }

    fun confirmLaunched() {
        if (_state.value is UpdateState.ReadyToConfirm) _state.value = UpdateState.Idle
    }

    private suspend fun downloadAndInstall(release: ReleaseInfo) {
        val installer = context.packageManager.packageInstaller
        installer.mySessions.forEach { runCatching { installer.abandonSession(it.sessionId) } }
        _state.value = UpdateState.Downloading(release, 0f)

        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            if (release.apkSize > 0) setSize(release.apkSize)
            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val sessionId = installer.createSession(params)
        activeSessionId = sessionId
        val session = installer.openSession(sessionId)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            client.newCall(Request.Builder().url(release.apkUrl).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Descarga: HTTP ${response.code}")
                val body = response.body ?: throw IOException("Descarga vacía")
                val total = if (release.apkSize > 0) release.apkSize else body.contentLength()
                session.openWrite("MisCompras.apk", 0, if (release.apkSize > 0) release.apkSize else -1).use { out ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    var lastPercent = -1
                    body.byteStream().use { input ->
                        while (true) {
                            coroutineContext.ensureActive()
                            val n = input.read(buffer)
                            if (n < 0) break
                            out.write(buffer, 0, n)
                            digest.update(buffer, 0, n)
                            done += n
                            val percent = if (total > 0) (done * 100 / total).toInt() else 0
                            if (percent != lastPercent) {
                                lastPercent = percent
                                _state.value = UpdateState.Downloading(release, (percent / 100f).coerceIn(0f, 1f))
                            }
                        }
                    }
                    session.fsync(out)
                }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (release.sha256 != null && !actual.equals(release.sha256, ignoreCase = true)) {
                throw IOException("El archivo descargado no coincide con el publicado (SHA-256). No se instalará.")
            }
            // FLAG_MUTABLE: el instalador debe poder añadir el estado y la intención de confirmación.
            val pending = PendingIntent.getBroadcast(
                context, sessionId,
                Intent(context, UpdateInstallReceiver::class.java).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            session.commit(pending.intentSender)
            _state.value = UpdateState.Idle // el resultado llega al receptor
        } catch (e: Exception) {
            runCatching { session.abandon() }
            activeSessionId = null
            if (e is kotlinx.coroutines.CancellationException) throw e
            _state.value = UpdateState.Failed(e.message ?: "Error desconocido")
        } finally {
            runCatching { session.close() }
        }
    }

    private fun abandonActiveSession() {
        activeSessionId?.let { id -> runCatching { context.packageManager.packageInstaller.abandonSession(id) } }
        activeSessionId = null
    }

    /** Lo llama [UpdateInstallReceiver] con el resultado del instalador del sistema. */
    internal fun onInstallStatus(status: Int, message: String?, confirmIntent: Intent?) {
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION ->
                if (confirmIntent != null) _state.value = UpdateState.ReadyToConfirm(confirmIntent)
            PackageInstaller.STATUS_SUCCESS, PackageInstaller.STATUS_FAILURE_ABORTED -> _state.value = UpdateState.Idle
            else -> _state.value = UpdateState.Failed(message ?: "El sistema no pudo instalar la actualización (código $status).")
        }
    }

    companion object {
        private const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000
    }
}
