/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Componentes de interfaz para la integración de la cámara utilizando CameraX.
 */

package com.example.tfg.ui.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable que renderiza la vista previa de la cámara en la interfaz de usuario.
 */
@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

/**
 * Función auxiliar para inicializar y recordar el estado del controlador de la cámara.
 */
@Composable
fun rememberCameraController(context: Context): LifecycleCameraController {
    return remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                androidx.camera.view.CameraController.IMAGE_CAPTURE or
                        androidx.camera.view.CameraController.VIDEO_CAPTURE
            )
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
}