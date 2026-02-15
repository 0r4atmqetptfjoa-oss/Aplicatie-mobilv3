package com.example.educationalapp.features.sounds

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.educationalapp.R
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// =========================================================================
// MAIN SCREEN CONTROLLER
// =========================================================================
@Composable
fun SoundsMainScreen(
    onExit: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<SoundCategory?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioEngine = remember { AudioEngine(context) }


    // Toggles (UI can bind to these)
    var sfxEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(sfxEnabled, musicEnabled, selectedCategory) {
        audioEngine.sfxEnabled = sfxEnabled
        audioEngine.musicEnabled = musicEnabled
        val ambient = selectedCategory?.ambientMusicRes ?: R.raw.main_menu_music
        audioEngine.playAmbient(ambient)

        // Preload category SFX to avoid first-tap lag
        selectedCategory?.items?.forEach { audioEngine.preloadSfx(it.soundRes) }
    }

    // Bind to lifecycle (pause/resume) and release
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(audioEngine)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(audioEngine)
            audioEngine.release()
        }
    }

    // Muzică ambientală - se schimbă în funcție de categorie
    

    // Tranziție între Meniu și Joc
    AnimatedContent(
        targetState = selectedCategory,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.9f) togetherWith 
            fadeOut(animationSpec = tween(400))
        },
        label = "PremiumScreenTransition"
    ) { category ->
        if (category == null) {
            CategoriesPremiumMenu(
                categories = SoundDataRepository.categories,
                onCategoryClick = { 
                    selectedCategory = it 
                }
            )
        } else {
            ImmersivePremiumDetailScreen(
                category = category,
                onBack = {
                    selectedCategory = null
                },
                audioEngine = audioEngine
            )
        }
    }
}

// =========================================================================
// ECRAN 1: MENIU PREMIUM (Carduri Egale și Mari)
// =========================================================================
@Composable
fun CategoriesPremiumMenu(
    categories: List<SoundCategory>,
    onCategoryClick: (SoundCategory) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu_landscape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp), // Padding mai mare
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -50 }
            ) {
                Text(
                    text = "Alege o Lume",
                    fontSize = 42.sp, // Titlu mai mare
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 12f)
                    )
                )
            }

            // Calculăm layout-ul pe 2 rânduri
            // Avem 7 categorii. Rândul 1: 4 carduri, Rândul 2: 3 carduri (centrate)
            val row1 = categories.take(4)
            val row2 = categories.drop(4)

            // RÂNDUL 1
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                row1.forEachIndexed { index, cat ->
                    val delay = index * 100
                    EnterAnimation(isVisible, delay) {
                        CategoryCardUltra(cat, onCategoryClick)
                    }
                }
            }

            // RÂNDUL 2
            Row(
                modifier = Modifier.fillMaxWidth(0.75f).weight(1f).padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                row2.forEachIndexed { index, cat ->
                    val delay = (index + 4) * 100
                    EnterAnimation(isVisible, delay) {
                        CategoryCardUltra(cat, onCategoryClick)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.EnterAnimation(visible: Boolean, delay: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, delayMillis = delay)) + 
                scaleIn(spring(dampingRatio = 0.6f), initialScale = 0.5f),
        modifier = Modifier.weight(1f).fillMaxHeight()
    ) {
        content()
    }
}

@Composable
fun CategoryCardUltra(
    category: SoundCategory,
    onClick: (SoundCategory) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) onClick(category)
                }
            },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = category.coverRes),
                contentDescription = category.title.asString(), // REPARAT: .asString()
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, category.themeColor.copy(alpha = 0.95f))
                        )
                    )
            )

            Text(
                text = category.title.asString(), // REPARAT: .asString()
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 4f)
                )
            )
        }
    }
}

// =========================================================================
// ECRAN 2: JOC IMERSIV (Buton Back Lemn)
// =========================================================================
@Composable
fun ImmersivePremiumDetailScreen(
    category: SoundCategory,
    onBack: () -> Unit,
    audioEngine: AudioEngine
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioEngine = remember { AudioEngine(context) }

    // Bind to lifecycle (pause/resume) and release
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(audioEngine)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(audioEngine)
            audioEngine.release()
        }
    }
    val particlesState = remember { mutableStateListOf<Particle>() }

