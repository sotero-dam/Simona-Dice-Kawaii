package com.example.simonadice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simonadice.data.RecordRepository
import com.example.simonadice.model.GameConfig
import com.example.simonadice.model.GameState
import com.example.simonadice.model.Record
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.random.Random

/**
 * ViewModel que maneja la lógica de Simon Dice, incluyendo el control del estado y la gestión del record.
 *
 * @param recordRepository Repositorio para guardar y leer el record. Se usa inyección de dependencia para la modularidad (MVVM).
 * @see <a href="https://developer.android.com/topic/libraries/architecture/viewmodel">Android Development Docs: ViewModel</a>
 */
class SimonViewModel(private val recordRepository: RecordRepository? = null) : ViewModel() {

    private val repo: RecordRepository
        get() = recordRepository ?: object : RecordRepository {
            override suspend fun getRecord(): Record = Record()
            override suspend fun saveRecord(record: Record) {}
        }

    // --- Variables de Estado (Mutables y Públicas) ---
    val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _message = MutableStateFlow("Start! ✨")
    val message: StateFlow<String> = _message.asStateFlow()

    // Indica qué botón está iluminado actualmente (null = ninguno)
    private val _activeButtonId = MutableStateFlow<Int?>(null)
    val activeButtonId: StateFlow<Int?> = _activeButtonId.asStateFlow()

    // Estado del Record: almacena el record actual de la app.
    private val _currentRecord = MutableStateFlow(Record())
    val currentRecord: StateFlow<Record> = _currentRecord.asStateFlow()

    // --- Variables internas ---
    val sequence = mutableListOf<Int>()
    var playerStep = 0

    // --- Lógica de Inicialización ---
    init {
        loadRecord() // Cargar el record al iniciar el ViewModel
    }

    /**
     * Inicia una nueva partida.
     */
    fun startGame() {
        if (_gameState.value == GameState.SIMON) return

        sequence.clear()
        playerStep = 0
        _level.value = 1
        _message.value = "Memorize... 💭"

        addToSequence()
        playSequence()
    }

    /**
     * Maneja la entrada del jugador (clic en un color).
     *
     * @param colorId El ID del color pulsado por el jugador.
     */
    fun handlePlayerInput(colorId: Int) {
        if (_gameState.value != GameState.PLAYER) return
        viewModelScope.launch {
            flashButton(colorId)
        }

        // Validar lógica
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
            // Error (Game Over)
            _gameState.value = GameState.GAMEOVER
            val maxRonda = _level.value
            _message.value = "Oh no! 💔 Lvl: $maxRonda"
            checkForNewRecord(maxRonda) // NUEVO: Verificar y guardar record
        }
    }

    // --- Lógica privada del juego ---

    private fun nextRound() {
        playerStep = 0
        _level.value++
        addToSequence()
        playSequence()
    }

    private fun addToSequence() {
        sequence.add(Random.nextInt(0, 4))
    }

    /**
     * Muestra la secuencia actual de botones.
     */
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

    /**
     * Ilumina un botón por un corto periodo de tiempo.
     *
     * @param id El ID del botón a iluminar.
     */
    private suspend fun flashButton(id: Int) {
        _activeButtonId.value = id
        delay(GameConfig.DURATION_LIGHT_MS)
        _activeButtonId.value = null
    }

    // --- Lógica de Record ---

    /**
     * Carga el record guardado previamente desde el repositorio.
     * Actualiza el StateFlow de _currentRecord.
     *
     * @see <a href="https://developer.android.com/topic/libraries/architecture/coroutines#viewmodel-scope">Android Development Docs: ViewModelScope</a>
     */
    private fun loadRecord() {
        viewModelScope.launch {
            _currentRecord.value = repo.getRecord()
            // Si el juego está en IDLE al inicio, actualizamos el mensaje.
            if (_gameState.value == GameState.IDLE) {
                updateStartMessage()
            }
        }
    }

    /**
     * Comprueba si la ronda alcanzada es un nuevo record y lo guarda si es así.
     *
     * @param maxRonda La ronda final alcanzada en la partida.
     * @see <a href="https://developer.android.com/topic/libraries/architecture/coroutines#suspend">Kotlin Docs: Suspend Functions</a>
     */
    private fun checkForNewRecord(maxRonda: Int) {
        if (maxRonda > _currentRecord.value.highScore) {
            val newRecord = Record(maxRonda, Date()) // Nueva marca de tiempo
            viewModelScope.launch {
                repo.saveRecord(newRecord)
                _currentRecord.value = newRecord // Actualiza el StateFlow del record
                _message.value += "\nNEW RECORD! 🎉"
            }
        }
    }

    /**
     * Función auxiliar para actualizar el mensaje inicial/IDLE con el record actual.
     */
    private fun updateStartMessage() {
        val record = _currentRecord.value
        // Formato simple de fecha y hora para el mensaje
        val dateString = if (record.highScore > 0) {
            android.text.format.DateFormat.format("dd/MM HH:mm", record.timestamp).toString()
        } else {
            "Nunca"
        }

        _message.value = "Start! ✨\nRecord: ${record.highScore} ($dateString)"
    }
}