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

// NOTE: unele fișiere încărcate anterior în conversație pot expira. Dacă îți cer să le revăd,
// te rog reîncarcă fișierul/fisierele respective.

// --- AUDIO ENGINE ---
// SFX (ding/buzz/chalk) prin SoundPool, iar VOCILE prin MediaPlayer (ca să NU se taie fraza și să putem aștepta finalul).
class ShapesAudioEngine(private val context: Context) {
    private val TAG = "ShapesAudioEngine"

    private fun canon(name: String): String = name.substringBeforeLast('.')

    private val soundPool: SoundPool
    private val sfxMap = mutableMapOf<String, Int>()
    private val voiceResMap = mutableMapOf<String, Int>() // raw resId pentru vocile (MediaPlayer)

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
            // SFX
            "sfx_correct_ding",
            "sfx_wrong_buzz",
            "sfx_chalk_draw",

            // VOX
            "vox_shapes_intro",
            "vox_error_try",
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
            if (resId == 0) {
                Log.w(TAG, "Missing raw sound: $name")
                return@forEach
            }

            if (name.startsWith("sfx_")) {
                sfxMap[name] = soundPool.load(context, resId, 1)
            } else if (name.startsWith("vox_")) {
                voiceResMap[name] = resId
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

    /**
     * Pornește o voce. Oprește orice voce curentă (de ex. întrebarea) ca să poată începe feedback-ul.
     * Întoarce un id de sesiune (util dacă vrei să aștepți sesiunea curentă).
     */
    fun playVoice(name: String): Int {
        val key = canon(name)
        val resId = voiceResMap[key] ?: run {
            Log.w(TAG, "Missing voice res: $key")
            return -1
        }

        stopVoice()

        voiceSessionId += 1
        val session = voiceSessionId

        voiceDone = kotlinx.coroutines.CompletableDeferred()

        // Duck muzica cât timp vorbește (mai premium, vocea se aude clar)
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
        // Curăță doar dacă e sesiunea curentă
        if (session == voiceSessionId) {
            try {
                voicePlayer?.release()
            } catch (_: Throwable) {}
            voicePlayer = null
            setBgVolume(bgBaseVol)
        }
    }

    fun stopVoice() {
        try {
            voicePlayer?.stop()
        } catch (_: Throwable) {}
        try {
            voicePlayer?.release()
        } catch (_: Throwable) {}
        voicePlayer = null
        if (!voiceDone.isCompleted) voiceDone.complete(Unit)
        setBgVolume(bgBaseVol)
    }

    /**
     * Așteaptă până când NU mai este nicio voce în redare.
     * Important: e robust dacă între timp începe o altă voce (loop până chiar se termină).
     */
    suspend fun awaitVoiceIdle() {
        while (true) {
            val player = voicePlayer
            if (player == null) return

            val session = voiceSessionId
            val done = voiceDone
            try {
                done.await()
            } catch (_: Throwable) {
                // ignore
            }

            // Dacă între timp a început altă voce, continuăm să așteptăm.
            if (voicePlayer == null) return
            if (voiceSessionId == session) {
                // ar trebui să fie null, dar ca siguranță:
                return
            }
        }
    }

    fun release() {
        stopVoice()
        bgPlayer?.stop()
        bgPlayer?.release()
        bgPlayer = null
        soundPool.release()
    }
}

// --- TABLET SPECS (auto-calibrate din PNG-urile tale) ---
private data class TabletSpec(
    val leftFrac: Float,
    val topFrac: Float,
    val widthFrac: Float,
    val heightFrac: Float
)

// Recturile sunt în coordonate relative la imaginea robotului (intrinsic size).
// Valorile sunt calculate pe baza PNG-urilor trimise (cu o mică margine interioară ca să nu deseneze pe colțuri).
private val TABLET_SPECS: Map<Int, TabletSpec> = mapOf(
    // robot fericit: tabla sus
    R.drawable.mascot_robot_happy to TabletSpec(
        leftFrac = 0.52445f - 0.38944f / 2f,
        topFrac = 0.17578f - 0.20281f / 2f,
        widthFrac = 0.38944f,
        heightFrac = 0.20281f
    ),
    // robot trist: tabla jos
    R.drawable.mascot_robot_sad to TabletSpec(
        leftFrac = 0.43198f - 0.38644f / 2f,
        topFrac = 0.75195f - 0.22000f / 2f,
        widthFrac = 0.38644f,
        heightFrac = 0.22000f
    ),
    // robot normal: tabla la mijloc-jos
    R.drawable.mascot_robot_holding_tablet to TabletSpec(
        leftFrac = 0.52104f - 0.40733f / 2f,
        topFrac = 0.62207f - 0.22172f / 2f,
        widthFrac = 0.40733f,
        heightFrac = 0.22172f
    )
)

private const val SHOW_TABLET_DEBUG = false

// --- GAME SCREEN ---
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

