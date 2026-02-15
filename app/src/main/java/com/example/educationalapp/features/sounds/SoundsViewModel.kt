package com.example.educationalapp.features.sounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SoundsUiState(
    val categories: List<SoundCategory> = emptyList(),
    val selectedCategoryId: String? = null
) {
    val selectedCategory: SoundCategory? = categories.firstOrNull { it.id == selectedCategoryId }
    val isInMenu: Boolean get() = selectedCategoryId == null
}

sealed interface SoundsEvent {
    data class PlayUiSfx(val sfx: UiSfx) : SoundsEvent
}

enum class UiSfx {
    CATEGORY_SELECT,
    BACK
}

class SoundsViewModel(
    private val repo: SoundCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SoundsUiState(categories = repo.categories)
    )
    val uiState: StateFlow<SoundsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SoundsEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    fun onCategoryClick(category: SoundCategory) {
        _uiState.update { it.copy(selectedCategoryId = category.id) }
        // no UI SFX on category select (only play on animal click)
    }

    fun onBack() {
        _uiState.update { it.copy(selectedCategoryId = null) }
        _events.tryEmit(SoundsEvent.PlayUiSfx(UiSfx.BACK))
    }
}

class SoundsViewModelFactory(
    private val repo: SoundCatalogRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SoundsViewModel::class.java)) {
            return SoundsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
