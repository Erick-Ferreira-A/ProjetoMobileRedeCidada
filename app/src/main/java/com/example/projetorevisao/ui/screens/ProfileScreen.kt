package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Avatar & Badges
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Erick Rondonia",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Morador de São Paulo • Nível 4 (Guardião Urbano)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Trust Score & XP Stat Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Text("⭐ Trust Score", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("98%", color = CitizenStatusColors.safe, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Verificado CPF", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Selo de Contribuição", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fiscal da Paulista", color = CitizenStatusColors.warning, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("750 / 1000 XP", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Geofencing Radius Settings Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📡 Raio de Alerta Geofenced",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Receba notificações em tempo real de furtos e alagamentos neste perímetro:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${"%.1f".format(radiusKm)} km",
                            color = MaterialTheme.colorScheme.primary,
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
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guardiões Cadastrados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    TextButton(onClick = { }) {
                        Text("+ Adicionar", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Maria R. (Mãe) — 🟢 Alertas Ativos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Carlos A. (Irmão) — 🟢 Alertas Ativos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
fun PreviewCitizenProfileScreen() {
    CitizenSPTheme(darkTheme = false) {
        CitizenProfileScreen()
    }
}

@Preview(name = "Escuro", showBackground = true)
@Composable
fun PreviewCitizenProfileScreenDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenProfileScreen()
    }
}
