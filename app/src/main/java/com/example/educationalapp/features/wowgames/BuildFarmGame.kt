package com.example.educationalapp.features.wowgames

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import com.example.educationalapp.R

// --- DATE ANIMALE ---
data class FarmAnimalData(
    val id: Int,
    val key: String,
    val label: String,
    @DrawableRes val resId: Int,
    @RawRes val tapSoundRes: Int,
    @RawRes val voiceInstructionRes: Int,
    val x: Float,
    val y: Float,
    val scale: Float = 1f
)

private data class FarmParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var size: Float,
    var rotation: Float,
    var color: Color
)

/**
 * --- PREMIUM AUDIO MANAGER ---
 * Prioritate: SFX taie Vocea. Vocea așteaptă liniștea.
 */
private class FarmAudioManager(private val context: Context) {
    private val soundPool: SoundPool
    private val loaded = mutableMapOf<Int, Int>()
    private var voicePlayer: MediaPlayer? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        // MaxStreams 2 permite suprapunerea sunetului de victorie peste animal
        soundPool = SoundPool.Builder().setAudioAttributes(attrs).setMaxStreams(2).build()
    }

    fun prime(@RawRes resId: Int) {
        if (loaded.containsKey(resId)) return
        loaded[resId] = soundPool.load(context, resId, 1)
    }

    fun playSFX(@RawRes resId: Int, rate: Float = 1f, vol: Float = 1f, stopVoice: Boolean = true) {
        if (stopVoice) stopVoice()
        
        val id = loaded[resId] ?: run {
            prime(resId)
            loaded[resId]
        }
        if (id != null && id != 0) {
            soundPool.play(id, vol, vol, 1, 0, rate)
        }
    }

    fun playVoice(@RawRes resId: Int) {
        stopVoice() // Safety first
        try {
            voicePlayer = MediaPlayer.create(context, resId)
            voicePlayer?.setOnCompletionListener { mp ->
                mp.release()
                if (voicePlayer === mp) voicePlayer = null
            }
            voicePlayer?.start()
        } catch (_: Exception) { }
    }

    fun stopVoice() {
        try {
            if (voicePlayer?.isPlaying == true) {
                voicePlayer?.stop()
            }
            voicePlayer?.release()
        } catch (_: Exception) { }
        voicePlayer = null
    }

    fun release() {
        soundPool.release()
        stopVoice()
    }
}

