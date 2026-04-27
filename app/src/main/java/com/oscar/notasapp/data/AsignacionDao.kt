package com.oscar.notasapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Vista plana usada para mostrar la lista de asignaciones con los datos
 * del alumno y la materia ya unidos vía JOIN.
 *
 * Incluye las 5 notas y el promedio calculado en SQL ignorando los NULL.
 */
data class AsignacionConDetalles(
    val id: Long,
    val alumnoId: Long,
    val materiaId: Long,
    val nota1: Double?,
    val nota2: Double?,
    val nota3: Double?,
    val nota4: Double?,
    val nota5: Double?,
    val promedio: Double,
    val alumnoNombre: String,
    val alumnoApellido: String,
    val materiaNombre: String
)

@Dao
interface AsignacionDao {

    @Insert
    suspend fun insert(asignacion: Asignacion): Long

    @Update
    suspend fun update(asignacion: Asignacion)

    @Delete
    suspend fun delete(asignacion: Asignacion)

    @Query("SELECT * FROM asignaciones WHERE id = :id")
    suspend fun getById(id: Long): Asignacion?

    /**
     * Muestra las asignaciones ya creadas para el profesor con las 5 notas
     * y el promedio calculado en SQL (suma de notas no nulas / cantidad de
     * notas no nulas, o 0 si no hay ninguna).
     */
    @Query("""
        SELECT a.id AS id, a.alumnoId AS alumnoId, a.materiaId AS materiaId,
               a.nota1 AS nota1, a.nota2 AS nota2, a.nota3 AS nota3,
               a.nota4 AS nota4, a.nota5 AS nota5,
               COALESCE(
                 (COALESCE(a.nota1,0) + COALESCE(a.nota2,0) + COALESCE(a.nota3,0)
                + COALESCE(a.nota4,0) + COALESCE(a.nota5,0))
                 /
                 NULLIF(
                   (CASE WHEN a.nota1 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota2 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota3 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota4 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota5 IS NULL THEN 0 ELSE 1 END), 0)
               , 0.0) AS promedio,
               al.nombre AS alumnoNombre, al.apellido AS alumnoApellido,
               m.nombre AS materiaNombre
        FROM asignaciones a
        INNER JOIN alumnos al ON a.alumnoId = al.id
        INNER JOIN materias m ON a.materiaId = m.id
        WHERE al.profesorId = :profesorId
        ORDER BY al.apellido ASC, m.nombre ASC
    """)
    fun observeByProfesor(profesorId: Long): Flow<List<AsignacionConDetalles>>

    /**
     * Materias existentes del profesor para mostrarlas primero en la pantalla
     * de asignaciones.
     */
    @Query("""
        SELECT *
        FROM materias
        WHERE profesorId = :profesorId
        ORDER BY nombre ASC
    """)
    fun observeMateriasParaAsignar(profesorId: Long): Flow<List<Materia>>

    /**
     * Alumnos existentes del profesor para poder seleccionarlos después de
     * elegir una materia.
     */
    @Query("""
        SELECT *
        FROM alumnos
        WHERE profesorId = :profesorId
        ORDER BY apellido ASC, nombre ASC
    """)
    fun observeAlumnosParaAsignar(profesorId: Long): Flow<List<Alumno>>

    /**
     * Alumnos que ya están inscritos en una materia específica.
     * Esto sirve para marcar como seleccionados los alumnos ya asignados.
     */
    @Query("""
        SELECT alumnoId
        FROM asignaciones
        WHERE materiaId = :materiaId
    """)
    suspend fun getAlumnoIdsPorMateria(materiaId: Long): List<Long>

    /**
     * Crea la inscripción del alumno en la materia solo si todavía no existe.
     * Así evitamos duplicados cuando el usuario vuelve a guardar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inscribirAlumnoEnMateria(asignacion: Asignacion)

    /**
     * Quita a un alumno de una materia cuando el usuario lo deselecciona.
     */
    @Query("""
        DELETE FROM asignaciones
        WHERE alumnoId = :alumnoId AND materiaId = :materiaId
    """)
    suspend fun quitarAlumnoDeMateria(alumnoId: Long, materiaId: Long)

    @Query("""
        SELECT a.id AS id, a.alumnoId AS alumnoId, a.materiaId AS materiaId,
               a.nota1 AS nota1, a.nota2 AS nota2, a.nota3 AS nota3,
               a.nota4 AS nota4, a.nota5 AS nota5,
               COALESCE(
                 (COALESCE(a.nota1,0) + COALESCE(a.nota2,0) + COALESCE(a.nota3,0)
                + COALESCE(a.nota4,0) + COALESCE(a.nota5,0))
                 /
                 NULLIF(
                   (CASE WHEN a.nota1 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota2 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota3 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota4 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota5 IS NULL THEN 0 ELSE 1 END), 0)
               , 0.0) AS promedio,
               al.nombre AS alumnoNombre, al.apellido AS alumnoApellido,
               m.nombre AS materiaNombre
        FROM asignaciones a
        INNER JOIN alumnos al ON a.alumnoId = al.id
        INNER JOIN materias m ON a.materiaId = m.id
        WHERE a.alumnoId = :alumnoId
        ORDER BY m.nombre ASC
    """)
    fun observeByAlumno(alumnoId: Long): Flow<List<AsignacionConDetalles>>

    @Query("SELECT COUNT(*) FROM asignaciones WHERE alumnoId = :alumnoId AND materiaId = :materiaId")
    suspend fun countDuplicate(alumnoId: Long, materiaId: Long): Int

    /**
     * Devuelve la asignación de un alumno en una materia (o null si no
     * existe). Sirve para abrir el formulario de evaluar.
     */
    @Query("""
        SELECT *
        FROM asignaciones
        WHERE alumnoId = :alumnoId AND materiaId = :materiaId
        LIMIT 1
    """)
    suspend fun findByAlumnoYMateria(alumnoId: Long, materiaId: Long): Asignacion?

    /**
     * Lista las asignaciones (con datos del alumno y promedio) de una
     * materia específica. Se usa en la pantalla de detalle de materia.
     */
    @Query("""
        SELECT a.id AS id, a.alumnoId AS alumnoId, a.materiaId AS materiaId,
               a.nota1 AS nota1, a.nota2 AS nota2, a.nota3 AS nota3,
               a.nota4 AS nota4, a.nota5 AS nota5,
               COALESCE(
                 (COALESCE(a.nota1,0) + COALESCE(a.nota2,0) + COALESCE(a.nota3,0)
                + COALESCE(a.nota4,0) + COALESCE(a.nota5,0))
                 /
                 NULLIF(
                   (CASE WHEN a.nota1 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota2 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota3 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota4 IS NULL THEN 0 ELSE 1 END) +
                   (CASE WHEN a.nota5 IS NULL THEN 0 ELSE 1 END), 0)
               , 0.0) AS promedio,
               al.nombre AS alumnoNombre, al.apellido AS alumnoApellido,
               m.nombre AS materiaNombre
        FROM asignaciones a
        INNER JOIN alumnos al ON a.alumnoId = al.id
        INNER JOIN materias m ON a.materiaId = m.id
        WHERE a.materiaId = :materiaId
        ORDER BY al.apellido ASC, al.nombre ASC
    """)
    fun observeByMateria(materiaId: Long): Flow<List<AsignacionConDetalles>>
}
