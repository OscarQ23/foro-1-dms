package com.oscar.notasapp.ui.alumnos

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoFormScreen(
    profesorId: Long,
    alumnoId: Long?,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AlumnosViewModel = viewModel(
        factory = AlumnosViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var carnet by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargado by remember { mutableStateOf(false) }
    val esNuevo = alumnoId == null || alumnoId == 0L

    LaunchedEffect(alumnoId) {
        if (!esNuevo && !cargado) {
            viewModel.obtener(alumnoId!!)?.let {
                nombre = it.nombre
                apellido = it.apellido
                carnet = it.carnet
            }
            cargado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esNuevo) "Nuevo alumno" else "Editar alumno") },
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
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; error = null },
                label = { Text("Nombre *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it; error = null },
                label = { Text("Apellido *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = carnet,
                onValueChange = { carnet = it },
                label = { Text("Carnet (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    if (nombre.isBlank() || apellido.isBlank()) {
                        error = "Nombre y apellido son obligatorios"
                        return@Button
                    }
                    scope.launch {
                        viewModel.guardar(alumnoId, nombre, apellido, carnet) {
                            onGuardado()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (esNuevo) "Crear alumno" else "Guardar cambios")
            }
        }
    }
}
