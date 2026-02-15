package com.example.educationalapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.educationalapp.common.PremiumConfetti
import com.example.educationalapp.utils.AudioHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// --- ASSET-URI ---
object MathAssets {
    val items = listOf(
        R.drawable.img_math_apple,
        R.drawable.img_math_banana,
        R.drawable.img_math_strawberry
    )

    fun getCharNumberImage(number: Int): Int {
        return when (number) {
            0 -> R.drawable.img_number_0
            1 -> R.drawable.img_number_1
            2 -> R.drawable.img_number_2
            3 -> R.drawable.img_number_3
            4 -> R.drawable.img_number_4
            5 -> R.drawable.img_number_5
            6 -> R.drawable.img_number_6
            7 -> R.drawable.img_number_7
            8 -> R.drawable.img_number_8
            9 -> R.drawable.img_number_9
            else -> R.drawable.img_number_0
        }
    }

    val iconPlus = R.drawable.plus_personaj
    val iconEqual = R.drawable.egal_personaj
    val iconQuestion = R.drawable.img_math_star
}

enum class MathGameMode { COUNTING, ADDITION }

data class MathLevelData(
    val mode: MathGameMode,
    val numberA: Int,
    val numberB: Int = 0,
    val correctAnswer: Int,
    val options: List<Int>,
    val itemResId: Int,
    val id: Int = Random.nextInt()
)

class MathGameLogic {
    fun generateLevel(levelIndex: Int): MathLevelData {
        val mode = if (levelIndex < 5) MathGameMode.COUNTING else MathGameMode.ADDITION
        val itemRes = if (MathAssets.items.isNotEmpty()) MathAssets.items.random() else R.drawable.img_math_apple

        if (mode == MathGameMode.COUNTING) {
            val answer = Random.nextInt(1, 10)
            val options = generateOptions(answer)
            return MathLevelData(mode, answer, 0, answer, options, itemRes)
        } else {
            val a = Random.nextInt(1, 6)
            val b = Random.nextInt(1, 6)
            val safeB = if (a + b > 10) 10 - a else b
            val answer = a + safeB
            val options = generateOptions(answer)
            return MathLevelData(mode, a, safeB, answer, options, itemRes)
        }
    }

    private fun generateOptions(correct: Int): List<Int> {
        val opts = mutableListOf<Int>()
        opts.add(correct)
        while (opts.size < 3) {
            val wrong = Random.nextInt(0, 11)
            if (wrong != correct && !opts.contains(wrong)) {
                opts.add(wrong)
            }
        }
        return opts.shuffled()
    }
}

