package com.example.educationalapp.features.learning.shapes

import android.content.Context
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.educationalapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// --- AUDIO ENGINE ---
class ShapesAudioEngine(private val context: Context) {
    private val TAG = "ShapesAudioEngine"

    private fun canon(name: String): String = name.substringBeforeLast('.')

    private val soundPool: SoundPool
    private val sfxMap = mutableMapOf<String, Int>()
    private val voiceResMap = mutableMapOf<String, Int>() 

    private var bgPlayer: MediaPlayer? = null
    private val bgBaseVol = 0.06f
    private val bgDuckedVol = 0.03f

    private var voicePlayer: MediaPlayer? = null
    private var voiceSessionId: Int = 0
    private var voiceDone = kotlinx.coroutines.CompletableDeferred<Unit>().apply { complete(Unit) }

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        val soundsToRegister = listOf(
            "sfx_correct_ding", "sfx_wrong_buzz", "sfx_chalk_draw",
            "vox_shapes_intro", "vox_error_try",
            "vox_quest_square", "vox_ans_square",
            "vox_quest_circle", "vox_ans_circle",
            "vox_quest_triangle", "vox_ans_triangle",
            "vox_quest_star", "vox_ans_star",
            "vox_quest_rectangle", "vox_ans_rectangle",
            "vox_quest_heart", "vox_ans_heart"
        )