    // Progresul desenului „cu cretă” pe tablă (premium)
    val drawProgress = remember { Animatable(0f) }

    // 1) Start / stop audio
    DisposableEffect(Unit) {
        audio.startMusic()
        onDispose { audio.release() }
    }

    // 2) Când se schimbă forma țintă, desenăm pe tablă + întrebarea
        LaunchedEffect(uiState.targetShape) {
        // IMPORTANT: nu pornim o întrebare nouă cât timp rulează încă vocea de feedback (ca să NU se taie fraza).
        audio.awaitVoiceIdle()

        if (isFirstRound) {
            delay(350)
            audio.playVoice("vox_shapes_intro")
            // așteptăm intro-ul complet (fără delay „hardcodate”)
            audio.awaitVoiceIdle()
            delay(250)
            isFirstRound = false
        } else {
            delay(250)
        }

        // încă o dată, ca siguranță (dacă s-a declanșat altă voce între timp)
        audio.awaitVoiceIdle()

        if (uiState.gameState == GameState.WAITING_INPUT) {
            // reset + desen (vizual) + sfx chalk (audio)
            drawProgress.snapTo(0f)
            audio.playSfx("sfx_chalk_draw", vol = 0.35f)

            // desen animat
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )

            delay(120)
            // dacă userul a apăsat foarte rapid și am intrat în feedback, nu mai pornim întrebarea
            if (uiState.gameState == GameState.WAITING_INPUT) {
                audio.playVoice(uiState.targetItem.audioQuestRes)
            }
        }
    }

    // 3) Feedback audio (corect / greșit)
    LaunchedEffect(uiState.gameState) {
        when (uiState.gameState) {
            GameState.CORRECT_FEEDBACK -> {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                audio.playSfx("sfx_correct_ding", vol = 1f)
                delay(450)
                audio.playVoice(uiState.targetItem.audioWinRes)
            }
            GameState.WRONG_FEEDBACK -> {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                audio.playSfx("sfx_wrong_buzz", vol = 1f)
                delay(450)
                audio.playVoice("vox_error_try")
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
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Row(modifier = Modifier.fillMaxSize()) {

            // LEFT: robot + tablă (shape drawn on tablet)
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(380.dp)
                        .offset(y = 30.dp)
                ) {
                    RobotWithTablet(
                        robotRes = robotImageRes,
                        shape = uiState.targetShape,
                        progress = drawProgress.value,
                        gameState = uiState.gameState,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Scor (premium pill)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Scor: ${uiState.score}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.65f),
                                blurRadius = 10f
                            )
                        )
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ui_btn_home),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp)
                        .size(62.dp)
                        .clickable { onBack() }
                )
            }

            // RIGHT: options
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(uiState.options) { item ->
                        val isWrong = uiState.wrongSelectionId == item.id
                        val isEnabled = uiState.gameState == GameState.WAITING_INPUT && !isFirstRound

                        ShapeOptionCard(
                            item = item,
                            isWrong = isWrong,
                            isEnabled = isEnabled,
                            onClick = { viewModel.onOptionSelected(item) }
                        )
                    }
                }
            }
        }

        // BRAVO overlay (premium)
        if (uiState.gameState == GameState.CORRECT_FEEDBACK) {
            val inf = rememberInfiniteTransition(label = "bravoPulse")
            val pulse by inf.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Text(
                "BRAVO!",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
                    .scale(pulse),
                fontSize = 54.sp,
                color = Color(0xFF37E84A),
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.75f),
                        blurRadius = 14f
                    )
                )
            )
        }
    }
}

