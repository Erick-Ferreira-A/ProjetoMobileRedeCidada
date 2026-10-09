package com.example.projetorevisao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.projetorevisao.ui.screens.CitizenHomeScreen
import com.example.projetorevisao.ui.screens.CitizenSosScreen
import com.example.projetorevisao.ui.theme.CitizenSPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CitizenSPTheme {
                var currentScreen by remember { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> CitizenHomeScreen(
                        onSosClick = { currentScreen = "sos" }
                    )
                    "sos" -> CitizenSosScreen(
                        onBackClick = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}