        soundsToRegister.forEach { rawName ->
            val name = canon(rawName)
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) {
                if (name.startsWith("sfx_")) {
                    sfxMap[name] = soundPool.load(context, resId, 1)
                } else if (name.startsWith("vox_")) {
                    voiceResMap[name] = resId
                }
            }
        }
    }

    fun startMusic() {
        val resId = context.resources.getIdentifier("bg_music_loop", "raw", context.packageName)
        if (resId != 0) {
            bgPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setVolume(bgBaseVol, bgBaseVol)
                start()
            }
        }
    }

    private fun setBgVolume(v: Float) {
        bgPlayer?.setVolume(v, v)
    }

    fun playSfx(name: String, vol: Float = 1f) {
        val key = canon(name)
        sfxMap[key]?.let { soundPool.play(it, vol, vol, 1, 0, 1f) }
    }

    fun playVoice(name: String): Int {
        val key = canon(name)
        val resId = voiceResMap[key] ?: return -1

        stopVoice()
        voiceSessionId += 1
        val session = voiceSessionId
        voiceDone = kotlinx.coroutines.CompletableDeferred()
        setBgVolume(bgDuckedVol)

        voicePlayer = MediaPlayer.create(context, resId).apply {
            setVolume(1.0f, 1.0f)
            setOnCompletionListener {
                if (!voiceDone.isCompleted) voiceDone.complete(Unit)
                cleanupVoice(session)
            }
            setOnErrorListener { _, _, _ ->
                if (!voiceDone.isCompleted) voiceDone.complete(Unit)
                cleanupVoice(session)
                true
            }
            start()
        }
        return session
    }

    private fun cleanupVoice(session: Int) {
        if (session == voiceSessionId) {
            voicePlayer?.release()
            voicePlayer = null
            setBgVolume(bgBaseVol)
        }
    }

    fun stopVoice() {
        voicePlayer?.let {
            try { it.stop() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
        voicePlayer = null
        if (!voiceDone.isCompleted) voiceDone.complete(Unit)
        setBgVolume(bgBaseVol)
    }

    suspend fun awaitVoiceIdle() {
        while (true) {
            if (voicePlayer == null) return
            val session = voiceSessionId
            voiceDone.await()
            if (voicePlayer == null || voiceSessionId == session) return
        }
    }

    fun release() {
        stopVoice()
        bgPlayer?.release()
        bgPlayer = null
        soundPool.release()
    }
}

private data class TabletSpec(
    val leftFrac: Float, val topFrac: Float, val widthFrac: Float, val heightFrac: Float
)

private val TABLET_SPECS: Map<Int, TabletSpec> = mapOf(
    R.drawable.mascot_robot_happy to TabletSpec(0.32973f, 0.07437f, 0.38944f, 0.20281f),
    R.drawable.mascot_robot_sad to TabletSpec(0.23876f, 0.64195f, 0.38644f, 0.22000f),
    R.drawable.mascot_robot_holding_tablet to TabletSpec(0.31737f, 0.51121f, 0.40733f, 0.22172f)
)

@Composable
fun ShapesGameScreen(
    viewModel: ShapesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    val audio = remember { ShapesAudioEngine(context) }
    var isFirstRound by remember { mutableStateOf(true) }

    val robotImageRes = when (uiState.gameState) {
        GameState.CORRECT_FEEDBACK -> R.drawable.mascot_robot_happy
        GameState.WRONG_FEEDBACK -> R.drawable.mascot_robot_sad
        else -> R.drawable.mascot_robot_holding_tablet
    }

    val drawProgress = remember { Animatable(0f) }

    DisposableEffect(Unit) {
        audio.startMusic()
        onDispose { audio.release() }
    }

    LaunchedEffect(uiState.targetShape) {
        audio.awaitVoiceIdle()

        if (isFirstRound) {
            delay(350)
            audio.playVoice("vox_shapes_intro")
            audio.awaitVoiceIdle()
            delay(250)
            isFirstRound = false
        } else {
            delay(250)
        }

        audio.awaitVoiceIdle()

        if (uiState.gameState == GameState.WAITING_INPUT) {
            drawProgress.snapTo(0f)
            audio.playSfx("sfx_chalk_draw", vol = 0.35f)
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
            delay(120)
            if (uiState.gameState == GameState.WAITING_INPUT) {
                audio.playVoice(uiState.targetItem.audioQuestRes)
            }
        }
    }

    LaunchedEffect(uiState.gameState) {
        when (uiState.gameState) {
            GameState.CORRECT_FEEDBACK -> {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                audio.playSfx("sfx_correct_ding", vol = 1f)
                delay(450)
                audio.playVoice(uiState.targetItem.audioWinRes)
                
                // Așteptăm terminarea audio-ului pentru a trece la runda următoare
                audio.awaitVoiceIdle()
                viewModel.onFeedbackFinished()
            }
            GameState.WRONG_FEEDBACK -> {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                audio.playSfx("sfx_wrong_buzz", vol = 1f)
                delay(450)
                audio.playVoice("vox_error_try")
                // Pentru eroare, resetăm starea după audio
                audio.awaitVoiceIdle()
                viewModel.onFeedbackFinished()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_shapes_layer1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = R.drawable.bg_shapes_layer2),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.5f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(380.dp).offset(y = 30.dp)) {
                    RobotWithTablet(
                        robotRes = robotImageRes,
                        shape = uiState.targetShape,
                        progress = drawProgress.value,
                        gameState = uiState.gameState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(modifier = Modifier.align(Alignment.TopStart).padding(18.dp).clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.22f)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(text = "Scor: ${uiState.score}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Image(painter = painterResource(id = R.drawable.ui_btn_home), contentDescription = "Back", modifier = Modifier.align(Alignment.TopEnd).padding(18.dp).size(62.dp).clickable { onBack() })
            }

            Box(modifier = Modifier.weight(0.5f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(uiState.options) { item ->
                        val isWrong = uiState.wrongSelectionId == item.id
                        val isEnabled = uiState.gameState == GameState.WAITING_INPUT && !isFirstRound
                        ShapeOptionCard(item = item, isWrong = isWrong, isEnabled = isEnabled, onClick = { viewModel.onOptionSelected(item) })
                    }
                }
            }
        }

        if (uiState.gameState == GameState.CORRECT_FEEDBACK) {
            val inf = rememberInfiniteTransition(label = "bravoPulse")
            val pulse by inf.animateFloat(0.98f, 1.06f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "pulse")
            Text("BRAVO!", modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp).scale(pulse), fontSize = 54.sp, color = Color(0xFF37E84A), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RobotWithTablet(robotRes: Int, shape: ShapeType, progress: Float, gameState: GameState, modifier: Modifier = Modifier) {
    val painter = painterResource(id = robotRes)
    val idle = rememberInfiniteTransition(label = "robotIdle")
    val floatY by idle.animateFloat(-4f, 4f, infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse), label = "floatY")
    val shakeT = rememberInfiniteTransition(label = "shake")
    val shake by shakeT.animateFloat(-4f, 4f, infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse), label = "shakeVal")
    val shakeApplied = if (gameState == GameState.WRONG_FEEDBACK) shake else 0f
    val baseScale by animateFloatAsState(if (gameState == GameState.CORRECT_FEEDBACK) 1.04f else 1f, tween(260), label = "robotScale")

    Box(modifier = modifier) {
        Image(painter = painter, contentDescription = "Robot", modifier = Modifier.fillMaxSize().offset(y = floatY.dp).rotate(shakeApplied).scale(baseScale), contentScale = ContentScale.Fit)
        Canvas(modifier = Modifier.fillMaxSize().offset(y = floatY.dp).rotate(shakeApplied).scale(baseScale)) {
            val spec = TABLET_SPECS[robotRes] ?: return@Canvas
            val intrinsic = painter.intrinsicSize
            val fitted = computeFittedImageRect(size, intrinsic)
            val tabletRect = Rect(fitted.left + spec.leftFrac * fitted.width, fitted.top + spec.topFrac * fitted.height, fitted.left + (spec.leftFrac + spec.widthFrac) * fitted.width, fitted.top + (spec.topFrac + spec.heightFrac) * fitted.height)
            clipRect(tabletRect.left, tabletRect.top, tabletRect.right, tabletRect.bottom) {
                val pad = min(tabletRect.width, tabletRect.height) * 0.12f
                val contentRect = Rect(tabletRect.left + pad, tabletRect.top + pad, tabletRect.right - pad, tabletRect.bottom - pad)
                drawPremiumChalkShape(shape, contentRect, progress.coerceIn(0f, 1f))
            }
        }
    }
}

private fun computeFittedImageRect(container: Size, intrinsic: Size): Rect {
    if (intrinsic.width <= 0f || intrinsic.height <= 0f) return Rect(0f, 0f, container.width, container.height)
    val scale = min(container.width / intrinsic.width, container.height / intrinsic.height)
    val drawnW = intrinsic.width * scale
    val drawnH = intrinsic.height * scale
    return Rect((container.width - drawnW) / 2f, (container.height - drawnH) / 2f, (container.width + drawnW) / 2f, (container.height + drawnH) / 2f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPremiumChalkShape(shape: ShapeType, rect: Rect, progress: Float) {
    val shapeColor = Color(0xFF00E5FF)
    val stroke = (min(rect.width, rect.height) * 0.07f).coerceAtLeast(6f)
    val fullPath = buildShapePath(shape, rect)
    val (segPath, tip) = trimPathAndTip(fullPath, progress)
    drawPath(segPath, shapeColor.copy(0.18f), style = Stroke(stroke * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(segPath, shapeColor.copy(0.28f), style = Stroke(stroke * 1.55f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(segPath, shapeColor.copy(0.92f), style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    tip?.let {
        drawCircle(shapeColor.copy(0.35f), stroke * 0.9f, it)
        drawCircle(Color.White.copy(0.22f), stroke * 0.45f, it)
    }
}

private fun buildShapePath(shape: ShapeType, rect: Rect): Path {
    val cx = rect.left + rect.width / 2f
    val cy = rect.top + rect.height / 2f
    return Path().apply {
        when (shape) {
            ShapeType.CIRCLE -> addOval(rect)
            ShapeType.SQUARE -> { val s = min(rect.width, rect.height); addRect(Rect(cx - s/2, cy - s/2, cx + s/2, cy + s/2)) }
            ShapeType.RECTANGLE -> addRect(rect)
            ShapeType.TRIANGLE -> { moveTo(cx, rect.top); lineTo(rect.right, rect.bottom); lineTo(rect.left, rect.bottom); close() }
            ShapeType.STAR -> {
                val outerR = min(rect.width, rect.height) * 0.5f; val innerR = outerR * 0.45f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR; val a = -PI/2 + i * (PI/5)
                    val x = cx + (cos(a) * r).toFloat(); val y = cy + (sin(a) * r).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            ShapeType.HEART -> {
                val ty = rect.top + rect.height * 0.3f; moveTo(cx, rect.bottom)
                cubicTo(rect.left, cy + rect.height * 0.1f, rect.left, ty, cx, ty)
                cubicTo(rect.right, ty, rect.right, cy + rect.height * 0.1f, cx, rect.bottom)
            }
        }
    }
}

private fun trimPathAndTip(path: Path, progress: Float): Pair<Path, Offset?> {
    val p = progress.coerceIn(0f, 1f)
    if (p <= 0f) return Path() to null
    val android = path.asAndroidPath()
    val pm = PathMeasure(android, false)
    val len = pm.length
    if (p >= 1f) {
        val pos = FloatArray(2); pm.getPosTan(len, pos, null)
        return path to Offset(pos[0], pos[1])
    }
    val out = AndroidPath()
    pm.getSegment(0f, len * p, out, true)
    val pos = FloatArray(2); pm.getPosTan(len * p, pos, null)
    return out.asComposePath() to Offset(pos[0], pos[1])
}

@Composable
fun ShapeOptionCard(item: ShapeItem, isWrong: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isWrong) 0.92f else 1f)
    val alpha by animateFloatAsState(if (isWrong) 0.55f else 1f)
    val idle = rememberInfiniteTransition(label = "float")
    val floatY by idle.animateFloat(-5f, 5f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse))
    val pressScale by animateFloatAsState(if (isEnabled) 1f else 0.98f)
    Box(modifier = Modifier.size(130.dp).scale(scale * pressScale).offset(y = floatY.dp).alpha(alpha).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(0.52f)).clickable(enabled = isEnabled) { onClick() }.padding(10.dp), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = item.imageRes), contentDescription = item.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}
