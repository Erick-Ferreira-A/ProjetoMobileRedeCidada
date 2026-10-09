package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    val crowdLevel: String,
    val lineAccent: Color,
    val updatedMinutesAgo: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenMetroScreen(
    onLineClick: (String) -> Unit = {}
) {
    val metroLines = listOf(
        MetroLine("Linha 1 - Azul (Jabaquara ↔ Tucuruvi)", "✅ Operação Normal", AccentEmeraldSafe, "Livre", Color(0xFF0054A6), 2),
        MetroLine("Linha 2 - Verde (Vila Prudente ↔ Vila Madalena)", "✅ Operação Normal", AccentEmeraldSafe, "Livre", Color(0xFF00805D), 5),
        MetroLine("Linha 3 - Vermelha (Palmeiras-Barra Funda ↔ Corinthians-Itaquera)", "⚠️ Plataforma Cheia na Estação Luz", AccentAmberWarning, "Lotado", Color(0xFFEE3338), 1),
        MetroLine("Linha 4 - Amarela (Vila Sônia ↔ Luz)", "✅ Operação Normal", AccentEmeraldSafe, "Moderado", Color(0xFFFFF000), 3),
        MetroLine("Linha 5 - Lilás (Capão Redondo ↔ Chácara Klabin)", "✅ Operação Normal", AccentEmeraldSafe, "Livre", Color(0xFFA0529C), 8),
        MetroLine("Linha 8 - Diamante (CPTM - Júlio Prestes ↔ Amador Bueno)", "🚨 Manutenção Emergencial / Lentidão", AccentRoseCritical, "Interditado", Color(0xFF97A098), 4),
        MetroLine("Linha 9 - Esmeralda (CPTM - Osasco ↔ Bruno Covas/Mendes-Vila Natal)", "✅ Operação Normal", AccentEmeraldSafe, "Livre", Color(0xFF00A88F), 6)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🚆 Metrô & CPTM SP",
                    color = TextExpressivePrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Situação das Linhas & Densidade de Passageiros",
                    color = TextExpressiveSecondary,
                    fontSize = 13.sp
                )
            }
            Surface(
                color = AccentCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AccentCyan))
            ) {
                Text(
                    text = "SPTrans / ViaQuatro",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Summary Banner (Gemini IA Transit Assistant)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentPurpleAI.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkHeader),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurpleAI.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resumo do Trânsito SP (Gemini IA)",
                        color = AccentPurpleAI,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "\"Fluxo intenso na Linha 3-Vermelha sentido Itaquera. Recomenda-se integrar via Linha 2-Verde em Ana Rosa para evitar a Sé.\"",
                        color = TextExpressiveSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
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
                        .border(1.dp, SurfaceBorderGlow, RoundedCornerShape(16.dp))
                        .clickable { onLineClick(line.name) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Line Color Bar Accent
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(line.lineAccent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = line.name,
                                    color = TextExpressivePrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = line.statusText,
                                    color = line.statusColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Atualizado há ${line.updatedMinutesAgo}m por passageiros",
                                    color = TextExpressiveMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = line.statusColor.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(line.statusColor))
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
