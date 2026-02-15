package com.example.educationalapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.navigation.backToGamesMenu

@Composable
fun PaywallScreen(
    navController: NavController,
    hasFullVersion: Boolean,
    onUnlock: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color(0xFF1976D2))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Upgrade la Full Version",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (hasFullVersion) {
                        "Full Version este deja activată ✅"
                    } else {
                        "Deblochează toate jocurile, poveștile și conținutul premium."
                    },
                    color = Color.White,
                    fontSize = 16.sp
                )

                if (!hasFullVersion) {
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cumpără / Activează")
                    }
                }

                OutlinedButton(
                    onClick = { navController.backToGamesMenu() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Înapoi")
                }
            }
        }
    }
}
