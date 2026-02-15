package com.example.educationalapp.features.learning.shapes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GameState { 
    WAITING_INPUT,    
    CORRECT_FEEDBACK, 
    WRONG_FEEDBACK    
}

data class ShapesUiState(
    val targetShape: ShapeType,
    val options: List<ShapeItem>,
    val targetItem: ShapeItem, 
    val score: Int = 0,
    val gameState: GameState = GameState.WAITING_INPUT,
    val wrongSelectionId: String? = null
)

@HiltViewModel
class ShapesViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val shapeQueue = ArrayDeque<ShapeType>()
    
    private val _uiState = MutableStateFlow(
        ShapesUiState(ShapeType.CIRCLE, emptyList(), ShapesAssets.allItems[0])
    )
    val uiState: StateFlow<ShapesUiState> = _uiState
    
    // Adăugăm un handler pentru audio în ViewModel (sau îl lăsăm în Screen dacă preferi, 
    // dar pentru onComplete e mai curat să avem un semnal aici)
    private var onAudioComplete: (() -> Unit)? = null

    init {
        refillQueue()
        nextRound()
    }

    private fun refillQueue() {
        val allShapes = ShapeType.values().toList()
        shapeQueue.addAll((allShapes + allShapes).shuffled())
    }

    fun onOptionSelected(selectedItem: ShapeItem) {
        if (_uiState.value.gameState != GameState.WAITING_INPUT) return

        val isCorrect = selectedItem.type == _uiState.value.targetShape

        if (isCorrect) {
            _uiState.value = _uiState.value.copy(
                gameState = GameState.CORRECT_FEEDBACK,
                score = _uiState.value.score + 10,
                wrongSelectionId = null
            )
            // Nu mai folosim delay(4500), așteptăm semnalul de la Screen/Audio
        } else {
            _uiState.value = _uiState.value.copy(
                gameState = GameState.WRONG_FEEDBACK,
                wrongSelectionId = selectedItem.id
            )
            
            viewModelScope.launch {
                delay(2000) // Feedback-ul de eroare poate rămâne cu un mic delay fix
                if (_uiState.value.gameState == GameState.WRONG_FEEDBACK) {
                    _uiState.value = _uiState.value.copy(
                        gameState = GameState.WAITING_INPUT,
                        wrongSelectionId = null
                    )
                }
            }
        }
    }

    fun onFeedbackFinished() {
        if (_uiState.value.gameState == GameState.CORRECT_FEEDBACK) {
            nextRound()
        }
    }

    private fun nextRound() {
        if (shapeQueue.isEmpty()) {
            refillQueue()
        }
        val nextShape = shapeQueue.removeFirst()
        val targetItem = ShapesAssets.getRandomItemForShape(nextShape)

        _uiState.value = _uiState.value.copy(
            targetShape = nextShape,
            targetItem = targetItem,
            options = generateOptions(nextShape, targetItem),
            gameState = GameState.WAITING_INPUT,
            wrongSelectionId = null
        )
    }

    private fun generateOptions(targetType: ShapeType, correctItem: ShapeItem): List<ShapeItem> {
        val opts = mutableListOf<ShapeItem>()
        opts.add(correctItem)
        
        while (opts.size < 4) {
            val distractor = ShapesAssets.getRandomDistractor(targetType)
            if (opts.none { it.id == distractor.id }) {
                opts.add(distractor)
            }
        }
        return opts.shuffled()
    }
}
