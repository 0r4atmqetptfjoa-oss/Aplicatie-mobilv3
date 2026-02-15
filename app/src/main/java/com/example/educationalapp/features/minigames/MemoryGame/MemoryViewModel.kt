package com.example.educationalapp.features.minigames.MemoryGame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val soundPlayer: MemorySoundPlayer
) : ViewModel() {

    var cards by mutableStateOf<List<MemoryCard>>(emptyList())
        private set

    var matches by mutableStateOf(0)
        private set

    // NIVELE: Număr de perechi (4 cărți -> 16 cărți)
    private val levels = listOf(2, 3, 4, 6, 7, 8)
    
    var currentLevelIndex by mutableStateOf(0)
        private set

    var pairCount by mutableStateOf(levels[0]) 
        private set

    private var selectedIds = mutableListOf<Int>()
    private var isChecking by mutableStateOf(false)
    private var consecutiveMatches = 0
    private var mistakesInRow = 0 

    // LISTA RESURSE (Numele reale verificate)
    private val fullImagePool = listOf(
        // Animale
        R.drawable.img_lion, R.drawable.img_elephant, R.drawable.img_giraffe,
        R.drawable.img_monkey, R.drawable.img_zebra, R.drawable.porc,
        R.drawable.caine_ferma, R.drawable.pisica_ferma, R.drawable.pinguin,
        R.drawable.img_hippo, R.drawable.koala, R.drawable.panda_rosu,
        // Vehicule
        R.drawable.masina_mica, R.drawable.masina_politie, R.drawable.masina_pompieri,
        R.drawable.ambulanta, R.drawable.camion, R.drawable.avion,
        // Mâncare
        R.drawable.food_apple, R.drawable.img_math_banana, R.drawable.food_cookie,
        R.drawable.food_donut, R.drawable.pizza_4_baked, R.drawable.food_broccoli,
        R.drawable.img_math_strawberry
    )

    init {
        resetGame()
        soundPlayer.playMusic()
        soundPlayer.playIntro()
    }

    fun resetGame() {
        matches = 0
        consecutiveMatches = 0
        mistakesInRow = 0
        selectedIds.clear()
        isChecking = false
        
        // Setăm numărul de perechi
        pairCount = levels[currentLevelIndex]

        // Alegem imagini noi aleatoriu la FIECARE resetare (chiar si la ultimul nivel)
        val chosen = fullImagePool.shuffled().take(pairCount)
        
        cards = (chosen + chosen).mapIndexed { index, res ->
            MemoryCard(id = index, imageRes = res)
        }.shuffled()
    }

    // Funcția care trece la nivelul următor sau rămâne la ultimul
    fun advanceLevel() {
        if (currentLevelIndex < levels.size - 1) {
            currentLevelIndex++
        } 
        // Dacă e la ultimul nivel, indexul nu crește, dar resetGame() se apelează
        // și generează o tablă nouă cu alte imagini.
        resetGame()
    }

    fun isGameWon() = matches >= pairCount

    fun onCardClick(card: MemoryCard) {
        if (isChecking || card.isMatched || card.isFlipped || selectedIds.size >= 2) return

        soundPlayer.playCardFlip()
        updateCard(card.id) { it.copy(isFlipped = true, isHinted = false) }
        selectedIds.add(card.id)

        if (selectedIds.size == 2) {
            checkPair()
        }
    }

    private fun checkPair() {
        isChecking = true
        viewModelScope.launch {
            delay(500)
            val id1 = selectedIds[0]
            val id2 = selectedIds[1]
            val card1 = cards.find { it.id == id1 }
            val card2 = cards.find { it.id == id2 }

            if (card1?.imageRes == card2?.imageRes) {
                // SUCCES
                matches++
                consecutiveMatches++
                mistakesInRow = 0
                
                soundPlayer.playCorrect()
                soundPlayer.playMatchSparkle()
                
                if (consecutiveMatches > 1) {
                    delay(300)
                    soundPlayer.playCombo()
                }

                updateCard(id1) { it.copy(isMatched = true, popToken = it.popToken + 1) }
                updateCard(id2) { it.copy(isMatched = true, popToken = it.popToken + 1) }

                if (isGameWon()) {
                    delay(600)
                    soundPlayer.playWin()
                }
            } else {
                // GREȘEALĂ
                consecutiveMatches = 0
                mistakesInRow++
                soundPlayer.playFail()
                
                updateCard(id1) { it.copy(isFlipped = false, shakeToken = it.shakeToken + 1) }
                updateCard(id2) { it.copy(isFlipped = false, shakeToken = it.shakeToken + 1) }

                // Hint automat după 2 greșeli
                if (mistakesInRow >= 2) {
                    delay(200)
                    requestHint()
                    mistakesInRow = 0
                }
            }
            selectedIds.clear()
            isChecking = false
        }
    }

    private fun requestHint() {
        if (isChecking || isGameWon() || selectedIds.isNotEmpty()) return
        val candidates = cards.filter { !it.isMatched && !it.isFlipped }
        if (candidates.isEmpty()) return

        val targetRes = candidates.random().imageRes
        val hintIds = cards.filter { it.imageRes == targetRes && !it.isMatched }.map { it.id }

        if (hintIds.isNotEmpty()) {
            soundPlayer.playCardFlip()
            hintIds.forEach { id -> updateCard(id) { it.copy(isHinted = true) } }

            viewModelScope.launch {
                delay(1200)
                hintIds.forEach { id -> updateCard(id) { it.copy(isHinted = false) } }
            }
        }
    }

    private fun updateCard(id: Int, transform: (MemoryCard) -> MemoryCard) {
        cards = cards.map { if (it.id == id) transform(it) else it }
    }

    override fun onCleared() {
        soundPlayer.stopMusic()
        super.onCleared()
    }
}