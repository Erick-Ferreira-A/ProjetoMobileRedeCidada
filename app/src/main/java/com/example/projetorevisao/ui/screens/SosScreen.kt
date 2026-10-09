package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.theme.*

@Composable
fun CitizenSosScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onBackClick) {
                    Text("← Voltar ao Início", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = CitizenStatusColors.critical.copy(alpha = 0.2f),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CitizenStatusColors.critical))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚨", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CENTRAL DE EMERGÊNCIA SP",
                color = CitizenStatusColors.critical,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "São Paulo — Discagem Rápida & Envio de Sinal GPS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EmergencyButton(
                title = "👮 Ligar 190 — PMESP",
                subtitle = "Polícia Militar do Estado de São Paulo",
                color = CitizenStatusColors.critical
            )
            EmergencyButton(
                title = "🚑 Ligar 192 — SAMU",
                subtitle = "Atendimento Móvel de Urgência",
                color = CitizenStatusColors.warning
            )
            EmergencyButton(
                title = "🚒 Ligar 193 — Bombeiros",
                subtitle = "Corpo de Bombeiros SP",
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Modo Guardião Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔒", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Modo Guardião / Coação Silencioso",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mantenha o botão SOS pressionado por 3s para enviar alertas de geolocalização e áudio ao vivo aos seus 2 Guardiões Cadastrados.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    title: String,
    subtitle: String,
    color: Color
) {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = if (color.luminance() > 0.179f) ObsidianBackground else Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = (if (color.luminance() > 0.179f) ObsidianBackground else Color.White).copy(alpha = 0.85f))
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
fun PreviewCitizenSosScreen() {
    CitizenSPTheme(darkTheme = false) {
        CitizenSosScreen()
    }
}

@Preview(name = "Escuro", showBackground = true)
@Composable
fun PreviewCitizenSosScreenDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenSosScreen()
    }
}
