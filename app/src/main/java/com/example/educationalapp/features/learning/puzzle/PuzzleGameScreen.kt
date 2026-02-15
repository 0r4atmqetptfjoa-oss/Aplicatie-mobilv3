package com.example.educationalapp.puzzle

import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.educationalapp.R
import com.example.educationalapp.alphabet.AlphabetSoundPlayer
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var size: Float
)

@Composable
fun PuzzleGameScreen(
    viewModel: PuzzleViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val soundPlayer = remember { AlphabetSoundPlayer(ctx) }

    var boardOffset by remember { mutableStateOf(Offset.Zero) }
    var boardSizePx by remember { mutableStateOf(Size.Zero) }
    var trayWidthPx by remember { mutableStateOf(0f) }
    val gapPx = with(density) { 12.dp.toPx() }
    var draggingId by remember { mutableStateOf<Int?>(null) }
    val particles = remember { mutableStateListOf<Particle>() }
    var prevLocked by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            if (particles.isEmpty()) continue
            val dt = 0.016f
            val gravity = 1200f
            val friction = 0.985f
            val it = particles.listIterator()
            while (it.hasNext()) {
                val p = it.next()
                p.vy += gravity * dt
                p.vx *= friction
                p.vy *= friction
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life -= dt
                if (p.life <= 0f) it.remove()
            }
        }
    }

    LaunchedEffect(uiState.pieces, uiState.isComplete, boardOffset) {
        val lockedNow = uiState.pieces.filter { it.isLocked }.map { it.id }.toSet()
        val newlyLocked = lockedNow - prevLocked
        if (newlyLocked.isNotEmpty()) {
            newlyLocked.forEach { id ->
                val p = uiState.pieces.firstOrNull { it.id == id } ?: return@forEach
                spawnSparkle(particles, boardOffset.x + p.targetX + p.width/2f, boardOffset.y + p.targetY + p.height/2f)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                soundPlayer.playDing()
            }
        }
        prevLocked = lockedNow
        if (uiState.isComplete && boardSizePx != Size.Zero) {
            spawnConfetti(particles, boardOffset.x, boardOffset.y, boardSizePx.width, boardSizePx.height)
            soundPlayer.playPositive()
        }
    }

    var startedLayoutKey by remember { mutableStateOf("") }
    LaunchedEffect(boardSizePx, trayWidthPx) {
        if (boardSizePx.width > 10f && boardSizePx.height > 10f && trayWidthPx > 10f) {
            val key = "${boardSizePx.width.toInt()}x${boardSizePx.height.toInt()}_${trayWidthPx.toInt()}"
            if (key != startedLayoutKey) {
                startedLayoutKey = key
                viewModel.startGame(ctx, boardSizePx.width, boardSizePx.height, boardSizePx.width + gapPx, trayWidthPx, boardSizePx.height)
            }
        }
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete && boardSizePx != Size.Zero && trayWidthPx > 0f) {
            delay(2000L)
            viewModel.startGame(ctx, boardSizePx.width, boardSizePx.height, boardSizePx.width + gapPx, trayWidthPx, boardSizePx.height)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_puzzle_table), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Image(painter = painterResource(id = R.drawable.ui_btn_home), contentDescription = "Back", modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).size(56.dp).zIndex(10000f).clickableNoIndication { soundPlayer.playClick(); onBack() })

        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val trayWdp: Dp = minOf(320.dp, maxOf(220.dp, maxWidth * 0.26f))
            trayWidthPx = with(density) { trayWdp.toPx() }
            val availW = maxWidth - trayWdp - 12.dp
            val aspect = 4f / 3f
            var boardW = availW
            var boardH = boardW / aspect
            if (boardH > maxHeight) { boardH = maxHeight; boardW = boardH * aspect }

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                BoardBox(modifier = Modifier.size(boardW, boardH), themeResId = uiState.currentThemeResId, pieces = uiState.pieces, onGloballyPositioned = { offset, size -> if (boardOffset != offset) boardOffset = offset; if (boardSizePx != size) boardSizePx = size })
                TrayBox(modifier = Modifier.width(trayWdp).height(boardH), remaining = uiState.pieces.count { !it.isLocked }, onShuffle = { soundPlayer.playClick(); viewModel.shuffleTray() })
            }
        }

        if (!uiState.isLoading && boardSizePx != Size.Zero) {
            val trayScale = remember(trayWidthPx, boardSizePx.height, uiState.pieces.size) { if (trayWidthPx <= 0f || boardSizePx.height <= 0f || uiState.pieces.isEmpty()) 0.75f else { val p = uiState.pieces.first(); val sx = (trayWidthPx/2f * 0.92f) / p.width.toFloat(); val sy = (boardSizePx.height/8f * 0.92f) / p.height.toFloat(); min(0.85f, maxOf(0.55f, min(sx, sy))) } }
            Canvas(modifier = Modifier.fillMaxSize().zIndex(500f)) { particles.forEach { p -> drawCircle(color = Color.White.copy(alpha = (p.life / 0.9f).coerceIn(0f, 1f)), radius = p.size, center = Offset(p.x, p.y)) } }
            uiState.pieces.forEach { piece ->
                val isDragging = draggingId == piece.id
                val inTray = !piece.isLocked && piece.currentX >= (uiState.trayStartX - 4f)
                val scale by animateFloatAsState(if (isDragging) 1.1f else if (inTray) trayScale else 1f, tween(140))
                val rot by animateFloatAsState(if (isDragging) (if (piece.id % 2 == 0) 2f else -2f) else 0f, tween(160))
                val elevation by animateDpAsState(if (isDragging) 20.dp else if (inTray) 4.dp else 8.dp, tween(140))
                val shape = PuzzleShape(piece.config)
                Image(bitmap = piece.bitmap, contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.offset { IntOffset((boardOffset.x + piece.currentX).roundToInt(), (boardOffset.y + piece.currentY).roundToInt()) }.size(with(density){piece.width.toDp()}, with(density){piece.height.toDp()}).zIndex(if (isDragging) 2000f else piece.id.toFloat()).shadow(elevation, shape, clip = false).clip(shape).border(width = 2.dp, color = Color.White.copy(if (piece.isLocked) 0.4f else 0.8f), shape = shape).pointerInput(piece.id, piece.isLocked) { if (!piece.isLocked) detectDragGestures(onDragStart = { draggingId = piece.id; viewModel.onPiecePickUp(piece.id) }, onDragEnd = { viewModel.onPieceDrop(piece.id); draggingId = null }, onDragCancel = { viewModel.onPieceDrop(piece.id); draggingId = null }) { _, dragAmount -> viewModel.onPieceDrag(piece.id, dragAmount.x, dragAmount.y) } }.graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rot))
            }
        }
        if (uiState.isLoading) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) { Text("Se încarcă...", color = Color.White) }
    }
}

