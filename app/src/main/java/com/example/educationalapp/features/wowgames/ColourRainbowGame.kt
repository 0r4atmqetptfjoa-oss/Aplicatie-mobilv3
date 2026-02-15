package com.example.educationalapp.features.wowgames

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.example.educationalapp.common.LocalSoundManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.educationalapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// -------------------- CONFIGURAȚIE CULORI (ROGVAIV) --------------------

data class WowColor(
    val name: String, 
    val color: Color, 
    val bucketResId: Int, 
    val audioName: String
)

val WowRainbowData = listOf(
    WowColor("Roșu", Color(0xFFFF2A2A), R.drawable.ui_bucket_red, "vox_color_red"),
    WowColor("Portocaliu", Color(0xFFFFA000), R.drawable.ui_bucket_orange, "vox_color_orange"),
    WowColor("Galben", Color(0xFFFFD600), R.drawable.ui_bucket_yellow, "vox_color_yellow"),
    WowColor("Verde", Color(0xFF00E676), R.drawable.ui_bucket_green, "vox_color_green"),
    WowColor("Albastru", Color(0xFF2979FF), R.drawable.ui_bucket_blue, "vox_color_blue"),
    WowColor("Indigo", Color(0xFF3F51B5), R.drawable.ui_bucket_indigo, "vox_color_indigo"), 
    WowColor("Violet", Color(0xFFD500F9), R.drawable.ui_bucket_purple, "vox_color_purple")
)

private data class RainbowParticle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float,
    val color: Color,
    val maxLife: Float
)

// -------------------- JOCUL --------------------

