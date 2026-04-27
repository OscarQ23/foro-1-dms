package com.oscar.notasapp.ui.alumnos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oscar.notasapp.data.Alumno
import com.oscar.notasapp.data.AppDatabase
import com.oscar.notasapp.data.AsignacionConDetalles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del CRUD de alumnos. Igual que en materias, todo se filtra por
 * [profesorId] para que cada profesor sólo vea su propia lista.
 */
class AlumnosViewModel(
    application: Application,
    private val profesorId: Long
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val alumnoDao = db.alumnoDao()
    private val asignacionDao = db.asignacionDao()

    val alumnos: StateFlow<List<Alumno>> = alumnoDao.observeByProfesor(profesorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guardar(
        id: Long?,
        nombre: String,
        apellido: String,
        carnet: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val n = nombre.trim()
            val a = apellido.trim()
            val c = carnet.trim()
            if (id == null || id == 0L) {
                alumnoDao.insert(Alumno(profesorId = profesorId, nombre = n, apellido = a, carnet = c))
            } else {
                alumnoDao.update(Alumno(id = id, profesorId = profesorId, nombre = n, apellido = a, carnet = c))
            }
            onDone()
        }
    }

    fun eliminar(alumno: Alumno) {
        viewModelScope.launch { alumnoDao.delete(alumno) }
    }

    suspend fun obtener(id: Long): Alumno? = alumnoDao.getById(id)

    /** Asignaciones de un alumno, expuestas como Flow simple. */
    fun asignacionesDe(alumnoId: Long): Flow<List<AsignacionConDetalles>> =
        asignacionDao.observeByAlumno(alumnoId)
}

class AlumnosViewModelFactory(
    private val application: Application,
    private val profesorId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AlumnosViewModel(application, profesorId) as T
}
