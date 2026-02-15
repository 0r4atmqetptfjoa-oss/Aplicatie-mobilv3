package com.example.educationalapp.features.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.educationalapp.R
import com.example.educationalapp.alphabet.AlphabetSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MagicGardenGameScreen(
    viewModel: MagicGardenViewModel = hiltViewModel(),
    hasFullVersion: Boolean = false,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundPlayer = remember { AlphabetSoundPlayer(ctx) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    var confettiBurstId by remember { mutableLongStateOf(0L) }
    var gardenCenter by remember { mutableStateOf(Offset.Zero) }
    val sparkles = remember { mutableStateListOf<Sparkle>() }

    fun spawnSparkle(at: Offset) {
        val s = Sparkle(id = System.currentTimeMillis() + sparkles.size, pos = at)
        sparkles.add(s)
        scope.launch {
            delay(420)
            sparkles.remove(s)
        }
    }

    LaunchedEffect(uiState.stage) {
        if (uiState.stage == GardenStage.GROWN) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            soundPlayer.playPositive()
            confettiBurstId = System.currentTimeMillis()
        } else if (uiState.stage == GardenStage.DUG || uiState.stage == GardenStage.SEEDED || uiState.stage == GardenStage.SPROUT) {
            soundPlayer.playDing()
        }
    }

    MagicConfettiBox(burstId = confettiBurstId) {
        Box(modifier = Modifier.fillMaxSize()) {

            val bgBreath by rememberInfiniteTransition(label = "bg").animateFloat(
                initialValue = 1.02f, targetValue = 1.05f,
                animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOutSine), RepeatMode.Reverse), label = "bgScale"
            )

            Image(
                painter = painterResource(id = R.drawable.bg_sunny_meadow),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(bgBreath),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ui_button_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(62.dp).clickable { soundPlayer.playClick(); onBack() }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Recoltă: ${uiState.harvestCount}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.graphicsLayer { shadowElevation = 8f }
                )
            }

            val sunAlpha by animateFloatAsState(if (uiState.isCloudMoved || uiState.stage == GardenStage.GROWN) 1f else 0.35f, tween(600), label = "sunAlpha")
            val sunPulse by rememberInfiniteTransition(label = "sun").animateFloat(1f, 1.08f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "sunPulse")

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 64.dp, end = 40.dp)) {
                Image(painter = painterResource(id = R.drawable.char_sun_happy), contentDescription = "Sun", modifier = Modifier.size(150.dp).alpha(sunAlpha).scale(sunPulse))
                if (uiState.isCloudMoved || uiState.stage == GardenStage.GROWN) {
                    SunRays(modifier = Modifier.matchParentSize().alpha(0.6f))
                }
            }

            DraggableCloud(
                isEnabled = (uiState.stage == GardenStage.SPROUT),
                isMovedAway = uiState.isCloudMoved,
                onMovedAway = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    soundPlayer.playClick()
                    viewModel.onCloudMovedAway()
                }
            )

            GardenPatch(
                stage = uiState.stage,
                plantRes = uiState.currentPlant.imageRes,
                progress = uiState.actionProgress,
                isCelebrating = uiState.isCelebrating,
                sparkles = sparkles,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 128.dp)
                    .size(360.dp)
                    .onGloballyPositioned {
                        val pos = it.positionInRoot()
                        gardenCenter = Offset(pos.x + it.size.width / 2f, pos.y + it.size.height / 2f)
                    }
                    .pointerInput(uiState.stage) {
                        if (uiState.stage == GardenStage.GROWN) {
                            detectTapGestures {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                soundPlayer.playClick()
                                viewModel.harvestAndNext()
                            }
                        }
                    }
            )

            ToolShelf(
                currentTool = viewModel.currentTool(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            )

            viewModel.currentTool()?.let { tool ->
                val toolRes = when (tool) {
                    ToolType.SHOVEL -> R.drawable.tool_shovel
                    ToolType.SEEDS -> R.drawable.tool_seed_bag
                    ToolType.WATER -> R.drawable.tool_watering_can
                }
                DraggableTool2026(
                    toolType = tool,
                    imageRes = toolRes,
                    patchCenter = gardenCenter,
                    stage = uiState.stage,
                    onWorkTick = { intensity ->
                        val d = (0.005f * intensity).coerceIn(0.002f, 0.04f)
                        viewModel.addActionProgress(d)
                        if (Random.nextInt(20) == 0) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    },
                    onMilestone = { at ->
                        spawnSparkle(at)
                    }
                )
            }

            if (uiState.stage == GardenStage.GROWN) {
                val popInf = rememberInfiniteTransition(label = "hint")
                val popScale by popInf.animateFloat(1f, 1.1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pop")
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).scale(popScale)) {
                    Text("Culege planta! 🥕", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.graphicsLayer { shadowElevation = 8f })
                }
            }

            MagicSquishyButton(
                onClick = { soundPlayer.playClick(); viewModel.resetGame() },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 16.dp),
                size = 64.dp
            ) {
                Image(painter = painterResource(id = R.drawable.icon_alphabet_replay), contentDescription = "Reset", modifier = Modifier.size(36.dp))
            }
        }
    }
}

