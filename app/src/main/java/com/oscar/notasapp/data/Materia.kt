package com.oscar.notasapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una materia (asignatura) impartida por un profesor.
 *
 * En el nuevo modelo la materia ya no contiene la nota: la nota vive en
 * la tabla puente [Asignacion] que relaciona alumno ↔ materia.
 */
@Entity(
    tableName = "materias",
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
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profesorId: Long,
    val nombre: String,
    val codigo: String = ""
)
