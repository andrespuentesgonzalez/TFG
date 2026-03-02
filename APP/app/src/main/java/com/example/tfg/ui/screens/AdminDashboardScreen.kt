/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla del panel de administración que visualiza estadísticas globales, gráficas de tendencia y registros de actividad del sistema.
 */

package com.example.tfg.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.model.LogReciente
import com.example.tfg.viewmodel.AdminDashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla del panel de control para administradores.
 * Muestra métricas clave, gráficas de evolución y logs del sistema mediante una interfaz scrollable.
 */
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val AdminDarkColor = Color(0xFF263238)
    val AdminAccentColor = Color(0xFF37474F)
    val BackgroundColor = Color(0xFFF5F7FA)

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas(token)
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
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AdminDarkColor, AdminAccentColor)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PANEL DE CONTROL",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Estadísticas del Sistema",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AdminDarkColor)
                    }
                } else if (viewModel.errorMessage.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Error al cargar datos", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text(viewModel.errorMessage, fontSize = 12.sp)
                            Button(onClick = { viewModel.cargarEstadisticas(token) }) { Text("Reintentar") }
                        }
                    }
                } else {
                    val s = viewModel.stats
                    if (s != null) {
                        DashboardSectionTitle("📈 Tendencia Semanal")
                        val datosGrafica = if (s.graficaSemanal.isNotEmpty()) s.graficaSemanal else listOf(0,0,0,0,0,0,0)

                        WeeklyTrendCardReal(datosGrafica, AdminDarkColor)

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardSectionTitle("👥 Resumen de Usuarios")

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                DashboardCard("Total", s.totalUsuarios.toString(), Icons.Default.Group, AdminDarkColor)
                                Spacer(modifier = Modifier.height(12.dp))
                                DashboardCard("Terapeutas", s.terapeutas.toString(), Icons.Default.MedicalServices, Color(0xFF1976D2))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                DashboardCard("Pacientes", s.pacientes.toString(), Icons.Default.SentimentSatisfiedAlt, Color(0xFF43A047))
                                Spacer(modifier = Modifier.height(12.dp))
                                DashboardCard("Admins", s.admins.toString(), Icons.Default.AdminPanelSettings, Color(0xFF7B1FA2))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardSectionTitle("🔥 Monitorización en Vivo")

                        WideStatCard(
                            title = "Sesiones Hoy",
                            value = s.sesionesHoy.toString(),
                            subtitle = "Actividad registrada hoy",
                            icon = Icons.Default.Today,
                            color = Color(0xFFF57C00)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val hayPendientes = s.pendientes > 0
                        val colorPendientes = if (hayPendientes) Color(0xFFD32F2F) else Color(0xFF388E3C)
                        val iconoPendientes = if (hayPendientes) Icons.Default.NotificationImportant else Icons.Default.CheckCircle

                        WideStatCard(
                            title = "Correcciones Pendientes",
                            value = s.pendientes.toString(),
                            subtitle = if(hayPendientes) "Requiere atención" else "Todo al día",
                            icon = iconoPendientes,
                            color = colorPendientes
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardSectionTitle("📚 Inventario y Calidad")

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CompactStatItem("Ejercicios", s.totalEjercicios.toString())
                                    CompactStatItem("Rutinas", s.totalRutinas.toString())
                                    CompactStatItem("Sesiones", s.totalSesiones.toString())
                                }
                                Divider(modifier = Modifier.padding(vertical = 16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nota Media Global: ", fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text(
                                        s.notaMedia.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = AdminDarkColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardSectionTitle("⏱️ Auditoría / Log Reciente")

                        if (s.logsRecientes.isNotEmpty()) {
                            RecentActivityLogReal(s.logsRecientes)
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "No hay actividad reciente.",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Tarjeta que renderiza una gráfica lineal personalizada con Canvas.
 * Muestra la tendencia de sesiones realizadas en los últimos 7 días.
 */
@Composable
fun WeeklyTrendCardReal(data: List<Int>, lineColor: Color) {
    val days = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE", Locale("es", "ES"))

        (6 downTo 0).map { i ->
            if (i == 0) {
                "HOY"
            } else {
                val date = today.minusDays(i.toLong())
                date.format(formatter).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
        }
    }

    val maxVal = (data.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, null, tint = lineColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sesiones Diarias", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = lineColor)
                Spacer(modifier = Modifier.weight(1f))
                Text("Total semana: ${data.sum()}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, start = 8.dp, end = 8.dp)) {
                val w = size.width
                val h = size.height
                val pointsCount = data.size.coerceAtLeast(2)
                val stepX = w / (pointsCount - 1)

                val path = Path()

                data.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = h - (value.toFloat() / maxVal * h)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(if (data.size > 1) w else 0f, h)
                fillPath.lineTo(0f, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                data.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = h - (value.toFloat() / maxVal * h)

                    drawCircle(color = Color.White, radius = 8f, center = Offset(x, y))
                    drawCircle(color = lineColor, radius = 5f, center = Offset(x, y))

                    if (i < days.size) {
                        val textoDia = days[i]
                        val esHoy = textoDia == "HOY"

                        drawContext.canvas.nativeCanvas.drawText(
                            textoDia,
                            x,
                            h + 40f,
                            Paint().apply {
                                color = if (esHoy) android.graphics.Color.BLACK else android.graphics.Color.GRAY
                                textSize = if (esHoy) 34f else 32f
                                textAlign = Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que lista los últimos eventos o acciones registradas en el sistema.
 */
@Composable
fun RecentActivityLogReal(logs: List<LogReciente>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            logs.forEachIndexed { index, log ->
                val (icon, color) = when(log.tipo) {
                    "alerta" -> Pair(Icons.Default.Warning, Color(0xFFD32F2F))
                    "exito" -> Pair(Icons.Default.CheckCircle, Color(0xFF43A047))
                    else -> Pair(Icons.Default.Info, Color(0xFF1976D2))
                }

                ActivityRow(log.usuario, log.accion, log.tiempo, icon, color)

                if (index < logs.size - 1) {
                    Divider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

/**
 * Fila individual para mostrar un evento en el log de actividad.
 */
@Composable
fun ActivityRow(user: String, action: String, time: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(user, fontSize = 11.sp, color = Color.Gray)
        }
        Text(time, fontSize = 11.sp, color = Color.LightGray)
    }
}

/**
 * Título de sección estilizado para el dashboard.
 */
@Composable
fun DashboardSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

/**
 * Tarjeta cuadrada pequeña para mostrar métricas individuales (ej. Total Usuarios).
 */
@Composable
fun DashboardCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Tarjeta rectangular ancha para mostrar información destacada (ej. Sesiones Hoy).
 */
@Composable
fun WideStatCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

/**
 * Elemento de estadística compacta para listas horizontales.
 */
@Composable
fun CompactStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}