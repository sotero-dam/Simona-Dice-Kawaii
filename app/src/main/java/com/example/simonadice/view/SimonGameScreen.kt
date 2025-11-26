package com.example.simonadice.view

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonadice.model.GameConfig
import com.example.simonadice.model.GameState
import com.example.simonadice.model.KawaiiColor
import com.example.simonadice.viewmodel.SimonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SimonGameScreen(viewModel: SimonViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val level by viewModel.level.collectAsState()
    val message by viewModel.message.collectAsState()
    val activeBtnId by viewModel.activeButtonId.collectAsState()

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    LaunchedEffect(activeBtnId) {
        activeBtnId?.let { id ->
            val toneType = when(id) {
                0 -> ToneGenerator.TONE_DTMF_1
                1 -> ToneGenerator.TONE_DTMF_3
                2 -> ToneGenerator.TONE_DTMF_5
                3 -> ToneGenerator.TONE_DTMF_7
                else -> ToneGenerator.TONE_DTMF_0
            }
            try {
                toneGenerator?.startTone(toneType, 150)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.GAMEOVER) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 500)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Simon Kawaii",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF48FB1)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LEVEL", color = Color(0xFFF8BBD0), fontWeight = FontWeight.Bold)
                Text(
                    text = if (level > 0) "$level" else "0",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEC407A)
                )
                Text(text = message, fontSize = 18.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KawaiiGameButton(GameConfig.COLORS[0], activeBtnId, gameState) { viewModel.handlePlayerInput(0) }
                KawaiiGameButton(GameConfig.COLORS[1], activeBtnId, gameState) { viewModel.handlePlayerInput(1) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KawaiiGameButton(GameConfig.COLORS[2], activeBtnId, gameState) { viewModel.handlePlayerInput(2) }
                KawaiiGameButton(GameConfig.COLORS[3], activeBtnId, gameState) { viewModel.handlePlayerInput(3) }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.startGame() },
            enabled = gameState != GameState.SIMON,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF06292),
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier.height(56.dp).width(200.dp),
            shape = CircleShape
        ) {
            Text(
                text = if (gameState == GameState.GAMEOVER) "Try Again? 🥺" else "Play! 🌸",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun KawaiiGameButton(
    colorData: KawaiiColor,
    activeId: Int?,
    gameState: GameState,
    onClick: () -> Unit
) {
    val isLit = activeId == colorData.id

    val targetColor = try {
        if (isLit) Color(android.graphics.Color.parseColor(colorData.activeHex))
        else Color(android.graphics.Color.parseColor(colorData.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "colorAnim"
    )

    val scale = if (isLit) 0.95f else 1f

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(animatedColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = gameState == GameState.PLAYER
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = colorData.label, fontSize = 40.sp)
        if (isLit) {
            Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.3f)))
        }
    }
}