package com.example.simonadice.model

/**
 * Representa un botón de color del juego.
 */
data class KawaiiColor(
    val id: Int,
    val name: String,
    val frequency: Double, // Frecuencia en Hz para el sonido
    val colorHex: String,  // Color normal (Hex)
    val activeHex: String, // Color iluminado (Hex)
    val label: String      // Emoji
)

/**
 * Estados posibles del juego.
 */
enum class GameState {
    IDLE,       // Esperando a empezar
    SIMON,      // Simón está mostrando la secuencia
    PLAYER,     // Turno del jugador
    GAMEOVER    // Juego terminado
}

/**
 * Configuración global del juego.
 */
object GameConfig {
    const val DURATION_LIGHT_MS = 500L
    const val DELAY_BETWEEN_MS = 250L

    val COLORS = listOf(
        KawaiiColor(0, "Mint", 329.63, "#6EE7B7", "#A7F3D0", "🌱"),   // Mi
        KawaiiColor(1, "Sakura", 440.00, "#F472B6", "#FBCFE8", "🌸"), // La
        KawaiiColor(2, "Sky", 554.37, "#7DD3FC", "#BAE6FD", "☁️"),   // Do#
        KawaiiColor(3, "Lemon", 659.25, "#FDE047", "#FEF9C3", "🍋")   // Mi agudo
    )
}