package app.miscompras.com.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updater: AppUpdater,
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {
    val state: StateFlow<UpdateState> = updater.state
    val messages: SharedFlow<String> = updater.messages
    val enabled: StateFlow<Boolean> = dataStore.updateChecksEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun checkOnLaunch() = updater.checkOnLaunch()
    fun checkNow() = updater.checkNow()
    fun startUpdate(release: ReleaseInfo) = updater.startUpdate(release)
    fun later() = updater.later()
    fun cancel() = updater.cancel()
    fun onResume() = updater.onAppResumed()
    fun confirmLaunched() = updater.confirmLaunched()
    fun dismissFailure() = updater.dismissFailure()
    fun setEnabled(value: Boolean) {
        viewModelScope.launch { dataStore.setUpdateChecksEnabled(value) }
    }
}
