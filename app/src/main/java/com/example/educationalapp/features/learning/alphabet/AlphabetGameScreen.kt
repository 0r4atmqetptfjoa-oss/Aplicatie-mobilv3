package com.example.educationalapp.alphabet

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.educationalapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AlphabetGameScreen(
    onBack: () -> Unit,
    viewModel: AlphabetGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val soundPlayer = remember { AlphabetSoundPlayer(context) }
    
    LaunchedEffect(state.soundOn) { soundPlayer.isEnabled = state.soundOn }
    DisposableEffect(Unit) { onDispose { soundPlayer.stop() } }

    // --- AUDIO LOGIC ---
    LaunchedEffect(state.currentQuestion) {
        if (state.soundOn && !state.isFinished) {
            delay(500)
            soundPlayer.playLetterSound(state.currentQuestion.baseLetter.toString())
        }
    }

    LaunchedEffect(state.isAnswerCorrect) {
        if (state.soundOn && state.isAnswerCorrect != null) {
            if (state.isAnswerCorrect == true) {
                delay(2500) 
                if (state.consecutiveCorrectAnswers > 0 && state.consecutiveCorrectAnswers % 3 == 0) {
                    soundPlayer.playPositive()
                } else {
                    soundPlayer.playDing()
                }
            } else {
                soundPlayer.playNegative()
            }
        }
    }
    
    LaunchedEffect(state.isFinished) {
        if (state.isFinished && state.soundOn) {
            delay(500)
            soundPlayer.playFinish()
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paintBackground(R.drawable.bg_game_alphabet)
    ) {
        if (state.isFinished) {
            GameFinishedView(
                score = state.score,
                onRestart = { viewModel.resetGame() },
                onBack = onBack
            )
        } else {
            // CONFETTI EFFECT (Doar când răspunsul e corect)
            if (state.isAnswerCorrect == true) {
                ConfettiEffect()
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val w = maxWidth
                val h = maxHeight

                val topBarH = 56.dp
                val bottomBandH = (h * 0.40f).coerceIn(120.dp, 240.dp)
                val optionsBandH = (h - topBarH - bottomBandH).coerceAtLeast(150.dp)

                val slotW = w / 3
                val optionW = (slotW * 0.78f).coerceIn(120.dp, 220.dp)
                val optionH = (optionsBandH * 0.88f).coerceIn(140.dp, 260.dp)

                // --- MODIFICARE: Dimensiune mai mică pentru cutia albastră ---
                val letterW = (w * 0.32f).coerceIn(180.dp, 300.dp) // Era 0.44f
                val letterH = (bottomBandH * 0.65f).coerceIn(80.dp, 140.dp)
                
                val showWord = (state.isAnswerCorrect == true)
                val letterFontSize = if (showWord) 28.sp else (letterH.value * 0.55f).sp
                
                val mascotSize = (bottomBandH * 0.74f).coerceIn(96.dp, 190.dp)

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Box(modifier = Modifier.height(topBarH).fillMaxWidth()) {
                        BackButton(onBack = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                        ScorePill(state.score, Modifier.align(Alignment.CenterEnd).padding(end = 4.dp))
                    }

                    // Opțiuni
                    Box(
                        modifier = Modifier.height(optionsBandH).fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            state.options.forEachIndexed { index, item ->
                                OptionCard(
                                    item = item,
                                    isSelected = (state.selectedOptionIndex == index),
                                    isCorrect = state.isAnswerCorrect,
                                    isLocked = state.isInputLocked,
                                    width = optionW,
                                    height = optionH,
                                    onClick = {
                                        soundPlayer.playWordSound(item.word)
                                        viewModel.onOptionSelected(item, index)
                                    }
                                )
                            }
                        }
                    }

                    // Footer
                    Box(modifier = Modifier.height(bottomBandH).fillMaxWidth()) {
                        val displayText = if (state.isAnswerCorrect == true && state.selectedOptionIndex != null) {
                            state.options[state.selectedOptionIndex!!].word.replaceFirstChar { it.uppercase() }
                        } else {
                            state.currentQuestion.displayLetter.ifBlank { state.currentQuestion.baseLetter.toString() }
                        }

                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Containerul pentru literă/cuvânt
                            LetterBadge(
                                text = displayText,
                                width = letterW,
                                height = letterH,
                                fontSize = letterFontSize
                            )
                            
                            Spacer(modifier = Modifier.width(24.dp)) // Spațiu mai mare între cutie și mascotă
                            
                            // Mascota
                            Box(
                                modifier = Modifier
                                    .size(mascotSize)
                                    .clickable { soundPlayer.playLetterSound(state.currentQuestion.baseLetter.toString()) },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                MascotView(mood = state.mascotMood, size = mascotSize)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CONFETTI EFFECT (PREMIUM 2026) ---
@Composable
fun ConfettiEffect() {
    val particles = remember { List(50) { ConfettiParticle() } }
    val time = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        time.animateTo(
            targetValue = 1f,
            animationSpec = tween(2000, easing = LinearEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        particles.forEach { p ->
            val progress = time.value
            val currentY = p.initialY + (height * 1.2f * progress * p.speed)
            val currentX = p.initialX + (sin(progress * 10 + p.id) * 50f) // Mișcare ondulată
            
            // Doar dacă e încă pe ecran
            if (currentY < height && progress < 1f) {
                drawCircle(
                    color = p.color.copy(alpha = (1f - progress)),
                    radius = p.size,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

class ConfettiParticle {
    val id = Random.nextInt()
    val initialX = Random.nextFloat() * 2000f // Aproximativ lățimea ecranului
    val initialY = -100f // Pleacă de sus
    val color = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta).random()
    val size = Random.nextFloat() * 15f + 10f
    val speed = Random.nextFloat() * 0.5f + 0.8f
}

// --- UI COMPONENTS ---

@Composable
fun LetterBadge(
    text: String, 
    width: Dp, 
    height: Dp, 
    fontSize: androidx.compose.ui.unit.TextUnit, 
    modifier: Modifier = Modifier
) {
    // Design nou: Glassmorphism (Gradient subtil + Border strălucitor)
    val shape = RoundedCornerShape(24.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2196F3),
            Color(0xFF03A9F4)
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(12.dp, shape, spotColor = Color(0xFF03A9F4)) // Umbră colorată
            .background(gradient, shape)
            .border(2.dp, Color.White.copy(alpha = 0.6f), shape) // Border mai fin
    ) {
        // Animație la schimbarea textului
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                // Textul nou vine de jos, cel vechi pleacă în sus
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut())
            }, 
            label = "textAnim"
        ) { targetText ->
            Text(
                text = targetText,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(2f, 4f),
                        blurRadius = 6f
                    )
                )
            )
        }
    }
}

@Composable
fun MascotView(mood: MascotMood, size: Dp) {
    val mascotRes = when (mood) {
        MascotMood.HAPPY, MascotMood.CELEBRATE -> AlphabetUi.Mascot.happy
        MascotMood.SURPRISED -> AlphabetUi.Mascot.surprised
        MascotMood.THINKING -> AlphabetUi.Mascot.thinking
        else -> AlphabetUi.Mascot.normal
    }

    // --- ANIMATIE MASCOTA ---
    // Nu mai folosim infiniteRepeatable. Folosim tranzitii bazate pe 'mood'.
    val scaleAnim = remember { Animatable(1f) }
    
    LaunchedEffect(mood) {
        if (mood == MascotMood.HAPPY || mood == MascotMood.CELEBRATE) {
            // Jump effect!
            scaleAnim.animateTo(1.2f, animationSpec = tween(300, easing = EaseOutBack))
            scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    Image(
        painter = painterResource(id = mascotRes),
        contentDescription = "Mascot",
        modifier = Modifier
            .size(size)
            .scale(scaleAnim.value) // Se aplică doar la Happy
    )
}

@Composable
fun OptionCard(
    item: AlphabetItem,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isLocked: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    // Feedback vizual: Glow verde sau roșu
    val borderColor = when {
        isSelected && isCorrect == true -> Color(0xFF4CAF50)
        isSelected && isCorrect == false -> Color(0xFFF44336)
        else -> Color.Transparent
    }
    
    // Scale animation la click
    val targetScale = if (isSelected) 1.1f else 1f
    val scale by animateFloatAsState(targetValue = targetScale, label = "cardScale", animationSpec = spring(dampingRatio = 0.6f))

    Box(
        modifier = Modifier
            .scale(scale)
            .width(width)
            .height(height)
            .clickable(enabled = !isLocked) { onClick() }
    ) {
        // Imaginea principală
        val bitmap = rememberScaledImageBitmap(item.imageRes, maxDim = 512)
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = item.word,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (isSelected) 16.dp else 4.dp, 
                        shape = RoundedCornerShape(16.dp),
                        spotColor = borderColor // Umbra ia culoarea răspunsului
                    )
            )
        }
        
        // Border Overlay (se desenează peste)
        if (borderColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, borderColor, RoundedCornerShape(16.dp))
            )
        }
    }
}

// ... Restul componentelor (BackButton, ScorePill, GameFinishedView, PaintBackground) rămân la fel ...
@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(64.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
        Image(painter = painterResource(R.drawable.ui_btn_home), contentDescription = "Back", modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ScorePill(score: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.shadow(8.dp, RoundedCornerShape(50)).background(Color(0xFFFFD700).copy(0.9f), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Scor: $score", fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun GameFinishedView(score: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Felicitări!", fontSize = 48.sp, color = Color.White)
        Spacer(Modifier.height(32.dp))
        Row {
            Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("Restart") }
            Spacer(Modifier.width(16.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Exit") }
        }
    }
}

@Composable
fun Modifier.paintBackground(resId: Int): Modifier = this.paint(painter = painterResource(id = resId), contentScale = ContentScale.Crop)