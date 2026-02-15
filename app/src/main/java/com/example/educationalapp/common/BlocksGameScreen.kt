package com.example.educationalapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.navigation.backToGamesMenu
import kotlin.random.Random

/**
 * ULTRA Blocks = Shadow Match Safari
 * - Identify the animal by its shadow
 * - 3 options, fast rounds, combo streak, confetti
 * - Uses drawable set: shadow_* + animals (elefant, girafa, hipopotam, leu, maimuta, tigru, zebra)
 */
@Composable
fun BlocksGameScreen(navController: NavController, starState: MutableState<Int>) {
    val particles = remember { ParticleController() }

    val deck = remember {
        listOf(
            ShadowCard(R.drawable.shadow_elephant, R.drawable.elefant, "Elefant"),
            ShadowCard(R.drawable.shadow_giraffe, R.drawable.girafa, "Girafă"),
            ShadowCard(R.drawable.shadow_hippo, R.drawable.hipopotam, "Hipopotam"),
            ShadowCard(R.drawable.shadow_lion, R.drawable.leu, "Leu"),
            ShadowCard(R.drawable.shadow_monkey, R.drawable.maimuta, "Maimuță"),
            ShadowCard(R.drawable.shadow_tiger, R.drawable.tigru, "Tigru"),
            ShadowCard(R.drawable.shadow_zebra, R.drawable.zebra, "Zebră"),
        )
    }

    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(1) }
    var question by remember { mutableStateOf(deck.random()) }

    fun nextQuestion() {
        round++
        question = deck.random()
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_game_puzzle,
        hud = GameHudState(
            title = "Umbre Safari",
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Shadow stage
            val shadowScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "shadowScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.06f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = question.shadowRes,
                    transitionSpec = { (fadeIn() togetherWith fadeOut()) },
                    label = "shadowSwap"
                ) { res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(20.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.12f), CircleShape)
                            .padding(18.dp)
                    )
                }

                Text(
                    text = "Cine este?",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            val options = remember(question) {
                val set = mutableSetOf(question)
                while (set.size < 3) set.add(deck.random())
                set.shuffled()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { opt ->
                    var center by remember { mutableStateOf(Offset.Zero) }
                    OptionCard(
                        imageRes = opt.fullRes,
                        label = opt.label,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                val p = coords.positionInRoot()
                                center = Offset(p.x + coords.size.width / 2f, p.y + coords.size.height / 2f)
                            },
                        onClick = {
                            if (opt == question) {
                                streak++
                                score += 10 + (streak.coerceAtMost(6) - 1) * 2
                                starState.value += 1
                                particles.burst(center, count = 110)
                                nextQuestion()
                            } else {
                                streak = 0
                                score = (score - 4).coerceAtLeast(0)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            if (streak >= 2) {
                Text(
                    text = "COMBO x$streak",
                    color = Color(0xFFFFF59D),
                    fontWeight = FontWeight.Black
                )
            } else {
                Spacer(Modifier.height(18.dp))
            }

            Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private data class ShadowCard(
    val shadowRes: Int,
    val fullRes: Int,
    val label: String
)

@Composable
private fun OptionCard(
    imageRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier.size(86.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
