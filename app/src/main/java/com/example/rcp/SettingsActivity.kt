package com.example.rcp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.core.content.edit

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val current = prefs.getString("start_screen", "main")

        setContent {
            SettingsScreen(
                initialOption = current ?: "main",
                onOptionChange = { newSelection ->
                    prefs.edit {
                        putString("start_screen", newSelection)
                    }
                }
            )
        }
    }
}
@Composable
fun SettingsScreen(
    initialOption: String,
    onOptionChange: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf(initialOption) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Elige la pantalla de inicio",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(
                selected = selectedOption == "main",
                onClick = {
                    selectedOption = "main"
                    onOptionChange("main")
                }
            )
            Text("Asistente RCP")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(
                selected = selectedOption == "second",
                onClick = {
                    selectedOption = "second"
                    onOptionChange("second")
                }
            )
            Text("Menú")
        }
    }
}
