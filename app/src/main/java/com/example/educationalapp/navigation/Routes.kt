package com.example.educationalapp.navigation

import kotlinx.serialization.Serializable

// --- Rute Principale ---
@Serializable object IntroRoute
@Serializable object MainMenuRoute
@Serializable object SettingsRoute
@Serializable object ParentalGateRoute
@Serializable object GamesMenuRoute
@Serializable object SoundsMenuRoute
@Serializable object PaywallRoute

// --- Rute Jocuri Învățare ---
@Serializable object AlphabetGraphRoute
@Serializable object AlphabetQuizRoute
@Serializable object AlphabetTracingRoute

@Serializable object ColorsRoute
@Serializable object ShapesRoute
@Serializable object PeekABooRoute
@Serializable object PuzzleRoute
@Serializable object MagicGardenRoute

// --- Rute Mini-Games ---
@Serializable object CookingRoute
@Serializable object MemoryRoute
@Serializable object BalloonPopRoute
@Serializable object AnimalBandRoute
@Serializable object EggSurpriseRoute
@Serializable object FeedMonsterRoute

// --- Rute Jocuri Vechi (Legacy) ---
@Serializable object HiddenObjectsRoute
@Serializable object SortingRoute
@Serializable object InstrumentsGameRoute
@Serializable object SequenceRoute
@Serializable object MathRoute
@Serializable object BlocksRoute
@Serializable object MazeRoute
@Serializable object ShadowMatchRoute
@Serializable object AnimalSortingRoute

// --- Rute Noi PREMIUM ---
@Serializable object ColorMixingRoute
@Serializable object ShapeTrainRoute
@Serializable object HabitatRescueRoute
@Serializable object MusicalPatternRoute
@Serializable object WeatherDressRoute

// --- Rute Secundare ---
@Serializable object InstrumentsMenuRoute
@Serializable object StoriesMenuRoute
@Serializable object SongsMenuRoute

// Sunete Instrumente
@Serializable object PianoSoundsRoute
@Serializable object XylofonSoundsRoute
@Serializable object GuitarSoundsRoute
@Serializable object HarpaSoundsRoute
@Serializable object SaxofonSoundsRoute
@Serializable object TobeSoundsRoute

// Cântece
@Serializable object Song1Route
@Serializable object Song2Route
@Serializable object Song3Route
@Serializable object Song4Route

// Wow Games
@Serializable object AlphabetAdventureRoute
@Serializable object NumbersMazeRoute
@Serializable object BuildFarmRoute
@Serializable object ColourRainbowRoute
@Serializable object InteractiveStoryRoute
@Serializable object WowGamesRoute
