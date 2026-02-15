package com.example.educationalapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.fx.rememberDrawableId
import com.example.educationalapp.navigation.backToGamesMenu
import kotlin.random.Random

/**
 * ULTRA Number Quiz
 * - Pick the requested number image among 3 options
 * - Combo + particles + bouncy animations
 */
@Composable
fun NumberQuizScreen(navController: NavController, starState: androidx.compose.runtime.MutableState<Int>) {
    val particles = remember { ParticleController() }
    val haptics = LocalHapticFeedback.current

    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(1) }

    var target by remember { mutableIntStateOf(Random.nextInt(0, 10)) }
    var options by remember { mutableStateOf(generateOptions(target)) }

    fun next(correct: Boolean, center: Offset) {
        if (correct) {
            streak++
            score += 10 + (streak.coerceAtMost(6) - 1) * 2
            starState.value += 1
            particles.burst(center, count = 90)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            streak = 0
            score = (score - 4).coerceAtLeast(0)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        round++
        target = Random.nextInt(0, 10)
        options = generateOptions(target)
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_game_math,
        hud = GameHudState(
            title = "Numărul Corect",
            score = score,
            levelLabel = "Runda $round",
            starCount = starState.value
        ),
        onBack = { navController.backToGamesMenu() },
        particleController = particles
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Text(
                text = "Atinge numărul:",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))

            // Big target badge
            val badgeScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "badge"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(badgeScale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                val id = rememberDrawableId("img_number_$target")
                if (id != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(110.dp)
                    )
                } else {
                    Text(text = target.toString(), color = Color.White, fontSize = 48.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { num ->
                    NumberOptionCard(
                        number = num,
                        modifier = Modifier.weight(1f),
                        onPick = { center -> next(num == target, center) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = if (streak >= 2) "COMBO x$streak" else "",
                color = Color(0xFFFFF59D),
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.weight(1f))
            Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun generateOptions(target: Int): List<Int> {
    val set = mutableSetOf(target)
    while (set.size < 3) set.add(Random.nextInt(0, 10))
    return set.shuffled()
}

@Composable
private fun NumberOptionCard(
    number: Int,
    modifier: Modifier = Modifier,
    onPick: (center: Offset) -> Unit
) {
    val id = rememberDrawableId("img_number_$number")
    var center by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .height(170.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f))
                )
            )
            .onGloballyPositioned { coords ->
                val p = coords.positionInRoot()
                center = Offset(p.x + coords.size.width / 2f, p.y + coords.size.height / 2f)
            }
            .clickable { onPick(center) }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = id,
            transitionSpec = { (fadeIn() togetherWith fadeOut()) },
            label = "numSwap"
        ) { res ->
            if (res != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
            } else {
                Text(text = number.toString(), color = Color.White, fontSize = 40.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = number.toString(), color = Color.White, fontWeight = FontWeight.Black)
    }
}
