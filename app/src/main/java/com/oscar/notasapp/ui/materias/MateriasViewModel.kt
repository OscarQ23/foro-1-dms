package com.oscar.notasapp.ui.materias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oscar.notasapp.data.AppDatabase
import com.oscar.notasapp.data.Materia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del CRUD de materias. Todas las consultas se filtran por
 * [profesorId] para mantener las sesiones independientes.
 */
class MateriasViewModel(
    application: Application,
    private val profesorId: Long
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).materiaDao()

    val materias: StateFlow<List<Materia>> = dao.observeByProfesor(profesorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guardar(id: Long?, nombre: String, codigo: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val limpiaNombre = nombre.trim()
            val limpiaCodigo = codigo.trim()
            if (id == null || id == 0L) {
                dao.insert(Materia(profesorId = profesorId, nombre = limpiaNombre, codigo = limpiaCodigo))
            } else {
                dao.update(Materia(id = id, profesorId = profesorId, nombre = limpiaNombre, codigo = limpiaCodigo))
            }
            onDone()
        }
    }

    fun eliminar(materia: Materia) {
        viewModelScope.launch { dao.delete(materia) }
    }

    suspend fun obtener(id: Long): Materia? = dao.getById(id)
}

class MateriasViewModelFactory(
    private val application: Application,
    private val profesorId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MateriasViewModel(application, profesorId) as T
}
