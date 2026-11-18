package com.example.projetorevisao.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.location.LOCATION_PERMISSIONS
import com.example.projetorevisao.data.location.LocationProvider
import com.example.projetorevisao.data.location.hasLocationPermission
import com.example.projetorevisao.ui.components.CitizenAppTopBar
import com.example.projetorevisao.ui.components.CitizenBottomNavBar
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.GuardianContact
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private data class EmergencyChannel(
    val number: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector
)

/** Abre o discador com o número preenchido. A ligação só acontece quando a pessoa toca em ligar. */
private fun Context.dial(number: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "Este aparelho não consegue fazer ligações. Disque $number.", Toast.LENGTH_LONG).show()
    }
}

private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

/** Texto do aviso de emergência, com link do mapa quando há localização. */
private fun sosMessage(latitude: Double?, longitude: Double?): String {
    val location = if (latitude != null && longitude != null) {
        "Minha localização: https://maps.google.com/?q=$latitude,$longitude"
    } else {
        "Não consegui obter minha localização."
    }
    return "🚨 SOS - Rede Cidadã: preciso de ajuda agora. $location"
}

/** Número no formato internacional que o WhatsApp exige (DDI 55 para números brasileiros sem DDI). */
private fun whatsAppNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }.removePrefix("00")
    return if (digits.length in 10..11) "55$digits" else digits
}

/** Abre a conversa do WhatsApp com o guardião e a mensagem já escrita. Falha para o SMS se não houver WhatsApp. */
private fun Context.sendSosToGuardian(phone: String, text: String): Boolean {
    val uri = Uri.parse("https://wa.me/${whatsAppNumber(phone)}?text=${Uri.encode(text)}")
    for (pkg in WHATSAPP_PACKAGES) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage(pkg))
            return true
        } catch (_: ActivityNotFoundException) {
            // tenta o próximo
        }
    }
    return composeSms(listOf(phone), text)
}

/** Compartilha a mensagem no WhatsApp para a pessoa escolher vários contatos de uma vez. */
private fun Context.shareSosOnWhatsApp(text: String, phones: List<String>): Boolean {
    for (pkg in WHATSAPP_PACKAGES) {
        try {
            startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text).setPackage(pkg))
            return true
        } catch (_: ActivityNotFoundException) {
            // tenta o próximo
        }
    }
    return composeSms(phones, text)
}

/** Plano B, para aparelhos sem WhatsApp. */
private fun Context.composeSms(phones: List<String>, text: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phones.joinToString(";"))).putExtra("sms_body", text))
    true
} catch (_: ActivityNotFoundException) {
    false
}

