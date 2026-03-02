/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Pantalla que guía al paciente durante la realización de los ejercicios. Gestiona la cámara, grabación, temporizadores y flujo de la sesión.
 */

package com.example.tfg.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.model.SessionManager
import com.example.tfg.ui.components.CameraPreview
import com.example.tfg.ui.components.VideoPlayerDialog
import com.example.tfg.ui.components.rememberCameraController
import com.example.tfg.viewmodel.FaseSesion
import com.example.tfg.viewmodel.PatientSessionViewModel
import kotlinx.coroutines.delay
import java.io.File

/**
 * Interfaz principal de ejecución de la sesión.
 * Administra el ciclo de vida de la captura de vídeo, permisos de cámara y los diferentes estados de la interfaz según el progreso del ejercicio.
 */
@Composable
fun PatientSessionScreen(
    idSesion: Int,
    viewModel: PatientSessionViewModel = viewModel(),
    onSessionFinished: () -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    var urlVideoReproducir by remember { mutableStateOf<String?>(null) }

    // Inicialización del controlador de cámara
    val cameraController = rememberCameraController(context)

    LaunchedEffect(cameraController) {
        cameraController.videoCaptureQualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.HD, Quality.SD, Quality.LOWEST)
        )
    }

    var recording: Recording? by remember { mutableStateOf(null) }

    // Gestión de permisos de cámara y audio
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            hasCameraPermission = perms[Manifest.permission.CAMERA] == true && perms[Manifest.permission.RECORD_AUDIO] == true
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    LaunchedEffect(idSesion) {
        if (token.isNotEmpty()) viewModel.cargarSesion(token, idSesion)
    }

    val ejercicio = viewModel.getEjercicioActual()

    // Lógica de grabación basada en el estado del ViewModel
    LaunchedEffect(viewModel.faseActual) {
        if (viewModel.faseActual == FaseSesion.GRABANDO) {
            if (hasCameraPermission) {
                val videoFile = File(context.filesDir, "VID_${System.currentTimeMillis()}.mp4")
                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                recording = cameraController.startRecording(
                    outputOptions,
                    AudioConfig.create(true),
                    ContextCompat.getMainExecutor(context)
                ) { event ->
                    if (event is VideoRecordEvent.Finalize) {
                        val archivoExisteYTieneDatos = videoFile.exists() && videoFile.length() > 0
                        val esErrorIgnorable = event.error == VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE

                        if (!event.hasError() || (esErrorIgnorable && archivoExisteYTieneDatos)) {
                            viewModel.onVideoRecorded(videoFile.absolutePath)
                        } else {
                            recording?.close()
                            recording = null
                            val errorMsg = "Error crítico (${event.error}): ${event.cause?.message}"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            android.util.Log.e("CAMARA_ERROR", errorMsg)
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Faltan permisos de cámara", Toast.LENGTH_LONG).show()
            }
        } else {
            try {
                recording?.stop()
            } catch (e: Exception) {
                // Ignorar errores al detener si ya estaba detenido
            }
            recording = null
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (viewModel.faseActual == FaseSesion.GRABANDO || viewModel.faseActual == FaseSesion.CUENTA_ATRAS) 0.dp else 16.dp)
                .padding(if (viewModel.faseActual != FaseSesion.GRABANDO && viewModel.faseActual != FaseSesion.CUENTA_ATRAS) padding else PaddingValues(0.dp)),
            contentAlignment = Alignment.Center
        ) {

            // Vista previa de cámara de fondo para fases activas
            if (viewModel.faseActual == FaseSesion.CUENTA_ATRAS ||
                viewModel.faseActual == FaseSesion.GRABANDO ||
                viewModel.faseActual == FaseSesion.REVIEW_EJERCICIO) {

                if (hasCameraPermission) {
                    CameraPreview(
                        controller = cameraController,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        Text("Sin permisos de cámara", color = Color.White)
                    }
                }
            }

            // Gestión de la interfaz según la fase actual de la sesión
            when (viewModel.faseActual) {

                FaseSesion.CARGANDO -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando tu sesión...")
                    }
                }

                FaseSesion.GUARDANDO -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32))
                        Spacer(Modifier.height(16.dp))
                        Text("Guardando tus progresos...", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                FaseSesion.PREPARACION -> {
                    if (ejercicio != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            LinearProgressIndicator(
                                progress = (viewModel.indiceEjercicioActual + 1) / viewModel.listaEjercicios.size.toFloat(),
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = Color(0xFF1565C0),
                                trackColor = Color.LightGray
                            )
                            Text(
                                "Ejercicio ${viewModel.indiceEjercicioActual + 1} de ${viewModel.listaEjercicios.size}",
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = ejercicio.nombre,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 38.sp,
                                color = Color(0xFF1565C0)
                            )

                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Duración: ${formatTiempo(ejercicio.duracionSegundos)}",
                                        color = Color(0xFF1565C0),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (ejercicio.videoUrl.isNullOrEmpty()) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                                            Text("Sin vídeo de ejemplo", color = Color.Gray)
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                if (!ejercicio.videoUrl.isNullOrBlank()) {
                                                    urlVideoReproducir = ejercicio.videoUrl
                                                }
                                            },
                                            modifier = Modifier.size(80.dp)
                                        ) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = "Reproducir", tint = Color.White, modifier = Modifier.fillMaxSize())
                                        }
                                        Text("Ver Ejemplo", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
                                ) {
                                    Text("Instrucciones:", fontWeight = FontWeight.Bold, color = Color.Black)
                                    Spacer(Modifier.height(4.dp))
                                    Text(ejercicio.descripcion, fontSize = 16.sp, color = Color.DarkGray)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.iniciarSecuenciaGrabacion() },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("¡ESTOY LISTO! GRABAR", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                FaseSesion.CUENTA_ATRAS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PREPÁRATE",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(40.dp))

                            Text(
                                text = "${viewModel.conteoRegresivo}",
                                fontSize = 140.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Colócate en posición...",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                FaseSesion.GRABANDO -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 60.dp)
                                .background(
                                    color = Color.Red.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(Modifier.width(16.dp))

                                Text(
                                    text = formatTiempo(viewModel.segundosRestantes),
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = if (viewModel.segundosRestantes < 5)
                                        LocalTextStyle.current.copy(color = Color.Red)
                                    else LocalTextStyle.current
                                )
                            }
                        }

                        Button(
                            onClick = {
                                recording?.stop()
                                recording = null
                                viewModel.finalizarGrabacionAnticipada()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                        ) {
                            Text("Detener", color = Color.White)
                        }
                    }
                }

                FaseSesion.REVIEW_EJERCICIO -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ejercicio Finalizado",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(60.dp))

                        Button(
                            onClick = { viewModel.confirmarYAvanzar() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text(
                                "CONTINUAR",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.repetirEjercicioActual() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF6C00)
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text(
                                "REPETIR",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                FaseSesion.RESUMEN_FINAL -> {
                    LaunchedEffect(Unit) {
                        delay(5000)
                        onSessionFinished()
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (viewModel.errorMessage.isNotEmpty()) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Red)
                            Spacer(Modifier.height(16.dp))
                            Text("Algo salió mal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(viewModel.errorMessage, color = Color.Gray, textAlign = TextAlign.Center)
                        } else {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color(0xFFFFC107))
                            Spacer(Modifier.height(24.dp))
                            Text("¡Sesión Completada!", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF1565C0))
                            Spacer(Modifier.height(8.dp))
                            Text("Tus ejercicios se han guardado correctamente.", fontSize = 18.sp, color = Color.Gray)

                            Spacer(Modifier.height(24.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.LightGray,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Volviendo al inicio...", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }

    if (urlVideoReproducir != null) {
        VideoPlayerDialog(
            videoUrl = urlVideoReproducir!!,
            autoClose = true,
            onDismiss = { urlVideoReproducir = null }
        )
    }
}

/**
 * Convierte una duración en segundos a una cadena con formato cronómetro.
 */
fun formatTiempo(segundos: Int): String {
    val min = segundos / 60
    val sec = segundos % 60
    return "%02d:%02d".format(min, sec)
}