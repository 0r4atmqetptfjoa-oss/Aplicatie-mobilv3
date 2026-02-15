package com.example.educationalapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.navigation.backToGamesMenu
import kotlin.math.abs
import kotlin.random.Random

/**
 * ULTRA Maze:
 * - Real maze generation (perfect maze, always solvable)
 * - Swipe OR on-screen controls
 * - Smooth, bouncy movement + haptics
 * - Glowing portal goal + coin pickups
 * - Confetti burst on win
 */
@Composable
fun MazeGameScreen(navController: NavController, starState: MutableState<Int>) {
    val haptics = LocalHapticFeedback.current
    val particles = remember { ParticleController() }

    var level by remember { mutableIntStateOf(1) }
    var moves by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var size by remember { mutableIntStateOf(levelToSize(level)) }
    var maze by remember { mutableStateOf(generateMaze(size)) }
    var player by remember { mutableStateOf(Cell(0, 0)) }
    var coins by remember { mutableStateOf(placeCoins(size, maze, count = (2 + level.coerceAtMost(4)))) }

    fun newLevel(nextLevel: Int) {
        level = nextLevel
        size = levelToSize(level)
        moves = 0
        player = Cell(0, 0)
        maze = generateMaze(size)
        coins = placeCoins(size, maze, count = (2 + level.coerceAtMost(5)))
    }

    LaunchedEffect(Unit) { newLevel(1) }

    fun tryMove(dir: Dir, canvasSize: Size? = null) {
        val next = player + dir
        if (!next.inBounds(size)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            return
        }
        if (maze.canMove(player, dir)) {
            player = next
            moves++
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

            if (coins.remove(next)) {
                score += 5
                // small sparkle at player
                canvasSize?.let {
                    val center = mazeToPxCenter(next, it, size)
                    particles.burst(center, count = 20)
                }
            }

            if (next.isGoal(size)) {
                score += 25 + coins.size * 2
                starState.value += 2

                canvasSize?.let {
                    particles.burst(mazeToPxCenter(next, it, size), count = 120)
                }
                newLevel(level + 1)
            }
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_magic_forest,
        hud = GameHudState(
            title = "Labirint Magic",
            score = score,
            levelLabel = "Lvl $level",
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

            AnimatedContent(
                targetState = size,
                transitionSpec = {
                    (fadeIn() togetherWith fadeOut())
                },
                label = "mazeLevelAnim"
            ) { boardSize ->
                var lastCanvasSize: Size? by remember { mutableStateOf(null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(6.dp)
                        .pointerInput(boardSize, maze, player) {
                            var totalDx = 0f
                            var totalDy = 0f
                            detectDragGestures(
                                onDrag = { _, dragAmount ->
                                    totalDx += dragAmount.x
                                    totalDy += dragAmount.y
                                },
                                onDragEnd = {
                                    val threshold = 36f
                                    val dx = totalDx
                                    val dy = totalDy
                                    totalDx = 0f
                                    totalDy = 0f
                                    if (abs(dx) < threshold && abs(dy) < threshold) return@detectDragGestures
                                    val dir = if (abs(dx) > abs(dy)) {
                                        if (dx > 0) Dir.E else Dir.W
                                    } else {
                                        if (dy > 0) Dir.S else Dir.N
                                    }
                                    tryMove(dir, lastCanvasSize)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
					// "size" exista si ca variabila Int in scope-ul exterior; aici vrem DrawScope.size (Size).
					lastCanvasSize = this.size
                        drawMaze(
                            sizeCells = boardSize,
                            maze = maze,
                            player = player,
                            coins = coins,
                        )
                    }
                }
            }

            // Controls
            MazeControls(
                onUp = { tryMove(Dir.N) },
                onLeft = { tryMove(Dir.W) },
                onRight = { tryMove(Dir.E) },
                onDown = { tryMove(Dir.S) },
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "Mutări: $moves • Monede rămase: ${coins.size}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ------------------------
// UI bits
// ------------------------

@Composable
private fun MazeControls(
    onUp: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onDown: () -> Unit,
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onUp,
            shape = CircleShape,
            colors = buttonColors,
            modifier = Modifier.size(56.dp)
        ) { Text("▲", fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onLeft, shape = CircleShape, colors = buttonColors, modifier = Modifier.size(56.dp)) {
                Text("◀", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Button(onClick = onRight, shape = CircleShape, colors = buttonColors, modifier = Modifier.size(56.dp)) {
                Text("▶", fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(6.dp))
        Button(onClick = onDown, shape = CircleShape, colors = buttonColors, modifier = Modifier.size(56.dp)) {
            Text("▼", fontWeight = FontWeight.Black)
        }
    }
}

// ------------------------
// Maze drawing + model
// ------------------------

private enum class Dir { N, E, S, W }

private data class Cell(val r: Int, val c: Int) {
    fun inBounds(n: Int) = r in 0 until n && c in 0 until n
    fun isGoal(n: Int) = r == n - 1 && c == n - 1
    operator fun plus(d: Dir): Cell = when (d) {
        Dir.N -> copy(r = r - 1)
        Dir.E -> copy(c = c + 1)
        Dir.S -> copy(r = r + 1)
        Dir.W -> copy(c = c - 1)
    }
}

private data class Maze(val n: Int, val walls: Array<IntArray>) {
    // bitmask: 1=N, 2=E, 4=S, 8=W (1 means wall present)
    fun canMove(from: Cell, dir: Dir): Boolean {
        val mask = walls[from.r][from.c]
        return when (dir) {
            Dir.N -> mask and 1 == 0
            Dir.E -> mask and 2 == 0
            Dir.S -> mask and 4 == 0
            Dir.W -> mask and 8 == 0
        }
    }
}

private fun levelToSize(level: Int): Int = (5 + (level - 1) / 2).coerceIn(5, 9)

private fun generateMaze(n: Int): Maze {
    // start fully-walled
    val walls = Array(n) { IntArray(n) { 1 or 2 or 4 or 8 } }
    val visited = Array(n) { BooleanArray(n) }

    fun carve(a: Cell, b: Cell, dir: Dir) {
        when (dir) {
            Dir.N -> {
                walls[a.r][a.c] = walls[a.r][a.c] and 1.inv()
                walls[b.r][b.c] = walls[b.r][b.c] and 4.inv()
            }
            Dir.E -> {
                walls[a.r][a.c] = walls[a.r][a.c] and 2.inv()
                walls[b.r][b.c] = walls[b.r][b.c] and 8.inv()
            }
            Dir.S -> {
                walls[a.r][a.c] = walls[a.r][a.c] and 4.inv()
                walls[b.r][b.c] = walls[b.r][b.c] and 1.inv()
            }
            Dir.W -> {
                walls[a.r][a.c] = walls[a.r][a.c] and 8.inv()
                walls[b.r][b.c] = walls[b.r][b.c] and 2.inv()
            }
        }
    }

    fun dfs(cur: Cell) {
        visited[cur.r][cur.c] = true
        val dirs = Dir.values().toMutableList().shuffled()
        for (d in dirs) {
            val nxt = cur + d
            if (!nxt.inBounds(n) || visited[nxt.r][nxt.c]) continue
            carve(cur, nxt, d)
            dfs(nxt)
        }
    }

    dfs(Cell(0, 0))
    return Maze(n, walls)
}

private fun placeCoins(n: Int, maze: Maze, count: Int): MutableSet<Cell> {
    val coins = mutableSetOf<Cell>()
    val forbidden = setOf(Cell(0, 0), Cell(n - 1, n - 1))
    var attempts = 0
    while (coins.size < count && attempts < 4000) {
        attempts++
        val c = Cell(Random.nextInt(n), Random.nextInt(n))
        if (c in forbidden) continue
        coins.add(c)
    }
    return coins
}

private fun mazeToPxCenter(cell: Cell, canvas: Size, n: Int): Offset {
    val pad = canvas.minDimension * 0.06f
    val grid = canvas.minDimension - pad * 2
    val step = grid / n
    val ox = (canvas.width - grid) / 2f
    val oy = (canvas.height - grid) / 2f
    return Offset(
        x = ox + pad + (cell.c + 0.5f) * step,
        y = oy + pad + (cell.r + 0.5f) * step
    )
}

private fun androidx.compose.ui.geometry.Size.minDimension(): Float = kotlin.math.min(width, height)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMaze(
    sizeCells: Int,
    maze: Maze,
    player: Cell,
    coins: Set<Cell>,
) {
    val pad = size.minDimension * 0.06f
    val grid = size.minDimension - pad * 2
    val step = grid / sizeCells
    val ox = (size.width - grid) / 2f
    val oy = (size.height - grid) / 2f

    // board panel
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.06f))
        ),
        topLeft = Offset(ox, oy),
        size = Size(grid, grid),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f)
    )

    // goal portal
    val goal = Cell(sizeCells - 1, sizeCells - 1)
    val goalCenter = Offset(ox + pad + (goal.c + 0.5f) * step, oy + pad + (goal.r + 0.5f) * step)
    val portalR = step * 0.38f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF8E24AA).copy(alpha = 0.90f), Color.Transparent),
            center = goalCenter,
            radius = portalR * 2.6f
        ),
        radius = portalR * 2.6f,
        center = goalCenter
    )
    drawCircle(color = Color(0xFFE1BEE7).copy(alpha = 0.85f), radius = portalR, center = goalCenter)

    // coins
    coins.forEach { c ->
        val center = Offset(ox + pad + (c.c + 0.5f) * step, oy + pad + (c.r + 0.5f) * step)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFF59D), Color(0xFFFFC107)),
                center = center,
                radius = step * 0.35f
            ),
            radius = step * 0.18f,
            center = center
        )
        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = step * 0.04f, center = center + Offset(-step * 0.05f, -step * 0.06f))
    }

    // walls
    val stroke = Stroke(width = (step * 0.10f).coerceIn(5f, 18f), cap = StrokeCap.Round)
    val wallColor = Color.White.copy(alpha = 0.90f)
    for (r in 0 until sizeCells) {
        for (c in 0 until sizeCells) {
            val mask = maze.walls[r][c]
            val x0 = ox + pad + c * step
            val y0 = oy + pad + r * step
            val x1 = x0 + step
            val y1 = y0 + step
            if (mask and 1 != 0) drawLine(wallColor, start = Offset(x0, y0), end = Offset(x1, y0), strokeWidth = stroke.width, cap = stroke.cap)
            if (mask and 2 != 0) drawLine(wallColor, start = Offset(x1, y0), end = Offset(x1, y1), strokeWidth = stroke.width, cap = stroke.cap)
            if (mask and 4 != 0) drawLine(wallColor, start = Offset(x0, y1), end = Offset(x1, y1), strokeWidth = stroke.width, cap = stroke.cap)
            if (mask and 8 != 0) drawLine(wallColor, start = Offset(x0, y0), end = Offset(x0, y1), strokeWidth = stroke.width, cap = stroke.cap)
        }
    }

    // player
    val playerCenter = Offset(ox + pad + (player.c + 0.5f) * step, oy + pad + (player.r + 0.5f) * step)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF29B6F6), Color(0xFF01579B)),
            center = playerCenter,
            radius = step * 0.50f
        ),
        radius = step * 0.23f,
        center = playerCenter
    )
    drawCircle(color = Color.White.copy(alpha = 0.65f), radius = step * 0.06f, center = playerCenter + Offset(-step * 0.06f, -step * 0.06f))
}
