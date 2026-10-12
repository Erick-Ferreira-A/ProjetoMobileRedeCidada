package com.example.projetorevisao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenStatusColors

@Composable
fun CitizenAppTopBar(
    currentScreen: String,
    trustScore: Int = 940,
    onNavigateHome: () -> Unit = {},
    onSosClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (currentScreen == "profile") {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = CitizenIcons.ArrowBack,
                            contentDescription = "Voltar ao Início",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    RedeCidadaLogo(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Text(
                        text = when (currentScreen) {
                            "metro" -> "Mobilidade & Rotas Seguras"
                            "reels" -> "Reels de Segurança SP"
                            "sos" -> "Central SOS & Guardiões"
                            "profile" -> "Perfil & APIs Conectadas"
                            else -> "Rede Cidadã SP"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = when (currentScreen) {
                            "metro" -> "Direto dos Trens • SPTrans • Safe Routing"
                            "reels" -> "Vídeos verificados • Bombeiros, PM e Comunidade"
                            "sos" -> "Acionamento 190/193 e Rede de Parentes Guardiões"
                            "profile" -> "Privacidade Anônima • Geofence • Google Cloud"
                            else -> "São Paulo • SSP-SP, Metrô & CGE Ao Vivo"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Community Trust Score Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CitizenStatusColors.verifiedBadgeBg,
                    modifier = Modifier.clickable(onClick = onProfileClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.Shield,
                            contentDescription = "Score de Confiabilidade",
                            tint = CitizenStatusColors.safe,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$trustScore",
                            style = MaterialTheme.typography.labelSmall,
                            color = CitizenStatusColors.safe,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Quick 190 SOS Pill
                if (currentScreen != "sos") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CitizenStatusColors.critical.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CitizenStatusColors.critical.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable(onClick = onSosClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = CitizenIcons.SosEmergency,
                                contentDescription = "SOS 190",
                                tint = CitizenStatusColors.critical,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "190",
                                style = MaterialTheme.typography.labelSmall,
                                color = CitizenStatusColors.critical,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Profile Button
                val isProfileSelected = currentScreen == "profile"
                IconButton(
                    onClick = if (isProfileSelected) onNavigateHome else onProfileClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isProfileSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        )
                ) {
                    Icon(
                        imageVector = CitizenIcons.Person,
                        contentDescription = "Perfil e APIs",
                        tint = if (isProfileSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenBottomNavBar(
    currentScreen: String,
    onSelectScreen: (String) -> Unit,
    onOpenReportSheet: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = { onSelectScreen("home") },
            icon = {
                Icon(
                    imageVector = CitizenIcons.Map,
                    contentDescription = "Mapa & Feed",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Mapa & Feed", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = currentScreen == "metro",
            onClick = { onSelectScreen("metro") },
            icon = {
                Icon(
                    imageVector = CitizenIcons.Train,
                    contentDescription = "Rotas & Metrô",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Mobilidade", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Central Orange "+ Reportar" Action Button (Figma 192:400)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onOpenReportSheet)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CitizenIcons.Add,
                        contentDescription = "Reportar Incidente Anônimo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reportar",
                    fontSize = 10.sp,
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        NavigationBarItem(
            selected = currentScreen == "reels",
            onClick = { onSelectScreen("reels") },
            icon = {
                Icon(
                    imageVector = CitizenIcons.Reels,
                    contentDescription = "Reels de Segurança",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Reels", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = currentScreen == "sos",
            onClick = { onSelectScreen("sos") },
            icon = {
                Icon(
                    imageVector = CitizenIcons.SosEmergency,
                    contentDescription = "SOS Emergência",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("SOS", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CitizenStatusColors.critical,
                selectedTextColor = CitizenStatusColors.critical,
                indicatorColor = CitizenStatusColors.critical.copy(alpha = 0.18f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
