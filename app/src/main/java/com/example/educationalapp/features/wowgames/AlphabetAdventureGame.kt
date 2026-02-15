package com.example.educationalapp.features.wowgames

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.educationalapp.R
import com.example.educationalapp.common.LocalSoundManager
import com.example.educationalapp.di.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.*
import kotlin.random.Random

// Needed for flipping plane images
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * AlphabetAdventureGame 3.1 – REPARAT & PREMIUM
 * - Include GlassPill si SpeechBubble (fix build error)
 * - Audio Secvential (fara suprapuneri)
 * - Fizica Plutire Lenta (fara tremurat)
 */

// -------------------- ENGINE DATA --------------------

private class Bubble(
    var x: Float,
    var y: Float,
    val char: Char,
    val speed: Float,
    val initialX: Float,
    val wobbleOffset: Float,     // Decalaj pentru miscarea pe X
    val floatOffset: Float,      // Decalaj pentru plutirea pe Y
    var scale: Float = 0f, 
    var targetScale: Float = 1f,
    var alpha: Float = 1f,
    var popping: Boolean = false,
    var popT: Float = 0f,
    val tintColor: Color
)

private class SparkleParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, 
    val color: Color
)

private class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    var life: Float = 1f, 
    val color: Color
)

// Represents a decorative airplane flying across the screen. Each plane moves horizontally
// at a fixed velocity and has its own bitmap image. Planes are spawned at random
// intervals near the top of the screen and removed once they leave the visible area.
private class Plane(
    var x: Float,
    var y: Float,
    var vx: Float,
    val bitmap: ImageBitmap
)

private val BubbleColors = listOf(
    Color(0xFFFFCDD2), Color(0xFFBBDEFB), Color(0xFFC8E6C9),
    Color(0xFFFFF9C4), Color(0xFFE1BEE7), Color(0xFFB2EBF2)
)

private val PraiseWords = listOf("Bravo!", "Super!", "Uau!", "Corect!", "Yey!", "Genial!")

// Factor used to determine the on‑screen size of airplane sprites relative to the
// diameter of bubbles. A value less than 1.0 means planes are smaller than
// bubbles. For example, 0.7f means the plane width will be 70% of the bubble
// diameter.
private const val PLANE_SIZE_FACTOR = 0.7f

private enum class GamePhase { INTRO, PLAY, REWARD }
private enum class MascotMood { IDLE, TALK, SAD, CHEER, HINT }

// -------------------- HELPERS --------------------

private fun loadRaw(context: Context, name: String): Int =
    context.resources.getIdentifier(name, "raw", context.packageName)

private fun loadDrawable(context: Context, name: String): Int =
    context.resources.getIdentifier(name, "drawable", context.packageName)

private fun loadDrawableFirst(context: Context, vararg names: String): Int {
    for (n in names) {
        val id = loadDrawable(context, n)
        if (id != 0) return id
    }
    return 0
}

@Composable
private fun painterByAnyNameOrNull(vararg names: String): Painter? {
    val context = LocalContext.current
    val id = remember(*names) { loadDrawableFirst(context, *names) }
    return if (id != 0) painterResource(id) else null
}

@Composable
private fun bitmapByAnyNameOrNull(vararg names: String): ImageBitmap? {
    val context = LocalContext.current
    val id = remember(*names) { loadDrawableFirst(context, *names) }
    return if (id != 0) ImageBitmap.imageResource(id) else null
}


// -------------------- RANDOMIZATION --------------------

private class AlphabetDeck {
    private var deck: MutableList<Char> = mutableListOf()
    private var index = 0
    private var last: Char? = null

    fun next(): Char {
        if (deck.isEmpty() || index >= deck.size) {
            deck = ('A'..'Z').toMutableList().apply { shuffle() }
            index = 0
            if (last != null && deck.size > 1 && deck.first() == last) {
                val swapIdx = 1 + Random.nextInt(deck.size - 1)
                val tmp = deck[0]
                deck[0] = deck[swapIdx]
                deck[swapIdx] = tmp
            }
        }
        val c = deck[index++]
        last = c
        return c
    }
}

private fun randomNonTarget(target: Char, last: Char? = null): Char {
    var c: Char
    var guard = 0
    do {
        c = ('A'.code + Random.nextInt(26)).toChar()
        guard++
    } while ((c == target || c == last) && guard < 20)
    return c
}

// -------------------- AUDIO SUBSYSTEMS --------------------

