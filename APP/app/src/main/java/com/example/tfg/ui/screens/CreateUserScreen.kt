/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de formulario para el registro de nuevos usuarios en el sistema.
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.viewmodel.CreateUserViewModel

/**
 * Interfaz de formulario diseñada para que el administrador registre nuevos usuarios asignando roles específicos como paciente, terapeuta o administrador.
 */
@Composable
fun CreateUserScreen(
    viewModel: CreateUserViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val AdminDarkColor = Color(0xFF263238)
    val AdminAccentColor = Color(0xFF37474F)
    val BackgroundColor = Color(0xFFF5F7FA)

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
                        imageVector = Icons.Default.PersonAdd,
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
                        text = "Crear Nuevo Usuario",
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
                        "Datos del Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminDarkColor,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    AdminInputText(
                        value = viewModel.dni,
                        onValueChange = { viewModel.dni = it },
                        label = "DNI (Solo números)",
                        icon = Icons.Default.Badge,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AdminInputText(
                        value = viewModel.correo,
                        onValueChange = { viewModel.correo = it },
                        label = "Correo Electrónico",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdminInputText(
                                value = viewModel.nombre,
                                onValueChange = { viewModel.nombre = it },
                                label = "Nombre",
                                icon = Icons.Default.Person
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            AdminInputText(
                                value = viewModel.apellidos,
                                onValueChange = { viewModel.apellidos = it },
                                label = "Apellidos",
                                icon = Icons.Default.PersonOutline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AdminInputText(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = "Contraseña",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OpcionRol("paciente", viewModel, Icons.Default.SentimentSatisfiedAlt)
                        OpcionRol("terapeuta", viewModel, Icons.Default.MedicalServices)
                        OpcionRol("admin", viewModel, Icons.Default.AdminPanelSettings)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = AdminDarkColor)
                    } else {
                        Button(
                            onClick = { viewModel.crearUsuario(token) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AdminDarkColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("GUARDAR USUARIO", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (viewModel.statusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.statusMessage,
                            color = if (viewModel.userCreatedSuccess) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = null, tint = AdminDarkColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar Operación", color = AdminDarkColor)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


/**
 * Componente de entrada de texto estilizado con iconos y validación de tipo de teclado para el panel de administración.
 */
@Composable
fun AdminInputText(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
 * Elemento de selección única que permite definir el rol del nuevo usuario mediante un botón de radio e icono representativo.
 */
@Composable
fun OpcionRol(
    rolNombre: String,
    viewModel: CreateUserViewModel,
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