package com.example.simonadice.viewmodel

import com.example.simonadice.data.RecordRepository
import com.example.simonadice.model.GameState
import com.example.simonadice.model.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Pruebas unitarias para SimonViewModel extremadamente sencillas, usando solo JUnit.
 *
 * NOTA: Para funcionar sin librerías de Coroutines de prueba, se usan Thread.sleep()
 * para dar tiempo al ViewModel de ejecutar sus coroutines internas.
 */
class SimonViewModelSimpleTest {

    // Repositorio de Records simulado (Mock simple)
    private lateinit var mockRepo: MockRecordRepository

    // ViewModel a probar
    private lateinit var viewModel: SimonViewModel

    /**
     * Implementación simple de un repositorio de prueba para controlar los datos.
     * Simula la interfaz RecordRepository.
     */
    private class MockRecordRepository(
        var savedRecord: Record = Record()
    ) : RecordRepository {
        // La implementación es síncrona. Se añade un sleep para ayudar a la prueba.
        override suspend fun getRecord(): Record {
            Thread.sleep(1)
            return savedRecord
        }
        override suspend fun saveRecord(record: Record) {
            Thread.sleep(1)
            savedRecord = record
        }
    }

    @Before
    fun setup() {
        // 1. Inicializa el mock con el record inicial (0, 0)
        mockRepo = MockRecordRepository()
        // 2. Pasa el mock al ViewModel
        viewModel = SimonViewModel(mockRepo)
        // 3. Esperamos un poco para que la Coroutine de loadRecord() en el init se ejecute
        Thread.sleep(50)
    }

    // ------------------------------------
    // PRUEBAS DE INICIO Y RECORD
    // ------------------------------------

    @Test
    fun init_loadsDefaultRecord() {
        // ASSERT: Se cargó el record por defecto (0) y se actualizó el mensaje inicial.
        assertEquals(0, viewModel.currentRecord.value.highScore)
        assertTrue(viewModel.message.value.contains("Start! ✨"))
    }

    @Test
    fun handlePlayerInput_gameOver_savesNewRecord() {
        // ARRANGE: Inyectar un record anterior de 1
        mockRepo.savedRecord = Record(1, Date(1000L))
        viewModel = SimonViewModel(mockRepo) // Reinicializar
        Thread.sleep(50)

        // Iniciar juego (Nivel 1, secuencia de 1 elemento)
        viewModel.startGame()
        Thread.sleep(50)

        val sequenceSizeBefore = viewModel.sequence.size // Será 1

        // ACT: Provocar Game Over
        val incorrectColorId = (viewModel.sequence.first() + 1) % 4
        viewModel.handlePlayerInput(incorrectColorId)
        Thread.sleep(50)

        // ASSERT: El nivel alcanzado (1) es > record anterior (1), por lo que se guarda.
        assertEquals(sequenceSizeBefore, mockRepo.savedRecord.highScore)
        assertEquals(GameState.GAMEOVER, viewModel.gameState.value)
        assertTrue(viewModel.message.value.contains("NEW RECORD! 🎉"))
    }

    // ------------------------------------
    // PRUEBAS DE LÓGICA DE JUEGO
    // ------------------------------------

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
        Thread.sleep(50)
        // Forzamos el estado para simular que Simon terminó de mostrar la secuencia
        viewModel._gameState.value = GameState.PLAYER

        // ACT: Acierta el primer paso
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
        Thread.sleep(50)
        viewModel._gameState.value = GameState.PLAYER

        // ACT: Falla el primer paso
        val incorrectColorId = (viewModel.sequence.first() + 1) % 4
        viewModel.handlePlayerInput(incorrectColorId)

        // ASSERT
        assertEquals(GameState.GAMEOVER, viewModel.gameState.value)
    }
}