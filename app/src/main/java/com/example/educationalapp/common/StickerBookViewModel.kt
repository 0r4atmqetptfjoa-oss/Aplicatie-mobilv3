package com.example.educationalapp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import com.example.educationalapp.data.StarManager
import kotlinx.coroutines.flow.StateFlow

data class Sticker(val name: String, val emoji: String, val requiredStars: Int)

class StickerBookViewModel : ViewModel() {

    val stickers = listOf(
        Sticker("Stea", "⭐", 0),
        Sticker("Cățel", "🐶", 2),
        Sticker("Pisică", "🐱", 4),
        Sticker("Mașină", "🚗", 6),
        Sticker("Măr", "🍎", 8),
        Sticker("Balon", "🎈", 10),
        Sticker("Muzică", "🎵", 12),
        Sticker("Curcubeu", "🌈", 15)
    )

    val feedback = mutableStateOf("")

    /**
     * Exposes the current star count from [StarManager] as a state flow. UI
     * components can collect this to update star indicators or enable/disable
     * stickers based on the player's progress.
     */
    val starCount: StateFlow<Int> = StarManager.starCount

    fun onStickerClick(sticker: Sticker) {
        val currentStars = starCount.value
        if (currentStars >= sticker.requiredStars) {
            feedback.value = "Ai selectat stickerul ${sticker.name}!"
            // Optionally subtract stars when selecting a sticker if it should cost stars
            // StarManager.addStars(-sticker.requiredStars)
        } else {
            feedback.value = "Stickerul ${sticker.name} este blocat. Obține ${sticker.requiredStars} stele."
        }
    }
}