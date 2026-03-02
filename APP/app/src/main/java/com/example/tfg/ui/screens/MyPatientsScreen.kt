/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla que lista los pacientes asignados al terapeuta y permite acceder a sus detalles o ver su próxima sesión programada.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.model.Usuario
import com.example.tfg.viewmodel.MyPatientsViewModel
import java.util.Locale

/**
 * Vista principal para el terapeuta donde se visualiza la cartera de pacientes bajo su supervisión y se gestionan las citas próximas
 */
@Composable
fun MyPatientsScreen(
    viewModel: MyPatientsViewModel = viewModel(),
    onBack: () -> Unit,
    onPatientClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    val TherapistDarkColor = Color(0xFF1565C0)
    val TherapistLightColor = Color(0xFF42A5F5)
    val BackgroundColor = Color(0xFFF5F7FA)

    LaunchedEffect(Unit) {
        viewModel.cargarMisPacientes(token)
    }

    Scaffold(
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TherapistDarkColor, TherapistLightColor)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MIS PACIENTES",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading && !viewModel.showSesionDialog) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TherapistDarkColor)
                }
            } else if (viewModel.listaPacientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tienes pacientes asignados.", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.listaPacientes) { paciente ->
                        PatientCard(
                            usuario = paciente,
                            onCardClick = { onPatientClick(paciente.id) },
                            onCalendarClick = { viewModel.cargarSiguienteSesion(token, paciente.id) }
                        )
                    }
                }
            }
        }
    }
    if (viewModel.showSesionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoSesion() },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = TherapistDarkColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detalles de Sesión", fontWeight = FontWeight.Bold, color = TherapistDarkColor)
                }
            },
            text = {
                if (viewModel.siguienteSesion != null) {
                    val sesion = viewModel.siguienteSesion!!
                    val rutina = viewModel.rutinaAsignada


                    val fechaStr = sesion.fecha.toString().replace("T", " ").substring(0, 16)

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(fechaStr, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Divider()

                        if (rutina != null) {

                            Column {
                                Text("Rutina Asignada:", fontSize = 12.sp, color = Color.Gray)
                                Text(rutina.nombre.uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TherapistDarkColor)
                            }

                            val totalMinutos = rutina.fichas?.sumOf {
                                if ((it.tiempoMinutos ?: 0) > 0) it.tiempoMinutos!! else (it.ejercicio.duracion ?: 0)
                            } ?: 0

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Duración aprox: $totalMinutos min", fontSize = 14.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(4.dp))

                            Text("Ejercicios a realizar:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                            rutina.fichas?.forEach { ficha ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = ficha.ejercicio.nombre ?: "Ejercicio",
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val detalle = if ((ficha.tiempoMinutos ?: 0) > 0) {
                                        "${ficha.tiempoMinutos}m"
                                    } else {
                                        "${ficha.series}x${ficha.repeticiones}"
                                    }
                                    Text(detalle, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            Text("No hay rutina asignada a esta sesión.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text(
                        text = viewModel.mensajeSesion.ifEmpty { "No hay información disponible." },
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.cerrarDialogoSesion() }) {
                    Text("Cerrar", color = TherapistDarkColor)
                }
            }
        )
    }
}

/**
 * Tarjeta individual que muestra los datos de contacto del paciente y un botón de acceso directo a la planificación de su siguiente sesión
 */
@Composable
fun PatientCard(
    usuario: Usuario,
    onCardClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF263238)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = usuario.correo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Button(
                onClick = { onCalendarClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Próxima sesión",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}