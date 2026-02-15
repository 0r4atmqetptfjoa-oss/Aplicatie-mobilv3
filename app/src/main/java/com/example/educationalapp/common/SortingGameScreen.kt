package com.example.educationalapp

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.navigation.backToGamesMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sin

// --- CLASE AUDIO ---
private class SimpleVoicePlayer(private val context: Context, private val onPlayingChanged: (Boolean) -> Unit) {
    private var player: ExoPlayer? = null
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { onPlayingChanged(isPlaying) }
        override fun onPlaybackStateChanged(playbackState: Int) { onPlayingChanged(player?.isPlaying == true) }
    }
    fun play(resId: Int) {
        if (resId == 0) return
        try {
            if (player == null) {
                player = ExoPlayer.Builder(context).build().apply {
                    val attrs = AudioAttributes.Builder().setUsage(androidx.media3.common.C.USAGE_GAME).setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_SPEECH).build()
                    setAudioAttributes(attrs, true); volume = 1.0f; addListener(listener)
                }
            }
            player?.apply { stop(); clearMediaItems(); setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(resId))); prepare(); play() }
        } catch (_: Exception) { onPlayingChanged(false) }
    }
    fun release() { try { player?.removeListener(listener); player?.release() } catch (_: Exception) { }; player = null; onPlayingChanged(false) }
}
private class TtsSpeaker(private val context: Context, private val onIsSpeakingChanged: (Boolean) -> Unit) {
    private var tts: TextToSpeech? = null; private var ready = false
    fun init(locale: Locale = Locale("ro", "RO")) { if (tts != null) return; tts = TextToSpeech(context) { status -> ready = status == TextToSpeech.SUCCESS; if (ready) { try { tts?.language = locale; tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() { override fun onStart(id: String?) { onIsSpeakingChanged(true) }; override fun onDone(id: String?) { onIsSpeakingChanged(false) }; @Deprecated("Deprecated in Java") override fun onError(id: String?) { onIsSpeakingChanged(false) }; override fun onError(id: String?, err: Int) { onIsSpeakingChanged(false) } }) } catch (_: Exception) { } } } }
    fun speak(text: String) { if (!ready) return; try { onIsSpeakingChanged(true); tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sorting_prompt") } catch (_: Exception) { onIsSpeakingChanged(false) } }
    fun stop() { try { tts?.stop() } catch (_: Exception) { }; onIsSpeakingChanged(false) }
    fun release() { try { tts?.shutdown() } catch (_: Exception) { }; tts = null; onIsSpeakingChanged(false) }
}
private fun loadRaw(context: Context, name: String): Int = context.resources.getIdentifier(name, "raw", context.packageName)

enum class RobotEmotion { IDLE, HAPPY, SAD, HINT, WIN, SURPRISED }

@Composable
fun SortingGameScreen(navController: NavController, starState: MutableState<Int>, viewModel: SortingGameViewModel = hiltViewModel()) {
    val context = LocalContext.current; val haptics = LocalHapticFeedback.current; val particles = remember { ParticleController() }; val scope = rememberCoroutineScope()
    var voicePlaying by remember { mutableStateOf(false) }; var ttsSpeaking by remember { mutableStateOf(false) }
    val voicePlayer = remember { SimpleVoicePlayer(context) { voicePlaying = it } }; val tts = remember { TtsSpeaker(context) { ttsSpeaking = it }.apply { init() } }
    DisposableEffect(Unit) { onDispose { voicePlayer.release(); tts.release() } }
    var lastInteractionMs by remember { mutableStateOf(System.currentTimeMillis()) }; var hintActive by remember { mutableStateOf(false) }; var robotEmotion by remember { mutableStateOf(RobotEmotion.IDLE) }
    val balloonItems = viewModel.items; val sortingMode = viewModel.sortingMode; val level = viewModel.level; val score = viewModel.score; val target = viewModel.currentTarget()

    val ascendingClips = listOf("sg_mode_ascending_01", "sg_mode_ascending_02"); val descendingClips = listOf("sg_mode_descending_01", "sg_mode_descending_02"); val evenOddClips = listOf("sg_mode_even_odd_01", "sg_mode_even_odd_02"); val correctClips = listOf("sg_correct_01", "sg_correct_02", "sg_correct_03"); val wrongClips = listOf("sg_wrong_01", "sg_wrong_02", "sg_wrong_03"); val bombClips = listOf("sg_bomb_hit_01", "sg_bomb_hit_02"); val powerClips = listOf("sg_powerup_collect_01", "sg_powerup_collect_02"); val hintAscendingClips = listOf("sg_hint_ascending_01"); val hintDescendingClips = listOf("sg_hint_descending_01"); val hintEvenOddClips = listOf("sg_hint_evenodd_01"); val comboClips = mapOf(2 to "sg_combo_02", 5 to "sg_combo_05", 10 to "sg_combo_10"); val levelCompleteClips = listOf("sg_level_complete_01", "sg_level_complete_02")

    LaunchedEffect(balloonItems, level) { delay(300); if (!voicePlaying) { val clipName = when (sortingMode) { SortingGameViewModel.SortingMode.ASCENDING -> ascendingClips.random(); SortingGameViewModel.SortingMode.DESCENDING -> descendingClips.random(); SortingGameViewModel.SortingMode.EVEN_ODD -> evenOddClips.random() }; voicePlayer.play(loadRaw(context, clipName)) }; lastInteractionMs = System.currentTimeMillis(); hintActive = false; robotEmotion = RobotEmotion.IDLE }
    LaunchedEffect(lastInteractionMs, balloonItems) { delay(6000); val hasNormal = balloonItems.any { it.type == SortingGameViewModel.BalloonType.NORMAL }; if (System.currentTimeMillis() - lastInteractionMs >= 6000L && hasNormal) { hintActive = true; robotEmotion = RobotEmotion.HINT; if (!voicePlaying && !ttsSpeaking) { val clipName = when (sortingMode) { SortingGameViewModel.SortingMode.ASCENDING -> hintAscendingClips.random(); SortingGameViewModel.SortingMode.DESCENDING -> hintDescendingClips.random(); SortingGameViewModel.SortingMode.EVEN_ODD -> hintEvenOddClips.random() }; voicePlayer.play(loadRaw(context, clipName)) } } }

    var streak by remember { mutableIntStateOf(0) }; val popped = remember { mutableStateMapOf<Int, Boolean>() }
    fun onBalloonClick(index: Int, item: SortingGameViewModel.BalloonItem, center: Offset) {
        val currentTarget = viewModel.currentTarget(); val wasCorrect = item.type == SortingGameViewModel.BalloonType.NORMAL && item.value == currentTarget
        lastInteractionMs = System.currentTimeMillis(); hintActive = false
        if (!wasCorrect && item.type == SortingGameViewModel.BalloonType.NORMAL) { streak = 0; haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); robotEmotion = RobotEmotion.SAD; scope.launch { delay(1500); if (robotEmotion == RobotEmotion.SAD) robotEmotion = RobotEmotion.IDLE } }
        if (ttsSpeaking) tts.stop(); if (voicePlaying) voicePlayer.release()
        viewModel.onBalloonClick(item) { stars -> starState.value += stars }
        if (wasCorrect) { streak++; popped[index] = true; haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); particles.burst(origin = center, count = 70 + (streak.coerceAtMost(6) * 10)); scope.launch { delay(450); popped.remove(index) }; robotEmotion = if (streak >= 3) RobotEmotion.SURPRISED else RobotEmotion.HAPPY; scope.launch { delay(1500); if (robotEmotion == RobotEmotion.HAPPY || robotEmotion == RobotEmotion.SURPRISED) robotEmotion = RobotEmotion.IDLE } }
        val clipName: String = when (item.type) { SortingGameViewModel.BalloonType.BOMB -> { robotEmotion = RobotEmotion.SAD; scope.launch { delay(1500); if(robotEmotion == RobotEmotion.SAD) robotEmotion = RobotEmotion.IDLE }; bombClips.random() }; SortingGameViewModel.BalloonType.POWERUP -> { robotEmotion = RobotEmotion.WIN; scope.launch { delay(1500); if(robotEmotion == RobotEmotion.WIN) robotEmotion = RobotEmotion.IDLE }; powerClips.random() }; SortingGameViewModel.BalloonType.NORMAL -> { if (wasCorrect) comboClips[streak] ?: correctClips.random() else wrongClips.random() } }
        voicePlayer.play(loadRaw(context, clipName))
        if (viewModel.feedback == "Nivel completat!") { robotEmotion = RobotEmotion.WIN; val lvlClip = levelCompleteClips.random(); scope.launch { delay(700); if (!voicePlaying) voicePlayer.play(loadRaw(context, lvlClip)) } }
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_game_sorting,
        // Adaugat spatii in titlu pentru a nu se suprapune cu butonul custom
        hud = GameHudState(title = "     Baloane în Ordine", score = score, levelLabel = "Lvl $level", starCount = starState.value),
        // Lasam un callback gol pentru compatibilitate, dar ascundem actiunea (butonul custom se ocupa de back)
        onBack = { }, 
        particleController = particles
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(64.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val targetText = when { target == null -> "–"; sortingMode == SortingGameViewModel.SortingMode.ASCENDING -> "Țintă: cel mai mic ($target)"; sortingMode == SortingGameViewModel.SortingMode.DESCENDING -> "Țintă: cel mai mare ($target)"; else -> "Țintă: $target" }
                    Text(targetText, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.weight(1f)); Text(if (streak >= 2) "COMBO x$streak" else "", color = Color(0xFFFFF59D), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(18.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), 
                    modifier = Modifier.fillMaxWidth().weight(1f), 
                    contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 160.dp), 
                    horizontalArrangement = Arrangement.spacedBy(10.dp), 
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // --- REZOLVARE CRASH ---
                    // Folosim item.id ca cheie unică, NU hashCode()
                    items(
                        count = balloonItems.size,
                        key = { index -> balloonItems[index].id } 
                    ) { i ->
                        val item = balloonItems[i]
                        BalloonNumber(item = item, indexSeed = (level * 100) + i, popped = popped[i] == true, hint = hintActive && item.type == SortingGameViewModel.BalloonType.NORMAL && item.value == target, onClick = { center -> onBalloonClick(i, item, center) })
                    }
                }
            }

            // --- BUTON BACK WOOD (Stânga Sus) ---
            Image(
                painter = painterResource(R.drawable.ui_btn_back_wood),
                contentDescription = "Inapoi",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopStart) 
                    .padding(start = 12.dp, top = 12.dp)
                    .size(56.dp)
                    .clickable { navController.backToGamesMenu() }
            )

            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 0.dp)) { MascotRobot(emotion = robotEmotion) }
        }
    }
}

