package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

data class Incident(
    val id: String,
    val title: String,
    val location: String,
    val distanceTime: String,
    val categoryColor: Color,
    val emoji: String,
    val confirmations: Int,
    val aiTip: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenHomeScreen(
    onSosClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val filters = listOf("Todos (12)", "📱 Furtos (6)", "🌧️ Alagamentos (3)", "🚆 Metrô/CPTM (2)", "⚠️ Escuro (1)")
    var selectedFilter by remember { mutableStateOf("Todos (12)") }

    val mockIncidents = run {
        listOf(
            Incident(
                id = "1",
                title = "Furto de Celular (Alerta Rápido)",
                location = "Av. Paulista, 1578 — Bela Vista",
                distanceTime = "200m • há 4 min",
                categoryColor = CitizenStatusColors.critical,
                emoji = "📱",
                confirmations = 14,
                aiTip = "Gemini Safety: Mantenha dispositivos nos bolsos internos na saída do metrô Trianon."
            ),
            Incident(
                id = "2",
                title = "Ponto de Alagamento Ativo (CGE SP)",
                location = "Marginal Pinheiros próx. Ponte Cidade Jardim",
                distanceTime = "1.2km • há 18 min",
                categoryColor = CitizenStatusColors.warning,
                emoji = "🌧️",
                confirmations = 8,
                aiTip = "Defesa Civil: Evitar a pista expressa sentido Interlagos."
            ),
            Incident(
                id = "3",
                title = "Operação Lenta Linha 4-Amarela",
                location = "Estação Paulista — Consolação",
                distanceTime = "800m • há 35 min",
                categoryColor = MaterialTheme.colorScheme.primary,
                emoji = "🚆",
                confirmations = 32,
                aiTip = null
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 6.dp,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rede Cidadã",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = CitizenStatusColors.safe.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "DEMO",
                                        color = CitizenStatusColors.safe,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Bela Vista, São Paulo — Feed Colaborativo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Text(text = "🔔", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Places Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pesquisar ruas ou trajetos em SP...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters) { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }

            // Simulated Google Map Vector View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🗺️ Mapa de São Paulo",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prévia ilustrativa • dados demonstrativos",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp
                    )
                }
            }

            // Incident List Sheet
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ocorrências Recentes",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onReportClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Reportar", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mockIncidents) { incident ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(incident.categoryColor.copy(alpha = 0.2f))
                                                .border(1.dp, incident.categoryColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = incident.emoji, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = incident.title,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = incident.location,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = incident.distanceTime,
                                                color = incident.categoryColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    incident.aiTip?.let { tip ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = CitizenStatusColors.insight.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tip,
                                                color = CitizenStatusColors.insight,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👍 ${incident.confirmations} confirmações",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        TextButton(onClick = { }) {
                                            Text("Confirmar Veracidade", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating SOS Action Button
        FloatingActionButton(
            onClick = onSosClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(64.dp),
            containerColor = CitizenStatusColors.critical,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Text(text = "SOS", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
fun PreviewCitizenHomeScreen() {
    CitizenSPTheme(darkTheme = false) {
        CitizenHomeScreen()
    }
}

@Preview(name = "Escuro", showBackground = true)
@Composable
fun PreviewCitizenHomeScreenDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenHomeScreen()
    }
}
