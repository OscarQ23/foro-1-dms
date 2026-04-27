package com.oscar.notasapp.ui.asignaciones

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oscar.notasapp.data.AsignacionConDetalles
import com.oscar.notasapp.data.Materia
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de una materia: muestra los alumnos inscritos,
 * permite eliminarlos y abrir el formulario de "Evaluar" (5 notas) por
 * cada uno. Un FAB abre un diálogo para inscribir alumnos no inscritos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaAlumnosScreen(
    profesorId: Long,
    materiaId: Long,
    onEvaluar: (alumnoId: Long, materiaId: Long) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AsignacionesViewModel = viewModel(
        factory = AsignacionesViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )
    val coroutineScope = rememberCoroutineScope()

    var materia by remember { mutableStateOf<Materia?>(null) }
    LaunchedEffect(materiaId) { materia = viewModel.obtenerMateria(materiaId) }

    val asignacionesFlow = remember(materiaId) {
        viewModel.observeAsignacionesDeMateria(materiaId)
    }
    val asignaciones by asignacionesFlow.collectAsState(initial = emptyList())
    val todosLosAlumnos by viewModel.alumnosParaAsignar.collectAsState()

    val idsInscritos = asignaciones.map { it.alumnoId }.toSet()
    val alumnosNoInscritos = todosLosAlumnos.filter { it.id !in idsInscritos }

    var aBorrar by remember { mutableStateOf<AsignacionConDetalles?>(null) }
    var mostrarPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(materia?.nombre ?: "Materia")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarPicker = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar alumno") }
            )
        }
    ) { padding ->
        if (asignaciones.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Sin alumnos inscritos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Toca el botón + para agregar alumnos a esta materia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = asignaciones, key = { "asig_${it.id}" }) { item ->
                    AlumnoInscritoCard(
                        item = item,
                        onEvaluar = { onEvaluar(item.alumnoId, materiaId) },
                        onEliminar = { aBorrar = item }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación.
    aBorrar?.let { item ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("Eliminar alumno") },
            text = {
                Text(
                    "¿Quitar a ${item.alumnoNombre} ${item.alumnoApellido} de esta " +
                            "materia? Se borrarán también sus 5 notas."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.quitarAlumno(item.alumnoId, materiaId)
                    aBorrar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aBorrar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para agregar alumno (selecciona uno de la lista de no inscritos).
    if (mostrarPicker) {
        AlertDialog(
            onDismissRequest = { mostrarPicker = false },
            title = { Text("Agregar alumno") },
            text = {
                if (alumnosNoInscritos.isEmpty()) {
                    Text(
                        "No hay alumnos disponibles. Todos tus alumnos ya están " +
                                "inscritos en esta materia, o no tienes alumnos cargados aún."
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(items = alumnosNoInscritos, key = { "alumno_${it.id}" }) { alumno ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.inscribirAlumno(alumno.id, materiaId)
                                    mostrarPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${alumno.apellido}, ${alumno.nombre}")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPicker = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun AlumnoInscritoCard(
    item: AsignacionConDetalles,
    onEvaluar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${item.alumnoApellido}, ${item.alumnoNombre}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = listOf(
                            item.nota1, item.nota2, item.nota3,
                            item.nota4, item.nota5
                        ).joinToString(" · ") { n ->
                            n?.let { "%.1f".format(it) } ?: "—"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "%.2f".format(item.promedio),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.promedio >= 6.0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedButton(
                onClick = onEvaluar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Evaluar")
            }
        }
    }
}
