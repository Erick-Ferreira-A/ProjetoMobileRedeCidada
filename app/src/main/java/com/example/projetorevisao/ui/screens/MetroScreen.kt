package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.theme.*

data class MetroLine(
    val name: String,
    val statusText: String,
    val statusColor: Color,
    val crowdLevel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenMetroScreen(
    onLineClick: (String) -> Unit = {}
) {
    val metroLines = listOf(
        MetroLine("Linha 1 - Azul", "✅ Operação Normal", StatusSafe, "Livre"),
        MetroLine("Linha 2 - Verde", "✅ Operação Normal", StatusSafe, "Livre"),
        MetroLine("Linha 3 - Vermelha", "⚠️ Plataforma Cheia (Luz)", AlertWarning, "Lotado"),
        MetroLine("Linha 4 - Amarela", "✅ Operação Normal", StatusSafe, "Moderado"),
        MetroLine("Linha 8 - Diamante (CPTM)", "🚨 Trem Parado / Lentidão", AlertCritical, "Interditado"),
        MetroLine("Linha 9 - Esmeralda (CPTM)", "✅ Operação Normal", StatusSafe, "Livre")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CitizenBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "🚆 Metrô & CPTM SP",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Status Colaborativo em Tempo Real",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Summary Banner (Gemini IA)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CitizenSurface
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🤖 Resumo Inteligente (Gemini IA)",
                    color = AlertTransit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Estação Luz com grande fluxo no horário de pico. Linha 8 apresentando intervalo maior entre trens.\"",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(metroLines) { line ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLineClick(line.name) },
                    colors = CardDefaults.cardColors(containerColor = CitizenSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = line.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = line.statusText,
                                color = line.statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = line.statusColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = line.crowdLevel,
                                color = line.statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCitizenMetroScreen() {
    CitizenSPTheme {
        CitizenMetroScreen()
    }
}