@Composable
private fun RobotWithTablet(
    robotRes: Int,
    shape: ShapeType,
    progress: Float,
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val painter = painterResource(id = robotRes)

    // Idle float (premium feel)
    val idle = rememberInfiniteTransition(label = "robotIdle")
    val floatY by idle.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Shake on wrong (fără graphicsLayer)
    val shakeT = rememberInfiniteTransition(label = "shake")
    val shake by shakeT.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeVal"
    )
    val shakeApplied = if (gameState == GameState.WRONG_FEEDBACK) shake else 0f

    // Slight scale on correct
    val baseScale by animateFloatAsState(
        targetValue = if (gameState == GameState.CORRECT_FEEDBACK) 1.04f else 1f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "robotScale"
    )

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = "Robot",
            modifier = Modifier
                .fillMaxSize()
                .offset(y = floatY.dp)
                .rotate(shakeApplied)
                .scale(baseScale),
            contentScale = ContentScale.Fit
        )

        // Canvas overlay EXACT pe zona tablei, în funcție de PNG-ul robotului
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = floatY.dp)
                .rotate(shakeApplied)
                .scale(baseScale)
        ) {
            val spec = TABLET_SPECS[robotRes] ?: return@Canvas

            val intrinsic = painter.intrinsicSize
            val fitted = computeFittedImageRect(container = size, intrinsic = intrinsic)

            val tabletRect = Rect(
                left = fitted.left + spec.leftFrac * fitted.width,
                top = fitted.top + spec.topFrac * fitted.height,
                right = fitted.left + (spec.leftFrac + spec.widthFrac) * fitted.width,
                bottom = fitted.top + (spec.topFrac + spec.heightFrac) * fitted.height
            )

            if (SHOW_TABLET_DEBUG) {
                drawRect(
                    color = Color.Red.copy(alpha = 0.7f),
                    topLeft = Offset(tabletRect.left, tabletRect.top),
                    size = Size(tabletRect.width, tabletRect.height),
                    style = Stroke(width = 3f)
                )
            }

            // desen pe tablă (clip)
            clipRect(
                left = tabletRect.left,
                top = tabletRect.top,
                right = tabletRect.right,
                bottom = tabletRect.bottom
            ) {
                val pad = min(tabletRect.width, tabletRect.height) * 0.12f
                val contentRect = Rect(
                    left = tabletRect.left + pad,
                    top = tabletRect.top + pad,
                    right = tabletRect.right - pad,
                    bottom = tabletRect.bottom - pad
                )
                drawPremiumChalkShape(shape = shape, rect = contentRect, progress = progress.coerceIn(0f, 1f))
            }
        }
    }
}

private fun computeFittedImageRect(container: Size, intrinsic: Size): Rect {
    // Dacă intrinsic e necunoscut, presupunem că imaginea umple tot.
    if (intrinsic.width <= 0f || intrinsic.height <= 0f) {
        return Rect(0f, 0f, container.width, container.height)
    }
    val scale = min(container.width / intrinsic.width, container.height / intrinsic.height)
    val drawnW = intrinsic.width * scale
    val drawnH = intrinsic.height * scale
    val left = (container.width - drawnW) / 2f
    val top = (container.height - drawnH) / 2f
    return Rect(left, top, left + drawnW, top + drawnH)
}


