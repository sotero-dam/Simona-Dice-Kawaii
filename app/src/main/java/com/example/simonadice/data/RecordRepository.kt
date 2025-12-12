package com.example.simonadice.data

import android.content.Context
import com.example.simonadice.model.Record
import java.util.Date

/**
 * Interfaz para la capa de Repositorio de Records.
 * Define las operaciones de guardado y lectura, haciendo que el ViewModel sea independiente del mecanismo de almacenamiento (SharedPreferences, BD, etc.).
 *
 * @see <a href="https://developer.android.com/topic/architecture/data-layer#repository">Android Development Docs: Data Layer (Repositories)</a>
 */
interface RecordRepository {
    suspend fun getRecord(): Record
    suspend fun saveRecord(record: Record)
}

/**
 * Implementación del Repositorio de Records utilizando SharedPreferences para guardar datos.
 *
 * @param context El contexto de la aplicación para acceder a SharedPreferences.
 * @see <a href="https://developer.android.com/training/data-storage/shared-preferences">Android Development Docs: SharedPreferences</a>
 */
class SharedPreferencesRecordRepository(context: Context) : RecordRepository {

    // Nombre del archivo de SharedPreferences y claves para guardar los datos.
    private val sharedPreferences = context.getSharedPreferences("simon_dice_record_file", Context.MODE_PRIVATE)

    // Claves del record.
    private val HIGH_SCORE_KEY = "high_score"
    private val TIMESTAMP_KEY = "timestamp"

    /**
     * Recupera el record guardado previamente (ronda más alta y marca de tiempo).
     *
     * @return Objeto Record con la puntuación y el tiempo guardados.
     * @see <a href="https://developer.android.com/training/data-storage/shared-preferences#ReadSharedPreferences">Android Development Docs: Lectura de SharedPreferences</a>
     */
    override suspend fun getRecord(): Record {
        val highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)
        // SharedPreferences solo guarda Long, por lo que guardamos el timestamp como Long.
        val timestampLong = sharedPreferences.getLong(TIMESTAMP_KEY, 0L)
        return Record(highScore, Date(timestampLong))
    }

    /**
     * Guarda el nuevo record (ronda más alta y marca de tiempo).
     * Usa apply() para guardar los datos de forma asíncrona y no bloquear el hilo principal.
     *
     * @param record El objeto Record a guardar.
     * @see <a href="https://developer.android.com/training/data-storage/shared-preferences#WriteSharedPreferences">Android Development Docs: Escritura de SharedPreferences</a>
     */
    override suspend fun saveRecord(record: Record) {
        sharedPreferences.edit()
            .putInt(HIGH_SCORE_KEY, record.highScore)
            // Convertimos Date a Long (millis) para guardar en SharedPreferences.
            .putLong(TIMESTAMP_KEY, record.timestamp.time)
            .apply() // Guarda de forma asíncrona
    }
}