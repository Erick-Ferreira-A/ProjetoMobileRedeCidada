package com.example.projetorevisao.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import com.example.projetorevisao.ui.designsystem.RcEmergencyCallChip
import com.example.projetorevisao.ui.designsystem.RcPrimaryButton
import com.example.projetorevisao.ui.designsystem.RcTextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.projetorevisao.data.location.LOCATION_PERMISSIONS
import com.example.projetorevisao.data.location.hasLocationPermission
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.CitizenStatusColors

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Etapa obrigatória: o app só continua com a localização liberada. O botão de ligar 190
 * fica sempre disponível aqui, para ninguém ficar sem acesso à emergência.
 */
@Composable
fun LocationPermissionGate(
    onGranted: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var message by remember { mutableStateOf<String?>(null) }
    var blockedForever by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) {
            onGranted()
        } else {
            blockedForever = activity != null &&
                LOCATION_PERMISSIONS.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
            message = if (blockedForever) {
                "A permissão foi bloqueada. Abra as configurações do app, toque em Permissões > Localização e escolha \"Permitir durante o uso do app\"."
            } else {
                "Sem a localização não é possível mostrar os alertas perto de você nem enviar sua posição no SOS."
            }
        }
    }

    fun requestOrContinue() {
        if (context.hasLocationPermission()) onGranted() else launcher.launch(LOCATION_PERMISSIONS)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topo: voltar (se houver) e emergência — longe do botão principal (TELAS §2.9).
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(CitizenIcons.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(Modifier.weight(1f))
            RcEmergencyCallChip()
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(CitizenIcons.LocationPin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Mostre o que acontece perto de você.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Para isso, a Rede Cidadã usa sua localização enquanto o app está aberto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GateReasonRow("Mostrar no mapa onde você está e os alertas ao redor.")
                    GateReasonRow("Marcar o local exato dos relatos que você enviar.")
                    GateReasonRow("Enviar sua posição aos guardiões quando você acionar o SOS.")
                    GateReasonRow("Só usamos a localização com o app aberto. Nada roda em segundo plano.")
                }
            }
            message?.let {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = it,
                    color = CitizenStatusColors.critical,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (blockedForever) {
                RcPrimaryButton(
                    text = "Abrir configurações",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                RcTextButton("Já liberei, continuar", onClick = ::requestOrContinue)
            } else {
                RcPrimaryButton(
                    text = "Permitir localização",
                    onClick = ::requestOrContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun GateReasonRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(CitizenIcons.VerifiedCheck, contentDescription = null, tint = CitizenStatusColors.safe, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
