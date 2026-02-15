package com.example.educationalapp.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight particle system (confetti + sparkles) designed for mini-games.
 * - No allocations per-frame (besides list iteration)
 * - Time-based updates (stable across recomposition)
 */

internal data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var shapeType: Int,
    val color: Color
) {
    fun step(dt: Float) {
        x += vx * dt
        y += vy * dt
        // gravity
        vy += 1200f * dt
        // drag
        vx *= (1f - 0.6f * dt).coerceIn(0.85f, 1f)
        rotation += rotationSpeed * dt
        alpha -= 1.25f * dt
        if (alpha < 0f) alpha = 0f
    }
}

@Stable
class ParticleController {
    private val particles: MutableList<Particle> = mutableListOf()

    /** Emit a burst (screen px). */
    fun burst(
        origin: Offset,
        colors: List<Color> = DefaultConfettiColors,
        count: Int = 70,
        spreadRad: Float = (Math.PI * 2).toFloat(),
        speedPxPerSec: FloatRange = FloatRange(380f, 1050f),
        sizePx: FloatRange = FloatRange(10f, 22f)
    ) {
        repeat(count) {
            val angle = Random.nextFloat() * spreadRad
            val speed = speedPxPerSec.random()
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed - Random.nextFloat() * 350f
            val shape = Random.nextInt(3) // 0 rect, 1 circle, 2 star-ish
            particles.add(
                Particle(
                    x = origin.x,
                    y = origin.y,
                    vx = vx,
                    vy = vy,
                    alpha = 1f,
                    size = sizePx.random(),
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 720f - 360f,
                    shapeType = shape,
                    color = colors.random()
                )
            )
        }
    }

    fun clear() = particles.clear()

    internal fun update(dt: Float) {
        var i = particles.size - 1
        while (i >= 0) {
            val p = particles[i]
            p.step(dt)
            if (p.alpha <= 0.01f || p.y > 100000f) particles.removeAt(i)
            i--
        }
    }

    internal fun draw(draw: (Particle) -> Unit) {
        particles.forEach(draw)
    }
}

data class FloatRange(val min: Float, val max: Float) {
    fun random(): Float = min + Random.nextFloat() * (max - min)
}

val DefaultConfettiColors = listOf(
    Color(0xFFFFC107), // amber
    Color(0xFFEF5350), // red
    Color(0xFFAB47BC), // purple
    Color(0xFF42A5F5), // blue
    Color(0xFF66BB6A), // green
    Color(0xFFFF7043)  // orange
)

/**
 * Fullscreen particle overlay.
 * Call [controller.burst] from your game logic (e.g., on correct answer).
 */
@Composable
fun ParticleOverlay(
    controller: ParticleController,
    modifier: Modifier = Modifier,
    maxParticleSize: Dp = 26.dp
) {
    var timeNs by remember { mutableStateOf<Long?>(null) }
    val maxPx = with(LocalDensity.current) { maxParticleSize.toPx() }

    LaunchedEffect(controller) {
        var last = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.033f)
            last = now
            timeNs = now
            controller.update(dt)
        }
    }

    // timeNs is just to invalidate at frame-rate.
    Canvas(modifier = modifier.fillMaxSize()) {
		// folosim variabila doar ca sa fortam invaldarea la frame-rate (fara nume rezervat "_")
		@Suppress("UNUSED_VARIABLE")
		val frameTick = timeNs
        controller.draw { p ->
            val a = p.alpha.coerceIn(0f, 1f)
            val color = p.color.copy(alpha = a)
            val s = p.size.coerceIn(2f, maxPx)
            when (p.shapeType) {
                0 -> withTransform({
                    translate(p.x, p.y)
                    rotate(p.rotation)
                }) {
                    drawRect(color = color, topLeft = Offset(-s / 2f, -s / 3f), size = androidx.compose.ui.geometry.Size(s, s * 0.66f))
                }
                1 -> drawCircle(color = color, radius = s / 2f, center = Offset(p.x, p.y))
                else -> withTransform({
                    translate(p.x, p.y)
                    rotate(p.rotation)
                }) {
                    // simple 4-point sparkle
                    drawLine(color = color, start = Offset(-s / 2f, 0f), end = Offset(s / 2f, 0f), strokeWidth = s * 0.18f)
                    drawLine(color = color, start = Offset(0f, -s / 2f), end = Offset(0f, s / 2f), strokeWidth = s * 0.18f)
                }
            }
        }
    }
}
