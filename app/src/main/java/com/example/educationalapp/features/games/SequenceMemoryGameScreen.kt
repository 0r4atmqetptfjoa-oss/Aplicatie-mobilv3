package com.example.educationalapp.features.games

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.educationalapp.R
import kotlin.math.roundToInt

// ================= DATE & CONFIGURARE VOLUM =================
data class TrapGameItem(
    val id: Int,
    val imageRes: Int,
    val soundFileName: String,
    val color: Color,
    val volume: Float // Volum individual pentru mixaj
)

val gameItemsData = listOf(
    // BOOST LA URS (KICK) - Se aude tare
    TrapGameItem(0, R.drawable.urs_panda, "trap_kick_hard", Color(0xFFFF0055), 1.0f),
    // BOOST LA CAINE (SNARE) - Se aude tare
    TrapGameItem(1, R.drawable.caine_ferma, "trap_snare_crisp", Color(0xFF00E5FF), 1.0f),
    // VOLUM REDUS LA RESTUL (Ca să iasă în evidență primii)
    TrapGameItem(2, R.drawable.vulpe_polara, "trap_hihat_tick", Color(0xFFFFD600), 0.4f),
    TrapGameItem(3, R.drawable.alphabet_i_iepure, "trap_vox_hey", Color(0xFFAA00FF), 0.5f),
    // BOOST LA PISICA (PLUCK) - Se aude tare
    TrapGameItem(4, R.drawable.alphabet_p_pisica, "trap_melody_pluck", Color(0xFF00E676), 1.0f),
    TrapGameItem(5, R.drawable.alphabet_t_testoasa, "trap_fx_siren", Color(0xFFFF9100), 0.4f)
)

enum class GameStatus { IDLE, SHOWING, WAITING, GAME_OVER }

// ================= AUDIO ENGINE (MIXER INCLUS) =================
class TrapSoundEngine(private val context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    
    private val allSounds = listOf(
        "trap_kick_hard", "trap_snare_crisp", "trap_hihat_tick", 
        "trap_vox_hey", "trap_melody_pluck", "trap_fx_siren",
        "trap_sys_intro", "trap_sys_win", "trap_sys_lose", 
        "trap_sys_hint", "math_sfx_whoosh"
    )

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(20).setAudioAttributes(audioAttributes).build()
        
        allSounds.forEach { name ->
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) soundMap[name] = soundPool.load(context, resId, 1)
        }
    }

    // Redă sunetul cu volumul specificat
    fun play(name: String, volume: Float = 1.0f) { 
        val soundId = soundMap[name]
        if (soundId != null) soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }
    
    fun release() { soundPool.release() }
}

// ================= ECRANUL PRINCIPAL =================
@Composable
fun SequenceMemoryGameScreen(navController: NavHostController) {
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userIndex by remember { mutableIntStateOf(0) }
    var gameStatus by remember { mutableStateOf(GameStatus.IDLE) }
    var score by remember { mutableIntStateOf(0) }
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Animație de SHAKE (Cutremur) pentru greșeală
    val shakeOffset = remember { Animatable(0f) }

    val context = LocalContext.current
    val soundEngine = remember { TrapSoundEngine(context) }
    val scope = rememberCoroutineScope()

    // --- Background Music ---
    DisposableEffect(Unit) {
        val bgMusic = try {
            MediaPlayer.create(context, R.raw.music_trap_loop).apply {
                isLooping = true
                setVolume(0.25f, 0.25f) // Volum mic la fundal ca să se audă tobele
                start()
            }
        } catch (e: Exception) { null }
        
        onDispose {
            try { if (bgMusic?.isPlaying == true) bgMusic.stop(); bgMusic?.release() } catch (e: Exception) {}
            soundEngine.release()
        }
    }

    suspend fun triggerShake() {
        shakeOffset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                0f at 0
                (-20f) at 50
                20f at 100
                (-15f) at 150
                15f at 200
                (-10f) at 250
                10f at 300
                0f at 400
            }
        )
    }

    suspend fun playSequence(seq: List<Int>) {
        gameStatus = GameStatus.SHOWING
        delay(600)
        for (index in seq) {
            highlightedIndex = index
            val item = gameItemsData[index]
            soundEngine.play(item.soundFileName, item.volume) // Folosim volumul custom
            delay(350)
            highlightedIndex = null
            delay(100)
        }
        gameStatus = GameStatus.WAITING
        userIndex = 0
    }

    fun startNewGame() {
        soundEngine.play("trap_sys_intro")
        sequence = listOf((0 until gameItemsData.size).random())
        score = 0
        scope.launch { delay(1500); playSequence(sequence) }
    }

    fun handleInput(index: Int) {
        if (gameStatus != GameStatus.WAITING) return
        
        val item = gameItemsData[index]
        // Sunet instant cand apasa utilizatorul
        soundEngine.play(item.soundFileName, item.volume)

        if (index == sequence[userIndex]) {
            userIndex++
            if (userIndex == sequence.size) {
                score++
                soundEngine.play("trap_sys_win")
                sequence = sequence + (0 until gameItemsData.size).random()
                scope.launch { delay(1000); playSequence(sequence) }
            }
        } else {
            gameStatus = GameStatus.GAME_OVER
            soundEngine.play("trap_sys_lose")
            scope.launch { triggerShake() }
        }
    }

    // ================= LAYOUT 2026 (LANDSCAPE SPLIT) =================
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. FUNDAL
        Image(
            painter = painterResource(id = R.drawable.trap_bg_pixar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Overlay difuz
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        // 2. CONȚINUT PRINCIPAL
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // --- PARTEA STÂNGĂ: GRIDUL DE ANIMALE (Pop-in animation) ---
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(gameItemsData.size) { index ->
                        // Intarziere la animatia de intrare in functie de index
                        EnteringAnimation(delay = index * 100) {
                            TrapPadButton(
                                item = gameItemsData[index],
                                isHighlighted = highlightedIndex == index,
                                isEnabled = gameStatus == GameStatus.WAITING,
                                onClick = { handleInput(index) }
                            )
                        }
                    }
                }
            }

            // --- PARTEA DREAPTĂ: SIDEBAR CONTROLS ---
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. BACK BUTTON
                Image(
                    painter = painterResource(id = R.drawable.ui_btn_back_wood),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(60.dp)
                        .bounceClick {
                            soundEngine.play("math_sfx_whoosh")
                            navController.popBackStack()
                        }
                )

                // 2. SCORE BUBBLE
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.scale(1.2f)
                ) {
                    Card(
                        modifier = Modifier.size(width = 140.dp, height = 60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6D4C41)),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF8D6E63))
                    ) {}
                    
                    Text(
                        text = "⭐ $score",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                }

                // 3. PLAY BUTTON (Cu animatie de apasare)
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameStatus == GameStatus.IDLE || gameStatus == GameStatus.GAME_OVER) {
                        PulsingPlayButton(
                            onClick = { startNewGame() }
                        )
                    } else {
                        // Emoji mare cand asculta
                        Text("👂", fontSize = 50.sp) 
                    }
                }
            }
        }
    }
}

