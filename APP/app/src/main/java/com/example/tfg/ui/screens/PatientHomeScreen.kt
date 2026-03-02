/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla principal del paciente. Muestra la próxima sesión programada y permite acceder al historial de rehabilitación.
 */

package com.example.tfg.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.viewmodel.PatientViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pantalla de inicio para el perfil de paciente que gestiona la visualización de la próxima cita de rehabilitación y el acceso a la sala de espera virtual.
 */
@Composable
fun PatientHomeScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateToHistory: () -> Unit = {},
    onJoinSession: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val nombreUsuario = SessionManager.getNombre(context) ?: "Paciente"
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.cargarProximaSesion(token)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (token.isNotEmpty()) {
                    viewModel.cargarProximaSesion(token)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Bienvenido,", fontSize = 16.sp, color = Color.Gray)
                Text(
                    text = nombreUsuario,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp))
                }
                viewModel.errorMessage.isNotEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(60.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No se pudo cargar la sesión", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Button(onClick = { viewModel.cargarProximaSesion(token) }) {
                            Text("Reintentar")
                        }
                    }
                }
                viewModel.proximaSesion == null -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(100.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.height(24.dp))
                            Text("¡Todo listo!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Spacer(Modifier.height(16.dp))
                            Text("No tienes sesiones pendientes.", fontSize = 18.sp, color = Color.Gray)
                        }
                    }
                }
                else -> {
                    val sesion = viewModel.proximaSesion!!
                    val (diaSemana, fechaStr, horaStr) = formatearFechaCompleta(sesion.fecha)
                    val esHoy = remember(sesion.fecha) { verificarSiEsHoyHome(sesion.fecha) }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                "TU SIGUIENTE SESIÓN",
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 2.sp,
                                fontSize = 14.sp
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = diaSemana,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fechaStr,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1565C0),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 64.sp
                                )
                                Text(
                                    text = horaStr,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1565C0),
                                    lineHeight = 64.sp
                                )
                            }

                            if (esHoy) {
                                Button(
                                    onClick = { onJoinSession(sesion.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = ButtonDefaults.buttonElevation(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text("ENTRAR", fontSize = 28.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Podrás acceder a la sesión\nel día programado",
                                        color = Color.DarkGray,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 28.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNavigateToHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("HISTORIAL", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Función de utilidad que descompone una fecha en formato ISO para obtener el nombre del día, la fecha corta y la hora de forma separada.
 */
fun formatearFechaCompleta(fechaIso: String?): Triple<String, String, String> {
    if (fechaIso.isNullOrEmpty()) {
        return Triple("Día desconocido", "--/--", "--:--")
    }

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fecha = LocalDateTime.parse(fechaIso, DateTimeFormatter.ISO_DATE_TIME)
            val diaSemana = fecha.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES")))
                .replaceFirstChar { it.uppercase() }
            val diaMes = fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
            val hora = fecha.format(DateTimeFormatter.ofPattern("HH:mm"))
            Triple(diaSemana, diaMes, hora)
        } else {
            val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdfInput.parse(fechaIso) ?: return Triple("", "", "")

            val sdfDiaSemana = java.text.SimpleDateFormat("EEEE", Locale("es", "ES"))
            val diaSemana = sdfDiaSemana.format(date).replaceFirstChar { it.uppercase() }

            val sdfDiaMes = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
            val diaMes = sdfDiaMes.format(date)

            val sdfHora = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            val hora = sdfHora.format(date)

            Triple(diaSemana, diaMes, hora)
        }
    } catch (e: Exception) {
        Triple("Fecha inválida", "??/??", "??:??")
    }
}

/**
 * Comprueba si la fecha de la sesión coincide con el día actual para habilitar el botón de acceso.
 */
fun verificarSiEsHoyHome(fechaIso: String?): Boolean {
    if (fechaIso.isNullOrEmpty()) return false
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fechaSesion = LocalDateTime.parse(fechaIso, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
            val hoy = java.time.LocalDate.now()
            fechaSesion.isEqual(hoy)
        } else {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateSesion = sdf.parse(fechaIso.take(10)) ?: return false
            val dateHoy = Date()

            val calSesion = Calendar.getInstance().apply { time = dateSesion }
            val calHoy = Calendar.getInstance().apply { time = dateHoy }

            calSesion.get(Calendar.YEAR) == calHoy.get(Calendar.YEAR) &&
                    calSesion.get(Calendar.DAY_OF_YEAR) == calHoy.get(Calendar.DAY_OF_YEAR)
        }
    } catch (e: Exception) {
        false
    }
}