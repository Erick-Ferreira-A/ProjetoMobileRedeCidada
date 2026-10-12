package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.GuardianContact
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

private data class EmergencyChannel(
    val number: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun CitizenSosScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onBackClick: () -> Unit = {}
) {
    var sosActive by rememberSaveable { mutableStateOf(false) }
    var silentMode by rememberSaveable { mutableStateOf(true) }

    val emergencyChannels = listOf(
        EmergencyChannel(
            number = "190",
            title = "Polícia Militar (COPOM SP)",
            subtitle = "Emergência policial, roubo em andamento ou risco à vida",
            color = CitizenStatusColors.critical,
            icon = CitizenIcons.PoliceShield
        ),
        EmergencyChannel(
            number = "193",
            title = "Corpo de Bombeiros SP",
            subtitle = "Incêndios, resgates, quedas de árvores e acidentes graves",
            color = BrandOrange,
            icon = CitizenIcons.Fire
        ),
        EmergencyChannel(
            number = "199",
            title = "Defesa Civil & Alagamentos",
            subtitle = "Risco de desabamento, enchentes severas e tempestades",
            color = BrandBlue,
            icon = CitizenIcons.FloodWater
        ),
        EmergencyChannel(
            number = "192",
            title = "SAMU Atendimento Médico",
            subtitle = "Socorro médico de urgência em vias públicas ou residências",
            color = CitizenStatusColors.safe,
            icon = CitizenIcons.SosEmergency
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = CitizenIcons.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column {
                    Text(
                        text = "Central SOS & Guardiões",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Acionamento rápido 190/193/199 com geolocalização criptografada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Main SOS Panic & Guardian Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sosActive) {
                        CitizenStatusColors.critical.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (sosActive) CitizenStatusColors.critical else CitizenStatusColors.critical.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                if (sosActive) CitizenStatusColors.critical else CitizenStatusColors.critical.copy(alpha = 0.15f)
                            )
                            .clickable { sosActive = !sosActive },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CitizenIcons.SosEmergency,
                            contentDescription = "Botão SOS",
                            tint = if (sosActive) Color.White else CitizenStatusColors.critical,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = if (sosActive) {
                            "ALERTA SOS ATIVO • TRANSMITINDO GPS"
                        } else {
                            "Toque para Acionar Alerta SOS"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (sosActive) CitizenStatusColors.critical else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (sosActive) {
                            "Seus 3 Guardiões e o canal de emergência receberam sua posição exata em ${viewModel.selectedNeighborhood}."
                        } else {
                            "Envia imediatamente sua coordenada GPS, nível de bateria e áudio ambiente criptografado para seus Guardiões."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { sosActive = !sosActive },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sosActive) MaterialTheme.colorScheme.surfaceContainerHigh else CitizenStatusColors.critical,
                                contentColor = if (sosActive) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        ) {
                            Text(
                                text = if (sosActive) "Encerrar SOS" else "Ativar SOS Agora",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (silentMode) CitizenStatusColors.verifiedBadgeBg else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (silentMode) CitizenStatusColors.safe else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { silentMode = !silentMode }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = CitizenIcons.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (silentMode) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (silentMode) "SOS Silencioso" else "Com Sirene",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (silentMode) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trusted Guardians Section (WalkSafe+ / Citizen Protect)
        item {
            Text(
                text = "Rede de Guardiões (Acompanhamento Ao Vivo)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        items(viewModel.guardians, key = { it.id }) { guardian ->
            GuardianContactCard(guardian = guardian)
        }

        // Official SP Emergency Channels
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Canais Oficiais de Emergência SP",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        items(emergencyChannels, key = { it.number }) { channel ->
            EmergencyChannelCard(channel = channel)
        }
    }
}

@Composable
private fun GuardianContactCard(guardian: GuardianContact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandBlue.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CitizenIcons.Groups,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = guardian.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${guardian.relation} • ${guardian.distanceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (guardian.isSharingLiveLocation) {
                    CitizenStatusColors.verifiedBadgeBg
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            ) {
                Text(
                    text = if (guardian.isSharingLiveLocation) "GPS Ativo" else "Pronto",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (guardian.isSharingLiveLocation) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmergencyChannelCard(channel: EmergencyChannel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(channel.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = channel.icon,
                        contentDescription = null,
                        tint = channel.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${channel.number} • ${channel.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = channel.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = channel.color
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CitizenIcons.PhoneCall,
                        contentDescription = "Ligar ${channel.number}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "SOS Screen - Light")
@Composable
fun PreviewCitizenSosLight() {
    CitizenSPTheme(darkTheme = false) {
        CitizenSosScreen()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "SOS Screen - Dark"
)
@Composable
fun PreviewCitizenSosDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenSosScreen()
    }
}
