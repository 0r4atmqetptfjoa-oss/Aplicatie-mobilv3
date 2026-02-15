package com.example.educationalapp.features.learning.shapes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definim stările exacte pentru reacția robotului
enum class GameState { 
    WAITING_INPUT,    // Robot normal (așteaptă)
    CORRECT_FEEDBACK, // Robot Happy (tăblița sus)
    WRONG_FEEDBACK    // Robot Sad (tăblița jos)
}

data class ShapesUiState(
    val targetShape: ShapeType,
    val options: List<ShapeItem>,
    val targetItem: ShapeItem, // Avem nevoie de itemul specific pt audio
    val score: Int = 0,
    val gameState: GameState = GameState.WAITING_INPUT,
    val wrongSelectionId: String? = null
)

@HiltViewModel
class ShapesViewModel @Inject constructor() : ViewModel() {

    private val shapeQueue = ArrayDeque<ShapeType>()
    
    // Inițializare dummy, se suprascrie în init
    private val _uiState = MutableStateFlow(
        ShapesUiState(ShapeType.CIRCLE, emptyList(), ShapesAssets.allItems[0])
    )
    val uiState: StateFlow<ShapesUiState> = _uiState

    init {
        refillQueue()
        nextRound()
    }

    private fun refillQueue() {
        val allShapes = ShapeType.values().toList()
        // Punem de două ori formele și le amestecăm
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
            
            viewModelScope.launch {
                // Așteptăm 4.5 secunde (cât durează "Bravo" + explicația)
                delay(4500)
                nextRound()
            }
        } else {
            _uiState.value = _uiState.value.copy(
                gameState = GameState.WRONG_FEEDBACK,
                wrongSelectionId = selectedItem.id
            )
            
            viewModelScope.launch {
                // Așteptăm 1.5 secunde pt feedback negativ
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    gameState = GameState.WAITING_INPUT,
                    wrongSelectionId = null
                )
            }
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