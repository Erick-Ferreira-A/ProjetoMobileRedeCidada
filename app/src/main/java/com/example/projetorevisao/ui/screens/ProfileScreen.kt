package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.ExternalApiConnectionStatus
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.RedeCidadaLogo
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

@Composable
fun CitizenProfileScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Identity & Trust Score Card (Figma UX Slide 6: Score de Reputação)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RedeCidadaLogo(size = 56.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cidadão Verificado SP",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = CitizenIcons.VerifiedCheck,
                                    contentDescription = "Verificado",
                                    tint = CitizenStatusColors.safe,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Região base: ${viewModel.selectedNeighborhood}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Score de Confiabilidade Comunitária",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prioriza seus relatos na triagem IA e combate fake news",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CitizenStatusColors.verifiedBadgeBg
                        ) {
                            Text(
                                text = "${viewModel.userTrustScore} pts",
                                style = MaterialTheme.typography.titleMedium,
                                color = CitizenStatusColors.safe,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Privacy, Anonymity & Geofence Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Privacidade & Geofence de Alertas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = CitizenIcons.VisibilityOff,
                                contentDescription = null,
                                tint = CitizenStatusColors.safe,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Anonimato por Padrão ao Reportar",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Oculta nome, foto de perfil e metadados EXIF automaticamente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = viewModel.anonymousReportingDefault,
                            onCheckedChange = { viewModel.toggleAnonymousDefault(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CitizenStatusColors.safe
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Raio de Notificações Críticas (Geofence)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", viewModel.geofenceRadiusKm)} km",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandOrange,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Slider(
                            value = viewModel.geofenceRadiusKm,
                            onValueChange = { viewModel.updateGeofenceRadius(it) },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandOrange,
                                activeTrackColor = BrandOrange
                            )
                        )
                    }
                }
            }
        }

        // Connected APIs Hub (SSP-SP, Metrô SP, Meteorologia CGE/INMET & Google Cloud)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CitizenIcons.CloudSync,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ecossistema de APIs Conectadas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Segurança Pública SP, Metrô SP, Meteorologia e Google Cloud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(viewModel.integratedApis, key = { it.id }) { api ->
            ConnectedApiStatusCard(api = api)
        }
    }
}

@Composable
private fun ConnectedApiStatusCard(api: ExternalApiConnectionStatus) {
    val categoryColor = when (api.category) {
        "Segurança SP" -> CitizenStatusColors.critical
        "Mobilidade SP" -> Color(0xFF9B3894)
        "Meteorologia" -> BrandOrange
        else -> BrandBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = api.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CitizenStatusColors.safe)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ONLINE • ${api.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = CitizenStatusColors.safe,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = api.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${api.provider} • ${api.endpointSummary}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = api.featuresProvided,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile & APIs - Light")
@Composable
fun PreviewCitizenProfileLight() {
    CitizenSPTheme(darkTheme = false) {
        CitizenProfileScreen()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Profile & APIs - Dark"
)
@Composable
fun PreviewCitizenProfileDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenProfileScreen()
    }
}
