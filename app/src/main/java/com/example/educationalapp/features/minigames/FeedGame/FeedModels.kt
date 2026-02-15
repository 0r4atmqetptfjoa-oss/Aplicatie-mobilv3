package com.example.educationalapp.features.wowgames

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

enum class MonsterState {
    IDLE,       // Așteaptă, respiră, clipește
    OPEN_MOUTH, // Când mâncarea zboară spre el
    EATING,     // Mestecă (Neam Neam)
    SAD         // Bleah (Mâncare rea)
}

data class FoodItem(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val isHealthy: Boolean = true // True = Yummy, False = Yuck
)