// ================= ANIMATED COMPONENTS =================

@Composable
fun EnteringAnimation(delay: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn()
    ) {
        content()
    }
}

@Composable
fun PulsingPlayButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Image(
        painter = painterResource(id = R.drawable.btn_main_play),
        contentDescription = "Play",
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            // Aplicam bounceClick AICI pentru efect la apasare
            .bounceClick { onClick() }
            .shadow(10.dp, CircleShape)
    )
}

@Composable
fun TrapPadButton(item: TrapGameItem, isHighlighted: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Stare locala pentru a aprinde butonul cand apasa utilizatorul (User Feedback)
    var manualHighlight by remember { mutableStateOf(false) }

    // Daca apasa utilizatorul sau e randul PC-ului
    val showGlow = isHighlighted || manualHighlight

    // Efect vizual complex
    val scale by animateFloatAsState(
        targetValue = when { 
            isPressed -> 0.85f // Se strange cand apesi
            showGlow -> 1.1f // Se mareste cand canta/lumineaza
            else -> 1f 
        }, 
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = if (showGlow) 25.dp else 5.dp, 
                shape = RoundedCornerShape(20.dp), 
                spotColor = item.color
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF222222).copy(alpha = 0.8f))
            .border(
                width = if (showGlow) 5.dp else 2.dp, 
                color = if (showGlow) item.color else Color.White.copy(alpha = 0.3f), 
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource, 
                indication = null, 
                enabled = isEnabled, 
                onClick = {
                    // Declansam flash-ul manual
                    manualHighlight = true
                    onClick()
                    // Stingem flash-ul dupa putin timp
                    // (Folosim coroutine scope-ul parintelui ar fi ideal, dar aici e un hack vizual rapid)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Resetam manualHighlight rapid ca sa fie doar un "flash"
        LaunchedEffect(manualHighlight) {
            if (manualHighlight) {
                delay(200)
                manualHighlight = false
            }
        }

        // Cerc Neon
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .background(Color.Black, CircleShape)
                .border(2.dp, if(showGlow) item.color else Color.DarkGray, CircleShape)
        )

        // Animalul
        Image(
            painter = painterResource(id = item.imageRes), 
            contentDescription = null, 
            modifier = Modifier.fillMaxSize(0.7f),
            contentScale = ContentScale.Fit
        )
        
        // Flash Light Explosion (Apare si la PC si la User acum)
        AnimatedVisibility(
            visible = showGlow,
            enter = fadeIn(tween(50)) + expandIn(tween(50)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(item.color.copy(alpha = 0.5f))
            )
        }
    }
}

// Extensie pentru click bouncy
@Composable
fun Modifier.bounceClick(onClick: () -> Unit): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, label = "bounce")

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onClick() }
        )
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
}