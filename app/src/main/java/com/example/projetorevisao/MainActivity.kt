package com.example.projetorevisao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.screens.CitizenHomeScreen
import com.example.projetorevisao.ui.screens.CitizenLoginScreen
import com.example.projetorevisao.ui.screens.CitizenMetroScreen
import com.example.projetorevisao.ui.screens.CitizenOnboardingScreen
import com.example.projetorevisao.ui.screens.CitizenProfileScreen
import com.example.projetorevisao.ui.screens.CitizenReelsScreen
import com.example.projetorevisao.ui.screens.CitizenSosScreen
import com.example.projetorevisao.ui.screens.ReportIncidentModalSheet
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

class MainActivity : ComponentActivity() {
    private val citizenViewModel: CitizenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitizenSPApp(viewModel = citizenViewModel)
        }
    }
}

@Composable
fun CitizenSPApp(
    viewModel: CitizenViewModel = rememberCitizenViewModel()
) {
    CitizenSPTheme {
        var currentScreen by rememberSaveable { mutableStateOf("onboarding") }
        var showReportSheet by rememberSaveable { mutableStateOf(false) }

        val showBottomBar = currentScreen != "onboarding" && currentScreen != "login"

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = {
                                viewModel.setHomeTab("feed")
                                currentScreen = "home"
                            },
                            icon = {
                                Icon(
                                    imageVector = CitizenIcons.Home,
                                    contentDescription = "Início",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Início", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen == "metro",
                            onClick = { currentScreen = "metro" },
                            icon = {
                                Icon(
                                    imageVector = CitizenIcons.Train,
                                    contentDescription = "Metrô & Rotas",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Rotas SP", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        // Central Orange "+ Reportar" Button (Figma 192:400)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showReportSheet = true }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(BrandOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CitizenIcons.Add,
                                        contentDescription = "Reportar Incidente",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Reportar",
                                    fontSize = 11.sp,
                                    color = BrandOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        NavigationBarItem(
                            selected = currentScreen == "reels",
                            onClick = { currentScreen = "reels" },
                            icon = {
                                Icon(
                                    imageVector = CitizenIcons.Reels,
                                    contentDescription = "Reels de Segurança",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Reels", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen == "sos",
                            onClick = { currentScreen = "sos" },
                            icon = {
                                Icon(
                                    imageVector = CitizenIcons.SosEmergency,
                                    contentDescription = "SOS Emergência",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("SOS", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CitizenStatusColors.critical,
                                selectedTextColor = CitizenStatusColors.critical,
                                indicatorColor = CitizenStatusColors.critical.copy(alpha = 0.18f),
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
                    "onboarding" -> CitizenOnboardingScreen(
                        onFinishOnboarding = { currentScreen = "login" }
                    )
                    "login" -> CitizenLoginScreen(
                        onLoginSuccess = { currentScreen = "home" },
                        onBackToOnboarding = { currentScreen = "onboarding" }
                    )
                    "home" -> CitizenHomeScreen(
                        viewModel = viewModel,
                        onSosClick = { currentScreen = "sos" },
                        onOpenReportSheet = { showReportSheet = true },
                        onOpenProfile = { currentScreen = "profile" }
                    )
                    "metro" -> CitizenMetroScreen(viewModel = viewModel)
                    "reels" -> CitizenReelsScreen(
                        viewModel = viewModel,
                        onOpenReportSheet = { showReportSheet = true }
                    )
                    "sos" -> CitizenSosScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = "home" }
                    )
                    "profile" -> CitizenProfileScreen(viewModel = viewModel)
                }
            }

            if (showReportSheet) {
                ReportIncidentModalSheet(
                    currentNeighborhood = viewModel.selectedNeighborhood,
                    defaultAnonymous = viewModel.anonymousReportingDefault,
                    onDismiss = { showReportSheet = false },
                    onSubmitReport = { category, title, description, address, isAnonymous ->
                        viewModel.addIncident(
                            category = category,
                            title = title,
                            description = description,
                            address = address,
                            isAnonymous = isAnonymous
                        )
                        currentScreen = "home"
                        viewModel.setHomeTab("feed")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    CitizenSPApp()
}