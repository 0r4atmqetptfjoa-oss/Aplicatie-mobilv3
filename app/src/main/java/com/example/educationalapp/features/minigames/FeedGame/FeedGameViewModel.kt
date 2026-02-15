package com.example.educationalapp.features.wowgames

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val monsterState: MonsterState = MonsterState.IDLE,
    val isFlying: Boolean = false,
    val flyingFoodRes: Int? = null,
    val flyStart: Offset = Offset.Zero,
    val score: Int = 0
)

@HiltViewModel
class FeedGameViewModel @Inject constructor(
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        // Game music (fallback to a generic track if needed)
        val gameMusic = soundManager.rawResId("bgm_monster_picnic").takeIf { it != 0 }
            ?: soundManager.rawResId("math_bg_music").takeIf { it != 0 }

        soundManager.enterGameMode(gameMusic, autoPlay = true, startVolume = 0.32f)

        // Preload commonly used SFX
        viewModelScope.launch {
            soundManager.loadSounds(
                listOf(
                    R.raw.sfx_whoosh,
                    R.raw.sfx_eat_funny,
                    R.raw.sfx_burp,
                    R.raw.sfx_yuck
                )
            )
        }

        // Intro: "Mi-e foame!"
        viewModelScope.launch {
            delay(800)
            soundManager.playVoiceByName("vox_feed_intro")
        }
    }

    fun onFoodSelected(food: FoodItem, startPos: Offset) {
        if (_uiState.value.isFlying) return

        // 1) START ZBOR
        _uiState.value = _uiState.value.copy(
            isFlying = true,
            flyingFoodRes = food.imageRes,
            flyStart = startPos,
            monsterState = MonsterState.OPEN_MOUTH
        )
        soundManager.playSound(R.raw.sfx_whoosh, duckMusic = false)

        viewModelScope.launch {
            delay(600) // Zbor

            // 2) IMPACT
            if (food.isHealthy) {
                // --- BUN ---
                _uiState.value = _uiState.value.copy(
                    isFlying = false,
                    monsterState = MonsterState.EATING,
                    score = _uiState.value.score + 10
                )

                soundManager.playSound(R.raw.sfx_eat_funny, duckMusic = false)
                soundManager.playVoiceByName("vox_feed_yummy")

                delay(2500)
                soundManager.playSound(R.raw.sfx_burp, duckMusic = false)

            } else {
                // --- RĂU ---
                _uiState.value = _uiState.value.copy(
                    isFlying = false,
                    monsterState = MonsterState.SAD
                )

                soundManager.playSound(R.raw.sfx_yuck, duckMusic = false)
                delay(300)
                soundManager.playVoiceByName("vox_feed_yuck")

                delay(1500)
            }

            // 3) RESET
            _uiState.value = _uiState.value.copy(monsterState = MonsterState.IDLE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopVoice()
        soundManager.exitGameMode()
    }
}
