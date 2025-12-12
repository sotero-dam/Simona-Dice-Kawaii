package com.example.simonadice.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.simonadice.model.Record
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Define el contrato de la base de datos: nombres de la BD, tabla y columnas.
 * El uso de un 'object' para las constantes evita errores de escritura (typos) en las referencias.
 */
private object DbContract {
    const val DB_NAME = "RecordSimon"
    const val DB_VERSION = 1
    const val TABLE_NAME = "record_table"
    const val COL_SCORE = "high_score" // Columna para la ronda más alta (el record)
    const val COL_TIME = "timestamp"   // Columna para la marca de tiempo (cuándo se logró)

    // Instrucción SQL para crear la tabla si no existe.
    const val SQL_CREATE =
        "CREATE TABLE $TABLE_NAME (" +
                "$COL_SCORE INTEGER," +
                "$COL_TIME INTEGER)"
}

/**
 * Clase auxiliar para gestionar la base de datos (creación y actualizaciones).
 * Extiende SQLiteOpenHelper para manejar automáticamente la creación inicial [onCreate]
 * y las migraciones de versión [onUpgrade].
 */
class RecordDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DbContract.DB_NAME,
    null,
    DbContract.DB_VERSION
) {
    /**
     * Llamado cuando la base de datos se crea por primera vez.
     * Aquí es donde se ejecuta el script SQL para crear el esquema de la tabla.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DbContract.SQL_CREATE)
        Log.i("SQLiteRepo", "Tabla de record creada.")
    }

    /**
     * Llamado cuando la versión de la base de datos cambia (por ejemplo, si DbContract.DB_VERSION pasa de 1 a 2).
     * En este proyecto simple, optamos por borrar los datos antiguos y recrear la tabla.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Borrar la tabla antigua si existe
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.TABLE_NAME}")
        // Crear la nueva tabla
        onCreate(db)
    }
}

/**
 * Implementación del Repositorio de Records utilizando SQLite.
 * Implementa la interfaz [RecordRepository] para traducir las operaciones de lectura y guardado a comandos de base de datos.
 *
 * @param context El contexto de la aplicación, necesario para inicializar el [RecordDbHelper].
 */
class SQLiteRecordRepository(context: Context) : RecordRepository {

    // Inicializa el Helper que nos dará acceso a la base de datos.
    private val dbHelper = RecordDbHelper(context)

    /**
     * Implementa la operación **SELECCIONAR (SELECT)**.
     * Recupera el record guardado desde la base de datos de forma asíncrona.
     *
     * @return El objeto [Record] con la puntuación y fecha. Devuelve [Record()] si no hay datos.
     */
    override suspend fun getRecord(): Record = withContext(Dispatchers.IO) {
        // Obtenemos una instancia de la base de datos en modo lectura.
        val db = dbHelper.readableDatabase
        var record = Record()

        // Ejecutamos una consulta SELECT con un Cursor.
        val cursor = db.query(
            DbContract.TABLE_NAME, // Nombre de la tabla
            arrayOf(DbContract.COL_SCORE, DbContract.COL_TIME), // Columnas a devolver
            null, null, null, null, // Filtros (WHERE, GROUP BY, HAVING) - nulos para seleccionar todo
            "${DbContract.COL_SCORE} DESC", // ORDER BY (ordenar por score descendente)
            "1" // LIMIT (solo la fila con el record más alto)
        )

        // Leemos el cursor y creamos el objeto Record.
        with(cursor) {
            if (moveToFirst()) { // Si hay resultados, nos movemos al primero
                // Obtenemos el índice de la columna y leemos el valor.
                val score = getInt(getColumnIndexOrThrow(DbContract.COL_SCORE))
                val timeMillis = getLong(getColumnIndexOrThrow(DbContract.COL_TIME))
                // Convertimos los milisegundos de nuevo a objeto Date.
                record = Record(score, Date(timeMillis))
            }
        }
        cursor.close() // Siempre cerrar el cursor para liberar recursos.

        Log.d("SQLiteRepo", "SELECT: Record cargado: Ronda ${record.highScore}")
        return@withContext record // Devolvemos el record
    }

    /**
     * Implementa la operación **INSERTAR / ACTUALIZAR (UPDATE)**.
     * Guarda el nuevo record de forma asíncrona.
     *
     * **Nota:** Para asegurar que solo existe un único record, se usa la estrategia de:
     * 1. **DELETE** de todos los registros.
     * 2. **INSERT** del nuevo record.
     *
     * @param record El objeto [Record] a guardar.
     */
    override suspend fun saveRecord(record: Record) {
        // Ejecutamos la operación en el contexto de IO para no bloquear el hilo principal (UI).
        withContext(Dispatchers.IO) {
            // Obtenemos una instancia de la base de datos en modo escritura.
            val db = dbHelper.writableDatabase

            // 1. DELETE: Borrar todos los registros anteriores.
            db.delete(DbContract.TABLE_NAME, null, null)

            // 2. INSERT: Preparamos los valores para la inserción.
            val values = ContentValues().apply {
                put(DbContract.COL_SCORE, record.highScore)
                // Convertimos Date a Long (milisegundos) para guardarla en SQLite, que solo almacena tipos primitivos.
                put(DbContract.COL_TIME, record.timestamp.time)
            }

            // 3. Insertamos la nueva fila.
            val newRowId = db.insert(DbContract.TABLE_NAME, null, values)

            if (newRowId != -1L) {
                Log.i("SQLiteRepo", "INSERT/UPDATE: Record guardado. Score: ${record.highScore}")
            } else {
                Log.e("SQLiteRepo", "ERROR al guardar el record en SQLite.")
            }
        }
    }
}