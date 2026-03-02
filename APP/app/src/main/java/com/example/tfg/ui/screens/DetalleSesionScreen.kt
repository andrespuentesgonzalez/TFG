/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de evaluación de sesiones donde el terapeuta revisa la grabación del paciente y asigna una calificación y feedback.
 */

package com.example.tfg.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.viewmodel.SesionViewModel

/**
 * Pantalla de evaluación detallada donde el terapeuta revisa la ejecución del ejercicio y asigna una calificación junto con comentarios de retroalimentación para el paciente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesionId: Int,
    viewModel: SesionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val sesion = viewModel.getSesionPorId(sesionId)
    var nota by remember { mutableStateOf(sesion?.puntuacion?.toFloat() ?: 5f) }
    var comentario by remember { mutableStateOf(sesion?.comentarioTerapeuta ?: "") }

    if (sesion == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val nombrePaciente = viewModel.obtenerNombrePaciente(sesion.idPaciente)
    val nombreRutina = viewModel.obtenerNombreRutina(sesion.idRutina)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación de Sesión") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(8.dp))
                        Text(nombrePaciente, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(8.dp))

                        Text(formatearFechaParaMostrar(sesion.fecha), fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Rutina: $nombreRutina", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("📹 GRABACIÓN DE LA SESIÓN", fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(Modifier.height(8.dp))

            if (!sesion.videoSesion.isNullOrEmpty()) {
                Button(
                    onClick = {
                        try {
                            // Intenta abrir el video en una aplicación externa o navegador
                            val url = sesion.videoSesion!!
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Manejo básico de errores si no hay app para abrir el link
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("VER VIDEO DEL PACIENTE")
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("No hay grabación disponible todavía.", color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            Text("📝 TU EVALUACIÓN", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
            Spacer(Modifier.height(16.dp))


            Text("Puntuación: ${nota.toInt()}/10", fontWeight = FontWeight.Bold)
            Slider(
                value = nota,
                onValueChange = { nota = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF2E7D32),
                    activeTrackColor = Color(0xFF4CAF50)
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Feedback para el paciente") },
                placeholder = { Text("Ej: Muy buen trabajo, intenta estirar más el brazo...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.evaluarSesion(token, sesionId, nota.toInt(), comentario) {
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("GUARDAR CORRECCIÓN")
                }
            }

            if (viewModel.errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(viewModel.errorMessage, color = Color.Red)
            }
        }
    }
}

/**
 * Utilidad auxiliar que recorta la cadena de fecha para mostrar únicamente la parte correspondiente al día mes y año ignorando la hora.
 */
fun formatearFechaParaMostrar(fecha: String?): String {
    if (fecha.isNullOrEmpty()) return "Fecha pendiente"
    return try {
        // Asumiendo formato ISO YYYY-MM-DDTHH:MM:SS
        if (fecha.length >= 10) fecha.substring(0, 10) else fecha
    } catch (e: Exception) {
        fecha
    }
}