private data class Sparkle(val id: Long, val pos: Offset)

@Composable
private fun GardenPatch(stage: GardenStage, plantRes: Int, progress: Float, isCelebrating: Boolean, sparkles: List<Sparkle>, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val dirtRes = when (stage) {
            GardenStage.DIRT -> R.drawable.prop_dirt_flat
            GardenStage.DUG -> R.drawable.prop_dirt_hole
            else -> R.drawable.prop_dirt_flat
        }
        Image(painter = painterResource(id = dirtRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        when (stage) {
            GardenStage.SPROUT -> {
                val sInf = rememberInfiniteTransition(label = "sprout")
                val sScale by sInf.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "s")
                Image(painter = painterResource(id = R.drawable.prop_plant_sprout), contentDescription = "Sprout", modifier = Modifier.size(130.dp).scale(sScale).offset(y = (-10).dp))
            }
            GardenStage.GROWN -> {
                val scale by animateFloatAsState(if (isCelebrating) 1.3f else 1.15f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow), label = "plantScale")
                Image(painter = painterResource(id = plantRes), contentDescription = "Plant", modifier = Modifier.size(280.dp).scale(scale).offset(y = (-20).dp))
            }
            else -> {}
        }
        if (stage == GardenStage.DIRT || stage == GardenStage.DUG || stage == GardenStage.SEEDED) {
            val label = when (stage) {
                GardenStage.DIRT -> "SAPĂ"; GardenStage.DUG -> "SEMINȚE"; GardenStage.SEEDED -> "APĂ"; else -> ""
            }
            ProgressRing(progress = progress, label = label)
        }
        SparkleLayer(sparkles = sparkles)
    }
}

@Composable
private fun ProgressRing(progress: Float, label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = Stroke(width = 14f, cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.25f), -90f, 360f, false, style = stroke)
            drawArc(Color(0xFF8BC34A), -90f, 360f * progress.coerceIn(0f, 1f), false, style = stroke)
        }
        Text(text = label, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).graphicsLayer { shadowElevation = 8f })
    }
}

@Composable
private fun SparkleLayer(sparkles: List<Sparkle>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        sparkles.forEach { s ->
            drawCircle(Color.Yellow.copy(alpha = 1f), radius = 8f, center = Offset(size.width/2 + s.pos.x, size.height/2 + s.pos.y))
        }
    }
}

@Composable
private fun ToolShelf(currentTool: ToolType?, modifier: Modifier = Modifier) {
    val alphaInactive = 0.35f; val alphaActive = 1f
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        ToolIcon(R.drawable.tool_shovel, "Sapă", currentTool == ToolType.SHOVEL, alphaInactive, alphaActive)
        Spacer(Modifier.width(24.dp))
        ToolIcon(R.drawable.tool_seed_bag, "Semințe", currentTool == ToolType.SEEDS, alphaInactive, alphaActive)
        Spacer(Modifier.width(24.dp))
        ToolIcon(R.drawable.tool_watering_can, "Apă", currentTool == ToolType.WATER, alphaInactive, alphaActive)
    }
}

@Composable
private fun ToolIcon(res: Int, label: String, active: Boolean, alphaInactive: Float, alphaActive: Float) {
    val a by animateFloatAsState(if (active) alphaActive else alphaInactive, tween(400), label = "toolAlpha")
    val s by animateFloatAsState(if (active) 1.15f else 1f, spring(dampingRatio = 0.6f), label = "toolScale")
    Image(painter = painterResource(id = res), contentDescription = label, modifier = Modifier.size(75.dp).scale(s).alpha(a))
}

@Composable
private fun DraggableTool2026(toolType: ToolType, imageRes: Int, patchCenter: Offset, stage: GardenStage, onWorkTick: (Float) -> Unit, onMilestone: (Offset) -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var initialCenter by remember { mutableStateOf(Offset.Zero) }
    val rotation by animateFloatAsState(if (isDragging) -15f else 0f, tween(200), label = "rot")
    val scale by animateFloatAsState(if (isDragging) 1.25f else 1f, spring(dampingRatio = 0.5f), label = "scale")

    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomCenter).padding(bottom = 100.dp).offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }.size(130.dp).zIndex(if (isDragging) 50f else 10f).onGloballyPositioned { if (initialCenter == Offset.Zero) { val pos = it.positionInRoot(); initialCenter = Offset(pos.x + it.size.width / 2f, pos.y + it.size.height / 2f) } }.pointerInput(toolType, stage) {
        detectDragGestures(onDragStart = { isDragging = true }, onDragEnd = { isDragging = false; offsetX = 0f; offsetY = 0f }, onDragCancel = { isDragging = false; offsetX = 0f; offsetY = 0f }) { change, dragAmount ->
            change.consume()
            offsetX += dragAmount.x; offsetY += dragAmount.y
            if (patchCenter != Offset.Zero && initialCenter != Offset.Zero) {
                val cur = initialCenter + Offset(offsetX, offsetY)
                val dist = (cur - patchCenter).getDistance()
                if (dist < 250f) {
                    val intensity = (abs(dragAmount.x) + abs(dragAmount.y)).coerceIn(1f, 50f)
                    onWorkTick(intensity)
                    if (intensity > 25f) onMilestone(cur - patchCenter)
                }
            }
        }
    }) {
        Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.fillMaxSize().scale(scale).rotate(rotation))
    }
}

