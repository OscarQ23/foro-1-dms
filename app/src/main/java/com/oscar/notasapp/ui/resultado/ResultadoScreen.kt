package com.oscar.notasapp.ui.resultado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oscar.notasapp.theme.ErrorRed
import com.oscar.notasapp.theme.SuccessGreen

/**
 * Muestra el resultado final: promedio + estado (aprobado / reprobado).
 */
@Composable
fun ResultadoScreen(
    promedio: Double,
    aprobado: Boolean,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val color = if (aprobado) SuccessGreen else ErrorRed
    val titulo = if (aprobado) "¡APROBADO!" else "REPROBADO"
    val mensaje = if (aprobado)
        "¡Felicitaciones! Tu rendimiento alcanzó el mínimo aprobatorio."
    else
        "Tu promedio está por debajo del mínimo (6.0). ¡A seguir esforzándose!"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (aprobado) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.displaySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Promedio: ${"%.2f".format(promedio)}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) { Text("Volver a editar") }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) { Text("Cerrar sesión") }
    }
}
