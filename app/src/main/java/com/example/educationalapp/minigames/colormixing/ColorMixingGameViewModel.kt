package com.example.educationalapp.minigames.colormixing

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Color Mixing mini‑game.  Provides simple logic for
 * combining two primary colours to produce a secondary colour.  In a real
 * implementation, you might also trigger audio feedback or analytics here.
 */
class ColorMixingGameViewModel : ViewModel() {
    /**
     * Mix two Compose [Color] objects and return an approximate secondary
     * colour.  If the combination is not recognised, a neutral colour is
     * returned.  This logic can be expanded to include more subtle blending.
     */
    fun mixColors(c1: Color, c2: Color): Color {
        val red = Color.Red
        val blue = Color.Blue
        val yellow = Color.Yellow
        return when {
            (c1 == red && c2 == yellow) || (c1 == yellow && c2 == red) -> Color(0xFFFFA500) // orange
            (c1 == red && c2 == blue) || (c1 == blue && c2 == red) -> Color(0xFF800080) // purple
            (c1 == yellow && c2 == blue) || (c1 == blue && c2 == yellow) -> Color(0xFF00FF00) // green
            else -> Color.Gray
        }
    }
}