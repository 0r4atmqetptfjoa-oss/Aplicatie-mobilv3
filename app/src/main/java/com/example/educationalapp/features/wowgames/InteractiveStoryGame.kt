package com.example.educationalapp.features.wowgames

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educationalapp.R

@Composable
fun InteractiveStoryGame(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val page: MutableState<Int> = remember { mutableStateOf(0) }
    Box(modifier = modifier.fillMaxSize()) {
        when (page.value) {
            0 -> {
                StoryScreen(
                    backgroundRes = R.drawable.bg_magic_forest,
                    title = "A curious rabbit finds a treasure chest.",
                    body = "What should the rabbit do?",
                    buttons = listOf(
                        "Open the chest" to { page.value = 1 },
                        "Walk away" to { page.value = 2 }
                    ),
                    onBack = onBack
                )
            }
            1 -> {
                StoryScreen(
                    backgroundRes = R.drawable.bg_magic_forest,
                    title = "The chest is locked!",
                    body = "Which key will open it?",
                    customContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                Text(
                                    text = "🔑◯",
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier.clickable { /* wrong key */ }
                                )
                                Text(
                                    text = "🔑□",
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier.clickable {
                                        page.value = 3
                                    }
                                )
                            }
                            Text(
                                text = "Tap the square key.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    },
                    buttons = listOf(),
                    onBack = onBack
                )
            }
            2 -> {
                StoryScreen(
                    backgroundRes = R.drawable.bg_sunny_meadow,
                    title = "The rabbit hops away happily.",
                    body = "Maybe next time the treasure will be discovered!",
                    buttons = listOf(
                        "Start over" to { page.value = 0 },
                        "Back to menu" to { onBack() }
                    ),
                    onBack = onBack
                )
            }
            3 -> {
                StoryScreen(
                    backgroundRes = R.drawable.bg_pizzeria_kitchen, // placeholder for castle
                    title = "The map leads to a castle!",
                    body = "Adventure awaits at the castle on the hill.",
                    buttons = listOf(
                        "Play again" to { page.value = 0 },
                        "Back to menu" to { onBack() }
                    ),
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun StoryScreen(
    backgroundRes: Int,
    title: String,
    body: String,
    buttons: List<Pair<String, () -> Unit>>,
    onBack: () -> Unit,
    customContent: (@Composable () -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
        )
        Button(onClick = onBack, modifier = Modifier.padding(16.dp)) {
            Text("← Back")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            customContent?.invoke()
            buttons.forEach { (label, action) ->
                Button(onClick = action) { Text(label) }
            }
        }
    }
}
