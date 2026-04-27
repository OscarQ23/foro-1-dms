package com.oscar.notasapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoDao {

    @Insert
    suspend fun insert(alumno: Alumno): Long

    @Update
    suspend fun update(alumno: Alumno)

    @Delete
    suspend fun delete(alumno: Alumno)

    @Query("SELECT * FROM alumnos WHERE profesorId = :profesorId ORDER BY apellido ASC, nombre ASC")
    fun observeByProfesor(profesorId: Long): Flow<List<Alumno>>

    @Query("SELECT * FROM alumnos WHERE id = :id")
    suspend fun getById(id: Long): Alumno?
}
