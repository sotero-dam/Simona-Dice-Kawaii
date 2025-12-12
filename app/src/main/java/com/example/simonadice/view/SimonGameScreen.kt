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
import android.text.format.DateFormat

/**
 * Pantalla principal del juego Simon Kawaii.
 *
 * Es la función Composable que construye toda la interfaz de usuario, observando el estado
 * del juego a través del [SimonViewModel]. Gestiona la disposición de los botones de juego,
 * el área de información (nivel, mensaje, record) y el botón de inicio/reintento.
 *
 * También inicializa y gestiona el [ToneGenerator] para los sonidos del juego.
 *
 * @param viewModel La instancia de [SimonViewModel] que gestiona la lógica y el estado del juego.
 */
@Composable
fun SimonGameScreen(viewModel: SimonViewModel) {
    // 1. Observación del estado del ViewModel
    val gameState by viewModel.gameState.collectAsState()
    val level by viewModel.level.collectAsState()
    val message by viewModel.message.collectAsState()
    val activeBtnId by viewModel.activeButtonId.collectAsState()
    val currentRecord by viewModel.currentRecord.collectAsState() // NUEVO: Leer el record actual

    // 2. Inicialización y gestión del ToneGenerator para sonidos
    val toneGenerator = remember {
        try {
            // Inicializa ToneGenerator para reproducir tonos a través del stream de música
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            // Manejo de errores en caso de fallo al crear el ToneGenerator
            null
        }
    }

    // 3. Liberación del ToneGenerator cuando el Composable se destruye
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    // 4. Efecto para reproducir el tono de un botón cuando se activa (estado SIMON o jugador)
    LaunchedEffect(activeBtnId) {
        activeBtnId?.let { id ->
            // Mapea el ID del botón a un tipo de tono DTMF específico
            val toneType = when(id) {
                0 -> ToneGenerator.TONE_DTMF_1
                1 -> ToneGenerator.TONE_DTMF_3
                2 -> ToneGenerator.TONE_DTMF_5
                3 -> ToneGenerator.TONE_DTMF_7
                else -> ToneGenerator.TONE_DTMF_0
            }
            try {
                // Inicia la reproducción del tono por 150ms
                toneGenerator?.startTone(toneType, 150)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 5. Efecto para reproducir un tono de error al entrar en el estado GAMEOVER
    LaunchedEffect(gameState) {
        if (gameState == GameState.GAMEOVER) {
            try {
                // Tono de error (duración de 500ms)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 500)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Estructura de la Interfaz de Usuario ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título del juego
        Text(
            text = "Simon Kawaii",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF48FB1)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de información (Nivel, Mensaje y Record)
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
                // Muestra el nivel actual (o 0 si aún no ha empezado)
                Text(
                    text = if (level > 0) "$level" else "0",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEC407A)
                )
                // Mensaje de estado (por ejemplo, "Tu turno", "Simon juega", "Game Over")
                Text(text = message, fontSize = 18.sp, color = Color.Gray, minLines = 2)

                // Renderiza la información del Record si es mayor que 0
                if (currentRecord.highScore > 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Formatea la marca de tiempo del record
                    val dateStr = DateFormat.format("dd/MM/yy HH:mm", currentRecord.timestamp).toString()
                    Text(
                        text = "🏆 Record: ${currentRecord.highScore} ($dateStr)",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Grid de botones de juego 2x2
        Column {
            // Fila superior de botones
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Botón 0 (llamada a la función Composable KawaiiGameButton)
                KawaiiGameButton(GameConfig.COLORS[0], activeBtnId, gameState) { viewModel.handlePlayerInput(0) }
                // Botón 1
                KawaiiGameButton(GameConfig.COLORS[1], activeBtnId, gameState) { viewModel.handlePlayerInput(1) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Fila inferior de botones
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Botón 2
                KawaiiGameButton(GameConfig.COLORS[2], activeBtnId, gameState) { viewModel.handlePlayerInput(2) }
                // Botón 3
                KawaiiGameButton(GameConfig.COLORS[3], activeBtnId, gameState) { viewModel.handlePlayerInput(3) }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Botón de Inicio/Reintento
        Button(
            onClick = { viewModel.startGame() },
            // El botón está deshabilitado durante la secuencia de Simon (GameState.SIMON)
            enabled = gameState != GameState.SIMON,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF06292),
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier.height(56.dp).width(200.dp),
            shape = CircleShape
        ) {
            // Cambia el texto del botón según el estado del juego
            Text(
                text = if (gameState == GameState.GAMEOVER) "Try Again? 🥺" else "Play! 🌸",
                fontSize = 20.sp
            )
        }
    }
}

/**
 * Composable que representa un único botón de color en el juego Simon.
 *
 * Controla la animación del color y la interactividad (clickable) según el estado del juego.
 *
 * @param colorData La configuración del color (ID, colores, etiqueta) definida en [KawaiiColor].
 * @param activeId El ID del botón que está actualmente encendido/activo (puede ser null).
 * @param gameState El estado actual del juego ([GameState]) para determinar si el botón es clicable.
 * @param onClick La lambda a ejecutar cuando el jugador presiona el botón.
 */
@Composable
fun KawaiiGameButton(
    colorData: KawaiiColor,
    activeId: Int?,
    gameState: GameState,
    onClick: () -> Unit
) {
    // Determina si este botón específico debe estar "encendido"
    val isLit = activeId == colorData.id

    // Calcula el color objetivo: color activo si está encendido, color normal en caso contrario
    val targetColor = try {
        if (isLit) Color(android.graphics.Color.parseColor(colorData.activeHex))
        else Color(android.graphics.Color.parseColor(colorData.colorHex))
    } catch (e: Exception) {
        // En caso de error de parseo, usa un color gris de fallback
        Color.Gray
    }

    // Animación de color: cambia suavemente entre el color normal y el color activo
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "colorAnim"
    )

    // Efecto de escala: el botón se reduce ligeramente cuando está encendido
    val scale = if (isLit) 0.95f else 1f

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale) // Aplica la escala animada
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(animatedColor) // Aplica el color animado
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Sin efecto visual predeterminado al presionar
                // Solo es clicable si el estado del juego es PLAYER (turno del jugador)
                enabled = gameState == GameState.PLAYER
            ) { onClick() }, // Llama a la función de manejo de entrada del jugador
        contentAlignment = Alignment.Center
    ) {
        // Etiqueta/número dentro del botón (texto de decoración)
        Text(text = colorData.label, fontSize = 40.sp)
        // Overlay de luz blanca cuando el botón está encendido para un efecto visual más fuerte
        if (isLit) {
            Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.3f)))
        }
    }
}