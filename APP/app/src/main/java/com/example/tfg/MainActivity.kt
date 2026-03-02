/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Punto de entrada de la aplicación. Configura la navegación global y define el grafo de pantallas para los diferentes roles de usuario.
 */

package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tfg.model.SessionManager
import com.example.tfg.ui.screens.* import com.example.tfg.ui.theme.TFGTheme

/**
 * Actividad principal que inicializa el contenedor de la interfaz de usuario y establece el tema visual de la aplicación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TFGTheme {
                AppNavigation()
            }
        }
    }
}

/**
 * Gestor de navegación central que define todas las rutas accesibles y la lógica de redirección según el rol del usuario autenticado.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "login") {

        // ==========================================
        //                 LOGIN
        // ==========================================

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    val rol = SessionManager.getRol(context)
                    val destino = when (rol) {
                        "admin" -> "admin_home"
                        "terapeuta" -> "therapist_home"
                        else -> "patient_home"
                    }
                    navController.navigate(destino) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        //               ADMINISTRADOR
        // ==========================================

        composable("admin_home") {
            AdminScreen(
                navController = navController,
                onLogout = {
                    SessionManager.clearSession(context)
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("admin_users_management") {
            UserManagementScreen(
                onCreateUser = { navController.navigate("admin_create_user") },
                onListUsers = { navController.navigate("admin_list_users") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("admin_create_user") {
            CreateUserScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("admin_list_users") {
            UserListScreen(
                onBack = { navController.popBackStack() },
                onEditUser = { userId ->
                    navController.navigate("admin_edit_user/$userId")
                }
            )
        }


        composable("admin_edit_user/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            if (userId != null) {
                EditUserScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        //                 TERAPEUTA
        // ==========================================

        composable("therapist_home") {
            TherapistScreen(
                navController = navController,
                onLogout = {
                    SessionManager.clearSession(context)
                    navController.navigate("login") { popUpTo(0) }
                }
            )
        }

        composable("therapist_exercises") {
            ExerciseScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("therapist_patients") {
            MyPatientsScreen(
                onBack = { navController.popBackStack() },
                onPatientClick = { pacienteId ->
                    navController.navigate("therapist_patient_history/$pacienteId")
                }
            )
        }

        composable(
            "therapist_patient_history/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
            PatientHistoryScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("therapist_routines") {
            RutinaScreen(
                onBack = { navController.popBackStack() },
                onRutinaClick = { rutinaId ->
                    navController.navigate("therapist_routine_detail/$rutinaId")
                }
            )
        }

        composable("therapist_routine_detail/{rutinaId}") { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getString("rutinaId")?.toIntOrNull()
            if (rutinaId != null) {
                RutinaDetailScreen(
                    rutinaId = rutinaId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("therapist_sessions") {
            SesionesScreen(
                onNavigateToCalendar = {
                    navController.navigate("therapist_sessions_calendar")
                },
                onNavigateToDetail = { sesionId ->
                    navController.navigate("therapist_session_detail/$sesionId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("therapist_sessions_calendar") {
            CalendarioScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("therapist_session_detail/{sesionId}") { backStackEntry ->
            val sesionId = backStackEntry.arguments?.getString("sesionId")?.toIntOrNull()
            if (sesionId != null) {
                DetalleSesionScreen(
                    sesionId = sesionId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        //                 PACIENTE
        // ==========================================

        composable("patient_home") {
            PatientHomeScreen(
                onNavigateToHistory = {
                    navController.navigate("patient_history")
                },
                onJoinSession = { sesionId ->
                    navController.navigate("session_active/$sesionId")
                }
            )
        }

        composable("patient_history") {
            val myId = SessionManager.getUserId(context)

            PatientHistoryScreen(
                patientId = myId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "session_active/{idSesion}",
            arguments = listOf(navArgument("idSesion") { type = NavType.IntType })
        ) { backStackEntry ->
            val idSesion = backStackEntry.arguments?.getInt("idSesion") ?: 0

            PatientSessionScreen(
                idSesion = idSesion,
                onSessionFinished = {
                    navController.navigate("patient_home") {
                        popUpTo("patient_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_stats") { PantallaConstruccion("ESTADÍSTICAS", navController) }

    }
}

/**
 * Pantalla auxiliar utilizada como marcador de posición para funcionalidades que se encuentran en fase de desarrollo.
 */
@Composable
fun PantallaConstruccion(titulo: String, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = titulo, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}