package com.example.educationalapp.features.wowgames

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educationalapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

// -------------------- DATA --------------------

val BubbleTints = listOf(
    Color(0xFFFFCDD2), Color(0xFFE1BEE7), Color(0xFFBBDEFB),
    Color(0xFFC8E6C9), Color(0xFFFFE0B2), Color(0xFFFFF9C4)
)

data class MathPack(
    val audioFile: String,
    val questionText: String,
    val correctKey: String,
    val distractors: List<String>
)

private fun generatePacks(): List<MathPack> {
    return listOf(
        MathPack("math_q_find_3", "Unde este cifra 3?", "3", listOf("1", "8", "6", "9")),
        MathPack("math_calc_1plus1", "Cât fac 1 + 1 ?", "2", listOf("5", "3", "4", "0")),
        MathPack("math_q_find_5", "Găsește cifra 5", "5", listOf("2", "0", "3", "1")),
        MathPack("math_calc_2plus2", "Cât fac 2 + 2 ?", "4", listOf("1", "3", "5", "6")),
        MathPack("math_find_kitten", "Unde este pisicuța?", "kitten", listOf("puppy", "monkey", "apple")),
        MathPack("math_find_apple", "Găsește mărul roșu!", "apple", listOf("banana", "orange", "strawberry")),
        MathPack("math_find_puppy", "Unde este cățelușul?", "puppy", listOf("kitten", "monkey", "0")),
        MathPack("math_find_banana", "Care este banana?", "banana", listOf("apple", "orange", "strawberry")),
        MathPack("math_find_monkey", "Găsește maimuțica!", "monkey", listOf("kitten", "puppy", "balloon")),
        MathPack("math_find_star", "Unde este steluța?", "star", listOf("balloon", "orange", "2"))
    )
}

// -------------------- PHYSICS --------------------

data class PhysicsBall(
    val id: Int,
    val key: String,
    var x: Float, var y: Float,
    var vx: Float = 0f, var vy: Float = 0f,
    val radius: Float,
    val color: Color,
    var scale: Float = 0f,
    var shakeOffset: Float = 0f,
    var squash: Float = 0f,
    var trailTimer: Int = 0,
    var floatPhase: Float = Random.nextFloat() * 2f * PI.toFloat()
) {
    fun update(gravX: Float, gravY: Float, dt: Float, tick: Int) {
        floatPhase += 0.018f
        val floatX = sin(floatPhase) * 0.14f
        val floatY = cos(floatPhase * 0.7f) * 0.14f

        val accelPx = radius * 2.6f
        vx += (gravX + floatX) * accelPx * dt
        vy += (gravY + floatY) * accelPx * dt

        val damping = exp(-1.9f * dt).coerceIn(0f, 1f)
        vx *= damping
        vy *= damping

        val maxSpeed = radius * 2.0f
        val sp = hypot(vx, vy)
        if (sp > maxSpeed && sp > 0f) {
            val k = maxSpeed / sp
            vx *= k
            vy *= k
        }

        x += vx * dt
        y += vy * dt

        squash *= exp(-7.0f * dt)
        
        if (abs(shakeOffset) > 0.1f) shakeOffset *= 0.85f else shakeOffset = 0f
        trailTimer++
    }

    fun constrain(bounds: Rect) {
        val restitution = 0.83f
        if (x - radius < bounds.left) { x = bounds.left + radius; vx = abs(vx) * restitution }
        if (x + radius > bounds.right) { x = bounds.right - radius; vx = -abs(vx) * restitution }
        if (y - radius < bounds.top) { y = bounds.top + radius; vy = abs(vy) * restitution }
        if (y + radius > bounds.bottom) { y = bounds.bottom - radius; vy = -abs(vy) * restitution }
    }
}

data class MazeParticle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var life: Float, var scale: Float, var rotation: Float, var color: Color,
    val isTrail: Boolean = false
)

// -------------------- AUDIO (PREMIUM) --------------------

