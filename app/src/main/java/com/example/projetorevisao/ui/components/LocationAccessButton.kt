package com.example.projetorevisao.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.projetorevisao.data.location.LOCATION_PERMISSIONS
import com.example.projetorevisao.data.location.LocationProvider
import com.example.projetorevisao.data.location.hasLocationPermission
import kotlinx.coroutines.launch

/**
 * Fluxo completo de permissão de localização:
 * 1. Toque no botão -> se já tem permissão, busca a posição direto.
 * 2. Se não tem -> mostra uma explicação ANTES do pedido do sistema.
 * 3. Pedido do sistema (aproximada + precisa juntas).
 * 4. Se negou e o sistema não vai mais perguntar -> orienta a abrir as Configurações do app.
 */
@Composable
fun LocationAccessButton(
    onLocation: (latitude: Double, longitude: Double) -> Unit,
    onProblem: (message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val provider = remember(context) { LocationProvider(context.applicationContext) }

    var showRationale by remember { mutableStateOf(false) }
    var showSettingsHelp by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    fun fetchLocation() {
        scope.launch {
            loading = true
            val location = provider.currentLocation()
            loading = false
            if (location != null) {
                onLocation(location.latitude, location.longitude)
            } else {
                onProblem("Não foi possível obter sua localização. Verifique se o GPS está ligado.")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        // Se negou e o sistema não quer mais explicar, o usuário só muda isso nas Configurações.
        val blockedForever = !granted && activity != null &&
            LOCATION_PERMISSIONS.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
        when {
            granted -> fetchLocation()
            blockedForever -> showSettingsHelp = true
            else -> onProblem("Sem a permissão de localização, escolha o bairro manualmente.")
        }
    }

    Button(
        onClick = { if (context.hasLocationPermission()) fetchLocation() else showRationale = true },
        enabled = !loading,
        modifier = modifier
    ) {
        Text(if (loading) "Buscando localização..." else "Usar minha localização")
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permitir localização?") },
            text = {
                Text(
                    "Usamos sua localização apenas enquanto o app está aberto, para mostrar " +
                        "relatos e informações da região onde você está. Você pode recusar e " +
                        "escolher o bairro manualmente."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(LOCATION_PERMISSIONS)
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("Agora não") }
            }
        )
    }

    if (showSettingsHelp) {
        AlertDialog(
            onDismissRequest = { showSettingsHelp = false },
            title = { Text("Localização bloqueada") },
            text = {
                Text("Para usar sua localização, ative a permissão em Configurações > Permissões > Localização.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsHelp = false
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }) { Text("Abrir configurações") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsHelp = false }) { Text("Cancelar") }
            }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
