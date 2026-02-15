package com.example.educationalapp.alphabet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController

/**
 * A simple screen that allows children to trace a letter with their finger. The
 * traced points are collected into a [Path] and drawn in real time. This
 * implementation demonstrates how a tracing mode could be integrated into
 * existing alphabet games. To check the correctness of the tracing, you could
 * compare the traced path with a reference path of the letter outline and
 * provide feedback accordingly.
 */
@Composable
fun AlphabetTracingScreen(letter: Char, navController: NavController) {
    val points = remember { mutableStateListOf<Offset>() }
    val tracedPath = remember { Path() }
    // Update the path whenever a new point is added
    if (points.isNotEmpty()) {
        tracedPath.reset()
        tracedPath.moveTo(points.first().x, points.first().y)
        for (pt in points.drop(1)) {
            tracedPath.lineTo(pt.x, pt.y)
        }
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        points.add(change.position)
                    }
                }
        ) {
            // Draw the traced line
            drawPath(path = tracedPath, color = primaryColor, style = Stroke(width = 8f))
            // Optionally draw the reference letter outline with low opacity here
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier) {
            Text("Înapoi")
        }
    }
}