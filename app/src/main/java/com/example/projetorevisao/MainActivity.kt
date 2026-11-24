package com.example.projetorevisao

import android.os.Bundle
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.WindowInsets
import com.example.projetorevisao.ui.screens.EditProfileScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projetorevisao.data.local.CitizenDatabase
import com.example.projetorevisao.data.repository.IncidentRepository
import com.example.projetorevisao.data.repository.PublicSafetyRepository
import com.example.projetorevisao.data.location.hasLocationPermission
import com.example.projetorevisao.data.session.SessionManager
import com.example.projetorevisao.ui.components.rememberUserLocationSync
import com.example.projetorevisao.ui.screens.LocationPermissionGate
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
import com.example.projetorevisao.ui.theme.RcAppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.projetorevisao.ui.viewmodel.AuthViewModel
import com.example.projetorevisao.ui.viewmodel.TransitViewModel
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.CitizenViewModelFactory
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

class MainActivity : ComponentActivity() {
    private val citizenViewModel: CitizenViewModel by viewModels {
        CitizenViewModelFactory(
            IncidentRepository(CitizenDatabase.getInstance(applicationContext).incidentDao(), applicationContext),
            PublicSafetyRepository()
        )
    }

    private val authViewModel: AuthViewModel by viewModels()
    private val transitViewModel: TransitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Precisa vir antes de criar os ViewModels: eles leem a sessão salva.
        SessionManager.init(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CitizenSPApp(viewModel = citizenViewModel, authViewModel = authViewModel, transitViewModel = transitViewModel) }
    }
}

@Composable
fun CitizenSPApp(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    authViewModel: AuthViewModel = remember { AuthViewModel() },
    transitViewModel: TransitViewModel = remember { TransitViewModel() }
) {
    CitizenSPTheme {
        val navController = rememberNavController()
        val entry by navController.currentBackStackEntryAsState()
        // Com sessão salva, abre direto no início; sem sessão, mostra a apresentação e o login.
        // A localização é obrigatória: quem já tem conta mas revogou a permissão passa pela tela de localização.
        val context = LocalContext.current
        val startDestination = remember {
            when {
                SessionManager.current == null && !SessionManager.guestMode -> "onboarding"
                context.hasLocationPermission() -> "home"
                else -> "location"
            }
        }
        val refreshUserLocation = rememberUserLocationSync(viewModel)
        val currentScreen = entry?.destination?.route ?: startDestination
        var showReportSheet by rememberSaveable { mutableStateOf(false) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val showNavigationShell = currentScreen !in setOf("onboarding", "login", "location")
        fun navigateTo(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // Depois de sair ou excluir a conta: volta ao login sem deixar telas antigas na pilha.
        fun resetTo(route: String) {
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }

        // Voltar em qualquer aba (ou no SOS) leva ao Início; no Início, sai do app (TELAS §1).
        BackHandler(enabled = currentScreen in setOf("metro", "reels", "sos", "profile")) {
            navController.popBackStack("home", inclusive = false)
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
        val contentMaxWidth = if (maxWidth >= 1_000.dp) 840.dp else 720.dp
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // No onboarding o topo azul passa por trás da barra de status (o próprio topo aplica o espaço).
            contentWindowInsets = if (currentScreen == "onboarding") WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                // Vídeos é tela cheia (modelo Reels): sem barra superior, o SOS fica no próprio feed.
                if (showNavigationShell && currentScreen != "reels") {
                    CitizenAppTopBar(
                        currentScreen = currentScreen,
                        areaLabel = viewModel.areaLabel,
                        onBack = {
                            if (currentScreen == "profile/edit") navController.popBackStack() else navigateTo("home")
                        },
                        onSosClick = { navigateTo("sos") }
                    )
                }
            },
            bottomBar = {
                if (showNavigationShell) {
                    // No feed de Vídeos a barra fica escura, como no Instagram/TikTok.
                    RcAppTheme(darkTheme = currentScreen == "reels" || isSystemInDarkTheme()) {
                        CitizenBottomNavBar(
                            currentScreen = currentScreen,
                            onSelectScreen = { target ->
                                navigateTo(target)
                            },
                            onOpenReportSheet = { showReportSheet = true }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = contentMaxWidth).fillMaxWidth().fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("onboarding") {
                        CitizenOnboardingScreen(onFinishOnboarding = {
                            refreshUserLocation()
                            navigateTo("login")
                        })
                    }
                    composable("location") {
                        LocationPermissionGate(onGranted = {
                            refreshUserLocation()
                            resetTo(if (SessionManager.current != null || SessionManager.guestMode) "home" else "login")
                        })
                    }
                    composable("login") {
                        CitizenLoginScreen(
                            onLoginSuccess = {
                                SessionManager.guestMode = false
                                resetTo("home")
                            },
                            onContinueAsGuest = {
                                SessionManager.guestMode = true
                                resetTo("home")
                            },
                            onBackToOnboarding = { navigateTo("onboarding") },
                            authViewModel = authViewModel
                        )
                    }
                    composable("home") {
                        CitizenHomeScreen(
                            viewModel,
                            onSosClick = { navigateTo("sos") },
                            onOpenReportSheet = { showReportSheet = true },
                            onOpenProfile = { navigateTo("profile") },
                            onCenterOnUser = refreshUserLocation
                        )
                    }
                    composable("metro") { CitizenMetroScreen(viewModel, transitViewModel) }
                    composable("reels") {
                        CitizenReelsScreen(
                            viewModel,
                            onOpenReportSheet = { showReportSheet = true },
                            onOpenIncident = { incident ->
                                viewModel.openIncidentDetail(incident)
                                navigateTo("home")
                            },
                            onSosClick = { navigateTo("sos") }
                        )
                    }
                    composable("sos") {
                        CitizenSosScreen(
                            viewModel,
                            onBackClick = { navigateTo("home") },
                            onRequestLogin = { navController.navigate("login") }
                        )
                    }
                    composable("profile") {
                        CitizenProfileScreen(
                            viewModel,
                            authViewModel = authViewModel,
                            onRequestLogin = { navController.navigate("login") },
                            onLoggedOut = { resetTo("login") },
                            onEditProfile = { navController.navigate("profile/edit") },
                            onOpenGuardians = { navigateTo("sos") },
                            onComingSoon = { what -> scope.launch { snackbar.showSnackbar("$what ainda está em integração. Em breve.") } }
                        )
                    }
                    composable("profile/edit") {
                        EditProfileScreen(
                            accountName = viewModel.currentUserName,
                            accountEmail = viewModel.currentUserEmail,
                            onSaved = {
                                navController.popBackStack()
                                scope.launch { snackbar.showSnackbar("Perfil salvo neste aparelho.") }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }
                }
            }

            if (showReportSheet) {
                ReportIncidentModalSheet(
                    currentNeighborhood = viewModel.areaLabel,
                    defaultAnonymous = viewModel.anonymousReportingDefault,
                    onDismiss = { showReportSheet = false },
                    onSubmitReport = { category, title, description, address, isAnonymous, attachments ->
                        viewModel.addIncident(category, title, description, address, isAnonymous, attachments)
                        showReportSheet = false
                        navigateTo("home")
                    }
                )
        }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() { CitizenSPApp() }
