package com.example.educationalapp.features.wowgames

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.educationalapp.R

data class FeedUiState(
    val monsterState: MonsterState = MonsterState.IDLE,
    val isFlying: Boolean = false,
    val flyingFoodRes: Int? = null,
    val flyStart: Offset = Offset.Zero,
    val score: Int = 0
)

@HiltViewModel
class FeedGameViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val audio = FeedAudioManager(application.applicationContext)
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        audio.startMusic()
        // Intro: "Mi-e foame!"
        viewModelScope.launch {
            delay(800) // Mică pauză să se încarce scena
            audio.playVoice("vox_feed_intro")
        }
    }

    fun onFoodSelected(food: FoodItem, startPos: Offset) {
        if (_uiState.value.isFlying) return 

        // 1. START ZBOR
        _uiState.value = _uiState.value.copy(
            isFlying = true,
            flyingFoodRes = food.imageRes,
            flyStart = startPos,
            monsterState = MonsterState.OPEN_MOUTH
        )
        audio.playSFX("throw")

        viewModelScope.launch {
            delay(600) // Zbor

            // 2. IMPACT
            if (food.isHealthy) {
                // --- BUN (MĂR, BROCCOLI, PEȘTE) ---
                _uiState.value = _uiState.value.copy(
                    isFlying = false,
                    monsterState = MonsterState.EATING,
                    score = _uiState.value.score + 10
                )
                
                audio.playSFX("eat") // NOM NOM NOM
                
                // FIX: Vorbește mereu acum!
                audio.playVoice("vox_feed_yummy") 
                
                delay(2500)
                audio.playSFX("burp") 

            } else {
                // --- RĂU (FURSEC, GOGOAȘĂ) ---
                _uiState.value = _uiState.value.copy(
                    isFlying = false,
                    monsterState = MonsterState.SAD
                )
                
                // FIX: Ordine sunete - întâi YUCK, apoi vocea
                audio.playSFX("yuck") 
                delay(300)
                audio.playVoice("vox_feed_yuck")
                
                delay(1500)
            }

            // 3. RESET
            _uiState.value = _uiState.value.copy(monsterState = MonsterState.IDLE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audio.release()
    }
}