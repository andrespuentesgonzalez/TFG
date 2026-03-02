/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de administración que lista todos los usuarios registrados y permite gestionar sus roles y asignaciones.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.model.Usuario
import com.example.tfg.viewmodel.UserListViewModel

/**
 * Interfaz de listado de usuarios que permite al administrador buscar, filtrar y gestionar las cuentas registradas en el sistema.
 */
@Composable
fun UserListScreen(
    viewModel: UserListViewModel = viewModel(),
    onBack: () -> Unit,
    onEditUser: (Int) -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    var usuarioABorrar by remember { mutableStateOf<Usuario?>(null) }
    var terapeutaParaAsignar by remember { mutableStateOf<Usuario?>(null) }
    var pacienteParaReasignar by remember { mutableStateOf<Usuario?>(null) }
    val AdminDarkColor = Color(0xFF263238)
    val AdminAccentColor = Color(0xFF37474F)
    val BackgroundColor = Color(0xFFF5F7FA)

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios(token)
    }

    Scaffold(
        containerColor = BackgroundColor,
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
                            colors = listOf(AdminDarkColor, AdminAccentColor)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PeopleAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ADMINISTRACIÓN", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Listado de Usuarios", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip("Todos", "todos", viewModel, AdminDarkColor)
                FiltroChip("Pacientes", "paciente", viewModel, AdminDarkColor)
                FiltroChip("Terapeutas", "terapeuta", viewModel, AdminDarkColor)
                FiltroChip("Admins", "admin", viewModel, AdminDarkColor)
                FiltroChip("⚠️ Sin Asignar", "asignaciones", viewModel, Color(0xFFD32F2F))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AdminDarkColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(viewModel.usuariosVisibles) { usuario ->
                        val nombreTerapeuta = if (usuario.rol == "paciente")
                            viewModel.obtenerNombreTerapeuta(usuario.idTerapeuta)
                        else ""

                        UsuarioCardModern(
                            usuario = usuario,
                            nombreTerapeutaDisplay = nombreTerapeuta,
                            onDeleteClick = { usuarioABorrar = usuario },
                            onEditClick = { onEditUser(usuario.id) },
                            onAssignClick = { terapeutaParaAsignar = usuario },
                            onChangeTherapistClick = { pacienteParaReasignar = usuario }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = AdminDarkColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver al Panel", color = AdminDarkColor)
                }
            }
        }
    }

    if (usuarioABorrar != null) {
        AlertDialog(
            onDismissRequest = { usuarioABorrar = null },
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Red) },
            title = { Text("¿Eliminar usuario?") },
            text = { Text("Estás a punto de eliminar a ${usuarioABorrar?.nombre}. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    onClick = { viewModel.eliminarUsuario(token, usuarioABorrar!!); usuarioABorrar = null }
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = { TextButton(onClick = { usuarioABorrar = null }) { Text("Cancelar") } },
            containerColor = Color.White
        )
    }

    if (pacienteParaReasignar != null) {
        val listaTerapeutas = viewModel.obtenerTerapeutas()

        AlertDialog(
            onDismissRequest = { pacienteParaReasignar = null },
            icon = { Icon(Icons.Default.SwitchAccount, null, tint = Color(0xFF1976D2)) },
            title = { Text("Cambiar Terapeuta") },
            text = {
                Column {
                    Text("Selecciona el nuevo terapeuta para ${pacienteParaReasignar!!.nombre}:", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

                    if (listaTerapeutas.isEmpty()) {
                        Text("No hay terapeutas disponibles.", color = Color.Red, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(listaTerapeutas) { terapeuta ->
                                if (terapeuta.id != pacienteParaReasignar!!.idTerapeuta) {
                                    ListItem(
                                        headlineContent = { Text("${terapeuta.nombre} ${terapeuta.apellidos}", fontWeight = FontWeight.SemiBold) },
                                        supportingContent = { Text("DNI: ${terapeuta.dni}", fontSize = 12.sp) },
                                        leadingContent = { Icon(Icons.Default.Person, null, tint = Color(0xFF1976D2)) },
                                        modifier = Modifier.clickable {
                                            viewModel.asignarTerapeuta(token, pacienteParaReasignar!!.id, terapeuta.id)
                                            pacienteParaReasignar = null
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pacienteParaReasignar = null }) { Text("Cancelar") } },
            containerColor = Color.White
        )
    }

    if (terapeutaParaAsignar != null) {
        val pacientesLibres = viewModel.obtenerPacientesSinAsignar()
        AlertDialog(
            onDismissRequest = { terapeutaParaAsignar = null },
            title = { Text("Asignar Paciente a ${terapeutaParaAsignar?.nombre}") },
            text = {
                if (pacientesLibres.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No hay pacientes libres", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(pacientesLibres) { paciente ->
                            ListItem(
                                headlineContent = { Text("${paciente.nombre} ${paciente.apellidos}") },
                                modifier = Modifier.clickable {
                                    viewModel.asignarTerapeuta(token, paciente.id, terapeutaParaAsignar!!.id)
                                    terapeutaParaAsignar = null
                                }
                            )
                            Divider()
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { terapeutaParaAsignar = null }) { Text("Cerrar") } },
            containerColor = Color.White
        )
    }
}

/**
 * Elemento de interfaz seleccionable utilizado para aplicar filtros por categoría a la lista de usuarios mostrada.
 */
@Composable
fun FiltroChip(texto: String, valorFiltro: String, viewModel: UserListViewModel, activeColor: Color) {
    val isSelected = viewModel.filtroActual == valorFiltro
    val bgColor = if (isSelected) activeColor else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) activeColor else Color.LightGray

    Surface(
        onClick = { viewModel.aplicarFiltro(valorFiltro) },
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

/**
 * Componente de tarjeta que muestra la información detallada de un usuario incluyendo controles para editar, borrar o gestionar asignaciones.
 */
@Composable
fun UsuarioCardModern(
    usuario: Usuario,
    nombreTerapeutaDisplay: String,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onAssignClick: () -> Unit,
    onChangeTherapistClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECEFF1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF455A64)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${usuario.nombre} ${usuario.apellidos}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    val (rolColor, rolBg) = when(usuario.rol) {
                        "admin" -> Pair(Color(0xFFD32F2F), Color(0xFFFFEBEE))
                        "terapeuta" -> Pair(Color(0xFF1976D2), Color(0xFFE3F2FD))
                        else -> Pair(Color(0xFF388E3C), Color(0xFFE8F5E9))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = rolBg, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = usuario.rol.uppercase(),
                                color = rolColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("DNI: ${usuario.dni}", fontSize = 12.sp, color = Color.Gray)
                    }


                    if (usuario.rol == "paciente") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalServices, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))

                            if (usuario.idTerapeuta != null) {
                                Text(
                                    text = nombreTerapeutaDisplay,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = onChangeTherapistClick,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwitchAccount,
                                        contentDescription = "Cambiar Terapeuta",
                                        tint = Color(0xFFEF6C00)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Sin terapeuta asignado",
                                    fontSize = 11.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (usuario.rol == "terapeuta") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onAssignClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7B1FA2)),
                    border = BorderStroke(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.PersonAddAlt1, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Asignar Paciente", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}