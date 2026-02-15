package com.example.educationalapp.minigames.weatherdress

import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Weather Dress‑Up mini‑game.  Defines a mapping
 * between weather conditions and appropriate clothing.  When the player
 * selects a weather and then clothing items, [matchClothing] can be used
 * to check if the item is appropriate.
 */
class WeatherDressUpGameViewModel : ViewModel() {
    val weathers = listOf(WeatherType.SUNNY, WeatherType.RAINY, WeatherType.SNOWY, WeatherType.WINDY)
    val clothes = listOf(ClothingType.SUNGLASSES, ClothingType.UMBRELLA, ClothingType.COAT, ClothingType.HAT)

    // Each weather requires two correct items to be considered complete
    val requiredMatches: Int = 2

    /**
     * Returns true if the clothing item matches the selected weather.  In a
     * full implementation this could update game state, play animations,
     * trigger audio cues, etc.
     */
    fun matchClothing(weather: WeatherType, clothing: ClothingType): Boolean {
        return when (weather) {
            WeatherType.SUNNY -> clothing == ClothingType.SUNGLASSES || clothing == ClothingType.HAT
            WeatherType.RAINY -> clothing == ClothingType.UMBRELLA || clothing == ClothingType.COAT
            WeatherType.SNOWY -> clothing == ClothingType.COAT || clothing == ClothingType.HAT
            WeatherType.WINDY -> clothing == ClothingType.HAT || clothing == ClothingType.COAT
        }
    }
}

/**
 * Weather conditions used in the Weather Dress‑Up game.  The [label]
 * property is displayed on the button.  In a real game, icons would be
 * loaded from resources.
 */
enum class WeatherType(val label: String) {
    SUNNY("Soare"),
    RAINY("Ploaie"),
    SNOWY("Zăpadă"),
    WINDY("Vânt")
}

/**
 * Clothing items.  The [label] is displayed on the button.  In the real
 * implementation you would reference image assets.
 */
enum class ClothingType(val label: String) {
    SUNGLASSES("Ochelari"),
    UMBRELLA("Umbrelă"),
    COAT("Haină"),
    HAT("Pălărie")
}