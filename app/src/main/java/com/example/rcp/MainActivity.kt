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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var animationKey = 0 // Variable para controlar reinicio de animación

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solo redirigir si es el primer lanzamiento
        if (savedInstanceState == null) {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val start = prefs.getString("start_screen", "main")
            val forceMain = intent.getBooleanExtra("forceMain", false)

            if (!forceMain && start == "second") {
                startActivity(Intent(this, SecondActivity::class.java))
                finish()
                return
            }
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                // Animación principal
                RcpAnimationWithCounter(animationKey = animationKey)

                val context = LocalContext.current
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            // Reinicia la animación antes de ir al menú
                            animationKey++
                            context.startActivity(Intent(context, SecondActivity::class.java))
                            finish()
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Menú")
                    }
                }
            }
        }
    }

    // Cuando la Activity vuelve al frente, reiniciamos la animación
    override fun onResume() {
        super.onResume()
        animationKey++
    }
}

@Composable
fun RcpAnimationWithCounter(animationKey: Int) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.beep) }

    var circles by remember { mutableStateOf(listOf<Pair<Float, Boolean>>()) }
    var centralFlash by remember { mutableStateOf(false) }
    var timeSinceLastCircle by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var beatCount by remember { mutableIntStateOf(0) }

    val centralColor by animateColorAsState(
        targetValue = if (centralFlash) Color(0xFFFF8800) else Color.White
    )

    val running = remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE -> {
                    running.value = false
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> running.value = true
                Lifecycle.Event.ON_DESTROY -> mediaPlayer.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); mediaPlayer.release() }
    }

    LaunchedEffect(animationKey) {
        circles = listOf()
        centralFlash = false
        timeSinceLastCircle = 0f
        beatCount = 0

        while (canvasWidth <= 0f) delay(20)

        val fps = 60
        val deltaTime = 1000f / fps
        val rhythmBpm = 110f
        val flashDuration = 150L
        val startX = -30f
        val centerX = canvasWidth / 2f
        val distanceToCenter = centerX - startX
        val intervalMs = 60_000f / rhythmBpm
        val framesPerInterval = intervalMs / deltaTime
        val speedPerFrame = distanceToCenter / framesPerInterval

        while (true) {
            if (!running.value) {
                delay(50)
                continue
            }

            timeSinceLastCircle += deltaTime

            if (timeSinceLastCircle >= intervalMs) {
                circles = circles + Pair(startX, false)
                timeSinceLastCircle = 0f
            }

            circles = circles.map { it.copy(first = it.first + speedPerFrame) }

            circles = circles.map { (x, triggered) ->
                if (!triggered && x >= centerX) {
                    try { mediaPlayer.start() } catch (_: Exception) {}
                    centralFlash = true
                    beatCount++
                    delay(flashDuration)
                    centralFlash = false
                    Pair(x, true)
                } else Pair(x, triggered)
            }

            circles = circles.filter { it.first < canvasWidth + 60f }

            delay(deltaTime.toLong())
        }
    }

    // Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
        canvasWidth = size.width
        val centerY = size.height / 2f

        drawRect(Color.White, size = size)
        val lineEnd = Offset(size.width - 20f, centerY)
        drawLine(Color.Gray, Offset(0f, centerY), lineEnd, strokeWidth = 6f)
        val arrowSize = 20f
        drawPath(Path().apply {
            moveTo(size.width, centerY)
            lineTo(size.width - arrowSize, centerY - arrowSize / 2)
            lineTo(size.width - arrowSize, centerY + arrowSize / 2)
            close()
        }, color = Color.Gray)

        drawCircle(Color.Black, 40f, Offset(canvasWidth / 2f, centerY), style = Stroke(4f))
        drawCircle(centralColor, 38f, Offset(canvasWidth / 2f, centerY))

        circles.forEach { (x, _) ->
            drawCircle(Color.Black, 30f, Offset(x, centerY), style = Stroke(3f))
            drawCircle(Color.Red, 28f, Offset(x, centerY))
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text("Compresiones: $beatCount", fontSize = 24.sp, color = Color.Black, modifier = Modifier.padding(top = 16.dp))
    }
}
