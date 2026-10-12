package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.IncidentCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentModalSheet(
    currentNeighborhood: String,
    defaultAnonymous: Boolean,
    onDismiss: () -> Unit,
    onSubmitReport: (
        category: IncidentCategory,
        title: String,
        description: String,
        address: String,
        isAnonymous: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by rememberSaveable { mutableStateOf(IncidentCategory.ROUBO_FURTO) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("Av. Carlos Caldeira Filho - $currentNeighborhood") }
    var isAnonymous by rememberSaveable { mutableStateOf(defaultAnonymous) }
    var mediaAttached by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reportar Incidente Agora",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Envio rápido em 15s • Triagem Google Cloud IA + SSP-SP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Anonymous Toggle Card (Figma UX Finding: "Anonimato é chave")
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isAnonymous) {
                    CitizenStatusColors.verifiedBadgeBg
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                border = BorderStroke(
                    1.dp,
                    if (isAnonymous) CitizenStatusColors.safe else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isAnonymous) CitizenIcons.VisibilityOff else CitizenIcons.Person,
                            contentDescription = null,
                            tint = if (isAnonymous) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAnonymous) "Denúncia 100% Anônima Ativa" else "Relato com Perfil Público",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isAnonymous) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sua identidade e metadados EXIF são ocultados para sua total proteção.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CitizenStatusColors.safe
                        )
                    )
                }
            }

            // Category Selector Row
            Text(
                text = "Tipo de Ocorrência",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentCategory.entries.forEach { category ->
                    val selected = category == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (category) {
                                    IncidentCategory.ROUBO_FURTO -> CitizenIcons.SosEmergency
                                    IncidentCategory.INCENDIO -> CitizenIcons.Fire
                                    IncidentCategory.ALAGAMENTO -> CitizenIcons.FloodWater
                                    IncidentCategory.TEMPESTADE -> CitizenIcons.Storm
                                    IncidentCategory.TRANSITO -> CitizenIcons.CarIncident
                                    IncidentCategory.OPERACAO_POLICIAL -> CitizenIcons.PoliceShield
                                    IncidentCategory.UTILIDADE_PUBLICA -> CitizenIcons.LocationPin
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.badgeColor.copy(alpha = 0.18f),
                            selectedLabelColor = category.badgeColor,
                            selectedLeadingIconColor = category.badgeColor
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título curto da ocorrência") },
                placeholder = { Text("Ex: Suspeitos em moto próximo à saída do metrô") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Localização (GPS Automático)") },
                leadingIcon = {
                    Icon(
                        imageVector = CitizenIcons.LocationPin,
                        contentDescription = null,
                        tint = BrandOrange
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detalhes para ajudar a comunidade e autoridades") },
                placeholder = { Text("Descreva o que aconteceu, sentido da via ou orientações de segurança...") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Photo / Reel Video Attachment Button
            OutlinedButton(
                onClick = { mediaAttached = !mediaAttached },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (mediaAttached) CitizenStatusColors.safe else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (mediaAttached) CitizenIcons.VerifiedCheck else CitizenIcons.Camera,
                    contentDescription = null,
                    tint = if (mediaAttached) CitizenStatusColors.safe else BrandOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (mediaAttached) {
                        "Foto/Vídeo anexado com desfoque facial automático (IA)"
                    } else {
                        "Anexar Foto ou Vídeo para o Reels de Segurança"
                    },
                    color = if (mediaAttached) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    onSubmitReport(
                        selectedCategory,
                        title,
                        description,
                        address,
                        isAnonymous
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = CitizenIcons.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publicar Alerta na Rede Cidadã (+15 pts)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportIncidentModalSheetPreview() {
    CitizenSPTheme {
        ReportIncidentModalSheet(
            currentNeighborhood = "Vila Mariana",
            defaultAnonymous = true,
            onDismiss = {},
            onSubmitReport = { _, _, _, _, _ -> }
        )
    }
}
