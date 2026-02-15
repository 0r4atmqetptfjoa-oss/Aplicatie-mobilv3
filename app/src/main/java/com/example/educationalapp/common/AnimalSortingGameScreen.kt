package com.example.educationalapp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.educationalapp.fx.AssetImage
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.navigation.backToGamesMenu
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * ULTRA Animal Sorting
 * - Drag the animal card into the correct habitat.
 * - Uses real drawable assets for animals + scenic habitats.
 * - Combo + particles + shake feedback.
 */
@Composable
fun AnimalSortingGameScreen(navController: NavController, starState: MutableState<Int>) {
    val particles = remember { ParticleController() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val habitats = remember {
        listOf(
            Habitat(
                key = "land",
                title = "PĂDURE",
                hint = "Animale de uscat",
                bgName = "bg_jungle_blur"
            ),
            Habitat(
                key = "water",
                title = "OCEAN",
                hint = "Animale de apă",
                bgName = "puzzle_ocean"
            ),
            Habitat(
                key = "air",
                title = "CER",
                hint = "Zburătoare",
                bgName = "bg_alphabet_sky"
            )
        )
    }

    val deck = remember {
        mutableStateListOf(
            AnimalCard("Leu", "leu", "land"),
            AnimalCard("Elefant", "elefant", "land"),
            AnimalCard("Girafă", "girafa", "land"),
            AnimalCard("Zebră", "zebra", "land"),
            AnimalCard("Maimuță", "maimuta", "land"),
            AnimalCard("Tigru", "tigru", "land"),
            AnimalCard("Iepure", "alphabet_i_iepure", "land"),
            AnimalCard("Veveriță", "alphabet_v_veverita", "land"),
            AnimalCard("Balena", "balena", "water"),
            AnimalCard("Hipopotam", "hipopotam", "water"),
            AnimalCard("Crab", "crab", "water"),
            AnimalCard("Albina", "albina_pufoasa", "air"),
            AnimalCard("Papagal", "alphabet_p_papagal", "air"),
            AnimalCard("Pinguin", "alphabet_p_pinguin", "water"),
            AnimalCard("Vultur", "alphabet_v_vultur", "air")
        ).apply { shuffle() }
    }

    var current by remember { mutableStateOf(deck.firstOrNull()) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var solvedCount by remember { mutableIntStateOf(0) }

    // Used for end-of-game fireworks location (px)
    var rootSize by remember { mutableStateOf(IntSize.Zero) }

    val shake = remember { Animatable(0f) }

    fun nextCard() {
        if (deck.isNotEmpty()) deck.removeAt(0)
        current = deck.firstOrNull()
        solvedCount++
    }

    fun reset() {
        deck.clear()
        deck.addAll(
            listOf(
                AnimalCard("Leu", "leu", "land"),
                AnimalCard("Elefant", "elefant", "land"),
                AnimalCard("Girafă", "girafa", "land"),
                AnimalCard("Zebră", "zebra", "land"),
                AnimalCard("Maimuță", "maimuta", "land"),
                AnimalCard("Tigru", "tigru", "land"),
                AnimalCard("Iepure", "alphabet_i_iepure", "land"),
                AnimalCard("Veveriță", "alphabet_v_veverita", "land"),
                AnimalCard("Balena", "balena", "water"),
                AnimalCard("Hipopotam", "hipopotam", "water"),
                AnimalCard("Crab", "crab", "water"),
                AnimalCard("Albina", "albina_pufoasa", "air"),
                AnimalCard("Papagal", "alphabet_p_papagal", "air"),
                AnimalCard("Pinguin", "alphabet_p_pinguin", "water"),
                AnimalCard("Vultur", "alphabet_v_vultur", "air")
            )
        )
        deck.shuffle()
        current = deck.firstOrNull()
        score = 0
        streak = 0
        solvedCount = 0
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_sunny_meadow,
        hud = GameHudState(
            title = "Animale & Habitat",
            score = score,
            levelLabel = "${solvedCount}/15",
            starCount = starState.value
        ),
        onBack = { navController.backToGamesMenu() },
        modifier = Modifier.onGloballyPositioned { rootSize = it.size },
        particleController = particles
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            val zones = remember { mutableStateListOf<ZoneRect>() }
            if (zones.size != habitats.size) {
                zones.clear()
                repeat(habitats.size) { zones.add(ZoneRect(habitats[it].key, Offset.Zero, IntSize.Zero)) }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(64.dp))

                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trage animalul în habitatul corect!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (streak >= 2) "COMBO x$streak" else "Fă o serie pentru bonus!",
                        color = Color(0xFFFFF59D),
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Draggable card area
                val card = current
                if (card == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ai terminat! 🎉",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { reset() }) { Text("Joacă din nou") }
                    }
                } else {
                    DraggableAnimalCard(
                        card = card,
                        shakeX = shake.value,
                        onDrop = { center ->
                            val hit = zones.firstOrNull { it.contains(center) }
                            val correct = hit?.key == card.habitat

                            if (correct) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                particles.burst(center, count = 120)
                                streak++
                                val bonus = (streak.coerceAtMost(7) - 1) * 2
                                score += 10 + bonus
                                starState.value += if (streak % 4 == 0) 2 else 1
                                nextCard()

                                // finale burst
                                if (current == null) {
                                    val cx = rootSize.width.coerceAtLeast(1) / 2f
                                    val cy = rootSize.height.coerceAtLeast(1) / 2f
                                    particles.burst(Offset(cx, cy), count = 220)
                                }
                            } else {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                streak = 0
                                score = (score - 3).coerceAtLeast(0)
                                scope.launch {
                                    shake.snapTo(0f)
                                    shake.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                                    shake.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    // Habitats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        habitats.forEachIndexed { idx, h ->
                            HabitatZone(
                                habitat = h,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(168.dp)
                                    .onGloballyPositioned { coords ->
                                        val p = coords.positionInRoot()
                                        zones[idx] = ZoneRect(h.key, Offset(p.x, p.y), coords.size)
                                    }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private data class Habitat(val key: String, val title: String, val hint: String, val bgName: String)
private data class AnimalCard(val name: String, val drawableName: String, val habitat: String)

private data class ZoneRect(val key: String, val topLeft: Offset, val size: IntSize) {
    fun contains(p: Offset): Boolean {
        val xOk = p.x >= topLeft.x && p.x <= topLeft.x + size.width
        val yOk = p.y >= topLeft.y && p.y <= topLeft.y + size.height
        return xOk && yOk
    }
}

@Composable
private fun DraggableAnimalCard(
    card: AnimalCard,
    shakeX: Float,
    onDrop: (center: Offset) -> Unit
) {
    var basePos by remember { mutableStateOf(Offset.Zero) }
    var baseSize by remember { mutableStateOf(IntSize.Zero) }
    var drag by remember { mutableStateOf(Offset.Zero) }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dragScale"
    )

    Box(
        modifier = Modifier
            .size(220.dp)
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f))
                )
            )
            .onGloballyPositioned { coords ->
                basePos = coords.positionInRoot()
                baseSize = coords.size
            }
            .pointerInput(card) {
                detectDragGestures(
                    onDrag = { change, amount ->
                        drag += Offset(amount.x, amount.y)
                    },
                    onDragEnd = {
                        val center = basePos + drag + Offset(baseSize.width / 2f, baseSize.height / 2f)
                        onDrop(center)
                        drag = Offset.Zero
                    },
                    onDragCancel = { drag = Offset.Zero }
                )
            }
            .background(Color.Transparent)
            .padding(12.dp)
            .offset { IntOffset((drag.x + (shakeX * 10f)).roundToInt(), drag.y.roundToInt()) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AssetImage(drawableName = card.drawableName, modifier = Modifier.size(140.dp))
            Spacer(Modifier.height(8.dp))
            Text(card.name, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun HabitatZone(habitat: Habitat, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(10.dp)
    ) {
        AssetImage(
            drawableName = habitat.bgName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.22f))
        )

        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalAlignment = Alignment.Start
        ) {
            Text(habitat.title, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(habitat.hint, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