private class SfxLimiter {
    private val lastPlayMs = HashMap<Int, Long>()
    fun play(soundManager: SoundManager, resId: Int, cooldownMs: Long = 80L) {
        if (resId == 0) return
        val now = System.currentTimeMillis()
        val last = lastPlayMs[resId] ?: 0L
        if (now - last < cooldownMs) return
        lastPlayMs[resId] = now
        soundManager.playSound(resId) 
    }
}

private class LoopingMusicPlayer(private val context: Context) {
    private var player: ExoPlayer? = null
    private var currentResId: Int = 0
    private var currentVolume: Float = 0f

    fun playLoop(resId: Int, startVolume: Float = 0f) {
        if (resId == 0) return
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(C.USAGE_GAME)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(attrs, true)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = startVolume
            }
        }
        val p = player ?: return
        if (currentResId == resId && p.isPlaying) return

        currentResId = resId
        currentVolume = startVolume.coerceIn(0f, 1f)
        p.volume = currentVolume

        try {
            p.stop()
            p.clearMediaItems()
            val uri = RawResourceDataSource.buildRawResourceUri(resId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.prepare()
            p.play()
        } catch (_: Exception) { }
    }

    suspend fun fadeTo(targetVolume: Float, durationMs: Long = 800L) {
        val p = player ?: return
        val from = currentVolume
        val to = targetVolume.coerceIn(0f, 1f)
        val steps = (durationMs / 32L).toInt().coerceAtLeast(1)
        for (i in 1..steps) {
            val t = i.toFloat() / steps.toFloat()
            currentVolume = from + (to - from) * t
            p.volume = currentVolume
            delay(32)
        }
        currentVolume = to
        p.volume = currentVolume
    }

    fun release() {
        try { player?.release() } catch (_: Exception) { }
        player = null
    }
}

private class SimpleVoicePlayer(
    private val context: Context,
    private val onPlayingChanged: (Boolean) -> Unit
) {
    private var player: ExoPlayer? = null
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { onPlayingChanged(isPlaying) }
        override fun onPlaybackStateChanged(playbackState: Int) {
            onPlayingChanged(player?.isPlaying == true)
        }
    }

    fun play(resId: Int) {
        if (resId == 0) return
        try {
            if (player == null) {
                player = ExoPlayer.Builder(context).build().apply {
                    val attrs = AudioAttributes.Builder()
                        .setUsage(C.USAGE_GAME)
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .build()
                    setAudioAttributes(attrs, true)
                    volume = 1.0f
                    addListener(listener)
                }
            }
            val p = player ?: return
            p.stop()
            p.clearMediaItems()
            
            val uri = RawResourceDataSource.buildRawResourceUri(resId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.prepare()
            p.play()
        } catch (_: Exception) { onPlayingChanged(false) }
    }

    suspend fun playAndAwait(resId: Int) {
        if (resId == 0) return
        play(resId)
        val p = player ?: return
        delay(50) 
        while (p.isPlaying || p.playbackState == Player.STATE_BUFFERING) {
            delay(100)
        }
    }

    fun stop() {
        try { player?.stop(); player?.clearMediaItems() } catch (_: Exception) { }
        onPlayingChanged(false)
    }

    fun release() {
        try { player?.removeListener(listener) } catch (_: Exception) { }
        try { player?.release() } catch (_: Exception) { }
        player = null
        onPlayingChanged(false)
    }
}

private class TtsSpeaker(
    private val context: Context,
    private val onIsSpeakingChanged: (Boolean) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var ready = false

    fun init(locale: Locale = Locale("ro", "RO")) {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                try { tts?.language = locale } catch (_: Exception) { }
                try {
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) { onIsSpeakingChanged(true) }
                        override fun onDone(utteranceId: String?) { onIsSpeakingChanged(false) }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) { onIsSpeakingChanged(false) }
                        override fun onError(utteranceId: String?, errorCode: Int) { onIsSpeakingChanged(false) }
                    })
                } catch (_: Exception) { }
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        try {
            onIsSpeakingChanged(true)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "alphabet_prompt")
        } catch (_: Exception) { onIsSpeakingChanged(false) }
    }

    fun stop() { try { tts?.stop() } catch (_: Exception) { }; onIsSpeakingChanged(false) }
    fun release() { try { tts?.shutdown() } catch (_: Exception) { }; tts = null }
}

// -------------------- UI COMPONENTS (Fixes GlassPill error) --------------------

@Composable
private fun GlassPill(
    content: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.90f), Color.White.copy(alpha = 0.50f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFF0F0F0))
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = Color(0xFF1B1B1B),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        )
    }
}

// -------------------- MAIN GAME --------------------

