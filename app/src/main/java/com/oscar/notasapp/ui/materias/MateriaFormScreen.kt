package com.oscar.notasapp.ui.materias

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
fun MateriaFormScreen(
    profesorId: Long,
    materiaId: Long?,           // null o 0L → creación
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MateriasViewModel = viewModel(
        factory = MateriasViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargado by remember { mutableStateOf(false) }
    val esNuevo = materiaId == null || materiaId == 0L

    LaunchedEffect(materiaId) {
        if (!esNuevo && !cargado) {
            viewModel.obtener(materiaId!!)?.let {
                nombre = it.nombre
                codigo = it.codigo
            }
            cargado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esNuevo) "Nueva materia" else "Editar materia") },
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
                label = { Text("Nombre de la materia *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        error = "El nombre es obligatorio"
                        return@Button
                    }
                    scope.launch {
                        viewModel.guardar(materiaId, nombre, codigo) {
                            onGuardado()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (esNuevo) "Crear materia" else "Guardar cambios")
            }
        }
    }
}
