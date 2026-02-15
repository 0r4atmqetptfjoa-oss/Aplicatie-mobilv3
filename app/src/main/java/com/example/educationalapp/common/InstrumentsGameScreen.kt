package com.example.educationalapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.fx.AssetImage
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.fx.rememberTonePlayer
import com.example.educationalapp.navigation.backToGamesMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * ULTRA Instruments Arcade
 *
 * Three modes, all using the drawable pack + procedural sounds:
 * 1) Xilofon Liber – tap bars, get instant neon particles.
 * 2) Repetă Melodia – memory game (Simon Says) with real tones.
 * 3) Ritm – falling notes, tap the lane on beat.
 */
@Composable
fun InstrumentsGameScreen(navController: NavController, starState: MutableState<Int>) {
    val particles = remember { ParticleController() }
    val tone = rememberTonePlayer()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var mode by remember { mutableStateOf(InstrumentMode.ARCADE) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }

    fun awardStars(count: Int) {
        starState.value += count
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_music_stage,
        hud = GameHudState(
            title = "Arcada Muzicală",
            score = score,
            levelLabel = when (mode) {
                InstrumentMode.ARCADE -> "Alege jocul"
                InstrumentMode.XYLOPHONE -> "Xilofon"
                InstrumentMode.MELODY -> "Melodie"
                InstrumentMode.RHYTHM -> "Ritm"
            },
            starCount = starState.value
        ),
        onBack = {
            if (mode == InstrumentMode.ARCADE) navController.backToGamesMenu() else mode = InstrumentMode.ARCADE
        },
        particleController = particles
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            AnimatedContent(targetState = mode, label = "instrumentMode") { m ->
                when (m) {
                    InstrumentMode.ARCADE -> InstrumentsArcadeMenu(
                        onPickXylo = { mode = InstrumentMode.XYLOPHONE },
                        onPickMelody = { mode = InstrumentMode.MELODY },
                        onPickRhythm = { mode = InstrumentMode.RHYTHM }
                    )

                    InstrumentMode.XYLOPHONE -> XylophoneFreePlay(
                        onNote = { note, at ->
                            tone.play(note)
                            particles.burst(at, count = 40)
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            score += 1
                        },
                        onBigCombo = {
                            awardStars(1)
                            score += 25
                            streak = 0
                        }
                    )

                    InstrumentMode.MELODY -> MelodyMirror(
                        tonePlay = { tone.play(it) },
                        onEarn = { stars, points ->
                            awardStars(stars)
                            score += points
                        },
                        onBurst = { particles.burst(it, count = 110) },
                        onHaptic = { haptics.performHapticFeedback(it) }
                    )

                    InstrumentMode.RHYTHM -> RhythmRun(
                        tonePlay = { tone.play(it) },
                        onEarn = { stars, points ->
                            awardStars(stars)
                            score += points
                        },
                        onBurst = { particles.burst(it, count = 80) },
                        onHaptic = { haptics.performHapticFeedback(it) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (streak >= 2) "COMBO x$streak" else "",
                    color = Color(0xFFFFF59D),
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
            }
            Spacer(Modifier.height(8.dp))

            // Tiny “wow” Easter egg: tap 8 notes quickly -> extra burst
            LaunchedEffect(mode) {
                streak = 0
            }
        }
    }
}

private enum class InstrumentMode { ARCADE, XYLOPHONE, MELODY, RHYTHM }

@Composable
private fun InstrumentsArcadeMenu(
    onPickXylo: () -> Unit,
    onPickMelody: () -> Unit,
    onPickRhythm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        AssetImage(
            drawableName = "alphabet_x_xilofon",
            modifier = Modifier.size(130.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Alege un mini-joc muzical",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeCard(title = "Xilofon", subtitle = "Liber & relax", icon = "icon_game_instruments", onClick = onPickXylo, modifier = Modifier.weight(1f))
            ModeCard(title = "Melodie", subtitle = "Memorie + sunet", icon = "icon_game_sequence", onClick = onPickMelody, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        ModeCard(title = "Ritm", subtitle = "Note pe beat", icon = "icon_game_math", onClick = onPickRhythm, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssetImage(drawableName = icon, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

@Composable
private fun XylophoneFreePlay(
    onNote: (note: Int, at: Offset) -> Unit,
    onBigCombo: () -> Unit
) {
    val colors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFFA726),
            Color(0xFFFFEB3B),
            Color(0xFF66BB6A),
            Color(0xFF26C6DA),
            Color(0xFF42A5F5),
            Color(0xFF7E57C2),
            Color(0xFFEC407A)
        )
    }

    var tapStreak by remember { mutableIntStateOf(0) }
    var lastNote by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Atinge barele! (încearcă să nu repeți aceeași notă)",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.10f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (i in 0 until 8) {
                val note = i
                val scale by animateFloatAsState(
                    targetValue = if (tapStreak > 0 && lastNote == note) 1.03f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "barScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(colors[i].copy(alpha = 0.95f), colors[i].copy(alpha = 0.60f))
                            )
                        )
                        .pointerInput(note) {
                            detectTapGestures { pos ->
                                onNote(note, pos)
                                if (lastNote != note) {
                                    tapStreak++
                                    if (tapStreak >= 8) {
                                        tapStreak = 0
                                        onBigCombo()
                                    }
                                } else {
                                    tapStreak = 0
                                }
                                lastNote = note
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = listOf("DO", "RE", "MI", "FA", "SOL", "LA", "SI", "DO")[i],
                        modifier = Modifier.padding(start = 16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun MelodyMirror(
    tonePlay: (Int) -> Unit,
    onEarn: (stars: Int, points: Int) -> Unit,
    onBurst: (Offset) -> Unit,
    onHaptic: (HapticFeedbackType) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var level by remember { mutableIntStateOf(1) }
    var isPlayingSequence by remember { mutableStateOf(true) }
    var userIndex by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Ascultă și repetă!") }

    val sequence = remember { mutableStateListOf<Int>() }

    fun extendSequence() {
        sequence.add(Random.nextInt(0, 8))
    }

    fun reset() {
        level = 1
        sequence.clear()
        extendSequence()
        userIndex = 0
        isPlayingSequence = true
        message = "Ascultă și repetă!"
    }

    LaunchedEffect(Unit) {
        if (sequence.isEmpty()) extendSequence()
    }

    LaunchedEffect(level, isPlayingSequence) {
        if (isPlayingSequence) {
            message = "Ascultă…"
            delay(500)
            sequence.forEachIndexed { idx, n ->
                tonePlay(n)
                delay(220)
                delay(60)
            }
            message = "Acum tu!"
            userIndex = 0
            isPlayingSequence = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Repetă Melodia",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(text = "Nivel $level • ${sequence.size} note", color = Color.White.copy(alpha = 0.9f))
        Spacer(Modifier.height(10.dp))
        Text(text = message, color = Color(0xFFFFF59D), fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))

        MelodyPads(
            enabled = !isPlayingSequence,
            onTap = { note, at ->
                tonePlay(note)
                onHaptic(HapticFeedbackType.TextHandleMove)

                val expected = sequence.getOrNull(userIndex)
                if (expected == note) {
                    userIndex++
                    onBurst(at)

                    if (userIndex >= sequence.size) {
                        // completed level
                        val stars = if (level % 3 == 0) 2 else 1
                        val points = 20 + (level * 4)
                        onEarn(stars, points)
                        onHaptic(HapticFeedbackType.LongPress)

                        level++
                        extendSequence()
                        isPlayingSequence = true
                    }
                } else {
                    message = "Ups! Încearcă iar."
                    onHaptic(HapticFeedbackType.LongPress)
                    scope.launch {
                        delay(650)
                        message = "Ascultă și repetă!"
                        isPlayingSequence = true
                    }
                }
            }
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { reset() }) { Text("Reset") }
        }
    }
}

@Composable
private fun MelodyPads(
    enabled: Boolean,
    onTap: (note: Int, at: Offset) -> Unit
) {
    val colors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFFA726),
            Color(0xFFFFEB3B),
            Color(0xFF66BB6A),
            Color(0xFF26C6DA),
            Color(0xFF42A5F5),
            Color(0xFF7E57C2),
            Color(0xFFEC407A)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (row in 0 until 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0 until 4) {
                    val i = row * 4 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors[i].copy(alpha = if (enabled) 0.92f else 0.45f))
                            .pointerInput(i, enabled) {
                                if (!enabled) return@pointerInput
                                detectTapGestures { pos -> onTap(i, pos) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = listOf("DO", "RE", "MI", "FA", "SOL", "LA", "SI", "DO")[i], color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

private data class FallingNote(
    val id: Int,
    val lane: Int,
    var y: Float
)

@Composable
private fun RhythmRun(
    tonePlay: (Int) -> Unit,
    onEarn: (stars: Int, points: Int) -> Unit,
    onBurst: (Offset) -> Unit,
    onHaptic: (HapticFeedbackType) -> Unit
) {
    val scope = rememberCoroutineScope()

    var bpm by remember { mutableIntStateOf(108) }
    var score by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }

    val lanes = 4
    val notes = remember { mutableStateListOf<FallingNote>() }

    // spawn loop
    LaunchedEffect(bpm) {
        notes.clear()
        misses = 0
        var id = 0
        while (true) {
            delay((60000 / bpm).toLong())
            notes.add(FallingNote(id = id++, lane = Random.nextInt(0, lanes), y = 0f))
        }
    }

    // fall loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            val iter = notes.listIterator()
            while (iter.hasNext()) {
                val n = iter.next()
                n.y += 8.5f
                if (n.y > 520f) {
                    // missed
                    iter.remove()
                    misses++
                    onHaptic(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Ritm Run",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(text = "Scor local: $score • Greșeli: $misses", color = Color.White.copy(alpha = 0.9f))
        Spacer(Modifier.height(10.dp))

        // lanes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(lanes) { lane ->
                Lane(
                    lane = lane,
                    notes = notes,
                    onHit = { hit, at ->
                        if (hit != null) {
                            notes.remove(hit)
                            val note = (lane * 2) % 8
                            tonePlay(note)
                            onBurst(at)
                            onHaptic(HapticFeedbackType.TextHandleMove)
                            score += 5

                            if (score % 40 == 0) {
                                onEarn(1, 20)
                            }
                        } else {
                            misses++
                            onHaptic(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { bpm = (bpm - 6).coerceAtLeast(84) }) { Text("- BPM") }
            Button(onClick = { bpm = (bpm + 6).coerceAtMost(156) }) { Text("+ BPM") }
        }
    }
}

@Composable
private fun Lane(
    lane: Int,
    notes: List<FallingNote>,
    onHit: (hit: FallingNote?, at: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val hitZoneY = 470f

    Box(
        modifier = modifier
            .height(560.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .pointerInput(lane) {
                detectTapGestures { pos ->
                    val hit = notes
                        .filter { it.lane == lane }
                        .minByOrNull { abs(it.y - hitZoneY) }
                        ?.takeIf { abs(it.y - hitZoneY) < 40f }
                    onHit(hit, pos)
                }
            }
    ) {
        // hit zone
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF59D).copy(alpha = 0.45f))
        )

        notes.filter { it.lane == lane }.forEach { n ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = n.y.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }
    }
}
