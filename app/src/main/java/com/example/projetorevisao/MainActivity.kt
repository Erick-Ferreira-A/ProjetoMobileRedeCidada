package com.example.projetorevisao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.saveable.rememberSaveable
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
        enableEdgeToEdge()
        setContent {
            CitizenSPApp()
        }
    }
}

@Composable
fun CitizenSPApp() {
    CitizenSPTheme {
        var currentScreen by rememberSaveable { mutableStateOf("login") }

        Scaffold(
            bottomBar = {
                if (currentScreen != "login") {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = { currentScreen = "home" },
                            icon = { Text("🗺️", fontSize = 20.sp) },
                            label = { Text("Mapa & Feed", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "metro",
                            onClick = { currentScreen = "metro" },
                            icon = { Text("🚆", fontSize = 20.sp) },
                            label = { Text("Metrô SP", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "sos",
                            onClick = { currentScreen = "sos" },
                            icon = { Text("🚨", fontSize = 20.sp) },
                            label = { Text("SOS", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CitizenStatusColors.critical,
                                selectedTextColor = CitizenStatusColors.critical,
                                indicatorColor = CitizenStatusColors.critical.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { currentScreen = "profile" },
                            icon = { Text("👤", fontSize = 20.sp) },
                            label = { Text("Perfil", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = MaterialTheme.colorScheme.background
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