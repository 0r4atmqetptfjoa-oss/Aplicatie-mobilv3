package com.example.educationalapp.common

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

// --- IMPORTURILE TALE ORIGINALE (CARE MERG) ---
import com.example.educationalapp.common.MainViewModel 
import com.example.educationalapp.navigation.*

import com.example.educationalapp.features.mainmenu.MainMenuScreen
import com.example.educationalapp.features.games.GamesMenuScreen

// Jocuri (Păstrăm pachetele exact cum erau în fișierul tău vechi)
import com.example.educationalapp.alphabet.AlphabetGameScreen
import com.example.educationalapp.features.learning.colors.ColorsGameScreen
import com.example.educationalapp.features.learning.shapes.ShapesGameScreen
import com.example.educationalapp.peekaboo.PeekABooGame
import com.example.educationalapp.puzzle.PuzzleGameScreen
import com.example.educationalapp.features.games.CookingGameScreen
import com.example.educationalapp.features.games.MagicGardenGameScreen
import com.example.educationalapp.features.minigames.MemoryGame.MemoryGameScreen
import com.example.educationalapp.BalloonGame.BalloonGameScreen
import com.example.educationalapp.AnimalBandGame.AnimalBandGame
import com.example.educationalapp.features.games.ShadowMatchGameScreen
import com.example.educationalapp.HiddenObjectsGameScreen
import com.example.educationalapp.SortingGameScreen
import com.example.educationalapp.InstrumentsGameScreen
import com.example.educationalapp.features.games.SequenceMemoryGameScreen
import com.example.educationalapp.MathGameScreen
import com.example.educationalapp.BlocksGameScreen
import com.example.educationalapp.MazeGameScreen
import com.example.educationalapp.AnimalSortingGameScreen

// WOW GAMES
import com.example.educationalapp.features.wowgames.AlphabetAdventureGame
import com.example.educationalapp.features.wowgames.NumbersMazeGame
import com.example.educationalapp.features.wowgames.BuildFarmGame
import com.example.educationalapp.features.wowgames.ColourRainbowGame
import com.example.educationalapp.features.wowgames.InteractiveStoryGame
import com.example.educationalapp.features.wowgames.WowGamesApp
import com.example.educationalapp.features.wowgames.EggGameScreen
import com.example.educationalapp.features.wowgames.FeedGameScreen

// SUNETE
import com.example.educationalapp.features.sounds.SoundsMainScreen

// Meniuri Secundare
import com.example.educationalapp.features.instruments.InstrumentsMenuScreen
import com.example.educationalapp.features.stories.StoriesMenuScreen
import com.example.educationalapp.features.songs.SongsMenuScreen

// Instrumente & Cântece
import com.example.educationalapp.features.instruments.InstrumentSoundScreen
import com.example.educationalapp.features.instruments.PianoDefinition
import com.example.educationalapp.features.instruments.XylofonDefinition
import com.example.educationalapp.features.instruments.GuitarDefinition
import com.example.educationalapp.features.instruments.HarpDefinition
import com.example.educationalapp.features.instruments.SaxophoneDefinition
import com.example.educationalapp.features.instruments.DrumsDefinition
import com.example.educationalapp.features.songs.SongPlayerScreen
import com.example.educationalapp.features.intro.IntroScreen

// --- IMPORTURI ACTUALIZATE PENTRU NOILE SETĂRI (Din folderul common) ---
import com.example.educationalapp.common.SettingsScreen
import com.example.educationalapp.PaywallScreen

