package com.example.educationalapp.features.instruments

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

// --- CONFIGURAȚII DE POZIȚIONARE ---
// Astea sunt clasele pe care le folosești în celelalte fișiere.

data class PosConfig(
    val x: Float, val y: Float, 
    val widthScale: Float = 1f, val heightScale: Float = 1f,
    val rotation: Float = 0f
)

data class StringConfig(
    val startX: Float, val startY: Float,
    val endX: Float, val endY: Float,
    val thickness: Dp
)

// --- LOGICA INTERNĂ (NU MODIFICA DECÂT DACĂ ȘTII CE FACI) ---

sealed class HitArea {
    abstract val id: Int
    abstract fun contains(position: Offset): Boolean
}

data class RectHitArea(override val id: Int, val rect: Rect) : HitArea() {
    override fun contains(position: Offset): Boolean = rect.contains(position)
}

data class LineHitArea(override val id: Int, val start: Offset, val end: Offset, val thicknessPx: Float) : HitArea() {
    override fun contains(position: Offset): Boolean {
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (dx == 0f && dy == 0f) return false
        val t = ((position.x - start.x) * dx + (position.y - start.y) * dy) / (dx * dx + dy * dy)
        val closestX = if (t < 0) start.x else if (t > 1) end.x else start.x + t * dx
        val closestY = if (t < 0) start.y else if (t > 1) end.y else start.y + t * dy
        val dist = sqrt((position.x - closestX).pow(2) + (position.y - closestY).pow(2))
        return dist <= (thicknessPx / 2 + 40f) 
    }
}

@Composable
fun GenericMultiTouchController(
    hitAreas: List<HitArea>,
    onHit: (Int) -> Unit,
    content: @Composable (pressedIds: Set<Int>) -> Unit
) {
    val pressedIds = remember { mutableStateListOf<Int>() }
    Box(
        modifier = Modifier.fillMaxSize().pointerInput(hitAreas) {
            awaitPointerEventScope {
                val activePointers = mutableMapOf<PointerId, Int?>()
                while (true) {
                    val event = awaitPointerEvent()
                    val currentFramePressed = mutableSetOf<Int>()
                    event.changes.forEach { change ->
                        if (change.pressed) {
                            val hit = hitAreas.find { it.contains(change.position) }
                            if (hit != null) {
                                currentFramePressed.add(hit.id)
                                if (activePointers[change.id] != hit.id) {
                                    onHit(hit.id)
                                    activePointers[change.id] = hit.id
                                }
                            } else activePointers[change.id] = null
                        } else activePointers.remove(change.id)
                    }
                    pressedIds.clear()
                    pressedIds.addAll(currentFramePressed)
                }
            }
        }
    ) { content(pressedIds.toSet()) }
}

fun playNote(player: Player?, particles: ParticleController, position: Offset) {
    player?.let { p ->
        p.seekTo(0)
        p.play()
        particles.burst(position)
    }
}

@Composable
fun InstrumentElement(bitmap: ImageBitmap, modifier: Modifier, isPressed: Boolean) {
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "scale")
    Image(
        bitmap = bitmap, contentDescription = null,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        contentScale = ContentScale.Fit
    )
}

@Composable
fun InstrumentString(bitmap: ImageBitmap?, start: Offset, end: Offset, thickness: Dp, isPressed: Boolean) {
    val density = LocalDensity.current
    val delta = end - start
    val lengthPx = delta.getDistance()
    val angle = Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble())).toFloat()
    val vibeOffset by animateFloatAsState(
        targetValue = if (isPressed) 5f else 0f,
        animationSpec = spring(dampingRatio = 0.2f, stiffness = 600f), label = "vibe"
    )
    bitmap?.let { btm ->
        Image(
            bitmap = btm, contentDescription = null, contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset { IntOffset(start.x.toInt(), start.y.toInt()) }
                .width(with(density) { lengthPx.toDp() })
                .height(thickness)
                .graphicsLayer {
                    rotationZ = angle
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    translationY = vibeOffset
                }
        )
    }
}