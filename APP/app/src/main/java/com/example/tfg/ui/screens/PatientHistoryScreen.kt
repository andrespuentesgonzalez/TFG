/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de historial del paciente. Muestra una lista de sesiones pasadas y una gráfica desplegable de la evolución.
 */

package com.example.tfg.ui.screens

import android.graphics.Paint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.model.Sesion
import com.example.tfg.viewmodel.PatientHistoryViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla que presenta el registro histórico de sesiones de un paciente permitiendo visualizar tanto el listado detallado como una gráfica de tendencia desplegable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryScreen(
    patientId: Int,
    viewModel: PatientHistoryViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    var mostrarGrafica by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.cargarHistorialDePaciente(token, patientId)
    }

    val historialCompletado = remember(viewModel.listaHistorial) {
        viewModel.listaHistorial.filter { it.puntuacion != null }
    }

    val datosGrafica = remember(historialCompletado) {
        historialCompletado.sortedBy { it.fecha }
    }

    val listaParaMostrar = remember(historialCompletado) {
        historialCompletado.sortedByDescending { it.fecha }
    }

    val BluePrimary = Color(0xFF1565C0)
    val OrangeAction = Color(0xFFEF6C00)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HISTORIAL",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        if (viewModel.nombrePaciente.isNotEmpty()) {
                            Text(
                                text = viewModel.nombrePaciente,
                                fontSize = 14.sp,
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .widthIn(min = 100.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("VOLVER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Button(
                            onClick = { mostrarGrafica = !mostrarGrafica },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mostrarGrafica) Color.Gray else OrangeAction
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .widthIn(min = 110.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (mostrarGrafica) "OCULTAR" else "GRÁFICA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFAFAFA)
                ),
                modifier = Modifier.height(100.dp)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(80.dp),
                    strokeWidth = 8.dp
                )
            }
            else if (listaParaMostrar.isEmpty()) {
                EmptyStateMessage(Modifier.align(Alignment.Center))
            }
            else {
                Column(modifier = Modifier.fillMaxSize()) {

                    AnimatedVisibility(
                        visible = mostrarGrafica,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(260.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Evolución",
                                    fontSize = 22.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))

                                if (datosGrafica.size < 2) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Hacen falta más sesiones", color = Color.Gray)
                                    }
                                } else {
                                    SimpleTrendChart(
                                        dataPoints = datosGrafica.map { it.puntuacion!! },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(listaParaMostrar) { sesion ->
                            HistoryItemRow(sesion)
                        }
                    }
                }
            }
        }
    }
}


/**
 * Componente gráfico simplificado que dibuja la tendencia de las puntuaciones obtenidas en las sesiones utilizando un lienzo de dibujo personalizado.
 */
@Composable
fun SimpleTrendChart(
    dataPoints: List<Int>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxScore = 10f

        val paddingTop = 40f
        val paddingBottom = 20f
        val drawingHeight = height - paddingTop - paddingBottom

        val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else 0f

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, score ->
            val x = index * stepX
            val y = paddingTop + (drawingHeight - (score / maxScore * drawingHeight))

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1565C0).copy(alpha = 0.2f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = path,
            color = Color(0xFF1565C0),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 35f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        dataPoints.forEachIndexed { index, score ->
            val x = index * stepX
            val y = paddingTop + (drawingHeight - (score / maxScore * drawingHeight))

            drawCircle(color = Color.White, radius = 14f, center = Offset(x, y))
            drawCircle(color = getColorForScore(score), radius = 10f, center = Offset(x, y))

            drawContext.canvas.nativeCanvas.drawText(
                score.toString(),
                x,
                y - 25f,
                textPaint
            )
        }
    }
}

/**
 * Elemento de lista que representa una sesión individual mostrando la fecha, calificación cualitativa y puntuación numérica.
 */
@Composable
fun HistoryItemRow(sesion: Sesion) {
    val (dia, mes) = getDiaMes(sesion.fecha)
    val nota = sesion.puntuacion ?: 0
    val colorNota = getColorForScore(nota)

    val (textoDescriptivo, numEstrellas) = when (nota) {
        in 9..10 -> Pair("Excelente", 5)
        in 7..8 -> Pair("Muy Bien", 4)
        in 5..6 -> Pair("Bien", 3)
        else -> Pair("Mejorable", 2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(dia, fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF1565C0))
                    Text(mes, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    textoDescriptivo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorNota
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (index < numEstrellas) Color(0xFFFFC107) else Color.LightGray
                        )
                    }
                }
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(50.dp),
                    color = colorNota.copy(alpha = 0.2f),
                    strokeWidth = 50.dp
                )
                Text(
                    text = nota.toString(),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = colorNota
                )
            }
        }
    }
}

/**
 * Mensaje visual que se muestra cuando no existen registros de sesiones finalizadas en el historial.
 */
@Composable
fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("No tienes historial aún", color = Color.Gray, fontSize = 20.sp)
    }
}

/**
 * Determina el color asociado a una puntuación numérica para indicar visualmente el nivel de desempeño.
 */
fun getColorForScore(score: Int): Color {
    return when (score) {
        in 9..10 -> Color(0xFF2E7D32)
        in 7..8 -> Color(0xFF66BB6A)
        in 5..6 -> Color(0xFFFFCA28)
        else -> Color(0xFFEF5350)
    }
}

/**
 * Función de utilidad que extrae el día y el nombre del mes a partir de una cadena de fecha en formato ISO.
 */
fun getDiaMes(fechaIso: String?): Pair<String, String> {
    if (fechaIso.isNullOrEmpty()) return Pair("--", "---")
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fecha = LocalDateTime.parse(fechaIso, DateTimeFormatter.ISO_DATE_TIME)
            val dia = fecha.format(DateTimeFormatter.ofPattern("dd"))
            val mes = fecha.format(DateTimeFormatter.ofPattern("MMM"))
            Pair(dia, mes.uppercase())
        } else {
            val dia = fechaIso.substring(8, 10)
            val mes = "MES " + fechaIso.substring(5, 7)
            Pair(dia, mes)
        }
    } catch (e: Exception) {
        Pair("??", "???")
    }
}