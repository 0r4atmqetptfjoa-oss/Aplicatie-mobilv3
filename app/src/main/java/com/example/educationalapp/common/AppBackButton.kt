package com.example.educationalapp.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.educationalapp.R
import com.example.educationalapp.common.LocalSoundManager

@Composable
fun AppBackButton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val soundManager = LocalSoundManager.current

    Box(
        modifier = modifier
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ui_btn_back_wood),
            contentDescription = "Înapoi",
            modifier = Modifier
                .size(64.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        soundManager.playSound(R.raw.sfx_whoosh)
                        onBack()
                    }
                }
        )
    }
}
