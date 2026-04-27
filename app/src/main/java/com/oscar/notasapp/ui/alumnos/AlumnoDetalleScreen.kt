package com.oscar.notasapp.ui.alumnos

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oscar.notasapp.data.Alumno
import com.oscar.notasapp.data.AsignacionConDetalles

/** Nota mínima para considerar al alumno aprobado. */
private const val NOTA_APROBATORIA = 6.0

/**
 * Detalle de un alumno: muestra sus materias asignadas, calcula el promedio
 * en tiempo real y permite navegar a la pantalla de resultado APROBADO/REPROBADO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoDetalleScreen(
    profesorId: Long,
    alumnoId: Long,
    onCalcular: (promedio: Double, aprobado: Boolean) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AlumnosViewModel = viewModel(
        factory = AlumnosViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )

    var alumno by remember { mutableStateOf<Alumno?>(null) }
    LaunchedEffect(alumnoId) { alumno = viewModel.obtener(alumnoId) }

    val asignacionesFlow = remember(alumnoId) { viewModel.asignacionesDe(alumnoId) }
    val asignaciones by asignacionesFlow.collectAsState(initial = emptyList())

    val promedio = if (asignaciones.isEmpty()) 0.0 else asignaciones.sumOf { it.promedio } / asignaciones.size
    val aprobado = promedio >= NOTA_APROBATORIA

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alumno?.let { "${it.nombre} ${it.apellido}" } ?: "Alumno") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Promedio actual",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "%.2f".format(promedio),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Basado en ${asignaciones.size} materia${if (asignaciones.size == 1) "" else "s"} asignada${if (asignaciones.size == 1) "" else "s"}.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "Materias asignadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (asignaciones.isEmpty()) {
                Text(
                    "Este alumno aún no tiene materias asignadas. Ve a la pantalla de Asignaciones para inscribirlo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = asignaciones, key = { it.id }) { item ->
                        AsignacionRow(item)
                    }
                }
            }

            Button(
                onClick = { onCalcular(promedio, aprobado) },
                enabled = asignaciones.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular resultado final")
            }
        }
    }
}

@Composable
private fun AsignacionRow(item: AsignacionConDetalles) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.materiaNombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "%.2f".format(item.promedio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.promedio >= NOTA_APROBATORIA)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
