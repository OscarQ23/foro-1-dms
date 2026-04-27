package com.oscar.notasapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {

    @Insert
    suspend fun insert(materia: Materia): Long

    @Update
    suspend fun update(materia: Materia)

    @Delete
    suspend fun delete(materia: Materia)

    @Query("SELECT * FROM materias WHERE profesorId = :profesorId ORDER BY nombre ASC")
    fun observeByProfesor(profesorId: Long): Flow<List<Materia>>

    @Query("SELECT * FROM materias WHERE id = :id")
    suspend fun getById(id: Long): Materia?
}
