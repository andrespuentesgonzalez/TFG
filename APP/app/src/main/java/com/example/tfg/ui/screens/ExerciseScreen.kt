/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de gestión de la biblioteca de ejercicios. Permite listar, filtrar, crear y visualizar ejercicios con soporte multimedia.
 */

package com.example.tfg.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.SessionManager
import com.example.tfg.ui.components.VideoPlayerDialog
import com.example.tfg.viewmodel.ExerciseViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Enumeración que define los diferentes criterios de ordenación y filtrado aplicables a la lista de ejercicios.
 */
enum class SortOption(val label: String) {
    ALPHA_ZA("Alfabéticamente (Z → A)"),
    ALPHA_AZ("Alfabéticamente (A → Z)"),
    ZONE("Zona"),
    DURATION_DESC("Duración (Más → Menos)"),
    DURATION_ASC("Duración (Menos → Más)"),
    MY_EXERCISES("Mis Ejercicios")
}

/**
 * Pantalla principal de la biblioteca de ejercicios donde los terapeutas pueden gestionar el catálogo de actividades de rehabilitación.
 * Incluye funcionalidades para subir vídeos de demostración y organizar los ejercicios por categorías.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token = SessionManager.getToken(context) ?: ""
    val currentUserId = SessionManager.getUserId(context)

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var ejercicioABorrar by remember { mutableStateOf<Ejercicio?>(null) }
    var ejercicioParaVerDetalle by remember { mutableStateOf<Ejercicio?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var urlVideoReproducir by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.ALPHA_AZ) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    // Lanzador para seleccionar videos de la galería del dispositivo
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                val internalPath = viewModel.copyVideoToInternalStorage(context, selectedUri)
                if (internalPath != null) {
                    viewModel.videoUrl = internalPath
                    Toast.makeText(context, "Vídeo guardado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al guardar el vídeo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    // Lógica de ordenamiento reactiva basada en la opción seleccionada
    val ejerciciosFiltrados = remember(viewModel.listaEjercicios, sortOption) {
        val list = viewModel.listaEjercicios
        when (sortOption) {
            SortOption.ALPHA_AZ -> list.sortedBy { it.nombre?.lowercase() }
            SortOption.ALPHA_ZA -> list.sortedByDescending { it.nombre?.lowercase() }
            SortOption.ZONE -> list.sortedBy { it.zona }
            SortOption.DURATION_ASC -> list.sortedBy { it.duracion }
            SortOption.DURATION_DESC -> list.sortedByDescending { it.duracion }
            SortOption.MY_EXERCISES -> list.filter { it.idTerapeuta == currentUserId }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarEjercicios(token)
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.limpiarFormulario(); showDialog = true },
                containerColor = TherapistDarkColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo Ejercicio") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Cabecera estilizada
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
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BIBLIOTECA DE EJERCICIOS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de filtrado y ordenamiento
            if (!viewModel.isLoading) {
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
                    val filterLabel = if (sortOption == SortOption.MY_EXERCISES) "Filtrar: " else "Orden: "
                    Text(
                        text = "$filterLabel${sortOption.label}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Box {
                        Icon(
                            imageVector = if (sortOption == SortOption.MY_EXERCISES) Icons.Default.FilterList else Icons.Default.Sort,
                            contentDescription = "Ordenar o Filtrar",
                            tint = TherapistDarkColor
                        )

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            SortOption.values().forEach { option ->
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

            // Lista de contenido
            if (viewModel.isLoading && !showDialog) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TherapistDarkColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ejerciciosFiltrados) { ejercicio ->
                        ExerciseCard(
                            ejercicio = ejercicio,
                            isMyExercise = ejercicio.idTerapeuta == currentUserId,
                            titleColor = TextPrimary,
                            subtitleColor = TextSecondary,
                            accentColor = TherapistDarkColor,
                            onCardClick = { ejercicioParaVerDetalle = ejercicio },
                            onPlayClick = {
                                if (!ejercicio.videoUrl.isNullOrBlank()) {
                                    val url = ejercicio.videoUrl
                                    urlVideoReproducir = if (url!!.startsWith("/")) "file://$url" else url
                                }
                            },
                            onEditClick = { viewModel.prepararEdicion(ejercicio); showDialog = true },
                            onDeleteClick = { ejercicioABorrar = ejercicio; showDeleteConfirmDialog = true }
                        )
                    }
                    if (ejerciciosFiltrados.isEmpty()) {
                        item {
                            Text(
                                text = if (sortOption == SortOption.MY_EXERCISES) "No has creado ningún ejercicio aún." else "No se encontraron ejercicios.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogos modales
    if (ejercicioParaVerDetalle != null) {
        val ej = ejercicioParaVerDetalle!!
        AlertDialog(
            onDismissRequest = { ejercicioParaVerDetalle = null },
            icon = { Icon(Icons.Default.Info, null, tint = TherapistDarkColor) },
            title = { Text(ej.nombre ?: "Detalle", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailRow("Zona", ej.zona ?: "General", TextPrimary, TextSecondary)
                    DetailRow("Duración", "${ej.duracion ?: 0} min", TextPrimary, TextSecondary)

                    if (ej.idTerapeuta == currentUserId) {
                        Text(
                            text = "Creado por mí",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TherapistDarkColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Descripción:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(ej.descripcion ?: "Sin descripción", fontSize = 14.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!ej.videoUrl.isNullOrBlank()) {
                        Button(
                            onClick = {
                                val url = ej.videoUrl
                                urlVideoReproducir = if (url!!.startsWith("/")) "file://$url" else url
                            },
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
            confirmButton = { TextButton(onClick = { ejercicioParaVerDetalle = null }) { Text("Cerrar", color = TherapistDarkColor) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteConfirmDialog && ejercicioABorrar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F)) },
            title = { Text("Eliminar Ejercicio", color = TextPrimary) },
            text = { Text("¿Estás seguro de que quieres borrar '${ejercicioABorrar!!.nombre}'? Esta acción no se puede deshacer.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarEjercicio(token, ejercicioABorrar!!.id); showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancelar", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialog) {
        val isEditing = viewModel.idEjercicioAEditar != null
        AlertDialog(
            onDismissRequest = { showDialog = false },
            modifier = Modifier.padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            tonalElevation = 8.dp,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = TherapistDarkColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isEditing) "EDITAR EJERCICIO" else "CREAR EJERCICIO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TherapistDarkColor,
                        textAlign = TextAlign.Center
                    )
                    Divider(modifier = Modifier.padding(top = 16.dp), color = DividerColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.nombre = it },
                        label = { Text("Nombre del ejercicio") },
                        leadingIcon = { Icon(Icons.Default.ShortText, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.zona,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Zona Afectada") },
                            leadingIcon = { Icon(Icons.Default.AccessibilityNew, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            viewModel.zonasDisponibles.forEach { z ->
                                DropdownMenuItem(
                                    text = { Text(z, color = TextPrimary) },
                                    onClick = { viewModel.zona = z; isDropdownExpanded = false },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.duracion,
                        onValueChange = { if(it.all{c->c.isDigit()}) viewModel.duracion=it },
                        label = { Text("Duración est. (min)") },
                        leadingIcon = { Icon(Icons.Default.Timer, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TherapistLightColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VideoLibrary, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Seleccionar Vídeo de Galería")
                        }

                        if (viewModel.videoUrl.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Vídeo adjuntado correctamente",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.descripcion,
                        onValueChange = { viewModel.descripcion = it },
                        label = { Text("Descripción / Instrucciones") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.guardar(token) { showDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = TherapistDarkColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Actualizar" else "Guardar Ejercicio")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
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
 * Componente de tarjeta que muestra la información resumida de un ejercicio en la lista.
 * Incluye controles de edición y borrado si el ejercicio pertenece al usuario actual.
 */
@Composable
private fun ExerciseCard(
    ejercicio: Ejercicio,
    isMyExercise: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val iconBgColor = if (isMyExercise) Color(0xFF1565C0) else Color(0xFF546E7A)

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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre ?: "Sin nombre",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoChip(ejercicio.zona ?: "General", Color(0xFFE0F7FA), Color(0xFF006064))
                    Spacer(modifier = Modifier.width(4.dp))
                    InfoChip("${ejercicio.duracion ?: 0} min", Color(0xFFFFF3E0), Color(0xFFE65100))
                }

                if (isMyExercise) {
                    Text(
                        text = "Mío",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!ejercicio.videoUrl.isNullOrBlank()) {
                    IconButton(onClick = onPlayClick) {
                        Icon(Icons.Default.PlayCircle, null, tint = Color(0xFFD32F2F))
                    }
                }

                if (isMyExercise) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = subtitleColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

/**
 * Chip informativo pequeño para mostrar metadatos del ejercicio como la zona corporal o duración.
 */
@Composable
private fun InfoChip(text: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * Fila auxiliar para mostrar pares de clave-valor en el diálogo de detalles.
 */
@Composable
private fun DetailRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = labelColor)
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}