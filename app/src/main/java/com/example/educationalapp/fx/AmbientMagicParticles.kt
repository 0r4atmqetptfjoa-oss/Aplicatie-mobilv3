package com.example.educationalapp.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

private data class Mote(
    val x: Float,          // 0..1
    val yOffset: Float,    // 0..1
    val radius: Float,     // px-ish
    val speed: Float,      // vertical speed factor
    val driftAmp: Float,   // lateral drift
    val driftSpeed: Float,
    val twinkleSpeed: Float,
    val phase: Float,
    val baseAlpha: Float
)

/**
 * Seasonless ambient particles: "magic dust / bubbles / motes".
 * Subtle by design (premium, not noisy).
 */
@Composable
fun AmbientMagicParticles(
    modifier: Modifier = Modifier,
    count: Int = 55
) {
    val motes = remember(count) {
        List(count) {
            Mote(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                radius = Random.nextFloat() * 6f + 2f,
                speed = Random.nextFloat() * 0.05f + 0.015f,
                driftAmp = Random.nextFloat() * 1.2f + 0.2f,
                driftSpeed = Random.nextFloat() * 0.9f + 0.35f,
                twinkleSpeed = Random.nextFloat() * 1.4f + 0.6f,
                phase = Random.nextFloat() * 10f,
                baseAlpha = Random.nextFloat() * 0.18f + 0.05f
            )
        }
    }

    var timeNs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            timeNs = now - start
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = timeNs / 1_000_000_000f

        // tiny motes
        motes.forEach { m ->
            val y = (h * m.yOffset + t * (m.speed * h)) % (h + m.radius * 6f)
            val drift = sin(t * m.driftSpeed + m.phase) * (m.driftAmp * w * 0.02f)
            val x = (w * m.x) + drift

            val twinkle = (sin(t * m.twinkleSpeed + m.phase) * 0.5f + 0.5f) // 0..1
            val a = (m.baseAlpha * (0.45f + 0.55f * twinkle)).coerceIn(0f, 0.28f)

            drawCircle(
                color = Color.White.copy(alpha = a),
                radius = m.radius,
                center = Offset(x, y)
            )
        }

        // a few big soft glows (very subtle)
        repeat(6) { i ->
            val px = w * (0.12f + i * 0.15f)
            val py = h * (0.20f + (i % 2) * 0.18f)
            val pulse = (sin(t * 0.25f + i) * 0.5f + 0.5f)
            drawCircle(
                color = Color.White.copy(alpha = 0.03f + 0.02f * pulse),
                radius = (w.coerceAtMost(h) * (0.10f + 0.03f * pulse)),
                center = Offset(px, py)
            )
        }
    }
}
