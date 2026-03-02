/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de visualización de progreso del paciente. Renderiza una gráfica personalizada con la evolución de las puntuaciones.
 */

package com.example.tfg.ui.screens

import android.graphics.Paint
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.model.Sesion
import com.example.tfg.viewmodel.PatientHistoryViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla que visualiza la evolución del rendimiento del paciente mediante una gráfica lineal de sus puntuaciones históricas obtenidas en las sesiones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientGraphScreen(
    viewModel: PatientHistoryViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial(token)
    }

    val datosGrafica = remember(viewModel.listaHistorial) {
        viewModel.listaHistorial
            .filter { it.puntuacion != null }
            .sortedBy { it.fecha }
    }

    val BluePrimary = Color(0xFF1565C0)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MI RENDIMIENTO",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("VOLVER", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFAFAFA)),
                modifier = Modifier.height(90.dp)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(60.dp))
            } else if (datosGrafica.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Necesitas realizar al menos 2 sesiones\npara ver tu gráfica de evolución.",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text(
                            "Evolución (Puntos 0 a 10)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(24.dp))

                        FullSizeTrendChart(
                            dataPoints = datosGrafica,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                val promedio = datosGrafica.map { it.puntuacion!! }.average()
                val ultimo = datosGrafica.last().puntuacion!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ultimo >= 7) Icons.Default.EmojiEvents else Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = BluePrimary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (ultimo >= 7) "¡Muy buen trabajo!" else "¡Sigue así!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = BluePrimary
                            )
                            Text(
                                text = "Tu nota media es: ${String.format("%.1f", promedio)}",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Componente gráfico personalizado que dibuja la tendencia de puntuaciones sobre el tiempo utilizando primitivas de dibujo en Canvas.
 */
@Composable
fun FullSizeTrendChart(
    dataPoints: List<Sesion>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val scores = dataPoints.map { it.puntuacion!! }
    val fechas = dataPoints.map { formatFechaCorta(it.fecha) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val paddingBottom = 60f
        val paddingTop = 40f
        val paddingHorizontal = 40f

        val drawingH = h - paddingBottom - paddingTop
        val drawingW = w - (paddingHorizontal * 2)

        val niveles = listOf(0, 5, 10)
        niveles.forEach { nivel ->
            val y = paddingTop + (drawingH - (nivel / 10f * drawingH))

            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(paddingHorizontal, y),
                end = Offset(w - paddingHorizontal, y),
                strokeWidth = 2f
            )

            drawContext.canvas.nativeCanvas.drawText(
                nivel.toString(),
                10f,
                y + 10f,
                Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 30f
                    textAlign = Paint.Align.LEFT
                }
            )
        }

        val stepX = if (scores.size > 1) drawingW / (scores.size - 1) else 0f
        val path = Path()

        scores.forEachIndexed { index, score ->
            val x = paddingHorizontal + (index * stepX)
            val y = paddingTop + (drawingH - (score / 10f * drawingH))

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(paddingHorizontal + (scores.size - 1) * stepX, h - paddingBottom)
        fillPath.lineTo(paddingHorizontal, h - paddingBottom)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1565C0).copy(alpha = 0.3f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = Color(0xFF1565C0),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        scores.forEachIndexed { index, score ->
            val x = paddingHorizontal + (index * stepX)
            val y = paddingTop + (drawingH - (score / 10f * drawingH))

            drawCircle(color = Color.White, radius = 16f, center = Offset(x, y))
            drawCircle(color = getColorForScore(score), radius = 10f, center = Offset(x, y))

            val skip = if (scores.size > 6) 2 else 1
            if (index % skip == 0 || index == scores.size - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    fechas[index],
                    x,
                    h - 10f,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

/**
 * Función auxiliar que transforma una fecha en formato ISO a una cadena corta de día y mes para los ejes de la gráfica.
 */
fun formatFechaCorta(fechaIso: String?): String {
    if (fechaIso.isNullOrEmpty()) return ""
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fecha = LocalDateTime.parse(fechaIso, DateTimeFormatter.ISO_DATE_TIME)
            fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
        } else {
            fechaIso.substring(8, 10) + "/" + fechaIso.substring(5, 7)
        }
    } catch (e: Exception) { "" }
}