LaunchedEffect(particlesState.size) {
        if (particlesState.isEmpty()) return@LaunchedEffect
        while (particlesState.isNotEmpty()) {
            val iterator = particlesState.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.update()
                if (p.isDead()) iterator.remove()
            }
            // 30fps is enough for sparkles and saves CPU on low-end devices.
            delay(33)
        }
    }


    

    val itemsPerPage = 8
    val realPageCount = (category.items.size + itemsPerPage - 1) / itemsPerPage

    // Infinite-ish carousel feeling: swipe left/right always works, pages wrap visually.
    val virtualPageCount = if (realPageCount > 1) 10_000 else 1
    val pagerState = rememberPagerState(
        initialPage = if (realPageCount > 1) virtualPageCount / 2 else 0,
        pageCount = { virtualPageCount }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundal Specific Lumii
        Image(
            painter = painterResource(id = category.backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Pager Interactiv
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 80.dp)
        ) { virtualPage ->
            val pageIndex = if (realPageCount <= 1) 0 else ((virtualPage % realPageCount) + realPageCount) % realPageCount
            val pageOffset = (pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction
            val scaleFactor = lerp(1f, 0.85f, pageOffset.absoluteValue.coerceIn(0f, 1f))
            val alphaFactor = lerp(1f, 0.5f, pageOffset.absoluteValue.coerceIn(0f, 1f))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        alpha = alphaFactor
                    },
                contentAlignment = Alignment.Center
            ) {
                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(startIndex + itemsPerPage, category.items.size)
                val pageItems = category.items.subList(startIndex, endIndex)

                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 32.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    val topRow = pageItems.take(4)
                    val bottomRow = if (pageItems.size > 4) pageItems.drop(4) else emptyList()

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        topRow.forEach { item ->
                            PremiumAnimalItem(item) { pos ->
                                if (item.soundRes != 0) audioEngine.playSfx(item.soundRes, volume = if (category.id.contains("insect", ignoreCase = true)) 1f else 1f)
                                repeat(8) { particlesState.add(createSparkle(pos)) }
                            }
                        }
                        repeat(4 - topRow.size) { Spacer(Modifier.width(160.dp)) }
                    }

                    if (bottomRow.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            bottomRow.forEach { item ->
                                PremiumAnimalItem(item) { pos ->
                                    if (item.soundRes != 0) audioEngine.playSfx(item.soundRes, volume = if (category.id.contains("insect", ignoreCase = true)) 1f else 1f)
                                    repeat(8) { particlesState.add(createSparkle(pos)) }
                                }
                            }
                            repeat(4 - bottomRow.size) { Spacer(Modifier.width(160.dp)) }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            particlesState.forEach { p -> drawStar(p) }
        }

        if (realPageCount > 1) {
            val currentRealPage = if (realPageCount <= 1) 0 else ((pagerState.currentPage % realPageCount) + realPageCount) % realPageCount

            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(realPageCount) { iteration ->
                    val color = if (currentRealPage == iteration) Color.White else Color.White.copy(alpha = 0.4f)
                    val size = if (currentRealPage == iteration) 14.dp else 10.dp
                    Box(modifier = Modifier.size(size).clip(CircleShape).background(color))
                }
            }
        }

        // BUTON BACK
        Image(
            painter = painterResource(id = R.drawable.ui_btn_back_wood),
            contentDescription = "Back",
            modifier = Modifier
                .padding(24.dp)
                .size(80.dp)
                .align(Alignment.TopStart)
                .clickable { onBack() }
        )
    }
}

@Composable
fun PremiumAnimalItem(
    item: SoundItem,
    onClick: (Offset) -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var centerPos by remember { mutableStateOf(Offset.Zero) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        finishedListener = { isAnimating = false },
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) (Random.nextInt(-15, 15)).toFloat() else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .size(170.dp)
            .onGloballyPositioned { coords ->
                val rootPos = coords.positionInRoot()
                centerPos = Offset(rootPos.x + coords.size.width / 2, rootPos.y + coords.size.height / 2)
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                transformOrigin = TransformOrigin.Center
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isAnimating = true
                onClick(centerPos)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.name.asString(), // REPARAT: .asString()
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

data class Particle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var size: Float, var alpha: Float, val color: Color, var rotation: Float
) {
    fun update() {
        x += vx
        y += vy
        alpha -= 0.02f
        size *= 0.94f
        rotation += 8f
    }
    fun isDead() = alpha <= 0f
}

fun createSparkle(center: Offset): Particle {
    val angle = Random.nextFloat() * 6.28f
    val speed = Random.nextFloat() * 15f + 8f
    return Particle(
        x = center.x, y = center.y, vx = cos(angle) * speed, vy = sin(angle) * speed,
        size = Random.nextFloat() * 30f + 10f, alpha = 1f,
        color = listOf(Color(0xFFFFD700), Color.White, Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFFF4081)).random(),
        rotation = Random.nextFloat() * 360f
    )
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(p: Particle) {
    val path = Path().apply {
        val half = p.size / 2
        moveTo(p.x, p.y - half)
        lineTo(p.x + half * 0.3f, p.y - half * 0.3f)
        lineTo(p.x + half, p.y)
        lineTo(p.x + half * 0.3f, p.y + half * 0.3f)
        lineTo(p.x, p.y + half)
        lineTo(p.x - half * 0.3f, p.y + half * 0.3f)
        lineTo(p.x - half, p.y)
        lineTo(p.x - half * 0.3f, p.y - half * 0.3f)
        close()
    }
    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
        drawPath(path = path, color = p.color, alpha = p.alpha, style = Fill)
    }
}

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    fun playSound(@RawRes soundResId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundResId).apply {
                setOnCompletionListener { it.release(); mediaPlayer = null }
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    fun release() { mediaPlayer?.release(); mediaPlayer = null }
}