package com.example.educationalapp.features.minigames.MemoryGame

data class MemoryCard(
    val id: Int,
    val imageRes: Int,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,

    // Variabile pentru animații
    val isHinted: Boolean = false,
    val shakeToken: Int = 0, 
    val popToken: Int = 0    
)