package com.example.educationalapp.features.wowgames

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EggGameState(
    val currentCreatureIndex: Int = 0,
    val stage: EggStage = EggStage.INTACT,
    val tapsInStage: Int = 0,
    val isCreatureHappy: Boolean = false,
    val hatchTrigger: Int = 0, // Counter pentru a declanșa efectul vizual
    val showNextButton: Boolean = false // Apare cu întârziere
)

@HiltViewModel
class EggGameViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val audio = EggAudioManager(application.applicationContext)
    private val _uiState = MutableStateFlow(EggGameState())
    val uiState: StateFlow<EggGameState> = _uiState

    private val tapsToCrack1 = 3
    private val tapsToCrack2 = 4 
    private val tapsToHatch = 5 

    init {
        audio.startMusic()
        viewModelScope.launch {
            delay(500)
            audio.playVoice("vox_egg_intro")
        }
    }

    fun onInteraction() {
        val state = _uiState.value
        
        if (state.stage == EggStage.HATCHED) {
            interactWithCreature()
            return
        }

        val newTaps = state.tapsInStage + 1
        var nextStage = state.stage
        var resetTaps = false

        when (state.stage) {
            EggStage.INTACT -> {
                audio.playSFX("tap")
                if (newTaps == 1) audio.playVoice("vox_egg_tap")
                if (newTaps >= tapsToCrack1) {
                    nextStage = EggStage.CRACK_1
                    resetTaps = true
                    audio.playSFX("crack_small")
                    audio.playVoice("vox_egg_crack")
                }
            }
            EggStage.CRACK_1 -> {
                audio.playSFX("tap")
                if (newTaps >= tapsToCrack2) {
                    nextStage = EggStage.CRACK_2
                    resetTaps = true
                    audio.playSFX("crack_big")
                }
            }
            EggStage.CRACK_2 -> {
                audio.playSFX("tap")
                if (newTaps >= tapsToHatch) {
                    nextStage = EggStage.HATCHED
                    resetTaps = true
                    hatchEgg()
                }
            }
            else -> {}
        }

        _uiState.value = state.copy(
            stage = nextStage,
            tapsInStage = if (resetTaps) 0 else newTaps
        )
    }

    private fun hatchEgg() {
        audio.playSFX("hatch")
        audio.playVoice("vox_egg_hatch")
        
        _uiState.value = _uiState.value.copy(
            isCreatureHappy = true,
            hatchTrigger = _uiState.value.hatchTrigger + 1 // Declanșează particulele
        )
        
        viewModelScope.launch {
            // Puiul e fericit 2 secunde
            delay(2000)
            _uiState.value = _uiState.value.copy(isCreatureHappy = false)
            
            // Abia acum arătăm butonul NEXT ca să nu fie apăsat din greșeală
            _uiState.value = _uiState.value.copy(showNextButton = true)
        }
    }

    private fun interactWithCreature() {
        val creature = EggAssets.creatures[_uiState.value.currentCreatureIndex]
        audio.playSFX("happy_${creature.id}")
        
        if (Math.random() < 0.4) audio.playVoice("vox_interact")

        _uiState.value = _uiState.value.copy(isCreatureHappy = true)
        viewModelScope.launch {
            delay(1200)
            _uiState.value = _uiState.value.copy(isCreatureHappy = false)
        }
    }

    fun nextLevel() {
        val nextIdx = (_uiState.value.currentCreatureIndex + 1) % EggAssets.creatures.size
        _uiState.value = EggGameState(
            currentCreatureIndex = nextIdx,
            stage = EggStage.INTACT,
            tapsInStage = 0,
            isCreatureHappy = false,
            showNextButton = false
        )
        
        viewModelScope.launch {
            audio.playVoice("vox_egg_intro") 
        }
    }

    override fun onCleared() {
        super.onCleared()
        audio.release()
    }
}