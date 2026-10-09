package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.theme.*

@Composable
fun CitizenProfileScreen() {
    var radiusKm by remember { mutableFloatStateOf(1.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Avatar & Badges
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(SurfaceDarkElevated)
                .border(2.dp, AccentCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Erick Rondonia",
            color = TextExpressivePrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Morador de São Paulo • Nível 4 (Guardião Urbano)",
            color = TextExpressiveSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Trust Score & XP Stat Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderGlow, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐ Trust Score", color = TextExpressiveSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("98%", color = AccentEmeraldSafe, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Verificado CPF", color = TextExpressiveMuted, fontSize = 10.sp)
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .width(1.dp)
                        .background(SurfaceBorderGlow)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Selo de Contribuição", color = TextExpressiveSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fiscal da Paulista", color = AccentAmberWarning, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("750 / 1000 XP", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Geofencing Radius Settings Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderGlow, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📡 Raio de Alerta Geofenced",
                    color = TextExpressivePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Receba notificações em tempo real de furtos e alagamentos neste perímetro:",
                    color = TextExpressiveSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perímetro Monitorado:",
                        color = TextExpressiveSecondary,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = AccentCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${"%.1f".format(radiusKm)} km",
                            color = AccentCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = radiusKm,
                    onValueChange = { radiusKm = it },
                    valueRange = 0.5f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = SurfaceDarkElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guardiões Cadastrados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderGlow, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 Guardiões de Confiança (2/3)",
                        color = TextExpressivePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    TextButton(onClick = { }) {
                        Text("+ Adicionar", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = SurfaceBorderGlow)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Maria R. (Mãe) — 🟢 Alertas Ativos", color = TextExpressiveSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Carlos A. (Irmão) — 🟢 Alertas Ativos", color = TextExpressiveSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCitizenProfileScreen() {
    CitizenSPTheme {
        CitizenProfileScreen()
    }
}