@Composable
private fun BoardBox(modifier: Modifier, themeResId: Int, pieces: List<PuzzlePiece>, onGloballyPositioned: (Offset, Size) -> Unit) {
    Box(modifier = modifier.shadow(14.dp, RoundedCornerShape(18.dp)).background(Color.Black.copy(0.2f), RoundedCornerShape(18.dp)).border(2.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).onGloballyPositioned { if (it.size.width > 0) onGloballyPositioned(it.positionInRoot(), Size(it.size.width.toFloat(), it.size.height.toFloat())) }) {
        if (themeResId != 0) { Image(painter = painterResource(id = themeResId), contentDescription = null, modifier = Modifier.fillMaxSize().alpha(0.2f), contentScale = ContentScale.Crop, colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) })) }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width/4f; val cellH = size.height/4f
            for (i in 1..3) { drawLine(Color.White.copy(0.1f), Offset(i*cellW, 0f), Offset(i*cellW, size.height), 1f); drawLine(Color.White.copy(0.1f), Offset(0f, i*cellH), Offset(size.width, i*cellH), 1f) }
            pieces.filter { !it.isLocked }.forEach { piece ->
                val outline = PuzzleShape(piece.config).createOutline(Size(piece.width.toFloat(), piece.height.toFloat()), layoutDirection, this)
                if (outline is Outline.Generic) { val p = Path(); p.addPath(outline.path, Offset(piece.targetX, piece.targetY)); drawPath(p, Color.White.copy(0.2f), style = Stroke(2f)) }
            }
        }
    }
}

@Composable
private fun TrayBox(modifier: Modifier, remaining: Int, onShuffle: () -> Unit) {
    Box(modifier = modifier.shadow(10.dp, RoundedCornerShape(18.dp)).background(Color.Black.copy(0.2f), RoundedCornerShape(18.dp)).border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).padding(12.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Piese: $remaining/16", color = Color.White.copy(0.9f)); Spacer(Modifier.weight(1f)); Button(onClick = onShuffle, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)), shape = RoundedCornerShape(8.dp)) { Text("Shuffle", fontSize = 12.sp) } }
            Spacer(Modifier.height(8.dp)); Text("Trage piesele pe tablă!", color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
    }
}

private fun spawnSparkle(particles: MutableList<Particle>, x: Float, y: Float) {
    repeat(12) { val a = Random.nextFloat() * 6.28f; val sp = 250f + Random.nextFloat() * 400f; particles.add(Particle(x, y, kotlin.math.cos(a)*sp, kotlin.math.sin(a)*sp - 300f, 0.5f + Random.nextFloat()*0.2f, 3f + Random.nextFloat()*3f)) }
}

private fun spawnConfetti(particles: MutableList<Particle>, bx: Float, by: Float, bw: Float, bh: Float) {
    repeat(100) { particles.add(Particle(bx + Random.nextFloat()*bw, by + Random.nextFloat()*bh*0.2f, -300f + Random.nextFloat()*600f, -900f + Random.nextFloat()*300f, 0.8f + Random.nextFloat()*0.6f, 3f + Random.nextFloat()*4f)) }
}

private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier = composed { clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() } }
