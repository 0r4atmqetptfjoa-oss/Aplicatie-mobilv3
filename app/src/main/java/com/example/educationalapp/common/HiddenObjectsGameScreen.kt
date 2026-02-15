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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.navigation.backToGamesMenu
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * ULTRA Hidden Objects:
 * - Not a grid. A real "scene" with objects positioned & rotated.
 * - Tap the correct object to collect it.
 * - Combo scoring + gentle motion + particle bursts.
 */
@Composable
fun HiddenObjectsGameScreen(navController: NavController, starState: MutableState<Int>) {
    val particles = remember { ParticleController() }

    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var found by remember { mutableIntStateOf(0) }
    val totalTargets = 10

    // Pool of drawables that exist in drawable.zip (see drawable_report/index_max.json)
    val pool = remember {
        listOf(
            R.drawable.img_math_apple,
            R.drawable.img_math_banana,
            R.drawable.img_math_strawberry,
            R.drawable.img_math_orange,
            R.drawable.img_math_star,
            R.drawable.img_math_balloon,
            R.drawable.shape_square_gift,
            R.drawable.shape_rect_book,
            R.drawable.shape_triangle_pizza,
            R.drawable.shape_circle_donut,
            R.drawable.albina_pufoasa,
            // In setul de resurse, iepure/veverita exista ca variante "alphabet_*".
            R.drawable.alphabet_i_iepure,
            R.drawable.alphabet_v_veverita,
            R.drawable.zebra,
            R.drawable.balena
        )
    }

    var targetRes by remember { mutableStateOf(pool.random()) }
    var roundKey by remember { mutableIntStateOf(0) }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_game_hiddenobjects,
        hud = GameHudState(
            title = "Obiecte Ascunse",
            score = score,
            levelLabel = "${found}/$totalTargets",
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

            TargetBar(
                targetRes = targetRes,
                streak = streak,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(6.dp)
            ) {
                val sceneW = maxWidth
                val sceneH = maxHeight
                val dens = LocalDensity.current.density

                val items = remember(roundKey, sceneW, sceneH) {
                    generateScene(
                        pool = pool,
                        targetRes = targetRes,
                        width = sceneW,
                        height = sceneH,
                        itemCount = 18
                    )
                }

                // Soft vignette for depth
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)),
                                radius = max(sceneW.value, sceneH.value) * 1.2f
                            )
                        )
                )

                items.forEach { obj ->
                    val bob by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "bob"
                    )
                    val alpha = if (obj.found) 0f else 1f

                    Image(
                        painter = painterResource(obj.resId),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .zIndex(if (obj.isTarget) 2f else 1f)
                            .offset(obj.x, obj.y)
                            .size(obj.size)
                            .graphicsLayer {
                                rotationZ = obj.rotation
                                scaleX = obj.scale
                                scaleY = obj.scale
                                this.alpha = alpha
                                translationY = (obj.bobAmpPx * kotlin.math.sin((roundKey + obj.seed) * 0.13f) * 0.04f).toFloat()
                            }
                            .clickable(enabled = !obj.found) {
                                if (obj.resId == targetRes) {
                                    obj.found = true
                                    found++
                                    streak++
                                    val bonus = (streak.coerceAtMost(7) - 1) * 2
                                    score += 10 + bonus
                                    starState.value += 1

                                    // Burst at approximate center (dp -> px not needed; particles uses px from layout)
                                    // We approximate using constraints (good enough visually)
                                    particles.burst(
                                        origin = Offset(
                                            x = (obj.x.value + obj.size.value / 2f) * dens,
                                            y = (obj.y.value + obj.size.value / 2f) * dens
                                        ),
                                        count = 80
                                    )

                                    // Next target / next round
                                    if (found >= totalTargets) {
                                        // big finale
                                        particles.burst(
                                            Offset(sceneW.value * dens / 2f, sceneH.value * dens / 2f),
                                            count = 160
                                        )
                                        // reset
                                        found = 0
                                        score += 25
                                        streak = 0
                                    }

                                    targetRes = pool.random()
                                    roundKey++
                                } else {
                                    streak = 0
                                    score = (score - 4).coerceAtLeast(0)
                                }
                            }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TargetBar(targetRes: Int, streak: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.14f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Găsește:",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.width(10.dp))
        Image(
            painter = painterResource(targetRes),
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = if (streak >= 2) "COMBO x$streak" else "",
            color = Color(0xFFFFF59D),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }
}

private class SceneObject(
    val resId: Int,
    val isTarget: Boolean,
    val x: Dp,
    val y: Dp,
    val size: Dp,
    val rotation: Float,
    val scale: Float,
    val seed: Int,
    val bobAmpPx: Float,
    foundInitially: Boolean = false
) {
    var found by mutableStateOf(foundInitially)
}

private fun generateScene(
    pool: List<Int>,
    targetRes: Int,
    width: Dp,
    height: Dp,
    itemCount: Int
): List<SceneObject> {
    val objects = mutableListOf<SceneObject>()

    // Reserve a bit of safe padding for HUD
    val padX = 10
    val padY = 10
    val w = max(1f, width.value)
    val h = max(1f, height.value)

    fun overlaps(x: Float, y: Float, s: Float): Boolean {
        return objects.any { o ->
            val dx = (o.x.value + o.size.value / 2f) - (x + s / 2f)
            val dy = (o.y.value + o.size.value / 2f) - (y + s / 2f)
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            dist < (o.size.value + s) * 0.42f
        }
    }

    val targetIndex = Random.nextInt(0, itemCount)
    for (i in 0 until itemCount) {
        val isTarget = i == targetIndex
        val res = if (isTarget) targetRes else pool.random()
        val base = if (isTarget) 56f else 44f
        val s = base + Random.nextFloat() * 26f

        var attempts = 0
        var x = 0f
        var y = 0f
        while (attempts < 80) {
            attempts++
            x = Random.nextFloat() * (w - s - padX) + padX
            y = Random.nextFloat() * (h - s - padY) + padY
            if (!overlaps(x, y, s)) break
        }

        objects.add(
            SceneObject(
                resId = res,
                isTarget = isTarget,
                x = x.dp,
                y = y.dp,
                size = s.dp,
                rotation = Random.nextFloat() * 26f - 13f,
                scale = 0.95f + Random.nextFloat() * 0.25f,
                seed = Random.nextInt(),
                bobAmpPx = 6f + Random.nextFloat() * 10f
            )
        )
    }
    // Shuffle draw order a bit
    return objects.shuffled()
}
