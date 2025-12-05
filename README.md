# Simona Dice Kawaii 🌸

Juego de **Simona Dice** (Simon Says), creado nativamente en **Kotlin**, empleando **Jetpack Compose** y arquitectura **MVVM**.

-----

## 📋 Especificación del Juego

### 1\. Objetivo del Juego

El objetivo principal es **memorizar y replicar secuencias de colores** generadas aleatoriamente por la aplicación. La dificultad aumenta progresivamente en cada ronda, añadiendo un nuevo color a la secuencia.

### 2\. Componentes de la Interfaz (UI)

La interfaz tiene los siguientes elementos:

  * **4 Botones Principales:** Los elementos interactivos que el jugador debe pulsar.
  * **Marcador:** Muestra el **Nivel** actual en la parte superior.
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
  * El sistema queda a la espera de que el usuario pulse "**Try Again**" para reiniciar.

-----

## Estados de la UI

Los estados de la aplicación se definen y se exponen mediante **`StateFlow`** de Kotlin, lo que permite un **flujo de datos unidireccional (UDF)** hacia la **View** (Activity/Fragment).

  * **`gameState`** (`StateFlow<GameState>`): Es el **Estado principal del juego**, indicando el modo actual: `IDLE`, `SIMON` (mostrando secuencia), `PLAYER` (turno del usuario), o `GAMEOVER`.
  * **`level`** (`StateFlow<Int>`): Representa el **Nivel o ronda actual** que el jugador está intentando completar.
  * **`message`** (`StateFlow<String>`): Contiene el **Mensaje de texto informativo** mostrado al usuario (ej. "Your Turn\! ✨").
  * **`activeButtonId`** (`StateFlow<Int?>`): **Controla la animación** al indicar qué botón debe estar iluminado en la interfaz en un momento dado.

### 3\. Flujo de Datos

1.  **Eventos:** La **View** envía acciones del usuario (ej. `handlePlayerInput(colorId)`) al **ViewModel**.
2.  **Lógica:** El **ViewModel** ejecuta la lógica, actualiza su **`MutableStateFlow`** interno (la fuente de verdad).
3.  **Observación:** La **View** está observando los **`StateFlow`** públicos, se actualiza automáticamente cuando detecta un cambio de estado, y renderiza la nueva interfaz.

-----

## 🛠 Arquitectura del Proyecto (MVVM)

El código está estructurado en paquetes para separar responsabilidades, facilitando el mantenimiento y la escalabilidad.

### 📂 Estructura de Carpetas

```
com.example.simonadice
 ├── 📂 model       (Datos y Configuración)
 │    ├── GameConfig.kt     // Constantes (Tiempos, Colores, Frecuencias)
 │    ├── GameState.kt      // Estados (IDLE, SIMON, PLAYER, GAMEOVER)
 │    └── KawaiiColor.kt    // Data class que define los botones
 │
 ├── 📂 view        (Interfaz de Usuario - Jetpack Compose)
 │    ├── MainActivity.kt      // Activity principal y configuración de ventana
 │    └── SimonGameScreen.kt   // Pantalla del juego (Composables y Animaciones)
 │
 └── 📂 viewmodel   (Lógica de Negocio)
       └── SimonViewModel.kt    // Gestión de estado (StateFlow) y lógica del juego
```

### 📦 GitFlow

  * **`main`**: Código de producción estable.
  * **`develop`**: Rama de desarrollo e integración.
  * **`feature/*`**: Ramas para nuevas funcionalidades.
  * **Tag `v1.0`**: Etiqueta del último commit.