@Composable
fun AlphabetAdventureGame(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // --- MANAGERS ---
    val soundManager = LocalSoundManager.current
    val bgmPlayer = remember { LoopingMusicPlayer(context) }
    val sfxLimiter = remember { SfxLimiter() }
    
    // Audio State
    var voiceLinePlaying by remember { mutableStateOf(false) }
    var ttsSpeaking by remember { mutableStateOf(false) }
    val isSpeaking = voiceLinePlaying || ttsSpeaking

    // Init Players
    val voicePlayer = remember { SimpleVoicePlayer(context) { voiceLinePlaying = it } }
    val tts = remember { TtsSpeaker(context) { ttsSpeaking = it }.apply { init() } }

    DisposableEffect(Unit) {
        onDispose { 
            bgmPlayer.release()
            voicePlayer.release()
            tts.release()
        }
    }

    // Smart Ducking
    LaunchedEffect(isSpeaking) {
        bgmPlayer.fadeTo(if (isSpeaking) 0.10f else 0.35f, durationMs = 400L)
    }

    // --- ASSETS ---
    val bgPainter = painterByAnyNameOrNull("bg_sky_clouds", "bg_alphabet_sky", "bg_sky")
    val bubbleBitmap = bitmapByAnyNameOrNull("bubble_glossy", "img_bubble_glossy", "bubble")
    val sparkleBitmap = bitmapByAnyNameOrNull("sparkle_star", "fx_star_sparkle", "sparkle")
    
    val pIdle01 = painterByAnyNameOrNull("princess_toddler_idle_01", "alessia_idle")
    val pIdle02 = painterByAnyNameOrNull("princess_toddler_idle_02_blink", "alessia_idle")
    val pTalk01 = painterByAnyNameOrNull("princess_toddler_talk_01", "alessia_vorbeste")
    val pTalk02 = painterByAnyNameOrNull("princess_toddler_talk_02", "alessia_vorbeste")
    val pSad01 = painterByAnyNameOrNull("princess_toddler_oops_01", "alessia_dezamagita")
    val pCheer01 = painterByAnyNameOrNull("princess_toddler_celebrate_01", "alessia_sarbatoreste")
    val pHint = painterByAnyNameOrNull("princess_toddler_think", "princess_toddler_point_up")

    // Preload letter bitmaps for A–Z. These images live in res/drawable and have names
    // like letter_a.png, letter_b.png, etc. We build a map keyed by the uppercase
    // character to its corresponding ImageBitmap so we can easily look them up when
    // rendering bubbles. If a particular letter image cannot be found the map entry
    // will be null and the code will gracefully fall back to drawing the character
    // with text instead of an image.
    val letterBitmaps = remember(context) {
        val map = mutableMapOf<Char, ImageBitmap?>()
        for (c in 'A'..'Z') {
            val name = "letter_${c.lowercaseChar()}"
            val resId = loadDrawableFirst(context, name)
            map[c] = if (resId != 0) ImageBitmap.imageResource(context.resources, resId) else null
        }
        map
    }

    // Preload plane bitmaps for decorative airplanes. The plane images are named
    // plane_1.png through plane_4.png and live in res/drawable. We prepare two
    // separate lists: one with the original images (for left‑to‑right motion) and
    // another containing horizontally flipped versions (for right‑to‑left motion).
    val planeBitmapsOrig = remember(context) {
        val list = mutableListOf<ImageBitmap>()
        for (i in 1..4) {
            val name = "plane_${i}"
            val resId = loadDrawableFirst(context, name)
            if (resId != 0) list.add(ImageBitmap.imageResource(context.resources, resId))
        }
        list
    }
    val planeBitmapsFlipped = remember(context) {
        planeBitmapsOrig.map { orig ->
            // Convert Compose ImageBitmap to an Android Bitmap
            val bmp: Bitmap = orig.asAndroidBitmap()
            val matrix = Matrix().apply { preScale(-1f, 1f) }
            // Create a new flipped bitmap using the matrix
            val flippedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            flippedBmp.asImageBitmap()
        }
    }

    // Paints
    val textPaintFill = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.parseColor("#40000000"))
        }
    }
    val textPaintStroke = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.STROKE
            typeface = textPaintFill.typeface
            strokeJoin = Paint.Join.ROUND
        }
    }
    val floatingTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    // --- LOGIC ---
    val bubbles = remember { ConcurrentLinkedQueue<Bubble>() }
    // Active airplane sprites that fly across the top of the screen. They are
    // updated in the main game loop and removed once off‑screen.
    val planes = remember { ConcurrentLinkedQueue<Plane>() }
    val particles = remember { ConcurrentLinkedQueue<SparkleParticle>() }
    val floatingTexts = remember { ConcurrentLinkedQueue<FloatingText>() }
    
    var phase by remember { mutableStateOf(GamePhase.INTRO) }
    var target by remember { mutableStateOf('A') }
    var stars by remember { mutableIntStateOf(0) }
    
    val targetDeck = remember { AlphabetDeck() }
    var lastDecoy by remember { mutableStateOf<Char?>(null) }
    
    var hintActive by remember { mutableStateOf(false) }
    var sadUntilMs by remember { mutableLongStateOf(0L) }
    var cheerUntilMs by remember { mutableLongStateOf(0L) }
    var lastInteractionMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var audioJob by remember { mutableStateOf<Job?>(null) }

    fun startAudioSequence(block: suspend () -> Unit) {
        // Cancel any currently running audio coroutine. Also stop any
        // currently playing voice or TTS so that new sounds do not overlap
        // with existing playback. This helps ensure a clean audio
        // experience without interleaving clips.
        audioJob?.cancel()
        // Stop speech audio before starting a new sequence
        try { voicePlayer.stop() } catch (_: Exception) {}
        try { tts.stop() } catch (_: Exception) {}
        audioJob = scope.launch { block() }
    }

    val popRes = remember { loadRaw(context, "sfx_bubble_pop") }
    val whooshRes = remember { loadRaw(context, "sfx_whoosh") }
    val promptIds = remember { IntArray(26) { i -> loadRaw(context, "prompt_" + ('a' + i)) } }
    val letterIds = remember { IntArray(26) { i -> loadRaw(context, "letter_" + ('a' + i)) } }
    
    val introIds = remember {
        listOf("intro_game_01","intro_game_02","intro_game_03","intro_game_04","intro_game_05")
            .map { loadRaw(context, it) }.filter { it != 0 }
    }
    val cheerIds = remember {
        listOf("sfx_cheer_01", "sfx_cheer_02", "sfx_cheer_03", "sfx_cheer_04", "sfx_cheer_05", "sfx_cheer_06")
            .map { loadRaw(context, it) }.filter { it != 0 }
    }
    val wrongIds = remember {
        listOf("sfx_wrong_01", "sfx_wrong_02").map { loadRaw(context, it) }.toIntArray()
    }

    LaunchedEffect(Unit) {
        try { soundManager.exitGameMode() } catch (_: Exception) {}
        bgmPlayer.playLoop(0) 
        
        if (introIds.isNotEmpty()) {
            val intro = introIds.random()
            if (whooshRes != 0) sfxLimiter.play(soundManager, whooshRes)
            delay(300)
            voicePlayer.playAndAwait(intro)
        }

        val bgMusic = loadRaw(context, "bg_music_loop")
        if (bgMusic != 0) {
            bgmPlayer.playLoop(bgMusic, 0f)
            bgmPlayer.fadeTo(0.35f, 1500L)
        }
        
        if (phase == GamePhase.INTRO) {
            phase = GamePhase.PLAY
            target = targetDeck.next()
        }
    }

    fun playPrompt() {
        startAudioSequence {
            hintActive = true
            val idx = target.code - 'A'.code
            if (idx in 0..25 && promptIds[idx] != 0) {
                voicePlayer.play(promptIds[idx])
            } else {
                tts.speak("Găsește litera $target")
            }
            delay(2000)
            hintActive = false
        }
    }

    fun nextLevel() {
        target = targetDeck.next()
        lastInteractionMs = System.currentTimeMillis()
        playPrompt()
    }

    BackHandler { onBack() }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var frameTrigger by remember { mutableLongStateOf(0L) }

    LaunchedEffect(phase) {
        while (isActive) {
            val now = System.currentTimeMillis()
            if (canvasSize.width > 0 && phase == GamePhase.PLAY) {
                val w = canvasSize.width.toFloat()
                val h = canvasSize.height.toFloat()
                val baseSize = (min(w, h) * 0.22f).coerceIn(180f, 320f)

                if (bubbles.size < 6 && Random.nextFloat() < 0.03f) {
                    val isTarget = (now - lastInteractionMs > 2500 && Random.nextBoolean()) || Random.nextFloat() < 0.4f
                    val char = if (isTarget) target else {
                        val c = randomNonTarget(target, lastDecoy)
                        lastDecoy = c
                        c
                    }
                    val startX = Random.nextFloat() * (w - baseSize) + baseSize / 2
                    bubbles.add(
                        Bubble(
                            x = startX,
                            y = h + baseSize,
                            char = char,
                            speed = h * 0.003f * (Random.nextFloat() * 0.3f + 0.8f),
                            initialX = startX,
                            wobbleOffset = Random.nextFloat() * 100f,
                            floatOffset = Random.nextFloat() * 100f,
                            tintColor = BubbleColors.random()
                        )
                    )
                }

                val bIter = bubbles.iterator()
                while (bIter.hasNext()) {
                    val b = bIter.next()
                    if (b.popping) {
                        // When popping we grow the bubble slightly and fade it out. Once the
                        // animation completes the bubble is removed from the list.
                        b.popT += 0.12f
                        b.scale = 1f + b.popT * 0.4f
                        b.alpha = (1f - b.popT).coerceAtLeast(0f)
                        if (b.popT >= 1f) bIter.remove()
                    } else {
                        // Apply upward movement based on the bubble's speed
                        b.y -= b.speed
                        // Horizontal sway to give a gentle wobble effect
                        val time = frameTrigger.toDouble() / 1_000_000_000.0
                        val sway = sin(time * 1.5 + b.wobbleOffset) * (baseSize * 0.15f)
                        b.x = b.initialX + sway.toFloat()

                        // Grow bubble scale until reaching its target
                        if (b.scale < b.targetScale) {
                            b.scale = (b.scale + 0.03f).coerceAtMost(b.targetScale)
                        }
                        // Remove if it floats off the top of the screen
                        if (b.y < -baseSize) bIter.remove()
                    }
                }

                // Apply simple repulsion between bubbles so that they gently
                // push away from each other when overlapping. Without this the
                // bubbles can stack on top of one another which looks unnatural.
                run {
                    val list = bubbles.toList()
                    for (i in 0 until list.size) {
                        val bi = list[i]
                        if (bi.popping) continue
                        for (j in i + 1 until list.size) {
                            val bj = list[j]
                            if (bj.popping) continue
                            // Radii based on visual size (including scaling and bubble graphic)
                            val radI = (baseSize * bi.scale * 1.45f) / 2f
                            val radJ = (baseSize * bj.scale * 1.45f) / 2f
                            val dx = bj.x - bi.x
                            val dy = bj.y - bi.y
                            val distSq = dx * dx + dy * dy
                            val minDist = radI + radJ
                            if (distSq < minDist * minDist) {
                                val dist = sqrt(distSq).coerceAtLeast(0.1f)
                                val overlap = minDist - dist
                                val nx = dx / dist
                                val ny = dy / dist
                                // Displace bubbles equally along the collision normal
                                bi.x -= nx * overlap / 2f
                                bi.y -= ny * overlap / 2f
                                bj.x += nx * overlap / 2f
                                bj.y += ny * overlap / 2f
                            }
                        }
                    }
                }

                // Spawn decorative airplanes. We only spawn planes during the play phase
                // and keep the frequency low so they feel like occasional surprises.
                if ((planeBitmapsOrig.isNotEmpty() || planeBitmapsFlipped.isNotEmpty()) && Random.nextFloat() < 0.005f) {
                    val fromLeft = Random.nextBoolean()
                    // Choose a bitmap based on the direction of travel. When flying from
                    // left to right we use the original image; when flying from right
                    // to left we use a horizontally flipped version so the plane does
                    // not appear to fly backwards.
                    val planeImg = if (fromLeft) {
                        planeBitmapsOrig.random()
                    } else {
                        planeBitmapsFlipped.random()
                    }
                    // Compute the on‑screen width of a bubble and then derive the
                    // plane size from it. The diameter of a bubble is baseSize * 1.45f.
                    val bubbleDiameter = baseSize * 1.45f
                    val planeWidth = bubbleDiameter * PLANE_SIZE_FACTOR
                    val halfWidth = planeWidth / 2f
                    // Spawn Y anywhere between 15% and 70% of the screen height so
                    // planes are clearly visible and not just near the top.
                    val spawnY = h * 0.15f + Random.nextFloat() * h * 0.55f
                    // Position x represents the centre of the plane; spawn just off
                    // the visible area on either side.
                    val startX = if (fromLeft) -halfWidth else w + halfWidth
                    // Base horizontal speed relative to screen width
                    val baseSpeed = w * 0.003f
                    val speedVari = w * 0.002f * Random.nextFloat()
                    val vx = (baseSpeed + speedVari) * if (fromLeft) 1f else -1f
                    planes.add(Plane(x = startX, y = spawnY, vx = vx, bitmap = planeImg))
                }

                // Update active planes and remove those that have moved fully off‑screen
                val plIter = planes.iterator()
                while (plIter.hasNext()) {
                    val pl = plIter.next()
                    pl.x += pl.vx
                    // Compute the plane half‑width based on the current baseSize so that
                    // removal remains consistent with how planes are drawn. We derive
                    // the on‑screen plane width from the bubble diameter and the
                    // PLANE_SIZE_FACTOR.
                    val bubbleDiameter = baseSize * 1.45f
                    val planeWidth = bubbleDiameter * PLANE_SIZE_FACTOR
                    val halfWidth = planeWidth / 2f
                    // Remove once the plane has flown completely past the view. Because
                    // pl.x stores the centre of the plane we compare against halfWidth
                    // when checking boundaries.
                    if ((pl.vx > 0 && pl.x - halfWidth > w) ||
                        (pl.vx < 0 && pl.x + halfWidth < 0f)) {
                        plIter.remove()
                    }
                }

                // If a plane overlaps with a bubble, gently push the bubble away so it
                // doesn’t intersect with the plane. We approximate each plane as a
                // circle with radius equal to half of its on‑screen width. Bubbles
                // retain their circular radius based on their scale. The bubble is
                // displaced along the normal vector pointing away from the plane.
                run {
                    // Precompute plane radius based on current bubble size and plane factor
                    val bubbleDiameter = baseSize * 1.45f
                    val planeWidth = bubbleDiameter * PLANE_SIZE_FACTOR
                    val planeRadius = planeWidth / 2f
                    val planeList = planes.toList()
                    val bubbleList = bubbles.toList()
                    for (pl in planeList) {
                        for (b in bubbleList) {
                            if (b.popping) continue
                            val radBubble = (baseSize * b.scale * 1.45f) / 2f
                            val dx = b.x - pl.x
                            val dy = b.y - pl.y
                            val distSq = dx * dx + dy * dy
                            val minDist = planeRadius + radBubble
                            if (distSq < minDist * minDist) {
                                val dist = sqrt(distSq).coerceAtLeast(0.1f)
                                val overlap = minDist - dist
                                val nx = dx / dist
                                val ny = dy / dist
                                b.x += nx * overlap
                                b.y += ny * overlap
                            }
                        }
                    }
                }
            }
            
            val pIter = particles.iterator()
            while (pIter.hasNext()) {
                val p = pIter.next()
                p.x += p.vx; p.y += p.vy; p.vy += 0.5f; p.life -= 0.04f
                if (p.life <= 0f) pIter.remove()
            }
            val ftIter = floatingTexts.iterator()
            while (ftIter.hasNext()) {
                val ft = ftIter.next()
                ft.y -= 2.0f; ft.life -= 0.015f
                if (ft.life <= 0f) ftIter.remove()
            }

            // If nothing has been tapped for a while and no audio is currently
            // playing then remind the user by replaying the prompt. This avoids
            // overlapping prompts because startAudioSequence will stop any
            // existing playback. Update lastInteractionMs so that prompts are
            // not repeated too frequently.
            if (phase == GamePhase.PLAY && !isSpeaking && now - lastInteractionMs > 6000) {
                playPrompt()
                lastInteractionMs = now
            }

            frameTrigger = System.nanoTime()
            delay(16)
        }
    }

    fun spawnConfetti(x: Float, y: Float, color: Color) {
        repeat(16) {
            val angle = Random.nextFloat() * 6.28f
            val sp = Random.nextFloat() * 12f + 5f
            particles.add(SparkleParticle(x, y, cos(angle)*sp, sin(angle)*sp, 1f, color))
        }
    }
    
    fun spawnFloatingText(x: Float, y: Float) {
        val phrase = PraiseWords.random()
        floatingTexts.add(FloatingText(x, y, phrase, 1f, Color.White))
    }

    fun onHit(b: Bubble) {
        if (b.char == target) {
            b.popping = true
            stars = (stars + 1).coerceAtMost(5)
            lastInteractionMs = System.currentTimeMillis()
            
            spawnConfetti(b.x, b.y, b.tintColor)
            spawnFloatingText(b.x, b.y)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            
            startAudioSequence {
                sfxLimiter.play(soundManager, popRes)
                delay(200)

                if (stars >= 5) {
                    cheerUntilMs = System.currentTimeMillis() + 4000
                    phase = GamePhase.REWARD
                    if (cheerIds.isNotEmpty()) {
                        voicePlayer.playAndAwait(cheerIds.random())
                    }
                    delay(500)
                    stars = 0
                    phase = GamePhase.PLAY
                    nextLevel()
                } else {
                    val idx = target.code - 'A'.code
                    if (idx in 0..25 && letterIds[idx] != 0) {
                        voicePlayer.playAndAwait(letterIds[idx])
                    }
                    nextLevel()
                }
            }
        } else {
            sadUntilMs = System.currentTimeMillis() + 1000
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val w = if (wrongIds.isNotEmpty()) wrongIds.random() else 0
            // Only play the wrong‑answer sound if no speech is currently playing
            if (w != 0 && !isSpeaking) sfxLimiter.play(soundManager, w)
            if (System.currentTimeMillis() - lastInteractionMs > 5000) {
                playPrompt()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgPainter != null) {
            Image(bgPainter, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF87CEEB), Color(0xFFE0F7FA)))))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (phase == GamePhase.INTRO) return@detectTapGestures
                        if (phase == GamePhase.REWARD) return@detectTapGestures 
                        
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val baseSize = (min(w, h) * 0.22f).coerceIn(180f, 320f)
                        val hitRadius = (baseSize * 1.45f) / 2f
                        
                        val hit = bubbles.toList().asReversed().firstOrNull { b ->
                            !b.popping && (offset.x - b.x).pow(2) + (offset.y - b.y).pow(2) < hitRadius.pow(2)
                        }
                        
                        if (hit != null) onHit(hit)
                        else {
                            particles.add(SparkleParticle(offset.x, offset.y, 0f, -5f, 0.5f, Color.White))
                        }
                    }
                }
        ) {
            val frame = frameTrigger.toDouble() / 1_000_000_000.0
            val w = size.width
            val h = size.height
            val baseSize = (min(w, h) * 0.22f).coerceIn(180f, 320f)

            textPaintFill.textSize = baseSize * 0.6f
            textPaintStroke.textSize = baseSize * 0.6f
            textPaintStroke.strokeWidth = baseSize * 0.04f

            // Draw any active airplanes before the bubbles. Planes are scaled
            // relative to the width of the canvas and centered on their x,y
            // coordinates. By drawing planes first we ensure that the bubbles
            // appear in front of them.
            planes.forEach { pl ->
                val bmp = pl.bitmap
                // Compute plane size relative to the bubble diameter. The diameter of
                // each bubble on screen is baseSize * 1.45f. We scale the plane
                // width using the PLANE_SIZE_FACTOR constant so that planes are
                // smaller than bubbles, then compute height based on the bitmap’s
                // aspect ratio.
                val bubbleDiameter = baseSize * 1.45f
                val planeWidth = bubbleDiameter * PLANE_SIZE_FACTOR
                val planeHeight = planeWidth * (bmp.height.toFloat() / bmp.width.toFloat())
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(
                        (pl.x - planeWidth / 2f).toInt(),
                        (pl.y - planeHeight / 2f).toInt()
                    ),
                    dstSize = IntSize(planeWidth.toInt(), planeHeight.toInt()),
                    alpha = 1f
                )
            }

            bubbles.forEach { b ->
                // Compute a pulsing scale when hinting the correct letter. When hintActive
                // is true and this bubble’s character matches the target, we apply a
                // sinusoidal modulation to its scale so that it visibly pulses on screen.
                val isHinted = hintActive && b.char == target
                val pulseFactor = if (isHinted) {
                    (1.0 + 0.1 * sin(frame * 6.0)).toFloat()
                } else {
                    1f
                }
                val drawScale = b.scale * pulseFactor

                // Compute the size of the bubble on screen using the modulated scale.
                val visualSize = baseSize * drawScale * 1.45f
                val rad = visualSize / 2f
                val drawX = b.x

                // Draw the bubble body. Prefer a bitmap if provided, otherwise fall back
                // to drawing simple circles.
                if (bubbleBitmap != null) {
                    drawImage(
                        image = bubbleBitmap,
                        dstOffset = IntOffset((drawX - rad).toInt(), (b.y - rad).toInt()),
                        dstSize = IntSize(visualSize.toInt(), visualSize.toInt()),
                        alpha = b.alpha
                    )
                } else {
                    drawCircle(b.tintColor.copy(alpha = 0.6f * b.alpha), rad, Offset(drawX, b.y))
                    drawCircle(
                        Color.White.copy(alpha = 0.4f * b.alpha),
                        rad * 0.3f,
                        Offset(drawX - rad * 0.3f, b.y - rad * 0.3f)
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.9f * b.alpha),
                        rad,
                        Offset(drawX, b.y),
                        style = Stroke(4f)
                    )
                }

                val floatY = sin(frame * 2.0 + b.floatOffset) * (baseSize * 0.06f)

                // Render the character using a bitmap if available; otherwise fall back to text.
                val letterImg = letterBitmaps[b.char]
                if (letterImg != null) {
                    // Scale the image relative to the bubble size and current bubble scale. The
                    // multiplier (0.8f) leaves a little padding inside the bubble so the
                    // character doesn’t touch the edges. Add the floatY offset so the
                    // letter gently bobs up and down with the bubble.
                    val letterSize = baseSize * drawScale * 0.8f
                    drawImage(
                        image = letterImg,
                        dstOffset = IntOffset(
                            (drawX - letterSize / 2f).toInt(),
                            ((b.y + floatY.toFloat()) - letterSize / 2f).toInt()
                        ),
                        dstSize = IntSize(letterSize.toInt(), letterSize.toInt()),
                        alpha = b.alpha
                    )
                } else {
                    // Fallback: draw the character with stroke and fill paints
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(
                            drawX,
                            b.y - (textPaintFill.descent() + textPaintFill.ascent()) / 2 + floatY.toFloat()
                        )
                        canvas.nativeCanvas.scale(drawScale, drawScale)
                        canvas.nativeCanvas.drawText(b.char.toString(), 0f, 0f, textPaintStroke)
                        canvas.nativeCanvas.drawText(b.char.toString(), 0f, 0f, textPaintFill)
                        canvas.nativeCanvas.restore()
                    }
                }
            }
            
            particles.forEach { p ->
                drawCircle(p.color.copy(alpha = p.life), 8f * p.life, Offset(p.x, p.y))
            }
            
            floatingTextPaint.textSize = baseSize * 0.45f
            floatingTexts.forEach { ft ->
                drawIntoCanvas { canvas ->
                    floatingTextPaint.alpha = (ft.life * 255).toInt().coerceIn(0, 255)
                    canvas.nativeCanvas.drawText(ft.text, ft.x, ft.y, floatingTextPaint)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassPill(content = {
                BasicText("Țintă: ", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray))
                AnimatedContent(
                    targetState = target,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
                    }, label = "TargetAnim"
                ) { char ->
                    BasicText("$char", style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFFE91E63)))
                }
            })
            
            GlassPill(content = {
                AnimatedContent(targetState = stars, label = "StarAnim") { count ->
                    Row {
                        repeat(5) { index ->
                            Icon(Icons.Rounded.Star, null, tint = if (index < count) Color(0xFFFFD700) else Color.LightGray, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            })
        }

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            val mood = when {
                phase == GamePhase.REWARD -> MascotMood.CHEER
                System.currentTimeMillis() < cheerUntilMs -> MascotMood.CHEER
                System.currentTimeMillis() < sadUntilMs -> MascotMood.SAD
                isSpeaking -> MascotMood.TALK
                hintActive -> MascotMood.HINT
                else -> MascotMood.IDLE
            }
            
            val painter = when (mood) {
                MascotMood.TALK -> if ((frameTrigger / 200000000) % 2 == 0L) pTalk01 else pTalk02
                MascotMood.CHEER -> pCheer01
                MascotMood.SAD -> pSad01
                MascotMood.HINT -> pHint
                else -> if ((frameTrigger / 1000000000) % 5 == 0L) pIdle02 else pIdle01 
            } ?: ColorPainter(Color.Transparent)

            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val scaleY by infiniteTransition.animateFloat(
                initialValue = 0.98f, targetValue = 1.02f,
                animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
                label = "scaleY"
            )

            Column(horizontalAlignment = Alignment.End) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = hintActive || phase == GamePhase.INTRO,
                    enter = scaleIn(transformOrigin = TransformOrigin(1f, 1f)),
                    exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f))
                ) {
                    SpeechBubble(text = if (phase == GamePhase.INTRO) "Salut! 👋" else "Caută $target!", modifier = Modifier.padding(bottom = 8.dp, end = 20.dp))
                }

                Image(painter = painter, contentDescription = "Mascot", modifier = Modifier.size(160.dp).graphicsLayer { this.scaleY = scaleY; this.scaleX = 1f / scaleY })
            }
        }

        Box(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
            val backPainter = painterByAnyNameOrNull("ui_btn_back_wood", "icon_back")
            if (backPainter != null) {
                Image(painter = backPainter, contentDescription = "Back", modifier = Modifier.size(64.dp).pointerInput(Unit) { detectTapGestures { onBack() } })
            } else {
                androidx.compose.material3.FloatingActionButton(onClick = onBack, containerColor = Color.White) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }
        }
    }
}