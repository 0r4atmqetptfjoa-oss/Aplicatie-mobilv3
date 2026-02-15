package com.example.educationalapp.features.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
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
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.educationalapp.R
import com.example.educationalapp.alphabet.rememberScaledImageBitmap
import com.example.educationalapp.common.AppBackButton
import com.example.educationalapp.designsystem.*
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
import kotlin.math.absoluteValue
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
        EntryPointAccessors.fromApplication(context.applicationContext, SoundManagerEntryPoint::class.java).soundManager()
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

    val pageSize = 2
    val pageCount = (games.size + pageSize - 1) / pageSize
    
    val virtualPageCount = 10_000
    val pagerState = rememberPagerState(initialPage = virtualPageCount / 2) { virtualPageCount }
    val isScrolling = pagerState.isScrollInProgress

    // OPTIMIZARE: Folosim State<Float> direct în graphicsLayer pentru a evita recompoziția întregului ecran
    val idle = rememberInfiniteTransition(label = "gamesMenuIdle")
    val idlePhaseState = idle.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart), label = "idlePhase"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LocalParallaxMainMenuBackground(imageRes = R.drawable.bg_menu_parallax_magic)
        
        // Reducem numărul de particule în timpul scroll-ului pentru a elibera GPU
        AmbientMagicParticles(modifier = Modifier.fillMaxSize(), count = if (isScrolling) 25 else 50)

        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(start = 80.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Aventurile Tale ✨",
                    fontFamily = KidFontFamily, fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White,
                    modifier = Modifier.graphicsLayer { 
                        translationY = sin(idlePhaseState.value) * 8f 
                    }
                )
                Spacer(Modifier.weight(1f))
                SurpriseButton {
                    soundManager.playClickIconSound()
                    val pick = games.random()
                    navController.navigate(pick.route)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 80.dp),
                pageSpacing = 40.dp,
                beyondViewportPageCount = 1
            ) { virtualPage ->
                val pageIndex = virtualPage % pageCount
                val start = pageIndex * pageSize
                val pageGames = games.drop(start).take(pageSize)
                
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pageGames.forEachIndexed { i, game ->
                        val color = pastelColors[(start + i) % pastelColors.size]
                        
                        val pageOffset = (pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction
                        val cardScale = lerp(1f, 0.82f, pageOffset.absoluteValue.coerceIn(0f, 1f))
                        val cardAlpha = lerp(1f, 0.5f, pageOffset.absoluteValue.coerceIn(0f, 1f))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.85f)
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    alpha = cardAlpha
                                    rotationY = pageOffset * 20f
                                    cameraDistance = 12f * density
                                }
                        ) {
                            FinalPremiumGameCard(
                                game = game,
                                cardColor = color,
                                idlePhaseState = idlePhaseState,
                                indexSeed = start + i,
                                isScrolling = isScrolling,
                                onClick = {
                                    soundManager.playClickIconSound()
                                    navController.navigate(game.route)
                                },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                    if (pageGames.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }

            val currentRealPage = remember(pagerState.currentPage) { pagerState.currentPage % pageCount }
            PagerDots(pageCount = pageCount, currentPage = currentRealPage, modifier = Modifier.padding(vertical = 24.dp))
        }

        AppBackButton(modifier = Modifier.align(Alignment.TopStart), onBack = { soundManager.playClickIconSound(); navController.popBackStack() })
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FinalPremiumGameCard(
    game: Game,
    cardColor: Color,
    idlePhaseState: State<Float>,
    indexSeed: Int,
    isScrolling: Boolean,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val haptic = LocalHapticFeedback.current
    val scaleAnim = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { 
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    
                    // Citim starea de animație DOAR în graphicsLayer pentru a evita recompoziția Cardului
                    val phase = idlePhaseState.value
                    translationY = sin(phase + indexSeed * 0.6f) * 15f
                    rotationZ = cos(phase * 0.8f + indexSeed * 0.3f) * 3f
                }
                .shadow(
                    elevation = if (isScrolling) 8.dp else 20.dp, // Reducem complexitatea umbrei la scroll
                    shape = RoundedCornerShape(50.dp),
                    spotColor = cardColor.copy(alpha = 0.6f)
                )
                .border(6.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(50.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scaleAnim.animateTo(0.82f, tween(100))
                        scaleAnim.animateTo(1.18f, spring(0.35f, 450f))
                        onClick()
                        scaleAnim.animateTo(1f, spring(0.5f, 300f))
                    }
                },
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fundal card cu gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.White, cardColor.copy(alpha = 0.25f), cardColor.copy(alpha = 0.45f))))
                )

                // Aura rotativă - folosim graphicsLayer pentru rotație
                val infiniteRotation = rememberInfiniteTransition(label = "aura")
                val angleState = infiniteRotation.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)))
                
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                        .graphicsLayer { 
                            // Oprim rotația în timpul scroll-ului pentru a economisi resurse
                            rotationZ = if (isScrolling) 0f else angleState.value 
                        }
                        .background(Brush.radialGradient(listOf(cardColor.copy(alpha = 0.5f), Color.Transparent)), CircleShape)
                )

                // ICONIȚA - Am redus maxDim la 512 pentru a fi mai rapid de încărcat și a ocupa mai puțină memorie
                val iconBitmap = rememberScaledImageBitmap(resId = game.icon, maxDim = 512)
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = game.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(0.92f)
                            .sharedElement(rememberSharedContentState(key = "icon-${game.sharedKey}"), animatedVisibilityScope)
                    )
                }

                // Overlay lucios - Dezactivat la scroll
                if (!isScrolling) {
                    Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width * 0.5f, 0f)
                            lineTo(0f, size.height * 0.5f)
                            close()
                        }
                        drawPath(path, Color.White)
                    }
                }

                // Titlu card
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = game.name,
                        fontFamily = KidFontFamily, fontSize = 26.sp, fontWeight = FontWeight.Black,
                        color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                }

                if (game.isPremium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(pageCount) { i ->
            val active = i == currentPage
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(if (active) 18.dp else 12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (active) 1f else 0.4f))
                    .border(2.dp, Color.White.copy(alpha = if (active) 0.8f else 0.2f), CircleShape)
            )
        }
    }
}

@Composable
private fun SurpriseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "btn")

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFF9800))))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Shuffle, null, tint = Color(0xFF5D4037), modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Text("SURPRIZĂ!", color = Color(0xFF5D4037), fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
private fun LocalParallaxMainMenuBackground(modifier: Modifier = Modifier, imageRes: Int) {
    val inf = rememberInfiniteTransition(label = "bg")
    val xState = inf.animateFloat(-40f, 40f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), label = "x")
    Image(
        painter = painterResource(id = imageRes), contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize().graphicsLayer { 
            translationX = xState.value
            scaleX = 1.2f
            scaleY = 1.2f 
        }
    )
}
