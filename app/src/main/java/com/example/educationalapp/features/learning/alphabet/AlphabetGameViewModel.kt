package com.example.educationalapp.alphabet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class MascotMood { THINKING, HAPPY, SURPRISED, CELEBRATE, IDLE }

data class AlphabetGameUiState(
    val currentQuestion: AlphabetItem,
    val options: List<AlphabetItem>,
    val questionIndex: Int,
    val totalQuestions: Int,
    val score: Int,
    val stars: Int,
    val consecutiveCorrectAnswers: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val isInputLocked: Boolean = false,
    val mascotMood: MascotMood = MascotMood.THINKING,
    val soundOn: Boolean = true
)

@HiltViewModel
class AlphabetGameViewModel @Inject constructor() : ViewModel() {

    private var questionDeck: List<AlphabetItem> = emptyList()
    private val _uiState: MutableStateFlow<AlphabetGameUiState> = MutableStateFlow(createInitialState(true))
    val uiState: StateFlow<AlphabetGameUiState> = _uiState

    init {
        startNewGame()
    }

    private fun startNewGame() {
        val allItems = AlphabetAssets.getItems().shuffled()
        if (allItems.isNotEmpty()) {
            questionDeck = allItems
            val firstQ = questionDeck[0]
            val firstOpts = generateOptionsFor(firstQ, allItems)
            
            _uiState.value = _uiState.value.copy(
                currentQuestion = firstQ,
                options = firstOpts,
                questionIndex = 0,
                totalQuestions = allItems.size,
                score = 0,
                stars = 0,
                consecutiveCorrectAnswers = 0,
                isFinished = false,
                isInputLocked = false,
                mascotMood = MascotMood.THINKING
            )
        }
    }

    private fun generateOptionsFor(correct: AlphabetItem, all: List<AlphabetItem>): List<AlphabetItem> {
        val distractors = all.filter { it.baseLetter != correct.baseLetter }.shuffled().take(2)
        return (distractors + correct).shuffled()
    }

    fun toggleSound() {
        _uiState.value = _uiState.value.copy(soundOn = !_uiState.value.soundOn)
    }

    fun onOptionSelected(selectedItem: AlphabetItem, index: Int) {
        val state = _uiState.value
        if (state.isInputLocked) return

        val isCorrect = (selectedItem.baseLetter == state.currentQuestion.baseLetter)

        if (isCorrect) {
            val newStreak = state.consecutiveCorrectAnswers + 1
            val isBigCelebration = (newStreak % 3 == 0)

            _uiState.value = state.copy(
                selectedOptionIndex = index,
                isAnswerCorrect = true,
                isInputLocked = true,
                score = state.score + 10,
                consecutiveCorrectAnswers = newStreak,
                mascotMood = if (isBigCelebration) MascotMood.CELEBRATE else MascotMood.HAPPY
            )

            viewModelScope.launch {
                // Așteptăm suficient pentru sunetul cuvântului + bravo
                val waitTime = if (isBigCelebration) 7000L else 5000L
                delay(waitTime)
                moveToNextOrFinish()
            }
        } else {
            _uiState.value = state.copy(
                selectedOptionIndex = index,
                isAnswerCorrect = false,
                isInputLocked = true,
                consecutiveCorrectAnswers = 0,
                mascotMood = MascotMood.SURPRISED
            )
            viewModelScope.launch {
                delay(2000L)
                _uiState.value = _uiState.value.copy(
                    selectedOptionIndex = null,
                    isAnswerCorrect = null,
                    isInputLocked = false,
                    mascotMood = MascotMood.THINKING
                )
            }
        }
    }

    private fun moveToNextOrFinish() {
        val state = _uiState.value
        val currentIndex = state.questionIndex
        
        if (currentIndex + 1 >= state.totalQuestions) {
            _uiState.value = state.copy(isFinished = true)
        } else {
            val nextIndex = currentIndex + 1
            val nextQ = questionDeck[nextIndex]
            val nextOpts = generateOptionsFor(nextQ, questionDeck)
            
            _uiState.value = state.copy(
                currentQuestion = nextQ,
                options = nextOpts,
                questionIndex = nextIndex,
                selectedOptionIndex = null,
                isAnswerCorrect = null,
                isInputLocked = false,
                mascotMood = MascotMood.THINKING
            )
        }
    }

    fun resetGame() {
        startNewGame()
    }

    private fun createInitialState(keepSoundOn: Boolean): AlphabetGameUiState {
        return AlphabetGameUiState(
            currentQuestion = AlphabetAssets.getItems().firstOrNull() ?: AlphabetItem('A', "A", "a", 0),
            options = emptyList(),
            questionIndex = 0,
            totalQuestions = 0,
            score = 0,
            stars = 0,
            soundOn = keepSoundOn
        )
    }
}