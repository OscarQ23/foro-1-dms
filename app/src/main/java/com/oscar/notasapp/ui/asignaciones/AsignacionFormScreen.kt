package com.oscar.notasapp.ui.asignaciones

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Formulario para evaluar (5 notas) un alumno en una materia.
 *
 * Se invoca SIEMPRE con un alumnoId y un materiaId conocidos. Si la
 * asignación todavía no existe se crea automáticamente con notas vacías;
 * si ya existe se cargan sus 5 notas para editarlas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignacionFormScreen(
    profesorId: Long,
    materiaId: Long,
    alumnoId: Long,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AsignacionesViewModel = viewModel(
        factory = AsignacionesViewModelFactory(
            context.applicationContext as Application,
            profesorId
        )
    )

    val notasTexto = remember { mutableStateListOf("", "", "", "", "") }
    var error by remember { mutableStateOf<String?>(null) }
    var asignacionId by remember { mutableStateOf<Long?>(null) }
    var alumnoLabel by remember { mutableStateOf("") }
    var materiaLabel by remember { mutableStateOf("") }
    var cargado by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // Carga inicial: obtiene/crea la asignación, sus notas y los nombres
    // del alumno y la materia para mostrarlos como cabecera.
    LaunchedEffect(alumnoId, materiaId) {
        if (cargado) return@LaunchedEffect
        val id = viewModel.obtenerOCrearAsignacionId(alumnoId, materiaId)
        asignacionId = id
        viewModel.obtener(id)?.let { existente ->
            notasTexto[0] = existente.nota1?.let { "%.2f".format(it) } ?: ""
            notasTexto[1] = existente.nota2?.let { "%.2f".format(it) } ?: ""
            notasTexto[2] = existente.nota3?.let { "%.2f".format(it) } ?: ""
            notasTexto[3] = existente.nota4?.let { "%.2f".format(it) } ?: ""
            notasTexto[4] = existente.nota5?.let { "%.2f".format(it) } ?: ""
        }
        viewModel.obtenerMateria(materiaId)?.let { materiaLabel = it.nombre }
        cargado = true
    }

    // Resolución del nombre del alumno desde el flow del ViewModel.
    val alumnos by viewModel.alumnos.collectAsState()
    LaunchedEffect(alumnos, alumnoId) {
        alumnos.firstOrNull { it.id == alumnoId }?.let {
            alumnoLabel = "${it.apellido}, ${it.nombre}"
        }
    }

    val notasParseadas: List<Double?> = notasTexto.map { txt ->
        if (txt.isBlank()) null else txt.toDoubleOrNull()
    }
    // Una nota está fuera de rango si el texto no está vacío y o bien no
    // parsea como número, o el número está fuera de [0, 10].
    val notasFueraDeRango: List<Boolean> = notasTexto.mapIndexed { i, txt ->
        if (txt.isBlank()) false
        else {
            val n = notasParseadas[i]
            n == null || n < 0.0 || n > 10.0
        }
    }
    val hayNotaInvalida = notasFueraDeRango.any { it }
    // El promedio en vivo solo considera notas válidas dentro de rango.
    val notasValidas = notasParseadas
        .filterIndexed { i, _ -> !notasFueraDeRango[i] }
        .filterNotNull()
    val promedioVivo = if (notasValidas.isEmpty()) 0.0 else notasValidas.average()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluar") },
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Materia",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        materiaLabel.ifBlank { "—" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Alumno",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        alumnoLabel.ifBlank { "—" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                "Notas (0 a 10). Puedes dejar vacías las que aún no tengas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            repeat(5) { i ->
                OutlinedTextField(
                    value = notasTexto[i],
                    onValueChange = { nuevo ->
                        notasTexto[i] = nuevo.replace(",", ".")
                        error = null
                    },
                    label = { Text("Nota ${i + 1}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Promedio (en vivo)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "%.2f".format(promedioVivo),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (promedioVivo >= 6.0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Calculado con ${notasValidas.size} de 5 notas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    if (guardando) return@Button

                    var hayInvalida = false
                    notasTexto.forEachIndexed { i, txt ->
                        if (txt.isNotBlank() && txt.toDoubleOrNull() == null) {
                            error = "La nota ${i + 1} no es un número válido"
                            hayInvalida = true
                            return@forEachIndexed
                        }
                    }
                    if (hayInvalida) return@Button

                    guardando = true
                    viewModel.guardar(
                        id = asignacionId,
                        alumnoId = alumnoId,
                        materiaId = materiaId,
                        notas = notasParseadas,
                        onError = {
                            guardando = false
                            error = it
                        },
                        onDone = onGuardado
                    )
                },
                enabled = !guardando && asignacionId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar notas")
            }
        }
    }
}
