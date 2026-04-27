package com.oscar.notasapp.ui.asignaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oscar.notasapp.data.Alumno
import com.oscar.notasapp.data.AppDatabase
import com.oscar.notasapp.data.Asignacion
import com.oscar.notasapp.data.AsignacionConDetalles
import com.oscar.notasapp.data.Materia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de asignaciones.
 *
 * Permite dos flujos:
 *  - Ver las asignaciones ya creadas con sus detalles.
 *  - Seleccionar una materia existente y marcar/desmarcar alumnos existentes
 *    para inscribirlos o quitarlos de esa materia.
 */
class AsignacionesViewModel(
    application: Application,
    private val profesorId: Long
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val asignacionDao = db.asignacionDao()
    private val materiaDao = db.materiaDao()
    private val alumnoDao = db.alumnoDao()

    val asignaciones: StateFlow<List<AsignacionConDetalles>> =
        asignacionDao.observeByProfesor(profesorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materiasParaAsignar: StateFlow<List<Materia>> =
        asignacionDao.observeMateriasParaAsignar(profesorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alumnosParaAsignar: StateFlow<List<Alumno>> =
        asignacionDao.observeAlumnosParaAsignar(profesorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Se mantienen estos nombres para las pantallas/formularios antiguos que todavía los usan.
    val materias: StateFlow<List<Materia>> = materiasParaAsignar
    val alumnos: StateFlow<List<Alumno>> = alumnosParaAsignar

    private val _materiaSeleccionada = MutableStateFlow<Materia?>(null)
    val materiaSeleccionada: StateFlow<Materia?> = _materiaSeleccionada.asStateFlow()

    private val _alumnoIdsInscritos = MutableStateFlow<List<Long>>(emptyList())
    val alumnoIdsInscritos: StateFlow<List<Long>> = _alumnoIdsInscritos.asStateFlow()

    fun seleccionarMateria(materia: Materia) {
        if (materia.profesorId != profesorId) return

        _materiaSeleccionada.value = materia
        _alumnoIdsInscritos.value = emptyList()

        viewModelScope.launch {
            try {
                _alumnoIdsInscritos.value = asignacionDao.getAlumnoIdsPorMateria(materia.id)
            } catch (_: Exception) {
                _alumnoIdsInscritos.value = emptyList()
            }
        }
    }

    fun cambiarInscripcion(
        alumnoId: Long,
        materiaId: Long,
        inscrito: Boolean
    ) {
        viewModelScope.launch {
            val alumno = alumnoDao.getById(alumnoId)
            val materia = materiaDao.getById(materiaId)

            if (alumno == null || materia == null ||
                alumno.profesorId != profesorId || materia.profesorId != profesorId
            ) {
                return@launch
            }

            if (inscrito) {
                asignacionDao.inscribirAlumnoEnMateria(
                    Asignacion(
                        alumnoId = alumnoId,
                        materiaId = materiaId
                    )
                )
                _alumnoIdsInscritos.value =
                    (_alumnoIdsInscritos.value + alumnoId).distinct()
            } else {
                asignacionDao.quitarAlumnoDeMateria(
                    alumnoId = alumnoId,
                    materiaId = materiaId
                )
                _alumnoIdsInscritos.value =
                    _alumnoIdsInscritos.value.filterNot { it == alumnoId }
            }
        }
    }

    /**
     * Guarda una asignación con sus 5 notas (cualquiera puede ser null si
     * el campo se dejó vacío). Cada nota válida debe estar entre 0 y 10.
     */
    fun guardar(
        id: Long?,
        alumnoId: Long,
        materiaId: Long,
        notas: List<Double?>,
        onError: (String) -> Unit,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (alumnoId <= 0L || materiaId <= 0L) {
                onError("Selecciona alumno y materia")
                return@launch
            }
            if (notas.size != 5) {
                onError("Debe haber 5 notas (pueden quedar vacías)")
                return@launch
            }
            notas.forEachIndexed { index, valor ->
                if (valor != null && (valor < 0.0 || valor > 10.0)) {
                    onError("La nota ${index + 1} debe estar entre 0 y 10")
                    return@launch
                }
            }
            // Sesión independiente: el alumno y la materia tienen que ser de este profesor.
            val alumno = alumnoDao.getById(alumnoId)
            val materia = materiaDao.getById(materiaId)
            if (alumno == null || materia == null ||
                alumno.profesorId != profesorId || materia.profesorId != profesorId
            ) {
                onError("Selección inválida")
                return@launch
            }

            if (id == null || id == 0L) {
                if (asignacionDao.countDuplicate(alumnoId, materiaId) > 0) {
                    onError("Este alumno ya está asignado a esa materia")
                    return@launch
                }
                asignacionDao.insert(
                    Asignacion(
                        alumnoId = alumnoId,
                        materiaId = materiaId,
                        nota1 = notas[0],
                        nota2 = notas[1],
                        nota3 = notas[2],
                        nota4 = notas[3],
                        nota5 = notas[4]
                    )
                )
            } else {
                asignacionDao.update(
                    Asignacion(
                        id = id,
                        alumnoId = alumnoId,
                        materiaId = materiaId,
                        nota1 = notas[0],
                        nota2 = notas[1],
                        nota3 = notas[2],
                        nota4 = notas[3],
                        nota5 = notas[4]
                    )
                )
            }
            onDone()
        }
    }

    fun eliminar(item: AsignacionConDetalles) {
        viewModelScope.launch {
            val real = asignacionDao.getById(item.id) ?: return@launch
            asignacionDao.delete(real)
        }
    }

    suspend fun obtener(id: Long): Asignacion? = asignacionDao.getById(id)

    // ---------- Soporte para la pantalla de detalle de materia ----------

    suspend fun obtenerMateria(materiaId: Long): Materia? {
        val materia = materiaDao.getById(materiaId) ?: return null
        return if (materia.profesorId == profesorId) materia else null
    }

    /** Lista de alumnos inscritos en una materia (con su promedio). */
    fun observeAsignacionesDeMateria(materiaId: Long): Flow<List<AsignacionConDetalles>> =
        asignacionDao.observeByMateria(materiaId)

    /**
     * Inscribe un alumno en una materia (sin notas todavía).
     * Si ya estaba inscrito no hace nada (gracias al índice único).
     */
    fun inscribirAlumno(alumnoId: Long, materiaId: Long) {
        viewModelScope.launch {
            val alumno = alumnoDao.getById(alumnoId) ?: return@launch
            val materia = materiaDao.getById(materiaId) ?: return@launch
            if (alumno.profesorId != profesorId || materia.profesorId != profesorId) return@launch
            asignacionDao.inscribirAlumnoEnMateria(
                Asignacion(alumnoId = alumnoId, materiaId = materiaId)
            )
        }
    }

    /** Quita la asignación de un alumno en una materia (borra sus notas). */
    fun quitarAlumno(alumnoId: Long, materiaId: Long) {
        viewModelScope.launch {
            asignacionDao.quitarAlumnoDeMateria(alumnoId, materiaId)
        }
    }

    /**
     * Devuelve el id de la asignación para (alumno, materia). Si todavía
     * no existe la crea con notas vacías. Permite abrir el form de evaluar
     * pasándole un id válido.
     */
    suspend fun obtenerOCrearAsignacionId(alumnoId: Long, materiaId: Long): Long {
        val existente = asignacionDao.findByAlumnoYMateria(alumnoId, materiaId)
        if (existente != null) return existente.id
        return asignacionDao.insert(
            Asignacion(alumnoId = alumnoId, materiaId = materiaId)
        )
    }
}

class AsignacionesViewModelFactory(
    private val application: Application,
    private val profesorId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AsignacionesViewModel(application, profesorId) as T
}
