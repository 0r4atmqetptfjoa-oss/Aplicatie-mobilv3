package com.example.educationalapp.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Provides a simple bounce animation for any composable when clicked. This modifier
 * scales the target down slightly on press and springs back to its original size
 * on release. Use this on buttons, cards or any interactive element to make
 * interactions feel more responsive and tactile.
 *
 * Example usage:
 *
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(100.dp)
 *         .background(Color.Red)
 *         .bouncyClickable { onBoxClicked() }
 * )
 * ```
 *
 * The animation uses a spring with medium stiffness. Adjust the stiffness
 * parameter to make the bounce softer or snappier.
 *
 * @param onClick Callback invoked when the click gesture is recognized.
 */
fun Modifier.bouncyClickable(onClick: () -> Unit): Modifier = composed {
    // Track whether the component is currently pressed
    val pressed = remember { mutableStateOf(false) }
    // Animate scale based on pressed state
    val scale by animateFloatAsState(
        targetValue = if (pressed.value) 0.92f else 1f,
        animationSpec = spring(stiffness = 400f), label = "bouncyScale"
    )
    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed.value = true
                    try {
                        // Wait until the press ends (finger lifted or gesture cancelled)
                        awaitRelease()
                    } finally {
                        pressed.value = false
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}