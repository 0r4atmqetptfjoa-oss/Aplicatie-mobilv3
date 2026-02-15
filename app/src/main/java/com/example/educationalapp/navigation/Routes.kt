package com.example.educationalapp.navigation

import kotlinx.serialization.Serializable

// --- Ecrane Principale ---
@Serializable object IntroRoute
@Serializable object MainMenuRoute
@Serializable object GamesMenuRoute
@Serializable object SettingsRoute
@Serializable object PaywallRoute
@Serializable object ParentalGateRoute
@Serializable object WowGamesRoute

// --- Meniuri Secundare ---
@Serializable object InstrumentsMenuRoute
@Serializable object SongsMenuRoute
@Serializable object StoriesMenuRoute
@Serializable object SoundsMenuRoute

// --- Sub-meniuri Sunete ---
@Serializable object WildSoundsRoute
@Serializable object MarineSoundsRoute
@Serializable object FarmSoundsRoute
@Serializable object BirdSoundsRoute
@Serializable object BirdSpritesRoute
@Serializable object VehicleSoundsRoute

// --- Instrumente Sunete ---
@Serializable object PianoSoundsRoute
@Serializable object XylofonSoundsRoute
@Serializable object GuitarSoundsRoute
@Serializable object HarpaSoundsRoute
@Serializable object SaxofonSoundsRoute
@Serializable object TobeSoundsRoute

// --- Rute pentru Cântece ---
@Serializable object Song1Route
@Serializable object Song2Route
@Serializable object Song3Route
@Serializable object Song4Route

// --- JOCURI ---
@Serializable object AlphabetQuizRoute
@Serializable object AlphabetGraphRoute

@Serializable object PeekABooRoute
@Serializable object ColorsRoute
@Serializable object ShapesRoute
@Serializable object PuzzleRoute
@Serializable object CookingRoute
@Serializable object MagicGardenRoute
@Serializable object MemoryRoute
@Serializable object BalloonPopRoute
@Serializable object AnimalBandRoute
@Serializable object HiddenObjectsRoute
@Serializable object SortingRoute
@Serializable object InstrumentsGameRoute
@Serializable object SequenceRoute
@Serializable object MathRoute
@Serializable object BlocksRoute
@Serializable object MazeRoute
@Serializable object ShadowMatchRoute
@Serializable object AnimalSortingRoute
@Serializable object CodingRoute
@Serializable object EggSurpriseRoute
@Serializable object FeedMonsterRoute

// --- PREMIUM MINI-GAMES ---
@Serializable object ColorMixingRoute
@Serializable object ShapeTrainRoute
@Serializable object HabitatRescueRoute
@Serializable object MusicalPatternRoute
@Serializable object WeatherDressRoute

// --- WOW GAMES (Individually) ---
@Serializable object AlphabetAdventureRoute
@Serializable object NumbersMazeRoute
@Serializable object BuildFarmRoute
@Serializable object ColourRainbowRoute
@Serializable object InteractiveStoryRoute
