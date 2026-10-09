package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val confirmations: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenHomeScreen(
    onSosClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val filters = listOf("Todos", "📱 Furtos", "🌧️ Alagamento", "🚆 Metrô/CPTM", "⚠️ Pontos Escuros")
    var selectedFilter by remember { mutableStateOf("Todos") }

    val mockIncidents = remember {
        listOf(
            Incident("1", "Furto de Celular", "Av. Paulista, 1578 — Bela Vista", "200m • há 4 min", AlertCritical, "📱", 14),
            Incident("2", "Ponto de Alagamento", "Marginal Pinheiros próx. Ponte Cidade Jardim", "1.2km • há 18 min", AlertWarning, "🌧️", 8),
            Incident("3", "Paralisação Linha 4-Amarela", "Estação Paulista — Consolação", "800m • há 35 min", AlertTransit, "🚆", 32)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CitizenBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar
            Surface(
                color = CitizenSurface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🛡️ Citizen SP",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "São Paulo, SP — Em Tempo Real",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { }) {
                            Text(text = "🔔", fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Places Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CitizenSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Para onde você vai em SP?",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AlertCritical,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = CitizenSurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Simulated Google Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF131C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🗺️ Mapa Interativo Google Maps SDK (SP)\n[Dark Vector Map Active]",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            // Bottom Feed Sheet of Recent Incidents
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = CitizenSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ocorrências Próximas",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onReportClick) {
                            Text("+ Reportar", color = AlertCritical, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val incident = mockIncidents.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CitizenSurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(incident.categoryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = incident.emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = incident.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = incident.location,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = incident.distanceTime,
                                    color = AlertCritical,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating SOS Action Button (IHC Thumb Zone)
        FloatingActionButton(
            onClick = onSosClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .size(64.dp),
            containerColor = AlertCritical,
            contentColor = TextPrimary,
            shape = CircleShape
        ) {
            Text(text = "SOS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