// DrawScope extension to access draw* apis
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPremiumChalkShape(
    shape: ShapeType,
    rect: Rect,
    progress: Float
) {
    val shapeColor = Color(0xFF00E5FF)
    val stroke = (min(rect.width, rect.height) * 0.07f).coerceAtLeast(6f)

    val fullPath = buildShapePath(shape, rect)
    val (segPath, tip) = trimPathAndTip(fullPath, progress)

    // Glow layers (fără nativeCanvas / blur, compatibil cu proiectul tău)
    drawPath(
        path = segPath,
        color = shapeColor.copy(alpha = 0.18f),
        style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = segPath,
        color = shapeColor.copy(alpha = 0.28f),
        style = Stroke(width = stroke * 1.55f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = segPath,
        color = shapeColor.copy(alpha = 0.92f),
        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // „Chalk tip” highlight (face desenul mai viu)
    if (tip != null) {
        drawCircle(
            color = shapeColor.copy(alpha = 0.35f),
            radius = stroke * 0.9f,
            center = tip
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = stroke * 0.45f,
            center = tip
        )
    }
}

private fun buildShapePath(shape: ShapeType, rect: Rect): Path {
    val w = rect.width
    val h = rect.height
    val cx = rect.left + w / 2f
    val cy = rect.top + h / 2f

    return Path().apply {
        when (shape) {
            ShapeType.CIRCLE -> addOval(rect)

            ShapeType.SQUARE -> {
                val s = min(w, h)
                val left = cx - s / 2f
                val top = cy - s / 2f
                addRect(Rect(left, top, left + s, top + s))
            }

            ShapeType.RECTANGLE -> addRect(rect)

            ShapeType.TRIANGLE -> {
                moveTo(cx, rect.top)
                lineTo(rect.right, rect.bottom)
                lineTo(rect.left, rect.bottom)
                close()
            }

            ShapeType.STAR -> {
                // 5-point star
                val outerR = min(w, h) * 0.50f
                val innerR = outerR * 0.45f
                val startAngle = -PI / 2.0
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val a = startAngle + i * (PI / 5.0)
                    val x = cx + (cos(a) * r).toFloat()
                    val y = cy + (sin(a) * r).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            ShapeType.HEART -> {
                // heart path scaled to rect
                val topY = rect.top + h * 0.30f
                val bottomY = rect.bottom
                val leftX = rect.left
                val rightX = rect.right
                moveTo(cx, bottomY)
                cubicTo(leftX, cy + h * 0.10f, leftX, topY, cx, topY)
                cubicTo(rightX, topY, rightX, cy + h * 0.10f, cx, bottomY)
            }
        }
    }
}

private fun trimPathAndTip(path: Path, progress: Float): Pair<Path, Offset?> {
    val p = progress.coerceIn(0f, 1f)
    if (p <= 0f) return Path() to null
    if (p >= 1f) {
        // tip at end
        val tip = estimateTip(path, 1f)
        return path to tip
    }

    val android = path.asAndroidPath()
    val pm = PathMeasure(android, false)
    val out = AndroidPath()
    val len = pm.length
    pm.getSegment(0f, len * p, out, true)

    val tip = run {
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        pm.getPosTan(len * p, pos, tan)
        Offset(pos[0], pos[1])
    }

    return out.asComposePath() to tip
}

private fun estimateTip(path: Path, progress: Float): Offset? {
    val android = path.asAndroidPath()
    val pm = PathMeasure(android, false)
    val len = pm.length
    if (len <= 0f) return null
    val p = progress.coerceIn(0f, 1f)
    val pos = FloatArray(2)
    val tan = FloatArray(2)
    pm.getPosTan(len * p, pos, tan)
    return Offset(pos[0], pos[1])
}

@Composable
fun ShapeOptionCard(
    item: ShapeItem,
    isWrong: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isWrong) 0.92f else 1f, label = "scale")
    val alpha by animateFloatAsState(targetValue = if (isWrong) 0.55f else 1f, label = "alpha")

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatVal"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.98f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "pressScale"
    )

    Box(
        modifier = Modifier
            .size(130.dp)
            .scale(scale * pressScale)
            .offset(y = floatY.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.52f))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}