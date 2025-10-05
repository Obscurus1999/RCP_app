package com.example.rcp

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RcpAnimationFinalCorrected()
        }
    }
}

@Composable
fun RcpAnimationFinalCorrected() {
    val circleRadius = 30f
    val centerRadius = 40f
    val rhythmBpm = 110f           // ritmo clínico recomendado
    val flashDuration = 150L
    val context = LocalContext.current

    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.beep) }
    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    var circles by remember { mutableStateOf(listOf<Pair<Float, Boolean>>()) } // x, triggered
    var centralFlash by remember { mutableStateOf(false) }
    var canvasWidth by remember { mutableStateOf(0f) }
    var timeSinceLastCircle by remember { mutableStateOf(0f) }

    val centralColor by animateColorAsState(
        targetValue = if (centralFlash) Color(0xFFFF8800) else Color.White
    )

    LaunchedEffect(canvasWidth) {
        if (canvasWidth <= 0f) return@LaunchedEffect

        val fps = 60
        val deltaTime = 1000f / fps
        val intervalMs = 60_000f / rhythmBpm
        val centerX = canvasWidth / 2f
        val startX = -circleRadius * 2f
        val distanceToCenter = centerX - startX
        val framesPerInterval = intervalMs / deltaTime
        val speedPerFrame = distanceToCenter / framesPerInterval

        while (true) {
            timeSinceLastCircle += deltaTime

            // Generar un nuevo círculo solo si pasó el intervalo de BPM
            if (timeSinceLastCircle >= intervalMs) {
                circles = circles + Pair(startX, false)
                timeSinceLastCircle = 0f
            }

            // Mover círculos
            circles = circles.map { it.copy(first = it.first + speedPerFrame) }

            // Detectar cruce central y activar sonido + flash
            circles = circles.map { (x, triggered) ->
                if (!triggered && x >= centerX) {
                    try { mediaPlayer?.start() } catch (_: Exception) {}
                    centralFlash = true
                    kotlinx.coroutines.delay(flashDuration)
                    centralFlash = false
                    Pair(x, true)
                } else Pair(x, triggered)
            }

            // Limpiar fuera de pantalla
            circles = circles.filter { it.first < canvasWidth + circleRadius * 2 }

            kotlinx.coroutines.delay(deltaTime.toLong())
        }
    }

    // --- Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
        canvasWidth = size.width
        val centerY = size.height / 2f

        // Fondo blanco
        drawRect(color = Color.White, size = size)

        // Línea horizontal + flecha
        val lineEnd = Offset(size.width - 20f, centerY)
        drawLine(Color.Gray, Offset(0f, centerY), lineEnd, strokeWidth = 6f)
        val arrowSize = 20f
        drawPath(Path().apply {
            moveTo(size.width, centerY)
            lineTo(size.width - arrowSize, centerY - arrowSize / 2)
            lineTo(size.width - arrowSize, centerY + arrowSize / 2)
            close()
        }, color = Color.Gray)

        // Círculo central con borde y parpadeo
        drawCircle(Color.Black, centerRadius, Offset(canvasWidth/2f, centerY), style = Stroke(4f))
        drawCircle(centralColor, centerRadius - 2f, Offset(canvasWidth/2f, centerY))

        // Círculos móviles
        circles.forEach { (x, _) ->
            drawCircle(Color.Black, circleRadius, Offset(x, centerY), style = Stroke(3f))
            drawCircle(Color.Red, circleRadius - 2f, Offset(x, centerY))
        }
    }
}













