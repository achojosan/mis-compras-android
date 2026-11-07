package app.miscompras.com.ui.screens.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.KnownItemRepository
import app.miscompras.com.domain.model.KnownItemWithRefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnownItemsViewModel @Inject constructor(
    private val repository: KnownItemRepository
) : ViewModel() {
    val items: StateFlow<List<KnownItemWithRefs>?> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteItem(id) }
    }
}
