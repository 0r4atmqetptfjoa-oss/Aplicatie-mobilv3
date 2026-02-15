package com.example.educationalapp.minigames.habitatrescue

import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Habitat Rescue mini‑game.  Maintains lists of animals
 * and habitats and checks whether a selected animal belongs to the chosen
 * habitat.
 */
class HabitatRescueGameViewModel : ViewModel() {
    val animals = listOf(AnimalType.FISH, AnimalType.SHEEP, AnimalType.BEAR, AnimalType.PENGUIN)
    val habitats = listOf(HabitatType.SEA, HabitatType.FARM, HabitatType.FOREST, HabitatType.ICE)

    /**
     * Return true if the pairing of animal and habitat is correct.
     */
    fun submitMatch(animal: AnimalType, habitat: HabitatType): Boolean {
        return when (animal) {
            AnimalType.FISH -> habitat == HabitatType.SEA
            AnimalType.SHEEP -> habitat == HabitatType.FARM
            AnimalType.BEAR -> habitat == HabitatType.FOREST
            AnimalType.PENGUIN -> habitat == HabitatType.ICE
        }
    }
}

/**
 * Enum describing animals used in the Habitat Rescue game.  The [label]
 * property is displayed in the UI.  Real implementation should load
 * corresponding images and sounds instead of plain text.
 */
enum class AnimalType(val label: String) {
    FISH("Pește"),
    SHEEP("Oaie"),
    BEAR("Urs"),
    PENGUIN("Pinguin")
}

/**
 * Enum describing habitats.  The [label] is displayed to the user.
 */
enum class HabitatType(val label: String) {
    SEA("Mare"),
    FARM("Fermă"),
    FOREST("Pădure"),
    ICE("Polul Sud")
}