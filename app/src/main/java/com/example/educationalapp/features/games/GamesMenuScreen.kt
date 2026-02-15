package com.example.educationalapp.features.games

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.R
import com.example.educationalapp.common.AppBackButton
import com.example.educationalapp.designsystem.PastelBlue
import com.example.educationalapp.designsystem.PastelLavender
import com.example.educationalapp.designsystem.PastelMint
import com.example.educationalapp.designsystem.PastelPeach
import com.example.educationalapp.designsystem.PastelPink
import com.example.educationalapp.designsystem.PastelYellow
import com.example.educationalapp.di.SoundManager
import com.example.educationalapp.fx.AmbientMagicParticles
import com.example.educationalapp.navigation.*
import com.example.educationalapp.ui.theme.KidFontFamily
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundManagerEntryPoint {
    fun soundManager(): SoundManager
}

data class Game(
    val name: String,
    val icon: Int,
    val route: Any,
    val sharedKey: String,
    val isPremium: Boolean = false
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GamesMenuScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val soundManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundManagerEntryPoint::class.java
        )
        entryPoint.soundManager()
    }

    val games = remember {
        listOf(
            Game("Aventură Alfabet", R.drawable.ic_alphabet_launcher, AlphabetAdventureRoute, "alphabet_adv"),
            Game("Labirint Numere", R.drawable.ic_math_maze, NumbersMazeRoute, "math_maze"),
            Game("Construiește Ferma", R.drawable.icon_game_animals, BuildFarmRoute, "farm_build"),
            Game("Curcubeu Culori", R.drawable.icon_game_colors, ColourRainbowRoute, "rainbow_color"),
            Game("Culori", R.drawable.icon_game_colors, ColorsRoute, "colors"),
            Game("Forme", R.drawable.icon_game_shapes, ShapesRoute, "shapes"),
            Game("Alfabet Quiz", R.drawable.icon_game_alphabet, AlphabetQuizRoute, "alphabet_quiz"),
            Game("Math Quiz", R.drawable.icon_game_math, MathRoute, "math_quiz"),
            Game("Sortare", R.drawable.icon_game_sorting, SortingRoute, "sorting"),
            Game("Puzzle", R.drawable.icon_game_puzzle, PuzzleRoute, "puzzle"),
            Game("Memorie", R.drawable.icon_game_memory, MemoryRoute, "memory"),
            Game("Secvențe", R.drawable.icon_game_sequence, SequenceRoute, "sequence"),
            Game("Gătit", R.drawable.ic_game_cooking, CookingRoute, "cooking"),
            Game("Ascunse", R.drawable.icon_game_hiddenobjects, HiddenObjectsRoute, "hidden"),
            Game("Grădină Magică", R.drawable.ic_game_garden, MagicGardenRoute, "garden"),
            Game("Umbre", R.drawable.ic_game_shadows, ShadowMatchRoute, "shadow"),
            Game("Balon Pop", R.drawable.ic_game_balloon, BalloonPopRoute, "balloon"),
            Game("Peek-a-Boo", R.drawable.icon_game_hiddenobjects, PeekABooRoute, "peek"),
            Game("Bandă Animale", R.drawable.icon_game_instruments, AnimalBandRoute, "band"),
            Game("Egg Surprise", R.drawable.ic_game_egg_surprise, EggSurpriseRoute, "egg"),
            Game("Feed Monster", R.drawable.ic_game_feed_monster, FeedMonsterRoute, "monster"),
            Game("Mix Culori", R.drawable.ic_premium_color_mix, ColorMixingRoute, "color_mix", isPremium = true),
            Game("Trenul Formelor", R.drawable.ic_premium_shape_train, ShapeTrainRoute, "shape_train", isPremium = true),
            Game("Habitate Animale", R.drawable.ic_premium_habitats, HabitatRescueRoute, "habitat_rescue", isPremium = true),
            Game("Parada Sunetelor", R.drawable.icon_game_instruments, MusicalPatternRoute, "musical_pattern", isPremium = true),
            Game("Garderobă Meteo", R.drawable.ic_premium_weather_dress, WeatherDressRoute, "weather_dress", isPremium = true),
        )
    }

    val pastelColors = remember {
        listOf(PastelPink, PastelBlue, PastelYellow, PastelMint, PastelLavender, PastelPeach)
    }

    val pageSize = 6
    val pageCount = remember(games.size) { (games.size + pageSize - 1) / pageSize }
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }
    val scope = rememberCoroutineScope()

    var showSwipeHint by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(6500)
        showSwipeHint = false
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 0) showSwipeHint = false
    }

    val idle = rememberInfiniteTransition(label = "gamesMenuIdle")
    val idlePhase by idle.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idlePhase"
    )
    val titleFloatY by idle.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleFloat"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LocalParallaxMainMenuBackground(imageRes = R.drawable.bg_menu_parallax_magic)
        
        AmbientMagicParticles(modifier = Modifier.fillMaxSize(), count = 70)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 92.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Alege un joc!",
                        fontFamily = KidFontFamily,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer { translationY = titleFloatY }
                    )
                    Text(
                        text = "Glisează ca să vezi mai multe",
                        fontFamily = KidFontFamily,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                SurpriseButton(
                    onClick = {
                        soundManager.playClickIconSound()
                        val pick = games[Random.nextInt(games.size)]
                        scope.launch {
                            delay(120)
                            navigateTo(navController, pick.route)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 0.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { pageIndex ->
                    val start = pageIndex * pageSize
                    val pageGames = games.drop(start).take(pageSize)
                    val rows = pageGames.chunked(3)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        for (rowIndex in 0 until 2) {
                            val rowGames = rows.getOrNull(rowIndex).orEmpty()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(22.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0 until 3) {
                                    val game = rowGames.getOrNull(col)
                                    if (game != null) {
                                        val color = pastelColors[(start + rowIndex * 3 + col) % pastelColors.size]

                                        GameCard(
                                            game = game,
                                            cardColor = color,
                                            idlePhase = idlePhase,
                                            indexSeed = (start + rowIndex * 3 + col),
                                            onClick = {
                                                soundManager.playClickIconSound()
                                                navigateTo(navController, game.route)
                                            },
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                SwipeHintHandOverlay(
                    visible = showSwipeHint && pagerState.currentPage == 0,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 56.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            PagerDots(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                idlePhase = idlePhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )
        }

        AppBackButton(
            modifier = Modifier.align(Alignment.TopStart),
            onBack = { navController.popBackStack() }
        )
    }
}

private fun navigateTo(navController: NavController, route: Any) {
    navController.navigate(route)
}

// -------------------------------------------------------------------------
// FUNCȚII AUXILIARE (SCOASE ÎN AFARA CLASEI PENTRU VIZIBILITATE GLOBALĂ)
// -------------------------------------------------------------------------

@Composable
private fun SwipeHintHandOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val fade by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = EaseInOutCubic),
        label = "handFade"
    )
    if (fade <= 0.01f) return

    val t = rememberInfiniteTransition(label = "handLoop")
    val slide by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handSlide"
    )
    val wiggle by t.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handWiggle"
    )
    val lift by t.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handLift"
    )

    val density = LocalDensity.current
    val travelPx = with(density) { 240.dp.toPx() }
    val xPx = (slide - 0.5f) * travelPx

    Column(
        modifier = modifier.alpha(fade),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.55f))
                .border(1.5.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Glisează ➜",
                fontFamily = KidFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3E2723)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.ui_tutorial_hand),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    translationX = xPx
                    translationY = lift
                    rotationZ = wiggle
                }
        )
    }
}

