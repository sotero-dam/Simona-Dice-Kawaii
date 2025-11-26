package com.example.simonadice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simonadice.model.GameConfig
import com.example.simonadice.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class SimonViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _message = MutableStateFlow("Start! ✨")
    val message: StateFlow<String> = _message.asStateFlow()

    // Indica qué botón está iluminado actualmente (null = ninguno)
    private val _activeButtonId = MutableStateFlow<Int?>(null)
    val activeButtonId: StateFlow<Int?> = _activeButtonId.asStateFlow()

    // --- Variables internas ---
    private val sequence = mutableListOf<Int>()
    private var playerStep = 0
    fun startGame() {
        if (_gameState.value == GameState.SIMON) return

        sequence.clear()
        playerStep = 0
        _level.value = 1
        _message.value = "Memorize... 💭"

        addToSequence()
        playSequence()
    }

    fun handlePlayerInput(colorId: Int) {
        if (_gameState.value != GameState.PLAYER) return
        viewModelScope.launch {
            flashButton(colorId)
        }

        // 2. Validar lógica
        if (colorId == sequence[playerStep]) {
            playerStep++
            if (playerStep == sequence.size) {
                // Ronda completada
                _gameState.value = GameState.IDLE
                _message.value = "Perfect! 💖"
                viewModelScope.launch {
                    delay(1000)
                    nextRound()
                }
            }
        } else {
            // Error
            _gameState.value = GameState.GAMEOVER
            _message.value = "Oh no! 💔 Lvl: ${_level.value}"
        }
    }

    // --- Lógica privada ---

    private fun nextRound() {
        playerStep = 0
        _level.value++
        addToSequence()
        playSequence()
    }

    private fun addToSequence() {
        sequence.add(Random.nextInt(0, 4))
    }

    private fun playSequence() {
        viewModelScope.launch {
            _gameState.value = GameState.SIMON
            _message.value = "Watch... 👀"
            delay(1000)

            sequence.forEach { colorId ->
                flashButton(colorId)
                delay(GameConfig.DELAY_BETWEEN_MS)
            }

            _gameState.value = GameState.PLAYER
            _message.value = "Your Turn! ✨"
        }
    }

    private suspend fun flashButton(id: Int) {
        _activeButtonId.value = id
        delay(GameConfig.DURATION_LIGHT_MS)
        _activeButtonId.value = null
    }
}