@Composable
fun ColourRainbowGame(onBack: () -> Unit) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val bandCount = WowRainbowData.size

    fun playSfx(name: String, rate: Float = 1f, volume: Float = 1f) {
        val resolved = when (name) {
            "sfx_pop_select" -> if (soundManager.rawResId("sfx_pop_select") != 0) "sfx_pop_select" else "sfx_bubble_pop"
            "sfx_magic_win" -> if (soundManager.rawResId("sfx_magic_win") != 0) "sfx_magic_win" else "sfx_magic_chime"
            else -> name
        }
        soundManager.playSoundByName(resolved, rate = rate, volume = volume, duckMusic = false)
    }

    // State
    var selectedColorIndex by remember { mutableIntStateOf(-1) }
    val bandProgress = remember { mutableStateListOf<Float>().apply { repeat(bandCount) { add(0f) } } }
    val bandCompleted = remember { mutableStateListOf<Boolean>().apply { repeat(bandCount) { add(false) } } }
    
    var isWin by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var hasStartedPainting by remember { mutableStateOf(false) }
    
    // Tutorial State
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showHandTutorial by remember { mutableStateOf(false) }

    // Frame Counter
    var gameFrame by remember { mutableLongStateOf(0L) }

    // Geometrie
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var arcCenter by remember { mutableStateOf(Offset.Zero) }
    var arcRadiusOuter by remember { mutableFloatStateOf(0f) }
    var bandThickness by remember { mutableFloatStateOf(0f) }
    var bucketTopLimit by remember { mutableFloatStateOf(0f) }
    var screenBottomLimit by remember { mutableFloatStateOf(0f) }

    // Particule & Nori
    val particles = remember { mutableStateListOf<RainbowParticle>() }
    val cloud1X = remember { Animatable(-0.2f) }
    val cloud2X = remember { Animatable(0.5f) }
    
    val cloudBitmap = ImageBitmap.imageResource(id = R.drawable.img_cloud_fluffy)

    // Lifecycle
    DisposableEffect(Unit) {
        soundManager.enterGameMode(
            soundManager.rawResId("bg_music_loop").takeIf { it != 0 },
            autoPlay = true,
            startVolume = 0.08f
        )
        soundManager.playVoiceByName("vox_intro")
        onDispose { soundManager.exitGameMode() }
    }

    LaunchedEffect(Unit) {
        // Preload main sounds to avoid lag on first interaction
        soundManager.loadSoundsByName(
            listOf(
                "sfx_pop_select", "sfx_bubble_pop", "sfx_paint_brush",
                "sfx_magic_win", "sfx_magic_chime",
                "vox_intro", "vox_tutorial", "vox_win", "vox_feedback_generic",
                "vox_color_red", "vox_color_orange", "vox_color_yellow",
                "vox_color_green", "vox_color_blue", "vox_color_indigo", "vox_color_purple"
            )
        )

        delay(3500)
        if (selectedColorIndex == -1) soundManager.playVoiceByName("vox_tutorial")
    }

    // Brush ambience while dragging (no looping stream needed)
    LaunchedEffect(isDragging, isWin) {
        if (!isWin && isDragging) {
            while (isActive) {
                playSfx("sfx_paint_brush", rate = 1f, volume = 0.85f)
                delay(260)
            }
        }
    }

    // GAME LOOP
    LaunchedEffect(Unit) {
        launch { cloud1X.animateTo(1.2f, infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart)) }
        launch { cloud2X.animateTo(1.2f, infiniteRepeatable(tween(80000, easing = LinearEasing), RepeatMode.Restart)) }

        var lastTime = System.nanoTime()
        while (isActive) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1e9f
            lastTime = now
            
            gameFrame++ 

            // Tutorial
            if (!isWin && !isDragging && (System.currentTimeMillis() - lastInteractionTime > 6000)) {
                showHandTutorial = true
            } else {
                showHandTutorial = false
            }

            // Particles
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life -= dt
                if (p.life <= 0f) iterator.remove()
            }
            withFrameNanos { }
        }
    }
    
    // --- GEOMETRIE ---
    fun getBandAngles(index: Int): Pair<Float, Float> {
        val radius = arcRadiusOuter - (index * bandThickness)
        val targetY = if (index >= 3) bucketTopLimit else screenBottomLimit
        val dy = targetY - arcCenter.y
        val safeDy = dy.coerceIn(-radius, radius)
        val angleDeg = Math.toDegrees(asin(safeDy / radius).toDouble()).toFloat()
        
        val startAngle = 180f - angleDeg
        val sweepAngle = 180f + (angleDeg * 2f)
        return startAngle to sweepAngle
    }

    // Touch Logic
    fun handleTouch(pos: Offset, fromDrag: Boolean) {
        lastInteractionTime = System.currentTimeMillis()
        if (isWin || selectedColorIndex == -1) return
        if (pos.y > bucketTopLimit + 25f) { isDragging = false; return }
        
        isDragging = fromDrag
        val dist = (pos - arcCenter).getDistance()
        val totalW = bandCount * bandThickness
        
        if (dist > arcRadiusOuter + bandThickness || dist < arcRadiusOuter - totalW - bandThickness/2) return

        val depth = arcRadiusOuter + bandThickness - dist 
        val rawIndex = ((depth - bandThickness/2f) / bandThickness).toInt().coerceIn(0, bandCount - 1)
        
        if (rawIndex == selectedColorIndex && !bandCompleted[rawIndex]) {
            if (!hasStartedPainting) hasStartedPainting = true

            val (bandStart, bandSweep) = getBandAngles(rawIndex)
            val angleRad = atan2(pos.y - arcCenter.y, pos.x - arcCenter.x)
            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
            if (angleDeg < 0f) angleDeg += 360f 
            
            if (angleDeg > bandStart + bandSweep || angleDeg < bandStart) return

            val relativeAngle = angleDeg - bandStart
            val touchProgress = (relativeAngle / bandSweep).coerceIn(0f, 1f)
            val currentP = bandProgress[rawIndex]
            
            if (touchProgress >= currentP - 0.25f) {
                val newP = (currentP + 0.08f).coerceAtMost(touchProgress + 0.15f).coerceAtMost(1f)
                bandProgress[rawIndex] = newP
                
                if (Random.nextFloat() < 0.6f) {
                    particles.add(RainbowParticle(
                        x = pos.x, y = pos.y,
                        vx = (Random.nextFloat() - 0.5f) * 300f, 
                        vy = (Random.nextFloat() - 0.5f) * 300f,
                        life = 0.3f + Random.nextFloat() * 0.4f,
                        color = WowRainbowData[rawIndex].color, maxLife = 0.7f
                    ))
                }

                if (newP >= 0.97f && !bandCompleted[rawIndex]) {
                    bandCompleted[rawIndex] = true
                    bandProgress[rawIndex] = 1f
                    playSfx("sfx_magic_win", rate = 1.2f)
                    soundManager.playVoiceByName("vox_feedback_generic")

                    if (bandCompleted.all { it }) {
                        isWin = true
                        isDragging = false
                        soundManager.playVoiceByName("vox_win")
                        repeat(250) {
                            particles.add(RainbowParticle(
                                x = canvasSize.width / 2f, y = canvasSize.height / 2f,
                                vx = (Random.nextFloat() - 0.5f) * 1500f,
                                vy = (Random.nextFloat() - 1f) * 1200f,
                                life = 1f + Random.nextFloat() * 2f,
                                color = WowRainbowData.random().color, maxLife = 3f
                            ))
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_magic_landscape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    canvasSize = size
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    
                    screenBottomLimit = h * 1.1f 
                    bucketTopLimit = h * 0.75f 
                    arcCenter = Offset(w / 2f, h * 0.92f)
                    
                    val availableHeight = arcCenter.y - (h * 0.08f)
                    val availableWidth = w * 0.48f
                    arcRadiusOuter = min(availableHeight, availableWidth)
                    bandThickness = arcRadiusOuter / 9.0f
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { handleTouch(it, true) },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ -> change.consume(); handleTouch(change.position, true) }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { handleTouch(it, true); isDragging = false })
                }
        ) {
            if (canvasSize == IntSize.Zero) return@Canvas
            
            val currentFrame = gameFrame 

            // Nori
            val cloudY1 = size.height * 0.1f
            val cloudY2 = size.height * 0.25f
            
            translate(left = (size.width * cloud1X.value) - 200f, top = cloudY1) {
                drawImage(image = cloudBitmap, alpha = 0.85f)
            }
            translate(left = (size.width * cloud2X.value) - 200f, top = cloudY2) {
                scale(scale = 0.8f, pivot = Offset.Zero) {
                    drawImage(image = cloudBitmap, alpha = 0.65f)
                }
            }

            // Curcubeu
            val strokeCap = StrokeCap.Round
            for (i in 0 until bandCount) {
                val radius = arcRadiusOuter - (i * bandThickness)
                val colorData = WowRainbowData[i]
                val progress = bandProgress[i]
                val (startA, sweepA) = getBandAngles(i)
                val topLeft = Offset(arcCenter.x - radius, arcCenter.y - radius)
                val drawSize = Size(radius * 2, radius * 2)
                
                val isCurrent = (i == selectedColorIndex && !bandCompleted[i])
                val pulseAlpha = if (isCurrent) (sin(System.currentTimeMillis() / 200.0) * 0.1 + 0.3).toFloat() else 0.2f
                
                drawArc(
                    color = Color.White.copy(alpha = pulseAlpha),
                    startAngle = startA, sweepAngle = sweepA, useCenter = false,
                    topLeft = topLeft, size = drawSize,
                    style = Stroke(width = bandThickness * 0.92f, cap = strokeCap)
                )

                if (progress > 0) {
                    drawArc(
                        color = colorData.color,
                        startAngle = startA, sweepAngle = sweepA * progress, useCenter = false,
                        topLeft = topLeft, size = drawSize,
                        style = Stroke(width = bandThickness * 0.92f, cap = strokeCap)
                    )
                    drawArc(
                        brush = Brush.radialGradient(
                             colors = listOf(Color.White.copy(alpha=0.4f), Color.Transparent),
                             center = arcCenter, radius = radius + bandThickness
                        ),
                        startAngle = startA, sweepAngle = sweepA * progress, useCenter = false,
                        topLeft = topLeft, size = drawSize,
                        style = Stroke(width = bandThickness * 0.3f, cap = strokeCap)
                    )
                }
            }

            // Particule
            particles.forEach { p ->
                val normLife = p.life / p.maxLife
                drawCircle(p.color.copy(alpha = normLife), (15f * normLife) + 3f, Offset(p.x, p.y))
            }
        }

        // --- DOCK GĂLEȚI ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(30.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                WowRainbowData.forEachIndexed { index, data ->
                    val isSelected = selectedColorIndex == index
                    val isDone = bandCompleted[index]
                    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f)

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .scale(scale)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    if (selectedColorIndex != index) {
                                        lastInteractionTime = System.currentTimeMillis()
                                        selectedColorIndex = index
                                        playSfx("sfx_pop_select")
                                        soundManager.playVoiceByName(data.audioName)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = data.bucketResId),
                            contentDescription = data.name,
                            modifier = Modifier.fillMaxSize().shadow(if (isSelected) 6.dp else 1.dp, CircleShape)
                        )
                        if (isDone) {
                            // FIX: Border importat prin androidx.compose.foundation.*
                            Box(modifier = Modifier.align(Alignment.TopEnd).size(16.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape))
                        }
                    }
                }
            }
        }
        
        // --- TUTORIAL MÂNUȚĂ ---
        if (showHandTutorial && selectedColorIndex != -1 && !bandCompleted[selectedColorIndex]) {
            val infiniteTransition = rememberInfiniteTransition(label = "hand")
            val handOffset by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "hand"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-120).dp - (140.dp * handOffset), x = ((selectedColorIndex - 3) * 60).dp)
                    .alpha(1f - handOffset)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ui_tutorial_hand),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).rotate(-15f)
                )
            }
        }
        
        // Buton BACK
        Image(
            painter = painterResource(id = R.drawable.ui_btn_back_wood),
            contentDescription = "Back",
            modifier = Modifier.padding(20.dp).size(64.dp).shadow(4.dp, CircleShape)
                .pointerInput(Unit) { detectTapGestures { onBack() } }
        )
        
        // --- MASCOTA SOARE ---
        val sunResId = when {
            isWin -> R.drawable.mascot_sun_win
            hasStartedPainting -> R.drawable.mascot_sun_happy
            else -> R.drawable.mascot_sun_sleep
        }
        val pulseScale by rememberInfiniteTransition(label="p").animateFloat(
            initialValue = 1f, targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label="p"
        )
        val winScale by animateFloatAsState(if (isWin) 1.2f else 1f)

        Image(
            painter = painterResource(id = sunResId),
            contentDescription = "Sun",
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(140.dp).scale(pulseScale * winScale)
        )
    }
}