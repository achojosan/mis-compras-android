package app.miscompras.com.ui.screens.recognition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.miscompras.com.data.repository.KnownItemRepository
import app.miscompras.com.domain.model.KnownItemWithRefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    repository: KnownItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: -1L
    val item: Flow<KnownItemWithRefs?> = repository.observeById(itemId)
}
