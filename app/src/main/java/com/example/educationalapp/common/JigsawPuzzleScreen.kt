package com.example.educationalapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.fx.rememberDrawableId
import com.example.educationalapp.navigation.backToGamesMenu
import kotlin.math.abs
import kotlin.random.Random

/**
 * ULTRA Picture Puzzle
 * - Real image sliding puzzle (3x3) using the drawable pack: puzzle_* assets
 * - Bouncy moves + particles + completion fireworks
 */
@Composable
fun JigsawPuzzleScreen(navController: NavController, starState: MutableState<Int>) {
    val particles = remember { ParticleController() }
    val haptics = LocalHapticFeedback.current

    val puzzlePool = remember {
        listOf(
            "puzzle_city",
            "puzzle_dino",
            "puzzle_ocean",
            "puzzle_space",
            "puzzle_unicorn",
            "puzzle_worldmap",
            "puzzle_pirate",
            "puzzle_race",
            "puzzle_princess"
        )
    }

    var imageName by remember { mutableStateOf(puzzlePool.random()) }
    val imageRes = rememberDrawableId(imageName, fallback = R.drawable.bg_game_puzzle)
	val context = LocalContext.current
	val bitmap = remember(imageRes) {
		BitmapFactory.decodeResource(context.resources, imageRes)
			?: Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
	}

    var moves by remember { mutableIntStateOf(0) }
    var solved by remember { mutableStateOf(false) }

	val gridSize = 3
	var board by remember { mutableStateOf(newSolvableBoard(gridSize)) }

    fun reset() {
        imageName = puzzlePool.random()
        moves = 0
        solved = false
		board = newSolvableBoard(gridSize)
    }

    fun tryMove(index: Int, burstAt: Offset?) {
        if (solved) return
        val blank = board.indexOf(0)
		val r = index / gridSize
		val c = index % gridSize
		val br = blank / gridSize
		val bc = blank % gridSize
        val manhattan = abs(r - br) + abs(c - bc)
        if (manhattan == 1) {
            val mutable = board.toMutableList()
            mutable[blank] = mutable[index]
            mutable[index] = 0
            board = mutable
            moves++

            val nowSolved = isSolved(board)
            if (nowSolved) {
                solved = true
                starState.value += 3
                if (burstAt != null) {
                    particles.burst(burstAt, count = 180)
                }
            } else {
                if (burstAt != null) particles.burst(burstAt, count = 30)
            }
        }
    }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_puzzle_table,
        hud = GameHudState(
            title = "Puzzle Imagine",
            score = moves,
            levelLabel = if (solved) "COMPLET!" else "Mutări",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (solved) "🎉 Bravo!" else "Glisează piesele",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = { reset() }) { Text("Alt Puzzle") }
            }

            Spacer(Modifier.height(12.dp))

            // Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f))
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val tileSize = 98.dp

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
					for (row in 0 until gridSize) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
							for (col in 0 until gridSize) {
								val idx = row * gridSize + col
                                val value = board[idx]
                                var center by remember { mutableStateOf(Offset.Zero) }

                                val pop by animateFloatAsState(
                                    targetValue = if (value == 0) 1f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                    label = "tilePop"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .scale(pop)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (value == 0) Color.Black.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f))
                                        .onGloballyPositioned { coords ->
                                            val p = coords.positionInRoot()
                                            center = Offset(p.x + coords.size.width / 2f, p.y + coords.size.height / 2f)
                                        }
                                        .clickable(enabled = value != 0 && !solved) { tryMove(idx, center) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value != 0) {
									PuzzleTile(
										bitmap = bitmap,
										tile = value,
										gridSize = gridSize,
										modifier = Modifier.fillMaxSize()
									)
                                    }
                                }
                            }
                        }
                    }
                }

                if (solved) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Puzzle complet în $moves mutări!",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(onClick = { navController.backToGamesMenu() }) { Text("Înapoi la Meniu") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PuzzleTile(
    bitmap: Bitmap,
    tile: Int,
    gridSize: Int,
    modifier: Modifier = Modifier
) {
    // tile is 1..(gridSize^2-1), 0 is blank
    val tileIndex = tile - 1
    val srcRow = tileIndex / gridSize
    val srcCol = tileIndex % gridSize

    val srcW = (bitmap.width / gridSize).coerceAtLeast(1)
    val srcH = (bitmap.height / gridSize).coerceAtLeast(1)
    val left = srcCol * srcW
    val top = srcRow * srcH
    val right = if (srcCol == gridSize - 1) bitmap.width else (srcCol + 1) * srcW
    val bottom = if (srcRow == gridSize - 1) bitmap.height else (srcRow + 1) * srcH
    val src = Rect(left, top, right, bottom)

    Canvas(modifier = modifier) {
        val dw = size.width.toInt().coerceAtLeast(1)
        val dh = size.height.toInt().coerceAtLeast(1)
        val dst = Rect(0, 0, dw, dh)
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmap(bitmap, src, dst, null)
        }
    }
}

private fun isSolved(board: List<Int>): Boolean {
    for (i in 0 until board.lastIndex) {
        if (board[i] != i + 1) return false
    }
    return board.last() == 0
}

private fun newSolvableBoard(size: Int): List<Int> {
    val total = size * size
    val nums = (0 until total).toMutableList()

    fun inversions(list: List<Int>): Int {
        var inv = 0
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                val a = list[i]
                val b = list[j]
                if (a != 0 && b != 0 && a > b) inv++
            }
        }
        return inv
    }

    fun isSolvable(list: List<Int>): Boolean {
        val inv = inversions(list)
        return if (size % 2 == 1) {
            inv % 2 == 0
        } else {
            // even width: depends on blank row from bottom
            val blankIndex = list.indexOf(0)
            val blankRowFromBottom = size - (blankIndex / size)
            if (blankRowFromBottom % 2 == 0) inv % 2 == 1 else inv % 2 == 0
        }
    }

    var attempt = 0
    while (attempt < 2000) {
        attempt++
        nums.shuffle()
        if (isSolvable(nums) && !isSolved(nums)) return nums.toList()
    }

    // fallback: a known solvable near-solved
    val fallback = (1 until total).toMutableList()
    fallback.add(0)
    // swap last two to avoid already solved
    val tmp = fallback[total - 2]
    fallback[total - 2] = fallback[total - 3]
    fallback[total - 3] = tmp
    return fallback
}
