package com.example.educationalapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    val starCount: StateFlow<Int> =
        prefs.starCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val soundEnabled: StateFlow<Boolean> =
        prefs.soundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val musicEnabled: StateFlow<Boolean> =
        prefs.musicEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val hardModeEnabled: StateFlow<Boolean> =
        prefs.hardModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val hasFullVersion: StateFlow<Boolean> =
        prefs.hasFullVersion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setStarCount(value: Int) {
        viewModelScope.launch { prefs.setStarCount(value) }
    }

    fun toggleSound() {
        viewModelScope.launch { prefs.setSoundEnabled(!soundEnabled.value) }
    }

    fun toggleMusic() {
        viewModelScope.launch { prefs.setMusicEnabled(!musicEnabled.value) }
    }

    fun toggleHardMode() {
        viewModelScope.launch { prefs.setHardModeEnabled(!hardModeEnabled.value) }
    }

    fun setHasFullVersion(value: Boolean) {
        viewModelScope.launch { prefs.setHasFullVersion(value) }
    }
}