@Composable
fun BuildFarmGame(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val audio = remember { FarmAudioManager(context) }

    // --- LISTA ANIMALE COMPLETĂ ---
    // Include sunetele VECHI pentru animalele vechi și cele NOI pentru restul
    val animals = remember {
        listOf(
            // --- ANIMALELE VECHI (Sunetele tale originale) ---
            FarmAnimalData(
                id = 0, key = "duck", label = "Rățușcă",
                resId = R.drawable.ratusca,
                tapSoundRes = R.raw.rata_sunet,          // <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_duck,
                x = 0.14f, y = 0.70f, scale = 0.95f
            ),
            FarmAnimalData(
                id = 1, key = "dog", label = "Cățel",
                resId = R.drawable.caine_ferma,
                tapSoundRes = R.raw.caine_latra,         // <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_dog,
                x = 0.28f, y = 0.42f, scale = 0.86f
            ),
            FarmAnimalData(
                id = 2, key = "cat", label = "Pisică",
                resId = R.drawable.pisica_ferma,
                tapSoundRes = R.raw.friendly_cartoon_cat,// <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_cat,
                x = 0.64f, y = 0.38f, scale = 0.84f
            ),
            FarmAnimalData(
                id = 3, key = "sheep", label = "Oaie",
                resId = R.drawable.alphabet_o_oaie,
                tapSoundRes = R.raw.sheep_bleating_baa,  // <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_sheep,
                x = 0.87f, y = 0.55f, scale = 1.00f
            ),
            FarmAnimalData(
                id = 4, key = "pig", label = "Purcel",
                resId = R.drawable.porc,
                tapSoundRes = R.raw.playful_pig_oinking, // <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_pig,
                x = 0.44f, y = 0.72f, scale = 1.06f
            ),
            FarmAnimalData(
                id = 5, key = "chick", label = "Pui",
                resId = R.drawable.pui_gaina,
                tapSoundRes = R.raw.baby_chick_peeping,  // <--- Sunet original
                voiceInstructionRes = R.raw.instr_find_chicken,
                x = 0.72f, y = 0.80f, scale = 1.14f
            ),

            // --- ANIMALELE NOI (Cu sunetele Funny) ---
            FarmAnimalData(
                id = 6, key = "cow", label = "Vacă",
                resId = R.drawable.vaca,
                tapSoundRes = R.raw.sfx_vaca_muu_lung,
                voiceInstructionRes = R.raw.instr_vaca_diva,
                x = 0.08f, y = 0.48f, scale = 1.25f
            ),
            FarmAnimalData(
                id = 7, key = "donkey", label = "Măgar",
                resId = R.drawable.magar,
                tapSoundRes = R.raw.sfx_magar_iha_rade,
                voiceInstructionRes = R.raw.instr_magar_confuz,
                x = 0.52f, y = 0.50f, scale = 1.10f
            ),
            FarmAnimalData(
                id = 8, key = "goose", label = "Gâscă",
                resId = R.drawable.gasca,
                tapSoundRes = R.raw.sfx_gasca_honk_alert,
                voiceInstructionRes = R.raw.instr_gasca_sefa,
                x = 0.92f, y = 0.75f, scale = 0.98f
            )
        )
    }

    LaunchedEffect(Unit) {
        animals.forEach { a -> audio.prime(a.tapSoundRes) }
    }

    var targetIdx by remember { mutableIntStateOf(Random.nextInt(animals.size)) }
    
    // Variabilă de stare pentru timer-ul de liniște (reparat tipul Job?)
    var idleVoiceJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleInstruction(delayMs: Long = 4000) {
        idleVoiceJob?.cancel()
        idleVoiceJob = scope.launch {
            delay(delayMs)
            if (isActive) {
                audio.playVoice(animals[targetIdx].voiceInstructionRes)
            }
        }
    }

    LaunchedEffect(Unit) {
        scheduleInstruction(delayMs = 1500)
    }

    // State grafică
    var t by remember { mutableFloatStateOf(0f) }
    val particles = remember { mutableStateListOf<FarmParticle>() }
    val parallaxX by animateFloatAsState(targetValue = sin(t * 0.18f) * 12f, label = "pX")
    val parallaxY by animateFloatAsState(targetValue = cos(t * 0.14f) * 8f, label = "pY")

    // Game Loop
    LaunchedEffect(Unit) {
        var last = 0L
        while (isActive) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                last = now
                t += dt

                val it = particles.listIterator()
                while (it.hasNext()) {
                    val p = it.next()
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.vy += 260f * dt
                    p.rotation += 140f * dt
                    p.life -= dt
                    p.size *= (1f - 0.9f * dt)
                    if (p.life <= 0f) it.remove()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val hPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val baseSizePx = (max(wPx, hPx) * 0.15f).coerceIn(120f, 340f)

        // Background
        Image(
            painter = painterResource(R.drawable.bg_farm_landscape),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = parallaxX
                    translationY = parallaxY
                    scaleX = 1.03f
                    scaleY = 1.03f
                },
            contentScale = ContentScale.Crop
        )

        // Vignette
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
                    center = androidx.compose.ui.geometry.Offset(wPx * 0.5f, hPx * 0.55f),
                    radius = min(wPx, hPx) * 0.85f
                )
            )
        )

        // Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val a = p.life.coerceIn(0f, 1f)
                withTransform({ translate(p.x, p.y); rotate(p.rotation) }) {
                    drawRect(p.color.copy(alpha = a), androidx.compose.ui.geometry.Offset(-p.size/2f, -p.size/2f), androidx.compose.ui.geometry.Size(p.size, p.size))
                }
            }
        }

        // --- ANIMALE ---
        animals.forEachIndexed { idx, a ->
            key(a.id) {
                val tapScale = remember { Animatable(1f) }
                val tapRot = remember { Animatable(0f) }
                val wrongShake = remember { Animatable(0f) }
                val phase = remember { Random.nextFloat() * (2f * PI.toFloat()) }
                
                // --- PREMIUM ANIMATION LOGIC: SQUASH & STRETCH ---
                val floatCycle = sin(t * 1.8f + phase)
                val floatAmp = baseSizePx * 0.035f
                val floatY = floatCycle * floatAmp

                val stretchFactor = 0.04f
                val scaleBreathX = 1f - (floatCycle * stretchFactor) 
                val scaleBreathY = 1f + (floatCycle * stretchFactor)

                val xPx = (wPx * a.x) + wrongShake.value
                val yPx = (hPx * a.y) + floatY
                
                val depthScale = (0.78f + a.y.coerceIn(0.2f, 0.95f) * 0.35f)
                val finalSize = baseSizePx * a.scale * depthScale

                Image(
                    painter = painterResource(a.resId),
                    contentDescription = a.label,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(with(density) { finalSize.toDp() })
                        .offset { IntOffset((xPx - finalSize/2f).roundToInt(), (yPx - finalSize/2f).roundToInt()) }
                        .graphicsLayer { 
                            rotationZ = tapRot.value 
                            scaleX = tapScale.value * scaleBreathX
                            scaleY = tapScale.value * scaleBreathY
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.85f)
                        }
                        .pointerInput(idx) {
                            detectTapGestures {
                                // --- LOGICĂ JOC & AUDIO ---
                                audio.stopVoice()
                                idleVoiceJob?.cancel()

                                // Redăm sunetul specific (Original sau Nou)
                                audio.playSFX(a.tapSoundRes, rate = 1f + Random.nextFloat() * 0.06f)

                                scope.launch { 
                                    tapScale.snapTo(0.85f)
                                    tapScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
                                }
                                scope.launch { 
                                    val dir = if (Random.nextBoolean()) 1f else -1f
                                    tapRot.snapTo(0f)
                                    tapRot.animateTo(15f*dir, spring(dampingRatio = 0.5f, stiffness = 400f)) 
                                    tapRot.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 500f)) 
                                }

                                if (idx == targetIdx) {
                                    // --- WIN ---
                                    scope.launch {
                                        delay(800)
                                        val winRaw = context.resources.getIdentifier("math_sfx_win_short", "raw", context.packageName)
                                        if (winRaw != 0) audio.playSFX(winRaw, vol = 0.6f, stopVoice = true)

                                        val burstColors = listOf(Color(0xFFFFD24A), Color(0xFF7CE0FF), Color(0xFFFF7AD9), Color(0xFF8BFF8B))
                                        repeat(40) {
                                            val ang = Random.nextFloat() * 6.28f
                                            val sp = 450f + Random.nextFloat() * 400f
                                            particles.add(FarmParticle(
                                                x = xPx, y = yPx, 
                                                vx = cos(ang)*sp, vy = sin(ang)*sp - 300f, 
                                                life = 0.8f+Random.nextFloat()*0.5f, 
                                                size = 10f+Random.nextFloat()*12f, 
                                                rotation = Random.nextFloat()*360f, 
                                                color = burstColors.random()
                                            ))
                                        }

                                        delay(2000)
                                        targetIdx = Random.nextInt(animals.size)
                                        scheduleInstruction(delayMs = 1000)
                                    }
                                } else {
                                    // --- WRONG ---
                                    scope.launch {
                                        wrongShake.snapTo(0f)
                                        wrongShake.animateTo(20f, spring(dampingRatio = 0.4f, stiffness = 600f))
                                        wrongShake.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 900f))
                                        scheduleInstruction(delayMs = 4000)
                                    }
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Back Btn
        val backIcon = context.resources.getIdentifier("ui_btn_back_wood", "drawable", context.packageName)
        if (backIcon != 0) {
            Image(
                painter = painterResource(backIcon), contentDescription = "Back",
                modifier = Modifier.padding(14.dp).size(64.dp).align(Alignment.TopStart)
                    .pointerInput(Unit) { detectTapGestures { onBack() } }
            )
        }
    }
}