class MathAudioManager(private val context: Context) {
    private val soundPool: SoundPool
    private var voicePlayer: MediaPlayer? = null
    
    // Background Music Player (local control for ducking)
    private var musicPlayer: MediaPlayer? = null
    
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private var isVoicePlaying: Boolean = false
    private var pendingVoice: Pair<String, String>? = null
    
    // Ducking animation
    private val scope = CoroutineScope(Dispatchers.Main)
    private var duckJob: Job? = null

    val sfxBounce: Int
    val sfxPop: Int
    val sfxWin: Int
    val sfxWrong: Int
    val sfxWhoosh: Int

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build()

        fun load(name: String): Int {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            return if (id != 0) soundPool.load(context, id, 1) else 0
        }

        sfxBounce = load("math_sfx_bounce")
        sfxPop = load("sfx_bubble_pop")
        sfxWin = load("math_sfx_win_short")
        sfxWrong = load("math_sfx_wrong")
        sfxWhoosh = load("math_sfx_whoosh")

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ro", "RO")
                tts?.setSpeechRate(0.9f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isVoicePlaying = true }
                    override fun onDone(utteranceId: String?) { handleVoiceEnd() }
                    @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { handleVoiceEnd() }
                    override fun onError(utteranceId: String?, errorCode: Int) { handleVoiceEnd() }
                })
                ttsReady = true
            }
        }
    }

    fun startMusic() {
        if (musicPlayer == null) {
            val resId = context.resources.getIdentifier("math_bg_music", "raw", context.packageName)
            if (resId != 0) {
                musicPlayer = MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(0.4f, 0.4f) // Base volume
                    start()
                }
            }
        }
    }

    private fun setMusicVolume(vol: Float) {
        musicPlayer?.setVolume(vol, vol)
    }

    fun playSFX(soundId: Int, rate: Float = 1.0f, vol: Float = 1.0f) {
        if (soundId != 0) soundPool.play(soundId, vol, vol, 1, 0, rate)
    }

    fun playVoice(audioName: String, fallbackText: String, interrupt: Boolean = true) {
        if (!interrupt && isVoicePlaying) {
            pendingVoice = audioName to fallbackText
            return
        }

        if (interrupt) {
            pendingVoice = null
            stopVoice()
        }

        // --- AUDIO DUCKING START ---
        setMusicVolume(0.05f) // Drop music to 5%
        // ---------------------------

        val resId = context.resources.getIdentifier(audioName, "raw", context.packageName)
        if (resId != 0) {
            try {
                voicePlayer = MediaPlayer.create(context, resId)
                isVoicePlaying = true
                voicePlayer?.setOnCompletionListener {
                    it.release()
                    voicePlayer = null
                    handleVoiceEnd()
                }
                voicePlayer?.start()
            } catch (e: Exception) {
                handleVoiceEnd() // Restore music if fail
                e.printStackTrace()
            }
        } else {
            if (ttsReady) {
                tts?.speak(fallbackText, TextToSpeech.QUEUE_FLUSH, null, "math_tts")
            } else {
                handleVoiceEnd() // No voice -> restore music immediately
            }
        }
    }

    private fun stopVoice() {
        try {
            voicePlayer?.stop()
            voicePlayer?.release()
        } catch (_: Exception) {}
        voicePlayer = null
        tts?.stop()
        isVoicePlaying = false
    }

    private fun handleVoiceEnd() {
        isVoicePlaying = false
        
        // Dacă avem coadă, redăm următorul (muzica rămâne jos)
        val next = pendingVoice
        pendingVoice = null
        if (next != null) {
            // Rulează pe UI thread pt siguranță
            scope.launch {
                playVoice(next.first, next.second, interrupt = true)
            }
        } else {
            // Nu mai e nimic de zis -> Ridicăm muzica înapoi
            scope.launch {
                // Fade in lent
                val steps = 10
                for (i in 1..steps) {
                    val v = 0.05f + (0.35f * (i.toFloat() / steps)) // Target 0.4f
                    setMusicVolume(v)
                    delay(50)
                }
            }
        }
    }

    fun release() {
        soundPool.release()
        voicePlayer?.release()
        musicPlayer?.release()
        tts?.stop()
        tts?.shutdown()
    }
}

