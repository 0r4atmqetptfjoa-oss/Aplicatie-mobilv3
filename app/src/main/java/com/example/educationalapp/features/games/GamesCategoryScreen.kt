package com.example.educationalapp.features.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educationalapp.R
import com.example.educationalapp.navigation.*

data class GameItem(
    val title: String,
    val icon: Int,
    val background: Int,
    val route: Any
)

val gamesList = listOf(
    GameItem("Balloon Pop", R.drawable.balloon_red, R.drawable.bg_alphabet_sky, BalloonPopRoute),
    GameItem("Peek-a-Boo", R.drawable.icon_game_hiddenobjects, R.drawable.bg_sunny_meadow, PeekABooRoute),
    GameItem("Alphabet", R.drawable.icon_game_alphabet, R.drawable.bg_game_alphabet, AlphabetGraphRoute),
    GameItem("Colors", R.drawable.icon_game_colors, R.drawable.bg_game_colors, ColorsRoute),
    GameItem("Shapes", R.drawable.icon_game_shapes, R.drawable.bg_game_shapes, ShapesRoute),
    GameItem("Puzzle", R.drawable.icon_game_puzzle, R.drawable.bg_game_puzzle, PuzzleRoute),
    GameItem("Memory", R.drawable.icon_game_memory, R.drawable.bg_game_memory, MemoryRoute),
    GameItem("Hidden Objects", R.drawable.icon_game_hiddenobjects, R.drawable.bg_game_hiddenobjects, HiddenObjectsRoute),
    GameItem("Sorting", R.drawable.icon_game_sorting, R.drawable.bg_game_sorting, SortingRoute),
    GameItem("Instruments", R.drawable.icon_game_instruments, R.drawable.bg_game_instruments, InstrumentsGameRoute),
    GameItem("Sequence", R.drawable.icon_game_sequence, R.drawable.bg_game_sequence, SequenceRoute),
    GameItem("Math", R.drawable.icon_game_math, R.drawable.bg_game_math, MathRoute),
    GameItem("Magic Garden", R.drawable.icon_game_hiddenobjects, R.drawable.bg_sunny_meadow, MagicGardenRoute),
    GameItem("Shadow Match", R.drawable.icon_game_shapes, R.drawable.bg_game_shapes, ShadowMatchRoute),
    GameItem("Egg Surprise", R.drawable.icon_game_memory, R.drawable.bg_magic_forest, EggSurpriseRoute),
    GameItem("Feed Monster", R.drawable.icon_game_hiddenobjects, R.drawable.game_bg, FeedMonsterRoute),
    GameItem("Animal Band", R.drawable.icon_game_instruments, R.drawable.bg_game_instruments, AnimalBandRoute),
)

@Composable
fun GamesCategoryScreen(onSelect: (GameItem) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(R.drawable.bg_category_games),
                contentScale = ContentScale.Crop
            )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(gamesList) { item ->
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable { onSelect(item) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
