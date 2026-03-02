/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de detalle de una rutina. Permite ver, añadir y eliminar los ejercicios que componen una rutina específica.
 */

package com.example.tfg.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.SessionManager
import com.example.tfg.ui.components.VideoPlayerDialog
import com.example.tfg.viewmodel.RutinaDetailViewModel

/**
 * Pantalla que muestra el contenido detallado de una rutina listando los ejercicios que la componen y permitiendo su gestión mediante la adición o eliminación de elementos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinaDetailScreen(
    rutinaId: Int,
    viewModel: RutinaDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    var showAddDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var ejercicioParaVer by remember { mutableStateOf<Ejercicio?>(null) }
    val rutinaState = viewModel.rutina

    var urlVideoReproducir by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(rutinaId) {
        viewModel.cargarDatos(token, rutinaId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.errorMessage = ""
                    showAddDialog = true
                },
                containerColor = TherapistDarkColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Añadir Ejercicio") }
            )
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
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TherapistDarkColor, TherapistLightColor)
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                if (rutinaState != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rutinaState.nombre.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        if (!rutinaState.descripcion.isNullOrEmpty()) {
                            Text(
                                text = rutinaState.descripcion,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading && rutinaState == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TherapistDarkColor)
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize()) {

                    val count = rutinaState?.fichas?.size ?: 0
                    Text(
                        text = "EJERCICIOS ($count)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val fichas = rutinaState?.fichas ?: emptyList()

                        if (fichas.isEmpty()) {
                            item {
                                EmptyStateMessage(TextSecondary, IconInactive)
                            }
                        }

                        items(fichas) { ficha ->
                            RutinaExerciseCard(
                                nombre = ficha.ejercicio.nombre ?: "Ejercicio",
                                duracion = ficha.tiempoMinutos ?: (ficha.ejercicio.duracion ?: 0),
                                zona = ficha.ejercicio.zona ?: "General",
                                hasVideo = !ficha.ejercicio.videoUrl.isNullOrEmpty(),
                                titleColor = TextPrimary,
                                iconColor = IconInactive,
                                onClick = { ejercicioParaVer = ficha.ejercicio },
                                onVideoClick = {
                                    if (!ficha.ejercicio.videoUrl.isNullOrBlank()) {
                                        urlVideoReproducir = ficha.ejercicio.videoUrl
                                    }
                                },
                                onDeleteClick = { viewModel.quitarEjercicioDeRutina(token, ficha.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (ejercicioParaVer != null) {
        val ej = ejercicioParaVer!!
        AlertDialog(
            onDismissRequest = { ejercicioParaVer = null },
            icon = { Icon(Icons.Default.Info, null, tint = TherapistDarkColor) },
            title = { Text(ej.nombre ?: "Detalle", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailRow("Zona", ej.zona ?: "General", TextPrimary, TextSecondary)
                    DetailRow("Duración Base", "${ej.duracion ?: 0} min", TextPrimary, TextSecondary)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Descripción:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Text(ej.descripcion ?: "Sin descripción", fontSize = 14.sp, color = TextSecondary)

                    if (!ej.videoUrl.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { urlVideoReproducir = ej.videoUrl },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver Vídeo")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { ejercicioParaVer = null }) { Text("Cerrar", color = TherapistDarkColor) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Añadir a la Rutina", fontWeight = FontWeight.Bold, color = TherapistDarkColor) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Selecciona un ejercicio de la biblioteca:", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.errorMessage.isNotEmpty()) {
                        Text(viewModel.errorMessage, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.ejercicioSeleccionado?.nombre ?: "Seleccionar...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.heightIn(max = 250.dp).background(Color.White)
                        ) {
                            val idsEnRutina = rutinaState?.fichas?.map { it.ejercicio.id } ?: emptyList()
                            val disponibles = viewModel.listaTodosEjercicios.filter { it.id !in idsEnRutina }

                            if (disponibles.isEmpty()) {
                                DropdownMenuItem(text = { Text("No quedan ejercicios disponibles", color = TextSecondary) }, onClick = {})
                            } else {
                                disponibles.forEach { ejercicio ->
                                    DropdownMenuItem(
                                        text = { Text(ejercicio.nombre ?: "Sin nombre", color = TextPrimary) },
                                        onClick = {
                                            viewModel.ejercicioSeleccionado = ejercicio
                                            isDropdownExpanded = false
                                        }
                                    )
                                    Divider(color = DividerColor.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.anadirEjercicioARutina(token, rutinaId) { showAddDialog = false }
                    },
                    enabled = viewModel.ejercicioSeleccionado != null && !viewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (urlVideoReproducir != null) {
        VideoPlayerDialog(
            videoUrl = urlVideoReproducir!!,
            onDismiss = { urlVideoReproducir = null }
        )
    }
}

/**
 * Componente de tarjeta que visualiza la información básica de un ejercicio dentro de la lista de la rutina incluyendo controles para ver video y eliminar.
 */
@Composable
fun RutinaExerciseCard(
    nombre: String,
    duracion: Int,
    zona: String,
    hasVideo: Boolean,
    titleColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    onVideoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = titleColor)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFECEFF1), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = zona,
                            fontSize = 10.sp,
                            color = Color(0xFF455A64),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = Color(0xFFE65100), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "$duracion min", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasVideo) {
                    IconButton(onClick = onVideoClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.PlayCircle, null, tint = Color(0xFFD32F2F))
                    }
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * Mensaje visual informativo que aparece cuando la rutina no tiene ejercicios asignados indicando cómo añadir nuevos elementos.
 */
@Composable
fun EmptyStateMessage(textColor: Color, iconColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.PlaylistAdd, null, tint = iconColor, modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Rutina vacía", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text("Pulsa el botón + para añadir ejercicios", fontSize = 14.sp, color = textColor)
    }
}

/**
 * Fila auxiliar para mostrar pares de etiqueta y valor en los diálogos de información.
 */
@Composable
private fun DetailRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = labelColor)
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}