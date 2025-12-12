# Simona Dice Kawaii 🌸

Juego de **Simona Dice** (Simon Says), creado nativamente en **Kotlin**, empleando **Jetpack Compose** y arquitectura **MVVM**.

-----

## 📋 Especificación del Juego

### 1\. Objetivo del Juego

El objetivo principal es **memorizar y replicar secuencias de colores** generadas aleatoriamente por la aplicación. La dificultad aumenta progresivamente en cada ronda, añadiendo un nuevo color a la secuencia.

### 2\. Componentes de la Interfaz (UI)

La interfaz tiene los siguientes elementos:

  * **4 Botones Principales:** Los elementos interactivos que el jugador debe pulsar.
  * **Marcador:** Muestra el **Nivel** actual en la parte superior y el **Record** histórico.
  * **Botón de Inicio/Mensaje de Estado:** Texto dinámico que indica de quién es el turno o el estado actual del juego.

### 3\. Lógica del Juego

#### Fase 1: Inicialización

Al pulsar "**Play**", se reinician las variables (`sequence`, `playerStep`, `level`). Se genera el primer color aleatorio utilizando `Random`.

#### Fase 2: Secuencia de Simón (Output)

1.  **Bloqueo de UI:** Los botones se deshabilitan (`enabled = gameState == PLAYER`) para evitar que el usuario interrumpa a Simón.
2.  **Reproducción:** El sistema recorre la lista de la secuencia usando **Corrutinas** (`viewModelScope`).
3.  **Feedback Visual:** Se utiliza `animateColorAsState` de Jetpack Compose para realizar una **transición suave de color** (iluminación) en cada botón activo, simulando el parpadeo de luz.
4.  **Feedback Auditivo:** La lógica de audio está preparada para reproducir frecuencias específicas.

#### Fase 3: Turno del Jugador (Input)

1.  Se **habilita** la interacción en la UI.
2.  Al pulsar un botón, se valida inmediatamente contra la secuencia almacenada en el **ViewModel**.
3.  **Acierto:** Si el color coincide, se avanza el paso. Si se completa la secuencia, se lanza la siguiente ronda tras una breve pausa.
4.  **Fallo:** Se dispara inmediatamente el estado `GAMEOVER`.

#### 4\. Condición de Derrota (Game Over)

Si el jugador pulsa un color incorrecto:

  * El estado cambia a **`GAMEOVER`**.
  * Se muestra el mensaje de derrota indicando el nivel alcanzado.
  * Se llama al repositorio para **verificar y guardar** el nuevo record si la ronda actual supera el anterior.
  * El sistema queda a la espera de que el usuario pulse "**Try Again**" para reiniciar.

-----

## Estados de la UI

Los estados de la aplicación se definen y se exponen mediante **`StateFlow`** de Kotlin, lo que permite un **flujo de datos unidireccional (UDF)** hacia la **View** (Activity/Fragment).

  * **`gameState`** (`StateFlow<GameState>`): Es el **Estado principal del juego**, indicando el modo actual: `IDLE`, `SIMON` (mostrando secuencia), `PLAYER` (turno del usuario), o `GAMEOVER`.
  * **`level`** (`StateFlow<Int>`): Representa el **Nivel o ronda actual** que el jugador está intentando completar.
  * **`currentRecord`** (`StateFlow<Record>`): Almacena la ronda más alta alcanzada y su fecha. **Se carga desde SQLite al iniciar.**
  * **`message`** (`StateFlow<String>`): Contiene el **Mensaje de texto informativo** mostrado al usuario (ej. "Your Turn\! ✨").
  * **`activeButtonId`** (`StateFlow<Int?>`): **Controla la animación** al indicar qué botón debe estar iluminado en la interfaz en un momento dado.

### Flujo de Datos

1.  **Eventos:** La **View** envía acciones del usuario (ej. `handlePlayerInput(colorId)`) al **ViewModel**.
2.  **Lógica:** El **ViewModel** ejecuta la lógica, actualiza su **`MutableStateFlow`** interno (la fuente de verdad).
3.  **Persistencia:** En caso de nuevo record, el ViewModel llama al **Repositorio** para guardar en SQLite.
4.  **Observación:** La **View** está observando los **`StateFlow`** públicos y se actualiza automáticamente.

-----

## 💾 Persistencia de Datos (SQLite)

La persistencia del **Record** (máxima ronda y fecha) se implementó utilizando una base de datos local **SQLite**.

### Principios Arquitectónicos

  * **Patrón Repositorio:** La capa de persistencia se migró del mecanismo inicial (`SharedPreferences`) a **SQLite** sin modificar el `SimonViewModel`. Esto se logra porque el ViewModel solo interactúa con la interfaz **`RecordRepository`**, no con su implementación específica.
  * **Inyección de Dependencia:** En `MainActivity.kt`, la factoría de ViewModel fue modificada para inyectar la instancia de **`SQLiteRecordRepository`**.

### 🛠 Implementación de SQLite (`SQLiteRecordRepository.kt`)

Este archivo contiene la lógica de base de datos para manejar el almacenamiento del record.

| Componente | Función | Operación SQL |
| :--- | :--- | :--- |
| **`RecordDbHelper`** | Clase auxiliar que extiende `SQLiteOpenHelper`. Se encarga de crear la tabla (`onCreate`) y gestionar versiones. | `CREATE TABLE` |
| **`getRecord()`** | Implementa la lectura. Busca la puntuación más alta. | **`SELECT * FROM record_table ORDER BY high_score DESC LIMIT 1`** |
| **`saveRecord()`** | Implementa la escritura. Se utiliza una estrategia de reemplazo para mantener un único registro: Borrar todo y luego insertar el nuevo record. | **`DELETE`** (todo) y luego **`INSERT`** |

-----

## 🛠 Arquitectura del Proyecto (MVVM)

El código está estructurado en paquetes para separar responsabilidades.

### 📂 Estructura de Carpetas

```
com.example.simonadice
 ├── 📂 data        (Capa de Acceso a Datos)
 │    ├── RecordRepository.kt       // Interfaz
 │    └── SQLiteRecordRepository.kt // Implementación de la BD
 │
 ├── 📂 model       (Datos y Configuración)
 │    ├── GameConfig.kt
 │    ├── GameState.kt
 │    ├── KawaiiColor.kt
 │    └── Record.kt                 // Data class del Record
 │
 ├── 📂 view        (Interfaz de Usuario - Jetpack Compose)
 │    ├── MainActivity.kt          // Inyección del Repositorio SQLite
 │    └── SimonGameScreen.kt
 │
 └── 📂 viewmodel   (Lógica de Negocio)
       └── SimonViewModel.kt        // Usa RecordRepository
```