@Composable
private fun SurpriseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "surpriseScale"
    )

    val bg = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFE082).copy(alpha = 0.92f),
            Color(0xFFFFCDD2).copy(alpha = 0.92f),
            Color(0xFFB3E5FC).copy(alpha = 0.92f)
        )
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(2.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Surpriză",
                tint = Color(0xFF5D4037),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Surpriză!",
                fontFamily = KidFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4E342E)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RowScope.GameCard(
    game: Game,
    cardColor: Color,
    idlePhase: Float,
    indexSeed: Int,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    var burstTick by remember { mutableStateOf(0) }
    
    val haptic = LocalHapticFeedback.current

    // ANIMATION PHYSICS 2026: Squash & Bounce controlat manual
    val scaleAnim = remember { Animatable(1f) }
    
    val pressTilt by animateFloatAsState(
        targetValue = if (pressed) -5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressTilt"
    )
    
    val idleBreath = 1f + (sin(idlePhase + indexSeed * 0.15f) * 0.012f)
    
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 12.dp,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "cardElevation"
    )

    with(sharedTransitionScope) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer {
                    val s = scaleAnim.value
                    scaleX = s
                    scaleY = s
                    rotationZ = pressTilt
                }
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                .sharedElement(
                    rememberSharedContentState(key = "card-${game.sharedKey}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    scope.launch {
                        // 1. Haptic Feedback
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        // 2. Squash (se strange)
                        scaleAnim.animateTo(
                            targetValue = 0.85f,
                            animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                        )

                        // 3. Bounce (sare inapoi)
                        scaleAnim.animateTo(
                            targetValue = 1.15f,
                            animationSpec = spring(
                                dampingRatio = 0.4f, // Foarte elastic
                                stiffness = 400f
                            )
                        )
                        
                        // 4. Mica pauza sa admiram efectul
                        delay(50)

                        // 5. Revenire normala
                        launch { scaleAnim.animateTo(1f) }

                        // 6. Navigare efectiva
                        onClick()
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BurstSparkles(trigger = burstTick, modifier = Modifier.matchParentSize())

                // --- ICONITA FULL CARD ---
                // O facem cat cardul de mare si o centram
                Image(
                    painter = painterResource(id = game.icon),
                    contentDescription = game.name,
                    contentScale = ContentScale.Fit, // Fit ca sa se vada toata, dar e mare
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(1.0f) // Ocupa tot spatiul cardului
                        .scale(1.1f) // Un mic boost ca sa "muste" din margini
                        .scale(idleBreath) // Respira
                        .sharedElement(
                            rememberSharedContentState(key = "icon-${game.sharedKey}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )

                if (game.isPremium) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Premium",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.75f)) // Mai opac pt citire usoara
                        .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "text-${game.sharedKey}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                ) {
                    Text(
                        text = game.name,
                        fontFamily = KidFontFamily,
                        fontSize = 15.sp, // Putin mai mic ca sa nu acopere iconita
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3E2723),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ShimmerGlossOverlay(
                    idlePhase = idlePhase,
                    indexSeed = indexSeed,
                    pressed = pressed,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun ShimmerGlossOverlay(
    idlePhase: Float,
    indexSeed: Int,
    pressed: Boolean,
    modifier: Modifier = Modifier
) {
    val shimmer = rememberInfiniteTransition(label = "cardShimmer")
    val x by shimmer.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200 + (indexSeed % 5) * 150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.38f else 0.26f,
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label = "shimmerAlpha"
    )
    val phase = sin(idlePhase + indexSeed * 0.2f) * 0.06f

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(36.dp))
            .alpha(alpha)
    ) {
        val w = maxWidth
        val bandW = w * (0.38f + phase)

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.26f), Color.Transparent, Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(bandW)
                .offset(x = (w * x) - bandW)
                .graphicsLayer { rotationZ = 18f }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.38f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun BurstSparkles(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger <= 0) return
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 420, easing = EaseInOutCubic))
    }
    val p = progress.value
    if (p <= 0f || p >= 1f) return
    val colors = listOf(Color.White, Color(0xFFFFF59D), Color(0xFFB3E5FC), Color(0xFFFFCCBC))

    Canvas(modifier = modifier) {
        val center = Offset(x = size.width * 0.5f, y = size.height * 0.46f)
        val travel = size.minDimension * 0.34f
        val baseR = size.minDimension * 0.030f
        val dotR = (baseR * (1f - p)).coerceAtLeast(2.2f)
        val a = (1f - p).coerceIn(0f, 1f)
        val count = 8
        for (i in 0 until count) {
            val ang = (i.toFloat() / count.toFloat()) * (2f * PI.toFloat())
            val dist = travel * (0.20f + 0.80f * p)
            val dx = cos(ang) * dist
            val dy = sin(ang) * dist
            drawCircle(color = colors[i % colors.size].copy(alpha = a), radius = dotR, center = Offset(center.x + dx, center.y + dy))
        }
    }
}

@Composable
private fun PagerDots(
    pageCount: Int,
    currentPage: Int = 0,
    idlePhase: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { i ->
            val selected = i == currentPage
            val pulse = 1f + (if (selected) (sin(idlePhase * 1.6f) * 0.06f) else 0f)
            val size = if (selected) 18.dp else 12.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(size)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.40f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = if (selected) 0.85f else 0.35f), shape = CircleShape)
            )
        }
    }
}

@Composable
private fun LocalParallaxMainMenuBackground(
    modifier: Modifier = Modifier,
    imageRes: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundParallax")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parallaxX"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parallaxScale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}
