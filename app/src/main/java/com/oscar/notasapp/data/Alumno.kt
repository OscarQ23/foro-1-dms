package com.oscar.notasapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Estudiante registrado por un profesor.
 *
 * El campo [profesorId] garantiza el aislamiento por sesión: cada profesor
 * solo ve sus propios alumnos.
 */
@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["profesorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profesorId")]
)
data class Alumno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profesorId: Long,
    val nombre: String,
    val apellido: String,
    val carnet: String = ""
)