@Composable
fun CitizenSosScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onBackClick: () -> Unit = {},
    onRequestLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPreparingSos by remember { mutableStateOf(false) }
    var showAddGuardian by rememberSaveable { mutableStateOf(false) }
    var localMessage by remember { mutableStateOf<String?>(null) }
    // Mensagem pronta esperando a escolha do guardião (quando há mais de um).
    var pendingSosText by remember { mutableStateOf<String?>(null) }
    val emergencyChannels = listOf(
        EmergencyChannel("190", "Polícia Militar", "Assalto em andamento, violência ou risco à vida", CitizenStatusColors.critical, CitizenIcons.PoliceShield),
        EmergencyChannel("193", "Corpo de Bombeiros", "Incêndios, resgates, quedas de árvores e acidentes graves", BrandOrange, CitizenIcons.Fire),
        EmergencyChannel("199", "Defesa Civil", "Enchentes, risco de desabamento e tempestades", BrandBlue, CitizenIcons.FloodWater),
        EmergencyChannel("192", "SAMU", "Atendimento médico de urgência", CitizenStatusColors.safe, CitizenIcons.SosEmergency)
    )


    fun sendSos() {
        val phones = viewModel.guardianPhones
        if (phones.isEmpty()) {
            localMessage = if (viewModel.isLoggedIn) {
                "Você ainda não tem guardiões. Cadastre alguém abaixo ou ligue 190."
            } else {
                "Entre na sua conta para cadastrar guardiões. Em perigo, ligue 190."
            }
            return
        }
        scope.launch {
            isPreparingSos = true
            localMessage = null
            val location = withTimeoutOrNull(5_000) { LocationProvider(context).currentLocation() }
            val text = sosMessage(location?.latitude, location?.longitude)
            viewModel.registerSosAlert(location?.latitude, location?.longitude)
            if (phones.size == 1) {
                if (!context.sendSosToGuardian(phones.first(), text)) {
                    localMessage = "Não há WhatsApp nem SMS neste aparelho. Ligue 190 se estiver em perigo."
                }
            } else {
                pendingSosText = text
            }
            isPreparingSos = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { sendSos() } // segue com ou sem localização

    fun onSosPressed() {
        if (context.hasLocationPermission() || viewModel.guardianPhones.isEmpty()) {
            sendSos()
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, CitizenStatusColors.critical.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Em perigo agora?",
                        style = MaterialTheme.typography.titleLarge,
                        color = CitizenStatusColors.critical,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Button(
                        onClick = { context.dial("190") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CitizenStatusColors.critical, contentColor = Color.White)
                    ) {
                        Icon(CitizenIcons.PhoneCall, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ligar 190 (Polícia)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    OutlinedButton(
                        onClick = ::onSosPressed,
                        enabled = !isPreparingSos,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, CitizenStatusColors.critical)
                    ) {
                        if (isPreparingSos) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CitizenStatusColors.critical)
                            Spacer(Modifier.width(8.dp))
                            Text("Obtendo localização…", color = CitizenStatusColors.critical)
                        } else {
                            Icon(CitizenIcons.SosEmergency, contentDescription = null, tint = CitizenStatusColors.critical, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Avisar meus guardiões (WhatsApp)", color = CitizenStatusColors.critical, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Abre o WhatsApp com a mensagem e sua localização prontas para cada guardião. " +
                            "Você só precisa tocar em enviar. O app não chama a polícia sozinho: para isso, ligue 190.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    (localMessage ?: viewModel.sosStatusMessage)?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Meus guardiões",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Pessoas que recebem seu aviso de SOS pelo WhatsApp.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (viewModel.isLoggedIn) {
                    TextButton(onClick = { showAddGuardian = true }) {
                        Icon(CitizenIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar")
                    }
                }
            }
        }

        if (!viewModel.isLoggedIn) {
            item {
                OutlinedButton(onClick = onRequestLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Entrar para cadastrar guardiões")
                }
            }
        } else if (viewModel.guardians.isEmpty()) {
            item {
                Text(
                    text = "Nenhum guardião cadastrado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        viewModel.guardiansMessage?.let { message ->
            item {
                Text(message, style = MaterialTheme.typography.bodySmall, color = CitizenStatusColors.critical)
            }
        }

        items(viewModel.guardians, key = { it.id }) { guardian ->
            GuardianContactCard(guardian = guardian, onRemove = { viewModel.removeGuardian(guardian.id) })
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Números de emergência",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(emergencyChannels, key = { it.number }) { channel ->
            EmergencyChannelCard(channel = channel, onCall = { context.dial(channel.number) })
        }
    }

    pendingSosText?.let { text ->
        AlertDialog(
            onDismissRequest = { pendingSosText = null },
            title = { Text("Avisar guardiões") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Toque em cada guardião para abrir a conversa no WhatsApp com a mensagem pronta.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    viewModel.guardians.forEach { guardian ->
                        OutlinedButton(
                            onClick = { context.sendSosToGuardian(guardian.phone, text) },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, CitizenStatusColors.safe)
                        ) { Text("Enviar para ${guardian.name}", color = CitizenStatusColors.safe, fontWeight = FontWeight.Bold) }
                    }
                    TextButton(
                        onClick = { context.shareSosOnWhatsApp(text, viewModel.guardianPhones) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Escolher vários contatos no WhatsApp") }
                }
            },
            confirmButton = { TextButton(onClick = { pendingSosText = null }) { Text("Fechar") } }
        )
    }

    if (showAddGuardian) {
        AddGuardianDialog(
            isSaving = viewModel.isSavingGuardian,
            errorMessage = viewModel.guardiansMessage,
            onDismiss = { showAddGuardian = false },
            onSave = { name, relation, phone ->
                viewModel.addGuardian(name, relation, phone) { showAddGuardian = false }
            }
        )
    }
}

@Composable
private fun AddGuardianDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var relation by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo guardião") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true)
                OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { Text("Relação (ex.: mãe, amigo)") }, singleLine = true)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { value -> phone = value.filter { it.isDigit() || it in "+ ()-" }.take(20) },
                    label = { Text("WhatsApp com DDD") },
                    placeholder = { Text("11 91234-5678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Text(
                    "Use o número de WhatsApp da pessoa e avise que ela pode receber um pedido de ajuda seu.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                errorMessage?.let { Text(it, color = CitizenStatusColors.critical, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && name.isNotBlank() && phone.count { it.isDigit() } >= 8,
                onClick = { onSave(name, relation, phone) }
            ) { Text(if (isSaving) "Salvando…" else "Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun GuardianContactCard(guardian: GuardianContact, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandBlue.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(CitizenIcons.Groups, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guardian.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${guardian.relation} • ${guardian.phone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRemove) { Text("Remover") }
        }
    }
}

@Composable
private fun EmergencyChannelCard(channel: EmergencyChannel, onCall: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Ligar ${channel.number}", onClick = onCall),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(channel.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(channel.icon, contentDescription = null, tint = channel.color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${channel.number} • ${channel.title}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = channel.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = CircleShape, color = channel.color) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(CitizenIcons.PhoneCall, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 820, name = "SOS Screen - Light")
@Composable
fun PreviewCitizenSosLight() {
    CitizenSPTheme(darkTheme = false) {
        Scaffold(
            topBar = { CitizenAppTopBar(currentScreen = "sos") },
            bottomBar = { CitizenBottomNavBar(currentScreen = "sos", onSelectScreen = {}, onOpenReportSheet = {}) }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) { CitizenSosScreen() }
        }
    }
}

@Preview(showBackground = true, heightDp = 820, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SOS Screen - Dark")
@Composable
fun PreviewCitizenSosDark() {
    CitizenSPTheme(darkTheme = true) {
        Scaffold(
            topBar = { CitizenAppTopBar(currentScreen = "sos") },
            bottomBar = { CitizenBottomNavBar(currentScreen = "sos", onSelectScreen = {}, onOpenReportSheet = {}) }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) { CitizenSosScreen() }
        }
    }
}
