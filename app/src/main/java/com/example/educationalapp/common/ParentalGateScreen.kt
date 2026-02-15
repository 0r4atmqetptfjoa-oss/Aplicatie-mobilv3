package com.example.educationalapp.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManagerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close

// --- Design Parental 2026 ---
private val ParentalGradient = Brush.verticalGradient(listOf(Color(0xFF263238), Color(0xFF455A64)))
private val KeypadBg = Color.White.copy(alpha = 0.1f)

@Composable
fun ParentalGateScreen(
    navController: NavController,
    onSuccessDestination: String = "settings_route"
) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember {
        EntryPointAccessors.fromApplication(ctx.applicationContext, SoundManagerEntryPoint::class.java).soundManager()
    }

    var factor1 by remember { mutableIntStateOf(Random.nextInt(3, 10)) }
    var factor2 by remember { mutableIntStateOf(Random.nextInt(2, 6)) }
    var userAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val correctAnswer = factor1 * factor2

    Box(modifier = Modifier.fillMaxSize().background(ParentalGradient)) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu_parallax_magic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.15f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Acces Părinți 🔒",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Rezolvă operația pentru a continua",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Zona Întrebării
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$factor1 x $factor2",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp).width(100.dp), color = Color.White.copy(alpha = 0.3f))
                    Text(
                        text = userAnswer.ifEmpty { "?" },
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isError) Color(0xFFEF5350) else Color(0xFF81C784)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tastatură Premium
            ParentalKeypad(
                onKey = { key ->
                    isError = false
                    soundManager.playClickIconSound()
                    when (key) {
                        "DEL" -> if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                        "OK" -> {
                            if (userAnswer.toIntOrNull() == correctAnswer) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                navController.popBackStack()
                                navController.navigate(onSuccessDestination)
                            } else {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                isError = true
                                userAnswer = ""
                                factor1 = Random.nextInt(3, 10)
                                factor2 = Random.nextInt(2, 6)
                            }
                        }
                        else -> if (userAnswer.length < 3) userAnswer += key
                    }
                }
            )
        }

        // Close
        IconButton(
            onClick = { soundManager.playClickIconSound(); navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun ParentalKeypad(onKey: (String) -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("DEL", "0", "OK")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    KeyButton(key) { onKey(key) }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "keyScale")

    Box(
        modifier = Modifier
            .size(75.dp, 65.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (text == "OK") Color(0xFF43A047) else if (text == "DEL") Color(0xFFD32F2F) else KeypadBg)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") Icon(Icons.Rounded.Backspace, contentDescription = null, tint = Color.White)
        else if (text == "OK") Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
        else Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
