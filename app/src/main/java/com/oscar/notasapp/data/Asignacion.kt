package com.oscar.notasapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla puente alumno ↔ materia que además guarda hasta 5 notas por
 * asignación y calcula el promedio como propiedad computada.
 *
 * Borrar el alumno o la materia elimina automáticamente sus asignaciones
 * gracias a las cascadas configuradas.
 */
@Entity(
    tableName = "asignaciones",
    foreignKeys = [
        ForeignKey(
            entity = Alumno::class,
            parentColumns = ["id"],
            childColumns = ["alumnoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Materia::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("alumnoId"),
        Index("materiaId"),
        Index(value = ["alumnoId", "materiaId"], unique = true)
    ]
)
data class Asignacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alumnoId: Long,
    val materiaId: Long,
    val nota1: Double? = null,
    val nota2: Double? = null,
    val nota3: Double? = null,
    val nota4: Double? = null,
    val nota5: Double? = null
) {
    /**
     * Promedio de las notas no nulas. Devuelve 0.0 si no hay ninguna.
     * Marcado con @Ignore para que Room no intente persistirlo.
     */
    @Ignore
    val promedio: Double = listOfNotNull(nota1, nota2, nota3, nota4, nota5)
        .let { if (it.isEmpty()) 0.0 else it.average() }
}
