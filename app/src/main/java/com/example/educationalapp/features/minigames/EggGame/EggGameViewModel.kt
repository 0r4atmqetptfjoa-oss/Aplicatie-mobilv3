package com.example.educationalapp.features.wowgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class EggGameUiState(
    val stage: EggStage = EggStage.INTACT,
    val taps: Int = 0,
    val isEggHatched: Boolean = false,
    val currentCreatureIndex: Int = 0,
    val isMusicOn: Boolean = true,
    val currentMood: CreatureMood = CreatureMood.NORMAL,
    val hatchTrigger: Int = 0,
    val showNextButton: Boolean = false
) {
    val isCreatureHappy: Boolean get() = currentMood == CreatureMood.HAPPY
}

enum class CreatureMood { NORMAL, HAPPY, SAD }

@HiltViewModel
class EggGameViewModel @Inject constructor(
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EggGameUiState(currentCreatureIndex = Random.nextInt(EggAssets.creatures.size)))
    val uiState: StateFlow<EggGameUiState> = _uiState

    init {
        // Music
        val bgm = soundManager.rawResId("bgm_egg_game").takeIf { it != 0 } ?: R.raw.math_bg_music
        soundManager.enterGameMode(gameMusicResId = bgm, startVolume = 0.25f)

        // Preload SFX
        viewModelScope.launch(Dispatchers.IO) {
            val base = listOf(
                "sfx_egg_tap",
                "sfx_egg_crack_small",
                "sfx_egg_crack_big",
                "sfx_egg_hatch"
            )
            val happy = EggAssets.creatures.map { "happy_${it.id}" }
            soundManager.loadSoundsByName(base + happy)
        }

        // Intro voice
        viewModelScope.launch {
            delay(800)
            soundManager.playVoiceByName("vox_egg_intro")
        }
    }

    fun toggleMusic() {
        val new = !_uiState.value.isMusicOn
        _uiState.update { it.copy(isMusicOn = new) }
        if (new) {
            soundManager.resumeGameMusic()
        } else {
            soundManager.pauseGameMusic()
        }
    }

    fun onInteraction() {
        onEggTapped()
    }

    fun onEggTapped() {
        if (_uiState.value.isEggHatched) {
            // If already hatched, maybe play happy sound on tap
            val creature = EggAssets.creatures[_uiState.value.currentCreatureIndex]
            soundManager.playSoundByName("happy_${creature.id}", duckMusic = false)
            _uiState.update { it.copy(currentMood = CreatureMood.HAPPY) }
            viewModelScope.launch {
                delay(1500)
                _uiState.update { it.copy(currentMood = CreatureMood.NORMAL) }
            }
            return
        }

        _uiState.update { it.copy(taps = it.taps + 1) }
        soundManager.playSoundByName("sfx_egg_tap", duckMusic = false)

        when (_uiState.value.stage) {
            EggStage.INTACT -> {
                if (_uiState.value.taps >= 5) {
                    _uiState.update { it.copy(stage = EggStage.CRACK_1, taps = 0) }
                    soundManager.playSoundByName("sfx_egg_crack_small", duckMusic = false)
                    soundManager.playVoiceByName("vox_egg_crack1")
                }
            }
            EggStage.CRACK_1 -> {
                if (_uiState.value.taps >= 7) {
                    _uiState.update { it.copy(stage = EggStage.CRACK_2, taps = 0) }
                    soundManager.playSoundByName("sfx_egg_crack_big", duckMusic = false)
                    soundManager.playVoiceByName("vox_egg_crack2")
                }
            }
            EggStage.CRACK_2 -> {
                if (_uiState.value.taps >= 9) {
                    hatchEgg()
                }
            }
            EggStage.HATCHED -> {}
        }
    }

    private fun hatchEgg() {
        _uiState.update { 
            it.copy(
                isEggHatched = true, 
                stage = EggStage.HATCHED,
                currentMood = CreatureMood.HAPPY,
                hatchTrigger = it.hatchTrigger + 1,
                showNextButton = true
            ) 
        }
        val creature = EggAssets.creatures[_uiState.value.currentCreatureIndex]
        soundManager.playSoundByName("sfx_egg_hatch", duckMusic = false)
        soundManager.playVoiceByName("vox_egg_hatched")

        // Also play the creature's "happy" jingle.
        soundManager.playSoundByName("happy_${creature.id}", duckMusic = false)

        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(currentMood = CreatureMood.NORMAL) }
        }
    }

    fun nextLevel() {
        resetGame()
    }

    fun resetGame() {
        _uiState.update { 
            EggGameUiState(
                isMusicOn = it.isMusicOn,
                currentCreatureIndex = Random.nextInt(EggAssets.creatures.size)
            )
        }
        viewModelScope.launch {
            delay(300)
            soundManager.playVoiceByName("vox_egg_intro")
        }
    }

    override fun onCleared() {
        soundManager.exitGameMode()
        super.onCleared()
    }
}
