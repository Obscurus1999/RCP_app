package com.example.rcp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var animationKey by remember { mutableStateOf(0) }
            val onResetAnimation = { animationKey++ }

            Box(modifier = Modifier.fillMaxSize()) {
                RcpAnimationWithCounter(animationKey = animationKey)

                // Botón para ir a segunda ventana
                val context = LocalContext.current
                Button(
                    onClick = {
                        onResetAnimation()
                        context.startActivity(Intent(context, SecondActivity::class.java))
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text("Ir a ventana 2")
                }
            }
        }
    }
}

@Composable
fun RcpAnimationWithCounter(animationKey: Int) {
    val circleRadius = 30f
    val centerRadius = 40f
    val rhythmBpm = 110f
    val flashDuration = 150L
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.beep) }
    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    var circles by remember { mutableStateOf(listOf<Pair<Float, Boolean>>()) }
    var centralFlash by remember { mutableStateOf(false) }
    var timeSinceLastCircle by remember { mutableStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(0f) }
    var beatCount by remember { mutableStateOf(0) }

    val centralColor by animateColorAsState(
        targetValue = if (centralFlash) Color(0xFFFF8800) else Color.White
    )

    LaunchedEffect(animationKey) {
        // Reiniciar todos los estados
        circles = listOf()
        centralFlash = false
        timeSinceLastCircle = 0f
        beatCount = 0

        if (canvasWidth <= 0f) return@LaunchedEffect

        val fps = 60
        val deltaTime = 1000f / fps
        val intervalMs = 60_000f / rhythmBpm
        val startX = -circleRadius * 2f
        val centerX = canvasWidth / 2f
        val distanceToCenter = centerX - startX
        val framesPerInterval = intervalMs / deltaTime
        val speedPerFrame = distanceToCenter / framesPerInterval

        while (true) {
            timeSinceLastCircle += deltaTime

            if (timeSinceLastCircle >= intervalMs) {
                circles = circles + Pair(startX, false)
                timeSinceLastCircle = 0f
            }

            circles = circles.map { it.copy(first = it.first + speedPerFrame) }

            circles = circles.map { (x, triggered) ->
                if (!triggered && x >= centerX) {
                    try { mediaPlayer?.start() } catch (_: Exception) {}
                    centralFlash = true
                    beatCount++
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
        drawCircle(Color.Black, centerRadius, Offset(canvasWidth / 2f, centerY), style = Stroke(4f))
        drawCircle(centralColor, centerRadius - 2f, Offset(canvasWidth / 2f, centerY))

        // Círculos móviles
        circles.forEach { (x, _) ->
            drawCircle(Color.Black, circleRadius, Offset(x, centerY), style = Stroke(3f))
            drawCircle(Color.Red, circleRadius - 2f, Offset(x, centerY))
        }
    }

    // Contador de latidos arriba
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text("Latidos: $beatCount", fontSize = 24.sp, color = Color.Black, modifier = Modifier.padding(top = 16.dp))
    }
}














