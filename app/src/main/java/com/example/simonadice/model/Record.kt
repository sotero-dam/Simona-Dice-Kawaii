package com.example.simonadice.model

import java.util.Date

/**
 * Clase de datos que representa el 'record' (máxima ronda y marca de tiempo).
 * Se utiliza en la capa de Domain/Model para mantener la lógica de negocio independiente del almacenamiento.
 *
 * @property highScore La ronda más alta alcanzada por el jugador.
 * @property timestamp Marca de tiempo (fecha y hora) en que se consiguió el record.
 * @see <a href="https://developer.android.com/kotlin/coroutines/kt-docs#data-classes">Kotlin Docs: Data Classes</a>
 */
data class Record(
    val highScore: Int = 0,
    val timestamp: Date = Date(0) // Usamos Date(0) para representar un record no establecido (01/01/1970)
)