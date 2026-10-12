package com.example.projetorevisao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.projetorevisao.ui.components.CitizenAppTopBar
import com.example.projetorevisao.ui.components.CitizenBottomNavBar
import com.example.projetorevisao.ui.screens.CitizenHomeScreen
import com.example.projetorevisao.ui.screens.CitizenLoginScreen
import com.example.projetorevisao.ui.screens.CitizenMetroScreen
import com.example.projetorevisao.ui.screens.CitizenOnboardingScreen
import com.example.projetorevisao.ui.screens.CitizenProfileScreen
import com.example.projetorevisao.ui.screens.CitizenReelsScreen
import com.example.projetorevisao.ui.screens.CitizenSosScreen
import com.example.projetorevisao.ui.screens.ReportIncidentModalSheet
import com.example.projetorevisao.ui.theme.CitizenSPTheme
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

        val showNavigationShell = currentScreen != "onboarding" && currentScreen != "login"
        val isFullScreenReelActive = currentScreen == "reels" && viewModel.selectedReelForPlayer != null

        Scaffold(
            topBar = {
                if (showNavigationShell && !isFullScreenReelActive) {
                    CitizenAppTopBar(
                        currentScreen = currentScreen,
                        trustScore = viewModel.userTrustScore,
                        onNavigateHome = { currentScreen = "home" },
                        onSosClick = { currentScreen = "sos" },
                        onProfileClick = { currentScreen = "profile" }
                    )
                }
            },
            bottomBar = {
                if (showNavigationShell) {
                    CitizenBottomNavBar(
                        currentScreen = currentScreen,
                        onSelectScreen = { target ->
                            if (target != "reels") {
                                viewModel.openReelPlayer(null)
                            }
                            currentScreen = target
                        },
                        onOpenReportSheet = { showReportSheet = true }
                    )
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