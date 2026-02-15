package com.example.educationalapp.minigames.musicalpattern

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Musical Pattern Parade mini‑game.  Generates a sequence of
 * instruments that the user must reproduce.  After each successful round
 * the sequence length increases by one element.  In a complete game the
 * sequence could be randomised and instrument sounds would be played via
 * Media3 or another audio engine.
 */
class MusicalPatternGameViewModel : ViewModel() {
    private val instrumentValues = InstrumentType.values()
    private var sequenceLength by mutableStateOf(2)
    var currentPattern by mutableStateOf(generatePattern(sequenceLength))
        private set

    /**
     * Generates a random pattern of the given length.
     */
    private fun generatePattern(length: Int): List<InstrumentType> {
        return List(length) { instrumentValues.random() }
    }

    /**
     * Called when the user successfully completes the current pattern.  Creates
     * a new pattern with one additional element.
     */
    fun advancePattern() {
        sequenceLength += 1
        currentPattern = generatePattern(sequenceLength)
    }

    /**
     * Resets the game back to a short pattern. Helpful if you add a restart
     * button in the UI.
     */
    fun reset() {
        sequenceLength = 2
        currentPattern = generatePattern(sequenceLength)
    }
}

/**
 * Enum listing the instruments used in the Musical Pattern game.  The [label]
 * property is displayed in the UI.  Real implementation should link to
 * appropriate audio resources.
 */
enum class InstrumentType(val label: String) {
    DRUMS("Tobe"),
    XYLOPHONE("Xilofon"),
    GUITAR("Chitară"),
    PIANO("Pian")
}