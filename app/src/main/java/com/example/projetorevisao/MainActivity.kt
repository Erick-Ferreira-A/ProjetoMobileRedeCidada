package com.example.projetorevisao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.screens.*
import com.example.projetorevisao.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CitizenSPApp()
        }
    }
}

@Composable
fun CitizenSPApp() {
    CitizenSPTheme {
        var currentScreen by remember { mutableStateOf("home") }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CitizenSurface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = { Text("🗺️", fontSize = 18.sp) },
                        label = { Text("Home", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "metro",
                        onClick = { currentScreen = "metro" },
                        icon = { Text("🚆", fontSize = 18.sp) },
                        label = { Text("Metrô SP", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "sos",
                        onClick = { currentScreen = "sos" },
                        icon = { Text("🚨", fontSize = 18.sp) },
                        label = { Text("SOS", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "profile",
                        onClick = { currentScreen = "profile" },
                        icon = { Text("👤", fontSize = 18.sp) },
                        label = { Text("Perfil", fontSize = 11.sp) }
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = CitizenBackground
            ) {
                when (currentScreen) {
                    "login" -> CitizenLoginScreen(
                        onLoginSuccess = { currentScreen = "home" }
                    )
                    "home" -> CitizenHomeScreen(
                        onSosClick = { currentScreen = "sos" }
                    )
                    "metro" -> CitizenMetroScreen()
                    "sos" -> CitizenSosScreen(
                        onBackClick = { currentScreen = "home" }
                    )
                    "profile" -> CitizenProfileScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    CitizenSPApp()
}