@Composable
private fun MascotRobot(emotion: RobotEmotion) {
    val resId = when(emotion) { RobotEmotion.IDLE -> R.drawable.robot_idle; RobotEmotion.HAPPY -> R.drawable.robot_happy; RobotEmotion.SAD -> R.drawable.robot_sad; RobotEmotion.HINT -> R.drawable.robot_hint; RobotEmotion.WIN -> R.drawable.robot_win; RobotEmotion.SURPRISED -> R.drawable.robot_surprised }
    val infinite = rememberInfiniteTransition(label = "robotFloat"); val dy by infinite.animateFloat(initialValue = 0f, targetValue = 15f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "dy")
    val drawableId = try { if (resId != 0) resId else R.drawable.robot_idle } catch (e: Exception) { 0 }
    if (drawableId != 0) { Image(painter = painterResource(id = drawableId), contentDescription = "Robot Mascot", modifier = Modifier.size(160.dp).offset { IntOffset(0, dy.roundToInt()) }.graphicsLayer { rotationZ = dy * 0.1f }) }
}

@Composable
private fun BalloonNumber(
    item: SortingGameViewModel.BalloonItem,
    indexSeed: Int,
    popped: Boolean,
    hint: Boolean = false,
    onClick: (center: Offset) -> Unit
) {
    val allBalloons = remember {
        listOf(
            R.drawable.balloon_blue, R.drawable.balloon_green,
            R.drawable.balloon_orange, R.drawable.balloon_purple,
            R.drawable.balloon_red, R.drawable.balloon_yellow
        )
    }

    val availableBalloons = remember(item.value) {
        val valueStr = item.value?.toString() ?: ""
        var filtered = allBalloons.toList()
        
        for (char in valueStr) {
            when(char) {
                '1' -> filtered = filtered.filter { it != R.drawable.balloon_green }
                '2', '7', '9' -> filtered = filtered.filter { it != R.drawable.balloon_blue }
                '3' -> filtered = filtered.filter { it != R.drawable.balloon_red }
                '4' -> filtered = filtered.filter { it != R.drawable.balloon_yellow }
                '5', '8' -> filtered = filtered.filter { it != R.drawable.balloon_purple }
                '6' -> filtered = filtered.filter { it != R.drawable.balloon_orange }
            }
        }
        if (filtered.isEmpty()) allBalloons else filtered
    }

    val selectedBalloonRes = availableBalloons[(indexSeed).absoluteValue % availableBalloons.size]

    val infinite = rememberInfiniteTransition(label = "balloonFloat")
    val t by infinite.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2200 + (indexSeed % 5) * 120, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "float")
    val popScale by animateFloatAsState(if (popped) 0.2f else 1f, tween(220), label = "popScale")
    val popAlpha by animateFloatAsState(if (popped) 0f else 1f, tween(220), label = "popAlpha")
    val hintScale by animateFloatAsState(if (hint) 1.15f else 1f, tween(600), label = "hintScale")
    val floatY = sin((t * Math.PI * 2).toFloat() + (indexSeed % 11)) * 7f
    var center by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
            .graphicsLayer { translationY = floatY; scaleX = popScale * hintScale; scaleY = popScale * hintScale; alpha = popAlpha }
            .background(Color.White.copy(alpha = 0.08f), CircleShape)
            .onGloballyPositioned { coords -> val p = coords.positionInRoot(); center = Offset(p.x + coords.size.width / 2f, p.y + coords.size.height / 2f) }
            .clickable(enabled = !popped) { onClick(center) },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(selectedBalloonRes), contentDescription = null, modifier = Modifier.size(125.dp))
        
        val context = LocalContext.current
        when (item.type) {
            SortingGameViewModel.BalloonType.NORMAL -> {
                val value = item.value
                if (value != null) {
                    val digits = value.toString().map { it }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-8).dp, Alignment.CenterHorizontally)
                    ) {
                        for (char in digits) {
                            val digitStr = char.toString()
                            val charWidth = if (char == '1') 22.dp else 40.dp
                            val resName = "img_number_${digitStr}"
                            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)

                            if (resId != 0) {
                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = digitStr,
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier.height(52.dp).width(charWidth) 
                                )
                            } else {
                                Text(text = digitStr, style = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White, shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 8f)))
                            }
                        }
                    }
                }
            }
            SortingGameViewModel.BalloonType.BOMB -> { Text("💣", style = TextStyle(fontSize = 44.sp, shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 8f))) }
            SortingGameViewModel.BalloonType.POWERUP -> { Text("⭐", style = TextStyle(fontSize = 42.sp, shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 8f))) }
        }
    }
}
private val Int.absoluteValue: Int get() = if (this < 0) -this else this