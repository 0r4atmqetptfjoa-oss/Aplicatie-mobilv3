package com.example.educationalapp.common

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
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- State-uri ---
    val starCount: StateFlow<Int> = userPreferencesRepository.starCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val soundEnabled: StateFlow<Boolean> = userPreferencesRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val musicEnabled: StateFlow<Boolean> = userPreferencesRepository.musicEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hardModeEnabled: StateFlow<Boolean> = userPreferencesRepository.hardModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val hapticEnabled: StateFlow<Boolean> = userPreferencesRepository.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // ✅ FIX: isPremium este sursa adevărului. hasFullVersion e doar alias.
    val isPremium: StateFlow<Boolean> = userPreferencesRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val dailyTimer: StateFlow<Int> = userPreferencesRepository.dailyTimerMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Acțiuni ---
    fun setStarCount(count: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setStarCount(count)
        }
    }
    
    // ✅ FIX: Aceasta era funcția lipsă
    fun setHasFullVersion(hasFull: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPremiumStatus(hasFull)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            userPreferencesRepository.setSoundEnabled(!soundEnabled.value)
        }
    }

    fun toggleMusic() {
        viewModelScope.launch {
            userPreferencesRepository.setMusicEnabled(!musicEnabled.value)
        }
    }
    
    fun toggleHaptics() {
        viewModelScope.launch {
            userPreferencesRepository.setHapticEnabled(!hapticEnabled.value)
        }
    }

    fun toggleHardMode() {
        viewModelScope.launch {
            userPreferencesRepository.setHardModeEnabled(!hardModeEnabled.value)
        }
    }
    
    fun setTimer(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyTimer(minutes)
        }
    }
    
    fun activatePremium() {
        viewModelScope.launch {
            userPreferencesRepository.setPremiumStatus(true)
        }
    }
}