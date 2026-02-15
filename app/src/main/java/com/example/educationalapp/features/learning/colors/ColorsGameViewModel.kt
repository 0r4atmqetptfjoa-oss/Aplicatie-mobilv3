package com.example.educationalapp.features.learning.colors

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val PROJECTILE_DURATION = 600L // Cât durează zborul

enum class GameState { WAITING_INPUT, PROJECTILE_FLYING, IMPACT, CELEBRATE }

data class ColorsUiState(
    val currentTarget: ColorItem,
    val options: List<ColorItem>,
    val score: Int = 0,
    val gameState: GameState = GameState.WAITING_INPUT,

    // Animație
    val projectileStart: Offset = Offset.Zero,
    val projectileEnd: Offset = Offset.Zero,
    val projectileColor: Color = Color.White,

    // Stare logică
    val isAnswerCorrect: Boolean? = null,
    val wrongSelectionId: String? = null,
    val poppedBalloonId: String? = null // ID-ul balonului care s-a spart și a devenit vopsea
)

@HiltViewModel
class ColorsGameViewModel @Inject constructor(
    private val soundManager: SoundManager
) : ViewModel() {

    private val questionQueue = ArrayDeque<ColorItem>()

    private val _uiState = MutableStateFlow(
        ColorsUiState(
            currentTarget = ColorsAssets.items.first(),
            options = emptyList()
        )
    )
    val uiState: StateFlow<ColorsUiState> = _uiState

    init {
        // Music
        val bgm = soundManager.rawResId("colors_bg_music").takeIf { it != 0 } ?: R.raw.math_bg_music
        soundManager.enterGameMode(gameMusicResId = bgm, startVolume = 0.25f)

        // SFX preloading (no first-tap silence)
        viewModelScope.launch(Dispatchers.IO) {
            soundManager.loadSoundsByName(
                listOf(
                    "sfx_throw_paint",
                    "sfx_splat",
                    "sfx_bell_win",
                    "sfx_wrong_buzz"
                )
            )
        }

        resetGame()
    }

    private fun resetGame() {
        questionQueue.clear()
        questionQueue.addAll(ColorsAssets.items.shuffled())
        nextQuestion()
    }

    fun onOptionSelected(selectedItem: ColorItem, startPos: Offset, targetPos: Offset) {
        if (_uiState.value.gameState != GameState.WAITING_INPUT) return

        val target = _uiState.value.currentTarget

        if (selectedItem.id == target.id) {
            // --- RĂSPUNS CORECT ---

            // 1. Spargem balonul și lansăm proiectilul
            _uiState.value = _uiState.value.copy(
                gameState = GameState.PROJECTILE_FLYING,
                projectileStart = startPos,
                projectileEnd = targetPos,
                projectileColor = selectedItem.colorValue,
                isAnswerCorrect = true,
                poppedBalloonId = selectedItem.id
            )

            soundManager.playSoundByName("sfx_throw_paint", rate = 1.2f)

            viewModelScope.launch {
                // Zborul vopselei
                delay(PROJECTILE_DURATION)

                // 2. IMPACT (Splat + Colorare Personaj)
                _uiState.value = _uiState.value.copy(
                    gameState = GameState.IMPACT,
                    score = _uiState.value.score + 10
                )

                soundManager.playSoundByName("sfx_splat")
                soundManager.playSoundByName("sfx_bell_win")

                // Așteptăm puțin să se vadă efectul de splat
                delay(500)

                // 3. CELEBRATE (Voce) - fără "delay" hardcodat: așteptăm fix cât durează speech.
                _uiState.value = _uiState.value.copy(gameState = GameState.CELEBRATE)
                soundManager.playVoiceByNameAndWait(target.audioWinRes)

                // 4. NEXT
                nextQuestion()
            }
        } else {
            // --- RĂSPUNS GREȘIT ---
            soundManager.playSoundByName("sfx_wrong_buzz")
            _uiState.value = _uiState.value.copy(wrongSelectionId = selectedItem.id)
            viewModelScope.launch {
                delay(500)
                _uiState.value = _uiState.value.copy(wrongSelectionId = null)
            }
        }
    }

    private fun nextQuestion() {
        val nextTarget = getNextTargetFromQueue()
        val allItems = ColorsAssets.items

        // Logică distractori unici (fără dubluri de culoare)
        val distractors = allItems
            .filter { it.colorValue != nextTarget.colorValue }
            .shuffled()
            .distinctBy { it.colorValue }
            .take(3)

        val options = (distractors + nextTarget).shuffled()

        _uiState.value = _uiState.value.copy(
            currentTarget = nextTarget,
            options = options,
            gameState = GameState.WAITING_INPUT,
            isAnswerCorrect = null,
            projectileStart = Offset.Zero,
            poppedBalloonId = null
        )

        viewModelScope.launch {
            delay(500)
            soundManager.playVoiceByName(nextTarget.audioQuestRes)
        }
    }

    private fun getNextTargetFromQueue(): ColorItem {
        if (questionQueue.isEmpty()) {
            questionQueue.addAll(ColorsAssets.items.shuffled())
        }
        return questionQueue.removeFirst()
    }

    override fun onCleared() {
        // Stop in-game music when leaving this destination.
        soundManager.exitGameMode()
        super.onCleared()
    }
}