// Jocuri Premium Noi
import com.example.educationalapp.minigames.colormixing.ColorMixingGameScreen
import com.example.educationalapp.minigames.colormixing.ColorMixingGameViewModel
import com.example.educationalapp.minigames.shapetrain.ShapeTrainGameScreen
import com.example.educationalapp.minigames.shapetrain.ShapeTrainGameViewModel
import com.example.educationalapp.minigames.habitatrescue.HabitatRescueGameScreen
import com.example.educationalapp.minigames.habitatrescue.HabitatRescueGameViewModel
import com.example.educationalapp.minigames.musicalpattern.MusicalPatternGameScreen
import com.example.educationalapp.minigames.musicalpattern.MusicalPatternGameViewModel
import com.example.educationalapp.minigames.weatherdress.WeatherDressUpGameScreen
import com.example.educationalapp.minigames.weatherdress.WeatherDressUpGameViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    // --- STATE-URI ---
    val starCount by viewModel.starCount.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val musicEnabled by viewModel.musicEnabled.collectAsStateWithLifecycle()
    val hardModeEnabled by viewModel.hardModeEnabled.collectAsStateWithLifecycle()
    
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    val starState = rememberSaveable { mutableIntStateOf(starCount) }
    LaunchedEffect(starCount) {
        if (starState.intValue != starCount) starState.intValue = starCount
    }
    LaunchedEffect(starState.intValue) {
        if (starState.intValue != starCount) viewModel.setStarCount(starState.intValue)
    }

    SharedTransitionLayout {
        NavHost(
            navController = navController, 
            startDestination = IntroRoute,
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {

            composable<IntroRoute> {
                IntroScreen(onDone = {
                    navController.navigate(MainMenuRoute) {
                        popUpTo(IntroRoute) { inclusive = true }
                    }
                })
            }

            composable<MainMenuRoute> { 
                MainMenuScreen(
                    navController = navController, 
                    starCount = starCount, 
                    musicEnabled = musicEnabled
                ) 
            }
            
            composable<SettingsRoute> {
                SettingsScreen(
                    navController = navController,
                    soundEnabled = soundEnabled,
                    musicEnabled = musicEnabled,
                    hapticEnabled = hapticEnabled,     
                    hardModeEnabled = hardModeEnabled,
                    isPremium = isPremium,             
                    onSoundChanged = { viewModel.toggleSound() },
                    onMusicChanged = { viewModel.toggleMusic() },
                    onHapticChanged = { viewModel.toggleHaptics() }, 
                    onHardModeChanged = { viewModel.toggleHardMode() },
                    onBuyPremium = { navController.navigate(PaywallRoute) } 
                )
            }
            
            composable<GamesMenuRoute> {
                GamesMenuScreen(
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }

            // --- JOCURILE TALE EXISTENTE (Neschimbate) ---
            navigation<AlphabetGraphRoute>(startDestination = AlphabetQuizRoute) {
                composable<AlphabetQuizRoute> { AlphabetGameScreen(onBack = { navController.backToGamesMenu() }) }
            }

            composable<PeekABooRoute> { PeekABooGame(onHome = { navController.backToGamesMenu() }) }
            composable<ColorsRoute> { ColorsGameScreen(onHome = { navController.backToGamesMenu() }) }
            composable<ShapesRoute> { ShapesGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<PuzzleRoute> { PuzzleGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<CookingRoute> { CookingGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<MagicGardenRoute> { MagicGardenGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<MemoryRoute> { MemoryGameScreen(onHome = { navController.backToGamesMenu() }) }
            composable<BalloonPopRoute> { BalloonGameScreen(onHome = { navController.backToGamesMenu() }) }
            composable<AnimalBandRoute> { AnimalBandGame(onHome = { navController.backToGamesMenu() }) }
            
            composable<HiddenObjectsRoute> { HiddenObjectsGameScreen(navController, starState) }
            composable<SortingRoute> { SortingGameScreen(navController, starState) }
            composable<InstrumentsGameRoute> { InstrumentsGameScreen(navController, starState) }
            composable<SequenceRoute> { SequenceMemoryGameScreen(navController) }
            composable<MathRoute> { MathGameScreen(navController, starState) }
            composable<BlocksRoute> { BlocksGameScreen(navController, starState) }
            composable<MazeRoute> { MazeGameScreen(navController, starState) }
            composable<ShadowMatchRoute> { ShadowMatchGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<AnimalSortingRoute> { AnimalSortingGameScreen(navController, starState) }
            
            composable<EggSurpriseRoute> { EggGameScreen(onBack = { navController.backToGamesMenu() }) }
            composable<FeedMonsterRoute> { FeedGameScreen(onBack = { navController.backToGamesMenu() }) }

            // --- JOCURILE NOI PREMIUM ---
            composable<ColorMixingRoute> {
                val vm: ColorMixingGameViewModel = viewModel()
                ColorMixingGameScreen(viewModel = vm)
            }
            composable<ShapeTrainRoute> {
                val vm: ShapeTrainGameViewModel = viewModel()
                ShapeTrainGameScreen(viewModel = vm)
            }
            composable<HabitatRescueRoute> {
                val vm: HabitatRescueGameViewModel = viewModel()
                HabitatRescueGameScreen(viewModel = vm)
            }
            composable<MusicalPatternRoute> {
                val vm: MusicalPatternGameViewModel = viewModel()
                MusicalPatternGameScreen(viewModel = vm)
            }
            composable<WeatherDressRoute> {
                val vm: WeatherDressUpGameViewModel = viewModel()
                WeatherDressUpGameScreen(viewModel = vm)
            }

            // --- SUNETE ---
            composable<SoundsMenuRoute> {
                SoundsMainScreen(onExit = { navController.popBackStack() })
            }

            composable<InstrumentsMenuRoute> { InstrumentsMenuScreen(navController) }
            composable<StoriesMenuRoute> { StoriesMenuScreen(navController) }
            composable<SongsMenuRoute> { SongsMenuScreen(navController) }

            composable<PianoSoundsRoute> { InstrumentSoundScreen(definition = PianoDefinition, navController = navController) }
            composable<XylofonSoundsRoute> { InstrumentSoundScreen(definition = XylofonDefinition, navController = navController) }
            composable<GuitarSoundsRoute> { InstrumentSoundScreen(definition = GuitarDefinition, navController = navController) }
            composable<HarpaSoundsRoute> { InstrumentSoundScreen(definition = HarpDefinition, navController = navController) }
            composable<SaxofonSoundsRoute> { InstrumentSoundScreen(definition = SaxophoneDefinition, navController = navController) }
            composable<TobeSoundsRoute> { InstrumentSoundScreen(definition = DrumsDefinition, navController = navController) }

            composable<Song1Route> { SongPlayerScreen(navController = navController, backStackEntry = it, starState = starState) }
            composable<Song2Route> { SongPlayerScreen(navController = navController, backStackEntry = it, starState = starState) }
            composable<Song3Route> { SongPlayerScreen(navController = navController, backStackEntry = it, starState = starState) }
            composable<Song4Route> { SongPlayerScreen(navController = navController, backStackEntry = it, starState = starState) }

            composable<AlphabetAdventureRoute> { AlphabetAdventureGame(onBack = { navController.popBackStack() }) }
            composable<NumbersMazeRoute> { NumbersMazeGame(onBack = { navController.popBackStack() }) }
            composable<BuildFarmRoute> { BuildFarmGame(onBack = { navController.popBackStack() }) }
            composable<ColourRainbowRoute> { ColourRainbowGame(onBack = { navController.popBackStack() }) }
            composable<InteractiveStoryRoute> { InteractiveStoryGame(onBack = { navController.popBackStack() }) }
            
            composable<WowGamesRoute> { WowGamesApp(navController = navController) }

            composable<PaywallRoute> {
                PaywallScreen(
                    navController = navController, 
                    hasFullVersion = isPremium, 
                    onUnlock = {
                        viewModel.activatePremium()
                        navController.backToGamesMenu()
                    }
                )
            }
        }
    }
}