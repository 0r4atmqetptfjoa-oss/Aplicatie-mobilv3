package com.example.educationalapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID // <--- IMPORT IMPORTANT
import kotlin.random.Random

/**
 * ViewModel backing the number sorting game.
 */
class SortingGameViewModel : ViewModel() {

    enum class SortingMode { ASCENDING, DESCENDING, EVEN_ODD }
    enum class BalloonType { NORMAL, BOMB, POWERUP }

    /** * Represents a single balloon in the game grid.
     * Am adaugat 'id' pentru a repara crash-ul cand apar duplicate.
     */
    data class BalloonItem(
        val value: Int?, 
        val type: BalloonType,
        val id: String = UUID.randomUUID().toString() // <--- ID UNIC GENERAT AUTOMAT
    )

    var level by mutableStateOf(1)
        private set

    var sortingMode by mutableStateOf(SortingMode.ASCENDING)
        private set

    var items by mutableStateOf(generateItems(1))
        private set

    var feedback by mutableStateOf("")
        private set

    var score by mutableStateOf(0)
        private set

    private var evenPhase = true

    fun currentTarget(): Int? {
        val normals = items.filter { it.type == BalloonType.NORMAL }.mapNotNull { it.value }
        if (normals.isEmpty()) return null
        return when (sortingMode) {
            SortingMode.ASCENDING -> normals.minOrNull()
            SortingMode.DESCENDING -> normals.maxOrNull()
            SortingMode.EVEN_ODD -> {
                if (evenPhase) {
                    val evens = normals.filter { it % 2 == 0 }
                    if (evens.isNotEmpty()) evens.minOrNull() else {
                        evenPhase = false
                        val odds = normals.filter { it % 2 != 0 }
                        odds.minOrNull()
                    }
                } else {
                    val odds = normals.filter { it % 2 != 0 }
                    odds.minOrNull()
                }
            }
        }
    }

    fun onBalloonClick(item: BalloonItem, onLevelComplete: (stars: Int) -> Unit) {
        when (item.type) {
            BalloonType.BOMB -> {
                feedback = "Ai lovit o bombă!"
                score = (score - 20).coerceAtLeast(0)
                items = items.filter { it.id != item.id } // Folosim ID pentru stergere sigura
            }
            BalloonType.POWERUP -> {
                feedback = "Power‑up! Extra puncte!"
                score += 15
                items = items.filter { it.id != item.id }
            }
            BalloonType.NORMAL -> {
                val target = currentTarget() ?: return
                val value = item.value ?: return
                if (value == target) {
                    feedback = "Corect!"
                    score += 10
                    items = items.filter { it.id != item.id }
                    
                    if (items.none { it.type == BalloonType.NORMAL }) {
                        level++
                        feedback = "Nivel completat!"
                        onLevelComplete(1)
                        items = generateItems(level)
                    }
                } else {
                    feedback = "Greșit!"
                    score = (score - 5).coerceAtLeast(0)
                }
            }
        }
    }

    fun generateItems(level: Int): List<BalloonItem> {
        sortingMode = when ((level - 1) % 3) {
            1 -> SortingMode.DESCENDING
            2 -> SortingMode.EVEN_ODD
            else -> SortingMode.ASCENDING
        }
        evenPhase = true
        val count = level + 4
        val values = List(count) { Random.nextInt(1, 100) }.toMutableList()
        val itemsList = mutableListOf<BalloonItem>()
        for (v in values) {
            itemsList.add(BalloonItem(v, BalloonType.NORMAL))
        }
        if (itemsList.size >= 5) {
            val indices = (itemsList.indices).shuffled()
            val bombIndex = indices[0]
            itemsList[bombIndex] = BalloonItem(null, BalloonType.BOMB)
            
            val powerIndex = indices[1]
            itemsList[powerIndex] = BalloonItem(null, BalloonType.POWERUP)
        }
        return itemsList
    }
}