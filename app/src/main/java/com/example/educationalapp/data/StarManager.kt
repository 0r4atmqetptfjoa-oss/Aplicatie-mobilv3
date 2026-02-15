package com.example.educationalapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * A singleton manager responsible for tracking the player's collected stars. Stars
 * act as a currency within the app and can be spent on unlocking stickers or
 * other rewards. Games should call [addStars] when awarding stars to the
 * player. UI components can observe [starCount] to reactively display the
 * current number of stars.
 */
object StarManager {
    private val _starCount = MutableStateFlow(0)
    /** Current total number of stars collected by the player. */
    val starCount: StateFlow<Int> get() = _starCount

    /**
     * Adds the given number of stars to the player's total. A negative value
     * subtracts stars (for example, when purchasing a sticker). This method
     * clamps the result to be at least zero.
     */
    fun addStars(amount: Int) {
        _starCount.update { current ->
            val newValue = current + amount
            if (newValue < 0) 0 else newValue
        }
    }
}