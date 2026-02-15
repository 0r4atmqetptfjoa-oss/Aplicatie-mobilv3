package com.example.educationalapp.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class FxType { PRESS, BURST }

private data class SparkParticle(
    val angle: Float,
    val dist: Float,
    val size: Float,
    val spin: Float,
    val phase: Float
)

private data class FxInstance(
    val id: Long,
    val type: FxType,
    val origin: Offset,
    val startNs: Long,
    val durationNs: Long,
    val particles: List<SparkParticle>
)

/**
 * Premium tap feedback fully in Compose (no extra assets needed):
 *  - on press: a soft ring pulse
 *  - on release: a little sparkle burst
 */
@Composable
fun TapSparkleBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onPressChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val fx = remember { mutableStateListOf<FxInstance>() }
    var timeNs by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            timeNs = now - start
        }
    }

    fun addFx(type: FxType, origin: Offset): Long {
        val now = timeNs
        val id = now + Random.nextLong(0, 9_999_999)
        val duration = if (type == FxType.PRESS) 220_000_000L else 520_000_000L
        val particles = if (type == FxType.BURST) {
            List(10) {
                SparkParticle(
                    angle = (Random.nextFloat() * (2f * PI.toFloat())),
                    dist = Random.nextFloat() * 56f + 34f,
                    size = Random.nextFloat() * 8f + 6f,
                    spin = (Random.nextFloat() * 2f - 1f) * 4f,
                    phase = Random.nextFloat() * 10f
                )
            }
        } else emptyList()

        fx.add(
            FxInstance(
                id = id,
                type = type,
                origin = origin,
                startNs = now,
                durationNs = duration,
                particles = particles
            )
        )

        // cap the list (avoid unbounded growth if spam taps)
        while (fx.size > 12) fx.removeAt(0)
        return id
    }

    fun removeFx(id: Long) {
        fx.removeAll { it.id == id }
    }

    Box(
        modifier = modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput

            detectTapGestures(
                onPress = { pos ->
                    onPressChanged(true)
                    val pressId = addFx(FxType.PRESS, pos)
                    val released = try {
                        tryAwaitRelease()
                    } finally {
                        onPressChanged(false)
                        removeFx(pressId)
                    }

                    if (released) {
                        addFx(FxType.BURST, pos)
                        onClick()
                    }
                }
            )
        }
    ) {
        content()

        Canvas(modifier = Modifier.fillMaxSize()) {
            // clean up finished fx
            fx.removeAll { (timeNs - it.startNs) > it.durationNs }

            fx.forEach { inst ->
                val p = ((timeNs - inst.startNs).toFloat() / inst.durationNs.toFloat()).coerceIn(0f, 1f)
                when (inst.type) {
                    FxType.PRESS -> drawPressRing(inst.origin, p)
                    FxType.BURST -> drawSparkleBurst(inst.origin, inst.particles, p)
                }
            }
        }
    }
}

private fun DrawScope.drawPressRing(origin: Offset, p: Float) {
    val ease = easeOutCubic(p)
    val r = 14f + ease * 54f
    val a = (0.18f * (1f - p)).coerceIn(0f, 0.18f)
    drawCircle(
        color = Color.White.copy(alpha = a),
        radius = r,
        center = origin,
        style = Stroke(width = 5f * (1f - p) + 1.5f)
    )
}

private fun DrawScope.drawSparkleBurst(origin: Offset, particles: List<SparkParticle>, p: Float) {
    val ease = easeOutBack(p)
    val fade = (1f - p).coerceIn(0f, 1f)

    // soft center pop
    drawCircle(
        color = Color.White.copy(alpha = 0.10f * fade),
        radius = 18f + 24f * ease,
        center = origin
    )

    particles.forEachIndexed { i, pt ->
        val d = pt.dist * ease
        val ca = cos(pt.angle.toDouble()).toFloat()
        val sa = sin(pt.angle.toDouble()).toFloat()
        val x = origin.x + ca * d
        val y = origin.y + sa * d

        val wobble = sin(((p * 6f) + pt.phase).toDouble()).toFloat()
        val size = pt.size * (0.9f + 0.25f * wobble)
        val alpha = (0.75f * fade).coerceIn(0f, 0.75f)

        // alternate between stars and circles
        if (i % 2 == 0) {
            val star = starPath(
                center = Offset(x, y),
                outerR = size,
                innerR = size * 0.45f,
                points = 5,
                rotation = pt.spin * p
            )
            drawPath(star, color = Color(0xFFFFF2B0).copy(alpha = alpha))
        } else {
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.85f),
                radius = size * 0.55f,
                center = Offset(x, y)
            )
        }
    }
}

private fun easeOutCubic(t: Float): Float {
    val p = (t - 1f)
    return p * p * p + 1f
}

private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val p = t - 1f
    return 1f + c3 * p * p * p + c1 * p * p
}

private fun starPath(
    center: Offset,
    outerR: Float,
    innerR: Float,
    points: Int,
    rotation: Float
): Path {
    val path = Path()
    val step = (PI.toFloat() * 2f) / (points * 2f)
    val startAngle = -PI.toFloat() / 2f + rotation

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val a = startAngle + step * i
        val ca = cos(a.toDouble()).toFloat()
        val sa = sin(a.toDouble()).toFloat()
        val x = center.x + ca * r
        val y = center.y + sa * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
