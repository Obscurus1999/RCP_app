package com.example.rcp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text("Esta es la segunda ventana", fontSize = 24.sp)

                    Spacer(modifier = Modifier.height(30.dp))

                    // 🔴 BOTÓN PARA ABRIR EL PDF
                    val context = this@SecondActivity

                    Button(
                        onClick = {
                            val intent = Intent(context, PdfActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Instrucciones RCP")
                    }

                }
            }
        }
    }
}

