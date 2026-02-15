package com.example.educationalapp.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManagerEntryPoint
import com.example.educationalapp.navigation.MainMenuRoute
import dagger.hilt.android.EntryPointAccessors

// --- Culori Premium 2026 ---
private val SettingsGradient = Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB)))
private val CardActiveGradient = Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
private val CardInactiveColor = Color.White.copy(alpha = 0.15f)
private val GoldGradient = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
private val SuccessGreen = Color(0xFF43A047)

@Composable
fun SettingsScreen(
    navController: NavController,
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    hapticEnabled: Boolean,
    hardModeEnabled: Boolean,
    isPremium: Boolean,
    onSoundChanged: () -> Unit,
    onMusicChanged: () -> Unit,
    onHapticChanged: () -> Unit,
    onHardModeChanged: () -> Unit,
    onBuyPremium: () -> Unit
) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember {
        EntryPointAccessors.fromApplication(ctx.applicationContext, SoundManagerEntryPoint::class.java).soundManager()
    }

    Box(modifier = Modifier.fillMaxSize().background(SettingsGradient)) {
        // Fundal Decorativ cu mișcare lentă (Parallax)
        val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
        val bgOffset by infiniteTransition.animateFloat(
            initialValue = -20f, targetValue = 20f,
            animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
            label = "bgX"
        )

        Image(
            painter = painterResource(id = R.drawable.bg_menu_parallax_magic),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = bgOffset.dp)
                .alpha(0.2f),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Header modern
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                IconButton(
                    onClick = { 
                        soundManager.playClickIconSound()
                        navController.popBackStack() 
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Image(painter = painterResource(id = R.drawable.ui_btn_home), contentDescription = "Back", modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Control Părinți ⚙️",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Secțiunea Configurații (Stânga)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    item { PremiumToggleCard("Sunete", if (soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff, soundEnabled) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); soundManager.playClickIconSound(); onSoundChanged() } }
                    item { PremiumToggleCard("Muzică", if (musicEnabled) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff, musicEnabled) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); soundManager.playClickIconSound(); onMusicChanged() } }
                    item { PremiumToggleCard("Vibrații", Icons.Rounded.Vibration, hapticEnabled) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); soundManager.playClickIconSound(); onHapticChanged() } }
                    item { PremiumToggleCard("Mod Greu", Icons.Rounded.Speed, hardModeEnabled) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); soundManager.playClickIconSound(); onHardModeChanged() } }
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Status & Premium (Dreapta)
                Column(modifier = Modifier.weight(0.7f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    PremiumStatusCard(isPremium, onBuyPremium)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Buton Ieșire stilizat
                    Surface(
                        onClick = { 
                            soundManager.playClickIconSound()
                            navController.navigate(MainMenuRoute) {
                                popUpTo(MainMenuRoute) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Înapoi la Jocuri", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumToggleCard(title: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )
    
    val bgModifier = if (isActive) Modifier.background(CardActiveGradient) else Modifier.background(CardInactiveColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(if (isActive) 12.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(bgModifier)
                .border(2.dp, if (isActive) Color.White else Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isActive) Color(0xFF303F9F) else Color.White.copy(alpha = 0.6f), 
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isActive) Color(0xFF1A237E) else Color.White
                )
                Text(
                    text = if (isActive) "ACTIV" else "DEZACTIVAT", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black, 
                    color = if (isActive) SuccessGreen else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun PremiumStatusCard(isPremium: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "goldShine")
    val shineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(enabled = !isPremium) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isPremium) Brush.linearGradient(listOf(Color(0xFF43A047), Color(0xFF2E7D32))) else GoldGradient)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPremium) Icons.Rounded.Verified else Icons.Rounded.Stars, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(48.dp).alpha(if (isPremium) 1f else shineAlpha)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = if (isPremium) "Premium Activ" else "Deblochează Tot", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = if (isPremium) "Toate jocurile sunt disponibile" else "Elimină reclamele și limitele", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}
