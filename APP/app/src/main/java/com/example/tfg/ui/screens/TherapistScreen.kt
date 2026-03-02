/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla principal del terapeuta que centraliza el acceso a la gestión de ejercicios, rutinas, sesiones y seguimiento de pacientes.
 */

package com.example.tfg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Panel de control principal para el perfil de terapeuta que facilita la navegación hacia las herramientas de administración de tratamientos y evaluación de pacientes.
 */
@Composable
fun TherapistScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val TherapistDarkColor = Color(0xFF1565C0)
    val TherapistLightColor = Color(0xFF42A5F5)
    val BackgroundColor = Color(0xFFF5F7FA)

    Scaffold(
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TherapistDarkColor, TherapistLightColor)
                        ),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bienvenido, Terapeuta",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Panel de Gestión",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(
                    title = "Gestión de Ejercicios",
                    subtitle = "Crear y editar ejercicios",
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFF5E35B1), // Morado suave
                    onClick = { navController.navigate("therapist_exercises") }
                )

                MenuButton(
                    title = "Gestión de Rutinas",
                    subtitle = "Organizar planes de trabajo",
                    icon = Icons.Default.ListAlt,
                    color = Color(0xFF00897B), // Verde azulado
                    onClick = { navController.navigate("therapist_routines") }
                )

                MenuButton(
                    title = "Sesiones y Evaluación",
                    subtitle = "Calendario y seguimiento",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFFE65100), // Naranja quemado
                    onClick = { navController.navigate("therapist_sessions") }
                )

                MenuButton(
                    title = "Mis Pacientes",
                    subtitle = "Listado y progreso individual",
                    icon = Icons.Default.Groups,
                    color = Color(0xFF1565C0), // Azul corporativo
                    onClick = { navController.navigate("therapist_patients") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Elemento gráfico de interfaz que estructura las opciones de navegación del menú principal mostrando título y subtítulo junto a un icono representativo.
 */
@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
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
                    color = Color(0xFF37474F)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
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