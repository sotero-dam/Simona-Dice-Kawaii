package com.example.simonadice.view

import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simonadice.data.SQLiteRecordRepository // <--- ¡CAMBIO AQUÍ!
import com.example.simonadice.viewmodel.SimonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Actividad principal (Entry Point) de la aplicación Simon Kawaii.
 *
 * Esta clase es responsable de:
 * 1. Configurar el entorno de Compose.
 * 2. Inicializar el [SimonViewModel] usando un patrón de inyección manual de dependencias
 * (utilizando SQLiteRecordRepository).
 * 3. Lanzar el Composable principal [SimonGameScreen].
 */
class MainActivity : ComponentActivity() {

    /**
     * Factoría de ViewModel para inyectar la dependencia del Repositorio.
     * Aquí le indicamos al ViewModel que debe usar la implementación de SQLite.
     */
    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Verifica que la clase solicitada sea el SimonViewModel
            if (modelClass.isAssignableFrom(SimonViewModel::class.java)) {

                // === CAMBIO CLAVE: INYECTAR EL REPOSITORIO DE SQLITE ===
                val repository = SQLiteRecordRepository(applicationContext)

                // 2. Crear el ViewModel pasándole el repositorio.
                @Suppress("UNCHECKED_CAST")
                return SimonViewModel(repository) as T
            }
            // Lanza una excepción si se intenta crear una clase de ViewModel no soportada.
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    /**
     * Inicialización del ViewModel.
     * Utiliza el delegado `by viewModels` para inicializar el [SimonViewModel].
     */
    private val viewModel: SimonViewModel by viewModels { viewModelFactory }

    /**
     * Método llamado cuando la actividad se crea por primera vez.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define el contenido de la UI usando Jetpack Compose
        setContent {
            MaterialTheme {
                // Surface: contenedor de fondo que aplica el color de fondo y ocupa todo el espacio
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF0F5) // Fondo Rosa Pastel
                ) {
                    // Llama al Composable principal de la pantalla del juego
                    // (Asumo que existe SimonGameScreen y que recibe el viewModel)
                    SimonGameScreen(viewModel)
                }
            }
        }
    }

    /**
     * Reproduce un tono de frecuencia dada en un hilo de fondo.
     *
     * @param freq Frecuencia del tono en Hz (ej: 440.0 para La4).
     * @param durationMs Duración del tono en milisegundos.
     */
    fun playTone(freq: Double, durationMs: Int) {
        // Lanza una Coroutine en el hilo de I/O para evitar bloquear la UI
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val sampleRate = 44100 // Frecuencia de muestreo estándar
            // Calcula el número total de muestras necesarias
            val numSamples = durationMs * sampleRate / 1000
            val sample = DoubleArray(numSamples) // Array para la onda sinusoidal (Double)
            val generatedSnd = ByteArray(2 * numSamples) // Array para los datos PCM de 16 bits

            // Generar onda sinusoidal
            for (i in 0 until numSamples) {
                // Fórmula para generar un valor de onda sinusoidal en el tiempo 'i'
                sample[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freq))
            }

            // Convertir la onda sinusoidal (Double) a formato PCM 16bit (Byte Array)
            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 32767).toInt().toShort() // Convierte a Short (rango de 16 bits)
                // Convierte Short a dos Bytes (Little-Endian)
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = (valShort.toInt() and 0xff00 ushr 8).toByte()
            }

            try {
                // Configuración y reproducción del AudioTrack
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setBufferSizeInBytes(generatedSnd.size)
                    .build()

                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                // Espera la duración del tono antes de liberar
                Thread.sleep(durationMs.toLong())
                audioTrack.release() // Libera los recursos del AudioTrack
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}