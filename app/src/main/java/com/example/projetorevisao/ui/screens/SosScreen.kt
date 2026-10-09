package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            .background(CitizenBackground)
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
                    Text("← Voltar", color = TextSecondary, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "🚨 CENTRAL DE EMERGÊNCIA",
                color = AlertCritical,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "São Paulo — Discagem Rápida de 1 Toque",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmergencyButton(
                title = "👮 Ligar 190 — PMESP",
                subtitle = "Polícia Militar do Estado de São Paulo",
                color = AlertCritical
            )
            EmergencyButton(
                title = "🚑 Ligar 192 — SAMU",
                subtitle = "Atendimento Móvel de Urgência",
                color = AlertWarning
            )
            EmergencyButton(
                title = "🚒 Ligar 193 — Bombeiros",
                subtitle = "Corpo de Bombeiros SP",
                color = AlertTransit
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CitizenSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 Modo Guardião / Coação Silencioso",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ao pressionar por 3 segundos, enviamos SMS com sua posição GPS ao vivo aos seus guardiões cadastrados.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = TextPrimary.copy(alpha = 0.8f))
        }
    }
}
