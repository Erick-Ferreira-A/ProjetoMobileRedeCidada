package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            .background(CitizenBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Profile Avatar & Badges
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AlertTransit),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Erick Rondonia",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Morador de São Paulo • Nível 4 (Guardião)",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Trust Score & XP Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CitizenSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐ Trust Score", color = TextSecondary, fontSize = 12.sp)
                    Text("98%", color = StatusSafe, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = CitizenSurfaceElevated
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Selo Atual", color = TextSecondary, fontSize = 12.sp)
                    Text("Fiscal da Paulista", color = AlertWarning, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Geofencing Radius Settings
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CitizenSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📡 Raio de Alerta Geofenced",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Receba notificações de ocorrências e alagamentos neste raio:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${"%.1f".format(radiusKm)} km ao redor de você",
                    color = AlertTransit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Slider(
                    value = radiusKm,
                    onValueChange = { radiusKm = it },
                    valueRange = 0.5f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AlertTransit,
                        activeTrackColor = AlertTransit
                    )
                )
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
