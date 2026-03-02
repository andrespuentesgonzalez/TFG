/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de calendario que permite visualizar y gestionar la agenda de sesiones de rehabilitación organizadas por meses.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.Sesion
import com.example.tfg.model.SessionManager
import com.example.tfg.viewmodel.SesionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

/**
 * Interfaz gráfica que representa un calendario interactivo donde el terapeuta puede consultar las sesiones programadas y sus detalles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    viewModel: SesionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    LaunchedEffect(Unit) {
        if (viewModel.listaSesiones.isEmpty()) {
            viewModel.cargarDatosTerapeuta(token)
        }
    }

    var fechaVisualizada by remember { mutableStateOf(Calendar.getInstance()) }
    val fechaHoy = remember { Calendar.getInstance() }
    var sesionesDiaSeleccionado by remember { mutableStateOf<List<Sesion>?>(null) }
    var fechaDiaSeleccionado by remember { mutableStateOf("") }
    val year = fechaVisualizada.get(Calendar.YEAR)
    val month = fechaVisualizada.get(Calendar.MONTH)
    val calendarCalc = fechaVisualizada.clone() as Calendar
    calendarCalc.set(Calendar.DAY_OF_MONTH, 1)
    val primerDiaSemanaRaw = calendarCalc.get(Calendar.DAY_OF_WEEK)
    val diasVaciosAlInicio = if (primerDiaSemanaRaw == Calendar.SUNDAY) 6 else primerDiaSemanaRaw - 2
    val maxDiasMes = calendarCalc.getActualMaximum(Calendar.DAY_OF_MONTH)
    val nombreMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(fechaVisualizada.time).uppercase()
    val totalCeldas = diasVaciosAlInicio + maxDiasMes
    val numeroSemanas = ceil(totalCeldas / 7.0).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario de Sesiones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val nuevo = fechaVisualizada.clone() as Calendar
                    nuevo.add(Calendar.MONTH, -1)
                    fechaVisualizada = nuevo
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
                }

                Text(
                    text = nombreMes,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = {
                    val nuevo = fechaVisualizada.clone() as Calendar
                    nuevo.add(Calendar.MONTH, 1)
                    fechaVisualizada = nuevo
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
                diasSemana.forEach { dia ->
                    Text(
                        text = dia,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
            ) {
                for (semana in 0 until numeroSemanas) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        for (columna in 0 until 7) {
                            val indiceAbsoluto = (semana * 7) + columna
                            val diaDelMes = indiceAbsoluto - diasVaciosAlInicio + 1
                            val esFinde = (columna == 5 || columna == 6)

                            if (diaDelMes in 1..maxDiasMes) {
                                val esHoy = (year == fechaHoy.get(Calendar.YEAR) &&
                                        month == fechaHoy.get(Calendar.MONTH) &&
                                        diaDelMes == fechaHoy.get(Calendar.DAY_OF_MONTH))

                                val mesStr = (month + 1).toString().padStart(2, '0')
                                val diaStr = diaDelMes.toString().padStart(2, '0')
                                val fechaActualStr = "$year-$mesStr-$diaStr"

                                val sesionesDelDia = viewModel.listaSesiones.filter {
                                    it.fecha?.startsWith(fechaActualStr) == true
                                }
                                val haySesion = sesionesDelDia.isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (esFinde) Color(0xFFF0F0F0) else Color.White)
                                        .border(0.5.dp, Color(0xFFE0E0E0))
                                        .clickable(enabled = haySesion) {
                                            sesionesDiaSeleccionado = sesionesDelDia
                                            fechaDiaSeleccionado = "$diaDelMes de $nombreMes"
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (esHoy) Color(0xFF1976D2) else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$diaDelMes",
                                                fontSize = 13.sp,
                                                fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
                                                color = if (esHoy) Color.White else if (esFinde) Color.Gray else Color.Black
                                            )
                                        }
                                        if (haySesion) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                sesionesDelDia.take(3).forEach { _ ->
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 1.dp)
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE65100))
                                                    )
                                                }
                                            }
                                            if (sesionesDelDia.isNotEmpty()) {
                                                val primerNombre = viewModel.obtenerNombrePaciente(sesionesDelDia[0].idPaciente).split(" ")[0]
                                                Text(
                                                    text = primerNombre,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF1565C0),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color(0xFFFAFAFA))
                                        .border(0.2.dp, Color(0xFFEEEEEE))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (sesionesDiaSeleccionado != null) {
        val listaSegura = sesionesDiaSeleccionado ?: emptyList()

        AlertDialog(
            onDismissRequest = { sesionesDiaSeleccionado = null },
            title = {
                Text(
                    text = "Sesiones: $fechaDiaSeleccionado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    items(listaSegura) { sesion ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val hora = if (sesion.fecha != null && sesion.fecha.length >= 16) {
                                        sesion.fecha.substring(11, 16)
                                    } else "--:--"
                                    Text(text = "Hora: $hora", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Paciente: ${viewModel.obtenerNombrePaciente(sesion.idPaciente)}")
                                Text("Rutina: ${viewModel.obtenerNombreRutina(sesion.idRutina)}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sesionesDiaSeleccionado = null }) { Text("Cerrar") }
            }
        )
    }
}