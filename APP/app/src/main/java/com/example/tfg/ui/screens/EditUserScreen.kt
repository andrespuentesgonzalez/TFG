/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de formulario para la modificación de datos de usuarios registrados en el sistema.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.viewmodel.EditUserViewModel

/**
 * Interfaz gráfica que permite al administrador actualizar la información personal y los permisos de acceso de un usuario específico.
 */
@Composable
fun EditUserScreen(
    userId: Int,
    viewModel: EditUserViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val AdminDarkColor = Color(0xFF263238)
    val AdminAccentColor = Color(0xFF37474F)
    val BackgroundColor = Color(0xFFF5F7FA)

    LaunchedEffect(Unit) {
        viewModel.cargarDatosUsuario(token, userId)
    }

    Scaffold(
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ADMINISTRACIÓN",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Editar Usuario",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Modificar Datos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminDarkColor,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AdminDarkColor)
                        }
                    } else {
                        AdminInputTextEdit(
                            value = viewModel.dni,
                            onValueChange = { viewModel.dni = it },
                            label = "DNI",
                            icon = Icons.Default.Badge,
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AdminInputTextEdit(
                            value = viewModel.correo,
                            onValueChange = { viewModel.correo = it },
                            label = "Correo Electrónico",
                            icon = Icons.Default.Email
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminInputTextEdit(
                                    value = viewModel.nombre,
                                    onValueChange = { viewModel.nombre = it },
                                    label = "Nombre",
                                    icon = Icons.Default.Person
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                AdminInputTextEdit(
                                    value = viewModel.apellidos,
                                    onValueChange = { viewModel.apellidos = it },
                                    label = "Apellidos",
                                    icon = Icons.Default.PersonOutline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Tipo de Perfil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OpcionRolEditar("paciente", viewModel, Icons.Default.SentimentSatisfiedAlt)
                            OpcionRolEditar("terapeuta", viewModel, Icons.Default.MedicalServices)
                            OpcionRolEditar("admin", viewModel, Icons.Default.AdminPanelSettings)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.guardarCambios(token, userId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AdminDarkColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (viewModel.statusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.statusMessage,
                            color = if (viewModel.updateSuccess) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = null, tint = AdminDarkColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar", color = AdminDarkColor)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


/**
 * Campo de texto personalizado con estilo visual coherente para los formularios de gestión.
 */
@Composable
fun AdminInputTextEdit(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF263238),
            focusedLabelColor = Color(0xFF263238),
            cursorColor = Color(0xFF263238)
        ),
        singleLine = true
    )
}

/**
 * Botón de selección única para definir el tipo de perfil o rol que tendrá el usuario en la aplicación.
 */
@Composable
fun OpcionRolEditar(
    rolNombre: String,
    viewModel: EditUserViewModel,
    icon: ImageVector
) {
    val isSelected = (viewModel.rol == rolNombre)
    val color = if (isSelected) Color(0xFF263238) else Color.Gray

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RadioButton(
            selected = isSelected,
            onClick = { viewModel.rol = rolNombre },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF263238))
        )
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(
            rolNombre.uppercase(),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}