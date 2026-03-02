/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla de inicio de sesión. Gestiona la autenticación de usuarios mediante credenciales y feedback visual de estado.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.viewmodel.LoginViewModel

/**
 * Pantalla de acceso principal que permite a los usuarios autenticarse en el sistema introduciendo su DNI y contraseña.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val CorporateBlue = Color(0xFF1565C0)
    val BackgroundColor = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFF37474F)
    val IconInactive = Color(0xFF546E7A)
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CorporateBlue,
        unfocusedBorderColor = IconInactive,
        focusedLabelColor = CorporateBlue,
        unfocusedLabelColor = TextSecondary,
        cursorColor = CorporateBlue,
        focusedLeadingIconColor = CorporateBlue,
        unfocusedLeadingIconColor = IconInactive
    )

    Scaffold(
        containerColor = BackgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CorporateBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = CorporateBlue,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TFG ANDRÉS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CorporateBlue,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Acceso a la plataforma",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = viewModel.dni,
                            onValueChange = { viewModel.dni = it },
                            label = { Text("DNI (Sin letra)") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.login(context, onLoginSuccess) },
                            enabled = !viewModel.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "INICIAR SESIÓN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                if (viewModel.loginStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    val isError = viewModel.loginStatus.contains("Error", ignoreCase = true) ||
                            viewModel.loginStatus.contains("incorrectos", ignoreCase = true)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = viewModel.loginStatus,
                            color = if (isError) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}