// --- MAIN SCREEN ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MathGameScreen(navController: NavController, starState: MutableState<Int>) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    val haptics = LocalHapticFeedback.current
    
    DisposableEffect(Unit) { onDispose { audioHelper.release() } }

    val gameLogic = remember { MathGameLogic() }
    val scope = rememberCoroutineScope()

    var currentLevelIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var consecutiveWins by remember { mutableStateOf(0) }
    var levelData by remember { mutableStateOf(gameLogic.generateLevel(0)) }
    
    var isGameOver by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var buttonsEnabled by remember { mutableStateOf(true) }
    
    // Hint Logic State
    var hintActive by remember { mutableStateOf(false) }
    var mistakesInLevel by remember { mutableStateOf(0) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Resetăm starea la fiecare nivel nou
    LaunchedEffect(levelData) {
        hintActive = false
        mistakesInLevel = 0
        buttonsEnabled = true
        lastInteractionTime = System.currentTimeMillis() // Reset timer inactivitate
        
        delay(600) 
        if (levelData.mode == MathGameMode.COUNTING) {
            audioHelper.playVoice("question_how_many")
        } else {
            audioHelper.playVoice("question_total")
        }
    }

    // Timer pentru Inactivitate (Hint automat după 8 secunde)
    LaunchedEffect(levelData, isGameOver) {
        while (!isGameOver) {
            delay(1000)
            if (buttonsEnabled && !hintActive) {
                if (System.currentTimeMillis() - lastInteractionTime > 8000) {
                    hintActive = true
                    audioHelper.playHint() // Sunet subtil de hint
                }
            }
        }
    }

    fun handleAnswer(selected: Int) {
        if (!buttonsEnabled) return
        
        lastInteractionTime = System.currentTimeMillis() // Resetăm timer-ul de inactivitate la orice apăsare

        if (selected == levelData.correctAnswer) {
            // --- RĂSPUNS CORECT ---
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            buttonsEnabled = false
            score += 10
            starState.value += 1
            consecutiveWins++
            showConfetti = true 
            
            // 1. Sunet magic imediat (SFX)
            audioHelper.playSfx("sfx_magic_win") 
            
            // 2. Verificăm dacă merită "BRAVO" (2 consecutive)
            if (consecutiveWins >= 2) {
                scope.launch {
                    delay(400) // Mică pauză să nu acopere sfx-ul complet
                    audioHelper.playVoice("voice_correct_bravo")
                }
            }

            scope.launch {
                delay(2000) 
                showConfetti = false
                if (currentLevelIndex < 19) {
                    delay(300)
                    currentLevelIndex++
                    levelData = gameLogic.generateLevel(currentLevelIndex)
                } else {
                    // --- FINAL JOC ---
                    audioHelper.playVoice("voice_level_complete")
                    isGameOver = true
                }
            }
        } else {
            // --- RĂSPUNS GREȘIT ---
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            consecutiveWins = 0
            mistakesInLevel++
            
            // 1. Sunet Buzz (SFX)
            audioHelper.playSfx("sfx_wrong_buzz")
            
            // 2. Voce Oops (Voice) - Se aud ambele
            scope.launch {
                delay(200) 
                audioHelper.playVoice("voice_wrong_oops")
            }

            // 3. Hint Logic: Dacă greșește de 2 ori, activăm hint-ul
            if (mistakesInLevel >= 2 && !hintActive) {
                hintActive = true
                // Putem reda și un sunet de hint aici opțional
            }
        }
    }

    // Parallax Background
    val bgParallax = rememberInfiniteTransition(label = "bgParallax")
    val bgDx by bgParallax.animateFloat(
        initialValue = -1.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bgDx"
    )
    val bgDy by bgParallax.animateFloat(
        initialValue = -0.8f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bgDy"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_game_math),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer { translationX = bgDx; translationY = bgDy }
        )

        SparkleOverlay(modifier = Modifier.fillMaxSize().zIndex(2f))

        if (showConfetti) {
            PremiumConfetti(modifier = Modifier.fillMaxSize().zIndex(50f))
        }

        if (isGameOver) {
            MathGameOverDialog(
                score = score,
                onRestart = {
                    audioHelper.playClick()
                    currentLevelIndex = 0
                    score = 0
                    consecutiveWins = 0
                    isGameOver = false
                    levelData = gameLogic.generateLevel(0)
                    buttonsEnabled = true
                },
                onHome = { 
                    audioHelper.playClick()
                    navController.popBackStack() 
                }
            )
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- HEADER ---
                GameHeaderClean(
                    score = score,
                    onBack = { 
                        audioHelper.playClick()
                        navController.popBackStack() 
                    },
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().zIndex(10f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- ZONA ÎNTREBĂRII ---
                    Box(
                        modifier = Modifier
                            .weight(0.65f) 
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                         AnimatedContent(
                            targetState = levelData,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                            }, label = "QuestionTransition"
                        ) { currentLevel ->
                            MathQuestionAreaSmart(levelData = currentLevel)
                        }
                    }

                    // --- RĂSPUNSURI (DREAPTA) ---
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val maxNumSize = min(maxWidth * 0.9f, maxHeight / 3.3f)

                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            levelData.options.forEach { number ->
                                val isCorrect = number == levelData.correctAnswer
                                // Hint Visual: Dacă hint e activ, estompăm pe cele greșite
                                val alpha = if (hintActive && !isCorrect) 0.3f else 1f
                                val showHighlight = hintActive && isCorrect

                                FittedPremiumCard(
                                    maxSize = maxNumSize,
                                    imageRes = MathAssets.getCharNumberImage(number),
                                    enabled = buttonsEnabled && (!hintActive || isCorrect), // Dezactivăm pe cele greșite în hint mode
                                    highlight = showHighlight,
                                    modifier = Modifier.graphicsLayer { this.alpha = alpha },
                                    onClick = {
                                        handleAnswer(number)
                                    }
                                )
                            }
                        }
                    }
                }

                // --- HINT BUTTON (STÂNGA JOS) ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 20.dp, start = 10.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (!hintActive && buttonsEnabled) {
                                audioHelper.playHint()
                                hintActive = true
                            }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        ShineStarIcon(modifier = Modifier.size(56.dp))
                    }
                }
            }
        }
    }
}

// --- RESTUL UI-ULUI (FittedPremiumCard, Header, etc) ---
// (Acestea rămân identice cu versiunea anterioară pentru consistență)

@Composable
fun FittedPremiumCard(
    maxSize: Dp,
    imageRes: Int,
    enabled: Boolean,
    highlight: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "press"
    )

    val borderColor = if (highlight) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f)
    val borderWidth = if (highlight) 4.dp else 2.dp
    val glowElevation = if (highlight) 20.dp else 6.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = maxSize * 0.7f, minHeight = maxSize * 0.7f, maxWidth = maxSize, maxHeight = maxSize)
            .scale(pressScale)
            .shadow(glowElevation, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFE0E0E0).copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun GameHeaderClean(
    score: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ui_btn_back_wood),
            contentDescription = "Back",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(64.dp)
                .clickable { onBack() }
        )

        ScorePill(score = score)
    }
}

