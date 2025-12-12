package com.example.simonadice.viewmodel

import com.example.simonadice.data.RecordRepository
import com.example.simonadice.model.GameState
import com.example.simonadice.model.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Date

class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    // Se ejecuta ANTES de cada test: establece el dispatcher de test.
    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    // Se ejecuta DESPUÉS de cada test: restaura el dispatcher original.
    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}

/**
 * Tests unitarios para SimonViewModel. Usa MainDispatcherRule para manejar
 * corrutinas
 */
class SimonViewModelSimpleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepo: MockRecordRepository
    private lateinit var viewModel: SimonViewModel

    /**
     * Implementación simple de un repositorio simulado (Mock).
     */
    private class MockRecordRepository(
        var savedRecord: Record = Record()
    ) : RecordRepository {
        override suspend fun getRecord(): Record {
            return savedRecord
        }

        override suspend fun saveRecord(record: Record) {
            savedRecord = record
        }
    }

    @Before
    fun setup() {
        mockRepo = MockRecordRepository()
        // La inicialización del ViewModel (que llama loadRecord) ahora funciona sin error de Dispatcher.
        viewModel = SimonViewModel(mockRepo)
        // No hace falta Thread.sleep().
    }

    @Test
    fun init_loadsDefaultRecord() {
        // ASSERT: Se carga el record por defecto (0)
        assertEquals(0, viewModel.currentRecord.value.highScore)
        assertTrue(viewModel.message.value.contains("Start! ✨"))
    }

    @Test
    fun handlePlayerInput_gameOver_savesNewRecord() {
        // ARRANGE: Inyectar un record previo de 1
        mockRepo.savedRecord = Record(1, Date(1000L))
        viewModel = SimonViewModel(mockRepo) // Re-inicializar

        // Iniciar juego (Nivel 1)
        viewModel.startGame()

        val sequenceSizeBefore = viewModel.sequence.size // Será 1

        // ACT: Provocar Game Over
        val incorrectColorId = (viewModel.sequence.first() + 1) % 4
        viewModel.handlePlayerInput(incorrectColorId)

        // ASSERT: Se guarda el nuevo record y el estado pasa a GAMEOVER
        assertEquals(sequenceSizeBefore, mockRepo.savedRecord.highScore)
        assertEquals(GameState.GAMEOVER, viewModel.gameState.value)
        assertTrue(viewModel.message.value.contains("NEW RECORD! 🎉"))
    }

    @Test
    fun startGame_initializesLevelAndSequence() {
        // ACT
        viewModel.startGame()

        // ASSERT
        assertEquals(1, viewModel.level.value)
        assertEquals(1, viewModel.sequence.size)
    }

    @Test
    fun handlePlayerInput_correctColor_advancesPlayerStep() {
        // ARRANGE
        viewModel.startGame()
        // Forzar estado a PLAYER
        viewModel._gameState.value = GameState.PLAYER

        // ACT: Introducir correctamente el primer paso
        val correctColorId = viewModel.sequence.first()
        viewModel.handlePlayerInput(correctColorId)

        // ASSERT
        assertEquals(1, viewModel.playerStep)
        assertEquals(GameState.PLAYER, viewModel.gameState.value)
    }

    @Test
    fun handlePlayerInput_incorrectColor_setsGameOver() {
        // ARRANGE
        viewModel.startGame()
        viewModel._gameState.value = GameState.PLAYER

        // ACT: Fallar el primer paso
        val incorrectColorId = (viewModel.sequence.first() + 1) % 4
        viewModel.handlePlayerInput(incorrectColorId)

        // ASSERT
        assertEquals(GameState.GAMEOVER, viewModel.gameState.value)
    }
}
