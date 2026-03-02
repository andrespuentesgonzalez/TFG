/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de gestión de rutinas. Permite al terapeuta listar, ordenar, filtrar y crear nuevas rutinas de ejercicios.
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
import androidx.compose.runtime.*
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
import com.example.tfg.viewmodel.RutinaViewModel

/**
 * Enumeración para definir los criterios de ordenación de la lista de rutinas.
 */
enum class SortRoutineOption(val label: String) {
    ALPHA_AZ("Alfabéticamente (A → Z)"),
    ALPHA_ZA("Alfabéticamente (Z → A)"),
    DURATION_DESC("Duración (Más → Menos)"),
    DURATION_ASC("Duración (Menos → Más)"),
    COUNT_DESC("Nº Ejercicios (Más → Menos)"),
    COUNT_ASC("Nº Ejercicios (Menos → Más)")
}

/**
 * Pantalla principal de la biblioteca de rutinas donde el terapeuta puede consultar el catálogo existente y crear nuevas planificaciones de ejercicios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinaScreen(
    viewModel: RutinaViewModel = viewModel(),
    onBack: () -> Unit,
    onRutinaClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""

    var showCreateDialog by remember { mutableStateOf(false) }

    var tempNombre by remember { mutableStateOf("") }
    var tempDesc by remember { mutableStateOf("") }

    var sortOption by remember { mutableStateOf(SortRoutineOption.ALPHA_AZ) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

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
        unfocusedLeadingIconColor = IconInactive
    )

    val rutinasOrdenadas = remember(viewModel.listaRutinas, sortOption) {
        val list = viewModel.listaRutinas
        when (sortOption) {
            SortRoutineOption.ALPHA_AZ -> list.sortedBy { it.nombre?.lowercase() }
            SortRoutineOption.ALPHA_ZA -> list.sortedByDescending { it.nombre?.lowercase() }

            SortRoutineOption.DURATION_DESC -> list.sortedByDescending { r ->
                r.fichas?.sumOf { f -> (f.tiempoMinutos ?: 0).takeIf { it > 0 } ?: (f.ejercicio.duracion ?: 0) } ?: 0
            }
            SortRoutineOption.DURATION_ASC -> list.sortedBy { r ->
                r.fichas?.sumOf { f -> (f.tiempoMinutos ?: 0).takeIf { it > 0 } ?: (f.ejercicio.duracion ?: 0) } ?: 0
            }

            SortRoutineOption.COUNT_DESC -> list.sortedByDescending { it.fichas?.size ?: 0 }
            SortRoutineOption.COUNT_ASC -> list.sortedBy { it.fichas?.size ?: 0 }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarRutinas(token)
        viewModel.cargarEjerciciosDisponibles(token)
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    tempNombre = ""
                    tempDesc = ""
                    viewModel.limpiarFormulario()
                    showCreateDialog = true
                },
                containerColor = TherapistDarkColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva Rutina") }
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
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BIBLIOTECA DE RUTINAS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!viewModel.isLoading && viewModel.listaRutinas.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .clickable { isSortMenuExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orden: ${sortOption.label}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Box {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Ordenar",
                            tint = TherapistDarkColor
                        )

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            SortRoutineOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (option == sortOption) TherapistDarkColor else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        sortOption = option
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TherapistDarkColor)
                }
            } else if (rutinasOrdenadas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay rutinas creadas.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rutinasOrdenadas) { rutina ->
                        val totalMinutos = rutina.fichas?.sumOf { ficha ->
                            val tiempoFicha = ficha.tiempoMinutos ?: 0
                            if (tiempoFicha > 0) tiempoFicha else (ficha.ejercicio.duracion ?: 0)
                        } ?: 0

                        val ejercicioCount = rutina.fichas?.size ?: 0

                        val authorName = if (rutina.terapeuta != null) {
                            "${rutina.terapeuta.nombre} ${rutina.terapeuta.apellidos}".trim()
                        } else {
                            "Terapeuta"
                        }

                        RoutineCard(
                            title = rutina.nombre,
                            description = rutina.descripcion,
                            author = authorName,
                            duration = totalMinutos,
                            exerciseCount = ejercicioCount,
                            titleColor = TextPrimary,
                            subtitleColor = TextSecondary,
                            iconColor = IconInactive,
                            onCardClick = { onRutinaClick(rutina.id) },
                            onDeleteClick = { viewModel.borrarRutina(token, rutina.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            modifier = Modifier.padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlaylistAdd, null, tint = TherapistDarkColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NUEVA RUTINA", color = TherapistDarkColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempNombre,
                        onValueChange = { tempNombre = it },
                        label = { Text("Nombre de la rutina") },
                        leadingIcon = { Icon(Icons.Default.Title, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = tempDesc,
                        onValueChange = { tempDesc = it },
                        label = { Text("Descripción (opcional)") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                    Text("Añadir Ejercicios", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "Seleccionar ejercicio...",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 250.dp).background(Color.White)
                        ) {
                            viewModel.listaTodosEjercicios.forEach { ejercicio ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = ejercicio.nombre ?: "Sin nombre", fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("${ejercicio.duracion ?: 0} min", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    },
                                    onClick = {
                                        viewModel.agregarEjercicioALista(
                                            idEjercicio = ejercicio.id,
                                            series = 3,
                                            repes = 10,
                                            minutos = ejercicio.duracion ?: 5
                                        )
                                        expanded = false
                                    }
                                )
                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            }
                        }
                    }

                    if (viewModel.ejerciciosSeleccionados.isNotEmpty()) {
                        Text(
                            text = "${viewModel.ejerciciosSeleccionados.size} ejercicios añadidos:",
                            color = TherapistDarkColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F7FA), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            viewModel.ejerciciosSeleccionados.forEachIndexed { index, ficha ->
                                val nombreEj = viewModel.nombreEjercicio(ficha.idEjercicio)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FitnessCenter, null, tint = IconInactive, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(nombreEj, fontSize = 13.sp, modifier = Modifier.weight(1f), color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Text("${ficha.tiempoMinutos} min", fontSize = 12.sp, color = TextSecondary)
                                    IconButton(
                                        onClick = { viewModel.quitarEjercicioDeLista(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempNombre.isNotEmpty()) {
                            viewModel.crearRutina(token, tempNombre, tempDesc) { showCreateDialog = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Rutina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Tarjeta que muestra la información resumida de una rutina incluyendo título, autor y métricas clave.
 */
@Composable
fun RoutineCard(
    title: String,
    description: String?,
    author: String,
    duration: Int,
    exerciseCount: Int,
    titleColor: Color,
    subtitleColor: Color,
    iconColor: Color,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ListAlt, null, tint = Color(0xFF1565C0))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = titleColor
                        )
                        if (!description.isNullOrEmpty()) {
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = subtitleColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = iconColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (author.isNotBlank()) author else "Desconocido",
                        fontSize = 12.sp,
                        color = subtitleColor
                    )
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "$exerciseCount ejs",
                            fontSize = 11.sp,
                            color = Color(0xFF00695C),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = Color(0xFFE65100), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$duration min",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}