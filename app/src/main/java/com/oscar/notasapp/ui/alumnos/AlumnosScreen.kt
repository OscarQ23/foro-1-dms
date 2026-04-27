package com.oscar.notasapp.ui.alumnos

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnosScreen(
    profesorId: Long,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    onDetalle: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AlumnosViewModel = viewModel(
        factory = AlumnosViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )
    val alumnos by viewModel.alumnos.collectAsState()
    var aBorrar by remember { mutableStateOf<Alumno?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alumnos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregar,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo alumno") }
            )
        }
    ) { padding ->
        if (alumnos.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tienes alumnos todavía.\nToca + para agregar uno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = alumnos, key = { it.id }) { alumno ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onDetalle(alumno.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${alumno.apellido}, ${alumno.nombre}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (alumno.carnet.isNotBlank()) {
                                    Text(
                                        "Carnet: ${alumno.carnet}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onEditar(alumno.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { aBorrar = alumno }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    aBorrar?.let { alumno ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("Eliminar alumno") },
            text = { Text("¿Eliminar a ${alumno.nombre} ${alumno.apellido}? También se borran todas sus asignaciones.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(alumno)
                    aBorrar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aBorrar = null }) { Text("Cancelar") }
            }
        )
    }
}