@Composable
private fun DraggableCloud(isEnabled: Boolean, isMovedAway: Boolean, onMovedAway: () -> Unit) {
    val ctx = LocalContext.current
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(if (isMovedAway) -500f else offsetX, tween(1000, easing = EaseInBack), label = "cloudX")
    if (isMovedAway && animatedOffsetX <= -480f) return
    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter).padding(top = 64.dp).offset { IntOffset(animatedOffsetX.roundToInt(), 0) }.size(200.dp).zIndex(15f).pointerInput(isEnabled) {
        if (isEnabled) detectDragGestures(onDragEnd = { if (offsetX < -120f) onMovedAway() else offsetX = 0f }, onDragCancel = { offsetX = 0f }) { change, dragAmount ->
            change.consume()
            offsetX = (offsetX + dragAmount.x).coerceAtMost(0f)
        }
    }) {
        val cloudRes = ctx.resources.getIdentifier("char_cloud_white", "drawable", ctx.packageName).let { if (it != 0) it else R.drawable.char_sun_happy }
        Image(painter = painterResource(id = cloudRes), contentDescription = "Cloud", modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun SunRays(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val inf = rememberInfiniteTransition(label = "rays")
    val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rot")
    val raysRes = ctx.resources.getIdentifier("fx_sun_rays", "drawable", ctx.packageName).let { if (it != 0) it else R.drawable.ui_icon_star }
    Image(painter = painterResource(id = raysRes), contentDescription = null, modifier = modifier.rotate(rot), contentScale = ContentScale.Fit)
}

@Composable
private fun MagicSquishyButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp? = null, shape: androidx.compose.ui.graphics.Shape = CircleShape, color: Color = Color.White, elevation: Dp = 6.dp, content: @Composable BoxScope.() -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(if (isPressed) 0.85f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "btnScale")
    Surface(onClick = onClick, modifier = modifier.scale(buttonScale).let { if (size != null) it.size(size) else it }, shape = shape, color = color, shadowElevation = elevation, interactionSource = interactionSource) { Box(contentAlignment = Alignment.Center, content = content) }
}

@Composable
private fun MagicConfettiBox(burstId: Long, modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    val colors = listOf(Color(0xFFFFEB3B), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFFF5722), Color.White)
    val particles = remember { mutableStateListOf<GardenConfettiParticle>() }
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        LaunchedEffect(burstId) {
            if (burstId > 0L) {
                particles.clear()
                repeat(70) { id ->
                    particles.add(GardenConfettiParticle(id, Random.nextFloat() * widthPx, -50f, colors.random(), Random.nextFloat() * 0.5f + 0.5f, (Random.nextFloat() - 0.5f) * 300f, Random.nextFloat() * 360f, (Random.nextFloat() - 0.5f) * 150f, 500f + Random.nextFloat() * 400f))
                }
                var lastTime = withFrameNanos { it }
                while (isActive && particles.isNotEmpty()) {
                    withFrameNanos { now ->
                        val dt = (now - lastTime) / 1e9f; lastTime = now
                        val newList = particles.map { p ->
                            p.apply {
                                x += (vx + sin(now / 1e9f * 3 + id) * 30) * dt
                                y += vy * dt
                                currentRotation += rotationSpeed * dt
                            }
                        }.filter { it.y < heightPx + 100 }
                        particles.clear(); particles.addAll(newList)
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            Canvas(modifier = Modifier.fillMaxSize().zIndex(999f)) {
                particles.forEach { p ->
                    withTransform({
                        translate(p.x, p.y)
                        rotate(p.currentRotation)
                        scale(p.scale, p.scale)
                    }) {
                        drawRect(p.color, Offset(-10f, -6f), Size(20f, 12f))
                    }
                }
            }
        }
    }
}

private data class GardenConfettiParticle(val id: Int, var x: Float, var y: Float, val color: Color, val scale: Float, val rotationSpeed: Float, var currentRotation: Float, var vx: Float, var vy: Float)
