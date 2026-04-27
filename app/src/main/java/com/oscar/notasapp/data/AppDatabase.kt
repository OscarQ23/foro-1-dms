package com.oscar.notasapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicación.
 *
 * Persiste profesores (User), materias, alumnos y asignaciones.
 * Se construye como singleton para evitar múltiples instancias compitiendo
 * por el mismo archivo SQLite.
 *
 * Versión 2 introduce las tablas alumnos y asignaciones, y reemplaza el
 * campo `nota` de la tabla materias. Como el modelo cambió drásticamente
 * usamos `fallbackToDestructiveMigration` — al actualizar a v2 se borra
 * la base anterior. Es aceptable para una app académica.
 *
 * Versión 3 reemplaza la única `nota` de Asignacion por cinco campos
 * `nota1..nota5` y el promedio se calcula en memoria.
 */
@Database(
    entities = [User::class, Materia::class, Alumno::class, Asignacion::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun materiaDao(): MateriaDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun asignacionDao(): AsignacionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notas_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
