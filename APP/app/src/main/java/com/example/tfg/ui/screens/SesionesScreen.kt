package com.example.tfg.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.ui.components.VideoPlayerDialog
import com.example.tfg.viewmodel.SesionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesionesScreen(
    viewModel: SesionViewModel = viewModel(),
    onNavigateToCalendar: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    var vistaActual by remember { mutableStateOf("MENU") }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    var showDialogCrear by remember { mutableStateOf(false) }
    var expandPacientes by remember { mutableStateOf(false) }
    var expandRutinas by remember { mutableStateOf(false) }

    var sesionSeleccionada by remember { mutableStateOf<com.example.tfg.model.Sesion?>(null) }
    var showDialogOpciones by remember { mutableStateOf(false) }
    var showDialogConfirmarBorrar by remember { mutableStateOf(false) }

    var showDialogEvaluar by remember { mutableStateOf(false) }
    var puntuacionSeleccionada by remember { mutableFloatStateOf(5f) }

    var urlVideoReproducir by remember { mutableStateOf<String?>(null) }

    var mostrarMenuOrden by remember { mutableStateOf(false) }
    var criterioOrden by remember { mutableStateOf("FECHA_DEFAULT") }
    val titulosTabs = listOf("Pendientes", "Realizadas", "Corregidas")

    val TherapistDarkColor = Color(0xFF1565C0)
    val TherapistLightColor = Color(0xFF42A5F5)
    val BackgroundColor = Color(0xFFF5F7FA)
    val TextPrimary = Color(0xFF263238)
    val TextSecondary = Color(0xFF37474F)
    val IconInactive = Color(0xFF546E7A)
    val DividerColor = Color(0xFFB0BEC5)

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TherapistDarkColor,
        unfocusedBorderColor = IconInactive,
        focusedLabelColor = TherapistDarkColor,
        unfocusedLabelColor = TextSecondary,
        cursorColor = TherapistDarkColor,
        focusedLeadingIconColor = TherapistDarkColor,
        unfocusedLeadingIconColor = IconInactive,
        focusedTrailingIconColor = TherapistDarkColor,
        unfocusedTrailingIconColor = IconInactive
    )

    LaunchedEffect(Unit) {
        viewModel.cargarDatosTerapeuta(token)
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            if (vistaActual == "LISTA") {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.limpiarFormulario()
                        showDialogCrear = true
                    },
                    containerColor = TherapistDarkColor,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nueva Sesión") }
                )
            }
        }
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
                    onClick = {
                        if (vistaActual == "LISTA") vistaActual = "MENU" else onBack()
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GESTIÓN DE SESIONES",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (vistaActual == "MENU") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuButtonCard(
                        title = "Calendario Mensual",
                        subtitle = "Vista general de planificación",
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF1976D2),
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        onClick = onNavigateToCalendar
                    )

                    MenuButtonCard(
                        title = "Ver Todas las Sesiones",
                        subtitle = "Listado completo y filtros",
                        icon = Icons.Default.List,
                        color = Color(0xFF009688),
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        onClick = { vistaActual = "LISTA" }
                    )

                    MenuButtonCard(
                        title = "Programar Nueva Sesión",
                        subtitle = "Crear cita individual",
                        icon = Icons.Default.AddCircle,
                        color = Color(0xFFFF9800),
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        onClick = {
                            viewModel.limpiarFormulario()
                            showDialogCrear = true
                        }
                    )
                }

            } else if (vistaActual == "LISTA") {

                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = BackgroundColor,
                    contentColor = TherapistDarkColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                            color = TherapistDarkColor
                        )
                    }
                ) {
                    titulosTabs.forEachIndexed { index, titulo ->
                        Tab(
                            selected = tabSeleccionado == index,
                            onClick = {
                                tabSeleccionado = index
                                criterioOrden = "FECHA_DEFAULT"
                            },
                            text = {
                                Text(
                                    titulo,
                                    fontSize = 12.sp,
                                    fontWeight = if (tabSeleccionado == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                BarraOrdenacion(
                    criterioOrden = criterioOrden,
                    mostrarMenu = mostrarMenuOrden,
                    onToggleMenu = { mostrarMenuOrden = !mostrarMenuOrden },
                    onSeleccionarOrden = { nuevoCriterio ->
                        criterioOrden = nuevoCriterio
                        mostrarMenuOrden = false
                    },
                    tabIndex = tabSeleccionado,
                    textColor = TextSecondary,
                    iconColor = IconInactive
                )

                val listaBase = when (tabSeleccionado) {
                    0 -> viewModel.listaSesiones.filter {
                        it.estado == "pendiente" && !esFechaPasada(it.fecha)
                    }
                    1 -> viewModel.listaSesiones.filter { it.estado == "realizada" }
                    else -> viewModel.listaSesiones.filter {
                        it.estado == "corregida" || (it.estado == "pendiente" && esFechaPasada(it.fecha))
                    }
                }

                val listaOrdenada = aplicarOrden(listaBase, criterioOrden, tabSeleccionado, viewModel)

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TherapistDarkColor)
                    }
                } else if (listaOrdenada.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, tint = IconInactive, modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No hay sesiones en esta categoría.", color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listaOrdenada) { sesion ->
                            ItemSesionCard(
                                sesion = sesion,
                                viewModel = viewModel,
                                titleColor = TextPrimary,
                                subtitleColor = TextSecondary,
                                iconColor = IconInactive,
                                onClick = {
                                    sesionSeleccionada = sesion
                                    showDialogOpciones = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialogOpciones && sesionSeleccionada != null) {
        val sesion = sesionSeleccionada!!
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(context, { _, h, m ->
            val fechaBase = viewModel.fechaSeleccionada.take(10)
            val nuevaFecha = "${fechaBase}T${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}:00"
            viewModel.reprogramarSesion(token, sesion.id, nuevaFecha) { showDialogOpciones = false }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        val datePicker = DatePickerDialog(context, { _, y, m, d ->
            val mes = (m+1).toString().padStart(2,'0')
            val dia = d.toString().padStart(2,'0')
            viewModel.fechaSeleccionada = "$y-$mes-$dia"
            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))


        AlertDialog(
            onDismissRequest = { showDialogOpciones = false },
            title = { Text("Gestión de Sesión", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SesionDetailRow("Paciente", viewModel.obtenerNombrePaciente(sesion.idPaciente), TextSecondary, TextPrimary)
                    SesionDetailRow("Fecha", formatearFechaSesion(sesion.fecha), TextSecondary, TextPrimary)

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DividerColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    val esExpirada = sesion.estado == "pendiente" && esFechaPasada(sesion.fecha)

                    if (sesion.estado == "realizada" || sesion.estado == "corregida" || esExpirada) {

                        if (esExpirada) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Icon(Icons.Default.EventBusy, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No realizada a tiempo", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                Text("Nota automática: 0", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = { datePicker.show() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reprogramar (Dar otra oportunidad)")
                            }

                        } else {
                            Text("Ejercicios Realizados:", fontWeight = FontWeight.Bold, color = TherapistDarkColor)
                            Spacer(modifier = Modifier.height(8.dp))

                            val detalles = sesion.ejerciciosDetalles
                            if (!detalles.isNullOrEmpty()) {
                                detalles.forEach { detalle ->
                                    val nombreEjercicio = viewModel.listaEjercicios.find { it.id == detalle.idEjercicio }?.nombre
                                        ?: "Ejercicio ${detalle.idEjercicio}"

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = nombreEjercicio, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)

                                        if (!detalle.videoUrl.isNullOrEmpty()) {
                                            Button(
                                                onClick = {
                                                    val ruta = detalle.videoUrl
                                                    urlVideoReproducir = if (ruta.startsWith("/")) "file://$ruta" else ruta
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = TherapistLightColor),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Ver", fontSize = 12.sp)
                                            }
                                        } else {
                                            Text("Sin vídeo", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            } else {
                                if (!sesion.videoSesion.isNullOrEmpty()) {
                                    Button(
                                        onClick = {
                                            val ruta = sesion.videoSesion!!
                                            urlVideoReproducir = if (ruta.startsWith("/")) "file://$ruta" else ruta
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor)
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null)
                                        Text("Ver Vídeo de la Sesión")
                                    }
                                } else {
                                    Text("No hay grabaciones disponibles.", color = Color.Gray)
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    puntuacionSeleccionada = sesion.puntuacion?.toFloat() ?: 5f
                                    showDialogEvaluar = true
                                    showDialogOpciones = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (sesion.estado == "corregida") "Editar Evaluación" else "Evaluar / Corregir")
                            }
                        }

                    } else if (sesion.estado == "pendiente") {
                        Button(
                            onClick = { datePicker.show() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cambiar Fecha")
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { showDialogConfirmarBorrar = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showDialogOpciones = false }) { Text("Cerrar", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogEvaluar && sesionSeleccionada != null) {
        val notaEntera = puntuacionSeleccionada.roundToInt()
        val colorNota = if(notaEntera < 5) Color.Red else if(notaEntera < 7) Color(0xFFFF9800) else Color(0xFF2E7D32)

        AlertDialog(
            onDismissRequest = { showDialogEvaluar = false },
            title = { Text("Evaluar Sesión", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nota: $notaEntera/10", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colorNota)
                    Slider(
                        value = puntuacionSeleccionada,
                        onValueChange = { puntuacionSeleccionada = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = colorNota,
                            activeTrackColor = colorNota
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.evaluarSesion(token, sesionSeleccionada!!.id, notaEntera, "") {
                            showDialogEvaluar = false
                            sesionSeleccionada = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showDialogEvaluar = false }) { Text("Cancelar", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogCrear) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(context, { _, h, m ->
            val fechaBase = if (viewModel.fechaSeleccionada.length >= 10) viewModel.fechaSeleccionada.take(10) else "2024-01-01"
            viewModel.fechaSeleccionada = "${fechaBase}T${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}:00"
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        val datePicker = DatePickerDialog(context, { _, y, m, d ->
            val mes = (m+1).toString().padStart(2,'0')
            val dia = d.toString().padStart(2,'0')
            viewModel.fechaSeleccionada = "$y-$mes-$dia"
            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        AlertDialog(
            onDismissRequest = { showDialogCrear = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.EventAvailable, null, tint = TherapistDarkColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nueva Sesión", color = TherapistDarkColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = formatearFechaSesion(viewModel.fechaSeleccionada),
                        onValueChange = {},
                        label = { Text("Fecha y Hora") },
                        readOnly = true,
                        trailingIcon = { IconButton(onClick = { datePicker.show() }) { Icon(Icons.Default.CalendarToday, null) } },
                        modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )
                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = expandPacientes, onExpandedChange = { expandPacientes = !expandPacientes }) {
                        OutlinedTextField(
                            value = viewModel.pacienteSeleccionado?.let { "${it.nombre} ${it.apellidos}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paciente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandPacientes) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )
                        ExposedDropdownMenu(
                            expanded = expandPacientes,
                            onDismissRequest = { expandPacientes = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            viewModel.listaPacientes.forEach { paciente ->
                                DropdownMenuItem(
                                    text = { Text("${paciente.nombre} ${paciente.apellidos}", color = TextPrimary) },
                                    onClick = { viewModel.pacienteSeleccionado = paciente; expandPacientes = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = expandRutinas, onExpandedChange = { expandRutinas = !expandRutinas }) {
                        OutlinedTextField(
                            value = viewModel.rutinaSeleccionada?.nombre ?: "Sin rutina",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rutina") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandRutinas) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )
                        ExposedDropdownMenu(
                            expanded = expandRutinas,
                            onDismissRequest = { expandRutinas = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(text = { Text("Ninguna", color = TextSecondary) }, onClick = { viewModel.rutinaSeleccionada = null; expandRutinas = false })
                            viewModel.listaRutinas.forEach { rutina ->
                                DropdownMenuItem(
                                    text = { Text(rutina.nombre, color = TextPrimary) },
                                    onClick = { viewModel.rutinaSeleccionada = rutina; expandRutinas = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = DividerColor.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))

                    Text("Tiempo preparación: ${viewModel.tiempoPreparacionSeleccionado} seg", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Slider(
                        value = viewModel.tiempoPreparacionSeleccionado.toFloat(),
                        onValueChange = { viewModel.tiempoPreparacionSeleccionado = it.toInt() },
                        valueRange = 0f..60f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9800),
                            activeTrackColor = Color(0xFFFF9800)
                        )
                    )
                    Text("Tiempo para que el paciente se coloque antes de grabar.", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.crearSesion(token) { showDialogCrear = false } },
                    enabled = viewModel.fechaSeleccionada.length > 10 && viewModel.pacienteSeleccionado != null,
                    colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Agendar") }
            },
            dismissButton = { TextButton(onClick = { showDialogCrear = false }) { Text("Cancelar", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogConfirmarBorrar && sesionSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { showDialogConfirmarBorrar = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F)) },
            title = { Text("Eliminar Sesión", color = TextPrimary) },
            text = { Text("¿Estás seguro de eliminar esta sesión? Esta acción no se puede deshacer.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarSesion(token, sesionSeleccionada!!.id) {
                            showDialogConfirmarBorrar = false
                            showDialogOpciones = false
                            sesionSeleccionada = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDialogConfirmarBorrar = false }) { Text("Cancelar", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (urlVideoReproducir != null) {
        VideoPlayerDialog(
            videoUrl = urlVideoReproducir!!,
            autoClose = false,
            onDismiss = { urlVideoReproducir = null }
        )
    }
}

@Composable
fun MenuButtonCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun ItemSesionCard(
    sesion: com.example.tfg.model.Sesion,
    viewModel: SesionViewModel,
    titleColor: Color,
    subtitleColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatearFechaSesion(sesion.fecha),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1976D2)
                    )
                }

                val (bgColor, textColor, text) = when {
                    sesion.estado == "corregida" ->
                        Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Nota: ${sesion.puntuacion}")

                    sesion.estado == "realizada" ->
                        Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Pendiente Corrección")

                    sesion.estado == "pendiente" && esFechaPasada(sesion.fecha) ->
                        Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Nota: 0 (No realizada)")

                    else ->
                        Triple(Color(0xFFF5F5F5), Color.Gray, "Programada")
                }

                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))

                val nombrePaciente = remember(sesion.idPaciente, viewModel.listaPacientes) {
                    val paciente = viewModel.listaPacientes.find { it.id == sesion.idPaciente }
                    if (paciente != null) "${paciente.nombre} ${paciente.apellidos}"
                    else "Paciente ID: ${sesion.idPaciente}"
                }

                Text(
                    text = nombrePaciente,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ListAlt, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.obtenerNombreRutina(sesion.idRutina),
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }
        }
    }
}

@Composable
fun BarraOrdenacion(
    criterioOrden: String,
    mostrarMenu: Boolean,
    onToggleMenu: () -> Unit,
    onSeleccionarOrden: (String) -> Unit,
    tabIndex: Int,
    textColor: Color,
    iconColor: Color
) {
    val textoOrden = when (criterioOrden) {
        "FECHA_ASC" -> "Fecha (Antigua -> Reciente)"
        "FECHA_DESC" -> "Fecha (Reciente -> Antigua)"
        "PACIENTE" -> "Paciente (A-Z)"
        "NOTA_DESC" -> "Nota (Alta a Baja)"
        "NOTA_ASC" -> "Nota (Baja a Alta)"
        "FECHA_DEFAULT" -> if(tabIndex == 0) "Fecha (Próximas)" else "Fecha (Recientes)"
        else -> "Fecha"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Orden: $textoOrden", fontSize = 12.sp, color = textColor)
        Box {
            IconButton(onClick = onToggleMenu, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Sort, "Ordenar", tint = iconColor)
            }
            DropdownMenu(expanded = mostrarMenu, onDismissRequest = onToggleMenu, modifier = Modifier.background(Color.White)) {
                DropdownMenuItem(text = { Text("Fecha (Reciente -> Antigua)", color = textColor) }, onClick = { onSeleccionarOrden("FECHA_DESC") })
                DropdownMenuItem(text = { Text("Fecha (Antigua -> Reciente)", color = textColor) }, onClick = { onSeleccionarOrden("FECHA_ASC") })
                DropdownMenuItem(text = { Text("Paciente (A-Z)", color = textColor) }, onClick = { onSeleccionarOrden("PACIENTE") })
                if (tabIndex == 2) {
                    Divider()
                    DropdownMenuItem(text = { Text("Nota (Mayor a Menor)", color = textColor) }, onClick = { onSeleccionarOrden("NOTA_DESC") })
                    DropdownMenuItem(text = { Text("Nota (Menor a Mayor)", color = textColor) }, onClick = { onSeleccionarOrden("NOTA_ASC") })
                }
            }
        }
    }
}


fun aplicarOrden(lista: List<com.example.tfg.model.Sesion>, criterio: String, tabIndex: Int, viewModel: SesionViewModel): List<com.example.tfg.model.Sesion> {
    return when (criterio) {
        "FECHA_ASC" -> lista.sortedBy { it.fecha }
        "FECHA_DESC" -> lista.sortedByDescending { it.fecha }
        "PACIENTE" -> lista.sortedBy { viewModel.obtenerNombrePaciente(it.idPaciente) }
        "NOTA_ASC" -> lista.sortedBy { it.puntuacion ?: 0 }
        "NOTA_DESC" -> lista.sortedByDescending { it.puntuacion ?: 0 }
        else -> if (tabIndex == 0) lista.sortedBy { it.fecha } else lista.sortedByDescending { it.fecha }
    }
}

fun formatearFechaSesion(fechaIso: String?): String {
    if (fechaIso.isNullOrBlank()) return ""
    if (fechaIso.length < 10) return fechaIso
    try {
        val anio = fechaIso.substring(0, 4)
        val mes = fechaIso.substring(5, 7)
        val dia = fechaIso.substring(8, 10)
        val hora = if (fechaIso.length >= 16) " " + fechaIso.substring(11, 16) else ""
        return "$dia/$mes/$anio$hora"
    } catch (e: Exception) {
        return fechaIso
    }
}

@Composable
private fun SesionDetailRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = labelColor)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = valueColor)
    }
}

fun esFechaPasada(fechaIso: String?): Boolean {
    if (fechaIso.isNullOrBlank()) return false
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val fechaSesion = sdf.parse(fechaIso)
        val ahora = Date()

        return fechaSesion != null && fechaSesion.before(ahora)
    } catch (e: Exception) {
        return false
    }
}