@Composable
private fun ScorePill(score: Int) {
    Box(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⭐ $score",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 6f))
        )
    }
}

@Composable
private fun ShineStarIcon(modifier: Modifier = Modifier) {
    val shine = rememberInfiniteTransition(label = "shine")
    val p by shine.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = "Hint",
            tint = Color(0xFFFFD700), // Gold
            modifier = Modifier.fillMaxSize()
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(degrees = -25f) {
                val w = size.width
                val h = size.height
                val stripeW = w * 0.35f
                val x = (p * w) - stripeW
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(x = x, y = 0f),
                    size = androidx.compose.ui.geometry.Size(width = stripeW, height = h)
                )
            }
        }
    }
}

@Composable
fun MathQuestionAreaSmart(levelData: MathLevelData) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (levelData.mode == MathGameMode.COUNTING) "Câte vezi?" else "Calculează:",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 4f)),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        val W = this.maxWidth
        val H = this.maxHeight

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(top = 20.dp)
        ) {
            if (levelData.mode == MathGameMode.COUNTING) {
                val count = levelData.numberA
                
                when {
                    count <= 3 -> {
                        val itemSize = (H * 0.6f).coerceAtMost(W / count * 0.8f)
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(count) {
                                GiantObject(resId = levelData.itemResId, size = itemSize)
                            }
                        }
                    }
                    count <= 6 -> {
                        val itemSize = (H * 0.4f).coerceAtMost(W / 3 * 0.9f)
                        val row1 = 3
                        val row2 = count - 3
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row { repeat(row1) { GiantObject(levelData.itemResId, itemSize) } }
                            Row { repeat(row2) { GiantObject(levelData.itemResId, itemSize) } }
                        }
                    }
                    else -> {
                        val columns = 4
                        val itemSize = (H * 0.28f).coerceAtMost(W / 4 * 0.9f)
                        GameGroupImagesGrid(count = count, imageRes = levelData.itemResId, columns = columns, size = itemSize)
                    }
                }

            } else {
                val maxEqSize = (H / 2.5f).coerceAtMost(W / 5.5f)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GiantObject(resId = MathAssets.getCharNumberImage(levelData.numberA), size = maxEqSize)
                    GiantObject(resId = MathAssets.iconPlus, size = maxEqSize * 0.7f)
                    GiantObject(resId = MathAssets.getCharNumberImage(levelData.numberB), size = maxEqSize)
                    GiantObject(resId = MathAssets.iconEqual, size = maxEqSize * 0.7f)
                    GiantObject(resId = MathAssets.iconQuestion, size = maxEqSize)
                }
            }
        }
    }
}

@Composable
fun GiantObject(resId: Int, size: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val dy by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + Random.nextInt(500), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dy"
    )

    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .graphicsLayer { translationY = dy },
        contentScale = ContentScale.Fit
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameGroupImagesGrid(count: Int, imageRes: Int, columns: Int, size: Dp) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
        maxItemsInEachRow = columns, 
        modifier = Modifier.wrapContentSize()
    ) {
        repeat(count) { 
             Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .padding(2.dp),
                 contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SparkleOverlay(modifier: Modifier = Modifier) {
    val sparkles = remember {
        List(34) {
            Sparkle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = Random.nextFloat() * 2.2f + 0.8f,
                speed = Random.nextFloat() * 0.06f + 0.02f,
                phase = Random.nextFloat() * (2f * PI).toFloat()
            )
        }
    }

    val tAnim = rememberInfiniteTransition(label = "sparkle")
    val t by tAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "t"
    )

    Canvas(modifier = modifier) {
        for (s in sparkles) {
            val yy = ((s.y + t * s.speed) % 1f)
            val a = (0.08f + 0.10f * (0.5f + 0.5f * sin((t * 2f * PI).toFloat() + s.phase))).toFloat()
            drawCircle(
                color = Color(0xFFFFF8D6).copy(alpha = a),
                radius = s.r,
                center = Offset(x = s.x * size.width, y = yy * size.height)
            )
        }
    }
}

private data class Sparkle(
    val x: Float,
    val y: Float,
    val r: Float,
    val speed: Float,
    val phase: Float
)

@Composable
fun MathGameOverDialog(score: Int, onRestart: () -> Unit, onHome: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Felicitări!", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50)) },
        text = { Text("Scor Final: $score ⭐", fontWeight = FontWeight.Bold) },
        confirmButton = { Button(onClick = onRestart) { Text("Din nou") } },
        dismissButton = { OutlinedButton(onClick = onHome) { Text("Acasă") } },
        containerColor = Color.White, shape = RoundedCornerShape(24.dp)
    )
}