// -------------------- GAME UI --------------------

@Composable
fun NumbersMazeGame(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val audio = remember { MathAudioManager(context) }

    val bgImage = painterResource(id = R.drawable.bg_game_math)
    val bubbleBase = ImageBitmap.imageResource(id = R.drawable.img_bubble_glossy)
    val sparkleImg = ImageBitmap.imageResource(id = R.drawable.fx_star_sparkle)
    val backIcon = painterResource(id = R.drawable.ui_btn_back_wood)

    val contentBitmaps = remember(context) {
        val map = mutableMapOf<String, ImageBitmap>()
        for (i in 0..9) {
            val id = context.resources.getIdentifier("img_number_$i", "drawable", context.packageName)
            if (id != 0) map["$i"] = resizeBitmap(ImageBitmap.imageResource(context.resources, id), 256, 256)
        }
        val extraItems = mapOf(
            "apple" to "img_math_apple", "banana" to "img_math_banana",
            "orange" to "img_math_orange", "strawberry" to "img_math_strawberry",
            "kitten" to "img_math_kitten", "puppy" to "img_math_puppy",
            "monkey" to "img_monkey", "balloon" to "img_math_balloon", "star" to "img_math_star"
        )
        for ((key, filename) in extraItems) {
            var id = context.resources.getIdentifier(filename, "drawable", context.packageName)
            if (id == 0) id = context.resources.getIdentifier(filename.replace("img_math_", "img_"), "drawable", context.packageName)
            if (id != 0) map[key] = resizeBitmap(ImageBitmap.imageResource(context.resources, id), 256, 256)
        }
        map
    }

    val packs = remember { generatePacks().shuffled() }
    var levelIndex by remember { mutableIntStateOf(0) }
    var isWin by remember { mutableStateOf(false) }
    
    val balls = remember { mutableStateListOf<PhysicsBall>() }
    val particles = remember { mutableStateListOf<MazeParticle>() }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tick by remember { mutableIntStateOf(0) }
    
    var targetGravX by remember { mutableFloatStateOf(0f) }
    var targetGravY by remember { mutableFloatStateOf(0f) }
    var gravX by remember { mutableFloatStateOf(0f) }
    var gravY by remember { mutableFloatStateOf(0f) }

    var bgParallax by remember { mutableStateOf(Offset.Zero) }
    var lastInteractionMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(Unit) {
        audio.startMusic() // Pornim muzica locală cu control de volum
        audio.playVoice("math_intro", "Bine ai venit!")

        onDispose {
            audio.release()
        }
    }

    fun startLevel() {
        if (canvasSize == IntSize.Zero) return
        balls.clear()
        val pack = packs[levelIndex]
        val options = (listOf(pack.correctKey) + pack.distractors).shuffled()
        
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        
        // --- DIMENSIUNI NOI MAI MICI (Reduced Sizes) ---
        val maxDim = max(w, h)
        val radiusScale = when {
            options.size <= 4 -> 0.125f // Era 0.145
            options.size <= 6 -> 0.115f // Era 0.130
            else -> 0.105f              // Era 0.118
        }
        val radius = maxDim * radiusScale

        val margin = (radius * 1.25f).coerceAtLeast(24f)
        val safeLeft = margin.toInt().coerceAtLeast(0)
        val safeTop = (h * 0.22f).toInt().coerceAtLeast(0)
        val safeRight = (w - margin).toInt().coerceAtLeast(safeLeft + 1)
        val safeBottom = (h - margin).toInt().coerceAtLeast(safeTop + 1)
        val safeRect = Rect(safeLeft, safeTop, safeRight, safeBottom)

        options.forEachIndexed { i, key ->
            var bx = 0f; var by = 0f; var tries = 0
            while(tries < 100) {
                bx = Random.nextFloat() * safeRect.width() + safeRect.left
                by = Random.nextFloat() * safeRect.height() + safeRect.top
                if (balls.none { dist(it.x, it.y, bx, by) < (radius*2.1f) }) break
                tries++
            }
            if (tries >= 100) { bx = safeRect.centerX().toFloat(); by = safeRect.centerY().toFloat() }
            val b = PhysicsBall(i, key, bx, by, radius = radius, color = BubbleTints[i % BubbleTints.size])
            val drift = radius * 0.18f
            b.vx = (Random.nextFloat() - 0.5f) * drift
            b.vy = (Random.nextFloat() - 0.5f) * drift
            balls.add(b)
        }
        audio.playVoice(pack.audioFile, pack.questionText, interrupt = false)
    }

    LaunchedEffect(canvasSize) {
        if (canvasSize != IntSize.Zero) { delay(500); startLevel() }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0f else (now - lastNanos) / 1e9f
                lastNanos = now
                val frameScale = (dt * 60f).coerceIn(0f, 3f)

                if (!isWin) {
                    val idleMs = System.currentTimeMillis() - lastInteractionMs
                    if (idleMs > 120) {
                        val decay = exp(-4.2f * dt).coerceIn(0f, 1f)
                        targetGravX *= decay
                        targetGravY *= decay
                    }

                    val gravSmooth = 1f - exp(-10f * dt).coerceIn(0f, 1f)
                    gravX += (targetGravX - gravX) * gravSmooth
                    gravY += (targetGravY - gravY) * gravSmooth

                    val parallaxTarget = Offset(-gravX * 22f, -gravY * 22f)
                    val parallaxSmooth = 1f - exp(-6f * dt).coerceIn(0f, 1f)
                    bgParallax = Offset(
                        x = bgParallax.x + (parallaxTarget.x - bgParallax.x) * parallaxSmooth,
                        y = bgParallax.y + (parallaxTarget.y - bgParallax.y) * parallaxSmooth
                    )

                    val bounds = Rect(0, 0, canvasSize.width, canvasSize.height)
                    for (i in balls.indices) {
                        val b = balls[i]
                        if (b.scale < 1f) b.scale = min(1f, b.scale + 0.08f * frameScale)
                        b.update(gravX, gravY, dt, tick)
                        b.constrain(bounds)

                        if (tick % 6 == 0) {
                            val bc = b.color
                            val trailColor = Color(
                                red = bc.red * 0.75f + 0.25f,
                                green = bc.green * 0.75f + 0.25f,
                                blue = bc.blue * 0.75f + 0.25f,
                                alpha = 0.55f
                            )
                            particles.add(
                                MazeParticle(
                                    x = b.x,
                                    y = b.y,
                                    vx = (Random.nextFloat() - 0.5f) * 14f,
                                    vy = (Random.nextFloat() - 0.5f) * 14f,
                                    life = 0.55f,
                                    scale = 0.35f,
                                    rotation = Random.nextFloat() * 360f,
                                    color = trailColor,
                                    isTrail = true
                                )
                            )
                        }
                        
                        for (j in i + 1 until balls.size) {
                            val other = balls[j]
                            val d = dist(b.x, b.y, other.x, other.y)
                            val minDist = b.radius + other.radius
                            if (d < minDist) {
                                val impact = hypot(b.vx - other.vx, b.vy - other.vy)
                                val impactNorm = (impact / (b.radius * 1.6f)).coerceIn(0f, 1f)
                                b.squash = max(b.squash, impactNorm)
                                other.squash = max(other.squash, impactNorm)

                                if (impactNorm > 0.18f && tick % 12 == 0) {
                                    audio.playSFX(
                                        audio.sfxBounce,
                                        rate = 0.95f + Random.nextFloat() * 0.18f,
                                        vol = min(1f, impactNorm)
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }

                                resolveCollision(b, other, d, minDist)
                            }
                        }
                    }

                    if (tick % 40 == 0 && canvasSize != IntSize.Zero) {
                        particles.add(
                            MazeParticle(
                                x = Random.nextFloat() * canvasSize.width,
                                y = Random.nextFloat() * canvasSize.height,
                                vx = (Random.nextFloat() - 0.5f) * 8f,
                                vy = (Random.nextFloat() - 0.5f) * 8f,
                                life = 1.2f,
                                scale = 0.18f,
                                rotation = Random.nextFloat() * 360f,
                                color = Color(1f, 1f, 1f, 0.35f),
                                isTrail = false
                            )
                        )
                    }
                    
                    val pIt = particles.iterator()
                    while(pIt.hasNext()) {
                        val p = pIt.next()
                        if (!p.isTrail) {
                            p.x += p.vx * frameScale; p.y += p.vy * frameScale
                            p.rotation += 5f * frameScale; p.life -= 0.03f * frameScale
                            p.scale *= 0.98f
                        } else {
                            p.life -= 0.05f * frameScale
                            p.scale *= 0.9f
                        }
                        if (p.life <= 0) pIt.remove()
                    }
                }
                tick++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { canvasSize = it }) {
        Image(
            painter = bgImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = bgParallax.x
                    translationY = bgParallax.y
                    scaleX = 1.06f
                    scaleY = 1.06f
                }
        )
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(levelIndex, canvasSize) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val start = down.position
                        var last = start
                        var total = Offset.Zero
                        var isDrag = false
                        val slop = viewConfiguration.touchSlop

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val delta = change.position - last
                            total += delta

                            if (!isDrag && total.getDistance() > slop) {
                                isDrag = true
                            }

                            if (isDrag && canvasSize != IntSize.Zero) {
                                lastInteractionMs = System.currentTimeMillis()
                                val drag = change.position - start
                                val maxDrag = min(canvasSize.width, canvasSize.height).toFloat() * 0.28f
                                targetGravX = (drag.x / maxDrag).coerceIn(-1f, 1f) * 1.7f
                                targetGravY = (drag.y / maxDrag).coerceIn(-1f, 1f) * 1.7f
                                change.consume()
                            }
                            last = change.position
                        }

                        if (!isDrag) {
                            lastInteractionMs = System.currentTimeMillis()
                            val clicked = balls.find { dist(it.x, it.y, start.x, start.y) < it.radius * 1.35f }
                            clicked?.let { b ->
                                if (b.key == packs[levelIndex].correctKey) {
                                    audio.playSFX(audio.sfxPop); audio.playSFX(audio.sfxWin)
                                    repeat(35) { 
                                        particles.add(MazeParticle(
                                            b.x, b.y, 
                                            (Random.nextFloat()-0.5f)*25f, 
                                            (Random.nextFloat()-0.5f)*25f, 
                                            1.2f, Random.nextFloat()*0.8f+0.4f, 
                                            Random.nextFloat()*360f, b.color
                                        )) 
                                    }
                                    if (levelIndex < packs.size - 1) { levelIndex++; startLevel() }
                                    else { isWin = true; balls.clear(); audio.playVoice("math_win", "Ai câștigat!") }
                                } else {
                                    audio.playSFX(audio.sfxWrong)
                                    b.vx = (Random.nextFloat() - 0.5f) * 40f
                                    b.vy = -35f
                                    b.shakeOffset = 30f
                                }
                            }
                        }
                    }
                }
        ) {
            particles.forEach { p ->
                val alpha = p.life.coerceIn(0f, 1f)
                withTransform({
                    translate(p.x, p.y)
                    rotate(p.rotation)
                    scale(p.scale * min(1f, p.life))
                }) {
                    drawImage(image = sparkleImg, dstOffset = IntOffset(-40, -40), dstSize = IntSize(80, 80), alpha = alpha)
                }
            }

            val maxDimPx = max(size.width, size.height)

            balls.forEach { b ->
                val shake = if (b.shakeOffset > 0) sin(tick * 0.9f) * b.shakeOffset else 0f
                val cx = b.x + shake
                val cy = b.y

                val hintScale = if (
                    !isWin && (System.currentTimeMillis() - lastInteractionMs) > 4000 && b.key == packs[levelIndex].correctKey
                ) 1f + 0.04f * sin(tick * 0.2f) else 1f

                val base = b.scale * hintScale
                val s = b.squash.coerceIn(0f, 1f)
                val scaleX = base * (1f + s * 0.08f)
                val scaleY = base * (1f - s * 0.06f)

                scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset(cx, cy)) {
                    val r = b.radius
                    val offset = IntOffset((cx - r).toInt(), (cy - r).toInt())
                    val dstSize = IntSize((r * 2).toInt(), (r * 2).toInt())

                    val clip = Path().apply {
                        addOval(ComposeRect(cx - r, cy - r, cx + r, cy + r))
                    }

                    clipPath(clip) {
                        drawImage(
                            image = bubbleBase,
                            dstOffset = offset,
                            dstSize = dstSize,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                b.color,
                                androidx.compose.ui.graphics.BlendMode.Modulate
                            )
                        )

                        contentBitmaps[b.key]?.let { contentBmp ->
                            val desired = (maxDimPx * 0.20f).toInt()
                            // --- CONTENT MAI MIC ---
                            // Am scăzut multiplicatorul de la 1.48f la 1.35f
                            // Asta face ca obiectul din interior (maimuță, măr etc.) să fie puțin mai mic și să aibă margini mai largi în bulă.
                            val cap = (r * 1.35f).toInt() 
                            val cSize = min(desired, cap)
                            val wobble = sin(tick * 0.07f + b.id) * 3.0f
                            withTransform({ rotate(wobble, pivot = Offset(cx, cy)) }) {
                                drawImage(
                                    image = contentBmp,
                                    dstOffset = IntOffset((cx - cSize / 2).toInt(), (cy - cSize / 2).toInt()),
                                    dstSize = IntSize(cSize, cSize)
                                )
                            }
                        }

                        drawCircle(
                            color = Color.White.copy(alpha = 0.18f),
                            radius = r * 0.55f,
                            center = Offset(cx - r * 0.25f, cy - r * 0.35f),
                            blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                        )
                    }
                }
            }
        }

        if (isWin) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)).pointerInput(Unit) { detectTapGestures { levelIndex=0; isWin=false; startLevel() } }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText("🏆 VICTORIE! 🏆", style = TextStyle(color = Color(0xFFFFD700), fontSize = 60.sp, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(20.dp))
                    BasicText("Atinge pentru a juca din nou", style = TextStyle(color = Color.White, fontSize = 24.sp))
                }
            }
        }
        Box(Modifier.padding(16.dp).align(Alignment.TopStart)) {
            Image(painter = backIcon, contentDescription = "Back", modifier = Modifier.size(72.dp).pointerInput(Unit) { detectTapGestures { audio.playSFX(audio.sfxWhoosh); onBack() } })
        }
    }
}

fun resizeBitmap(img: ImageBitmap, w: Int, h: Int): ImageBitmap {
    val bmp = img.asAndroidBitmap()
    return Bitmap.createScaledBitmap(bmp, w, h, true).asImageBitmap()
}
fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt((x2-x1).pow(2) + (y2-y1).pow(2))
fun resolveCollision(b1: PhysicsBall, b2: PhysicsBall, dist: Float, minDist: Float) {
    val d = dist.coerceAtLeast(0.001f)
    val nx = (b2.x - b1.x) / d
    val ny = (b2.y - b1.y) / d
    val overlap = (minDist - dist) * 0.5f
    b1.x -= nx * overlap; b1.y -= ny * overlap
    b2.x += nx * overlap; b2.y += ny * overlap
    val rvx = b2.vx - b1.vx; val rvy = b2.vy - b1.vy
    val vn = rvx * nx + rvy * ny
    if (vn < 0) {
        val j = -(2.0f) * vn * 0.5f
        b1.vx -= j * nx; b1.vy -= j * ny
        b2.vx += j * nx; b2.vy += j * ny
    }
}