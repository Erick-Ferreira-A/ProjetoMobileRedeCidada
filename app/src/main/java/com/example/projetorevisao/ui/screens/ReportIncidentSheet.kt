package com.example.projetorevisao.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.projetorevisao.data.media.CameraCapture
import com.example.projetorevisao.data.media.MediaAttachment
import com.example.projetorevisao.ui.components.LocalMediaThumbnail
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
        isAnonymous: Boolean,
        attachments: List<Uri>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by rememberSaveable { mutableStateOf(IncidentCategory.ROUBO_FURTO) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("Próximo a $currentNeighborhood") }
    var isAnonymous by rememberSaveable { mutableStateOf(defaultAnonymous) }
    var formError by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var attachments by remember { mutableStateOf(listOf<Uri>()) }
    // Seletor de fotos do Android: não precisa de permissão de armazenamento.
    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MediaAttachment.MAX_ATTACHMENTS)
    ) { picked ->
        attachments = (attachments + picked).distinct().take(MediaAttachment.MAX_ATTACHMENTS)
    }

    // --- Câmera do aparelho (foto ou vídeo), com permissão pedida na hora do uso ---
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) } // "photo" ou "video"

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingCaptureUri
        if (saved && uri != null) attachments = (attachments + uri).take(MediaAttachment.MAX_ATTACHMENTS)
        pendingCaptureUri = null
    }
    val captureVideo = rememberLauncherForActivityResult(CameraCapture.LimitedVideoCapture()) { saved ->
        val uri = pendingCaptureUri
        if (saved && uri != null) attachments = (attachments + uri).take(MediaAttachment.MAX_ATTACHMENTS)
        pendingCaptureUri = null
    }

    fun openCamera(action: String) {
        cameraMessage = null
        try {
            if (action == "photo") {
                val uri = CameraCapture.newPhotoUri(context)
                pendingCaptureUri = uri
                takePicture.launch(uri)
            } else {
                val uri = CameraCapture.newVideoUri(context)
                pendingCaptureUri = uri
                captureVideo.launch(uri)
            }
        } catch (_: ActivityNotFoundException) {
            cameraMessage = "Este aparelho não tem um app de câmera disponível."
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pendingCameraAction
        pendingCameraAction = null
        if (granted && action != null) {
            openCamera(action)
        } else if (!granted) {
            cameraMessage = "Sem acesso à câmera não dá para tirar foto pelo app. Você pode liberar nas configurações ou escolher da galeria."
        }
    }

    fun requestCamera(action: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera(action)
        } else {
            pendingCameraAction = action
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
                        text = "O relato fica visível para quem usa o app na região. Não é enviado à polícia: em perigo, ligue 190.",
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
                                text = if (isAnonymous) "Publicar como anônimo" else "Publicar com meu nome",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isAnonymous) CitizenStatusColors.safe else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No modo anônimo, seu nome não aparece para outras pessoas.",
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
                            selectedContainerColor = category.tone.container,
                            selectedLabelColor = category.tone.color,
                            selectedLeadingIconColor = category.tone.color
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; formError = false },
                label = { Text("Título curto da ocorrência") },
                placeholder = { Text("Ex: Suspeitos em moto próximo à saída do metrô") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; formError = false },
                label = { Text("Localização (edite ou informe manualmente)") },
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
                onValueChange = { description = it; formError = false },
                label = { Text("Detalhes do relato") },
                placeholder = { Text("Descreva o que aconteceu, sentido da via ou orientações de segurança...") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (formError) {
                Text(
                    text = "Preencha o título, o endereço e a descrição para salvar o relato.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Photo / Reel Video Attachment Button
            if (attachments.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(attachments, key = { it.toString() }) { uri ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LocalMediaThumbnail(uri = uri, isVideo = MediaAttachment.isVideo(context, uri), size = 76.dp)
                            TextButton(onClick = { attachments = attachments - uri }) { Text("Remover") }
                        }
                    }
                }
            }

            val canAddMore = attachments.size < MediaAttachment.MAX_ATTACHMENTS
            Text(
                text = "Fotos e vídeos (${attachments.size}/${MediaAttachment.MAX_ATTACHMENTS})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AttachButton("Foto", enabled = canAddMore, modifier = Modifier.weight(1f)) { requestCamera("photo") }
                AttachButton("Vídeo", enabled = canAddMore, modifier = Modifier.weight(1f)) { requestCamera("video") }
                AttachButton("Galeria", enabled = canAddMore, modifier = Modifier.weight(1f)) {
                    mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            }
            cameraMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (title.isBlank() || address.isBlank() || description.isBlank()) {
                        formError = true
                        return@Button
                    }
                    onSubmitReport(
                        selectedCategory,
                        title,
                        description,
                        address,
                        isAnonymous,
                        attachments
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
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
                    text = if (attachments.isEmpty()) "Enviar relato" else "Enviar relato com ${attachments.size} anexo(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AttachButton(label: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Icon(CitizenIcons.Camera, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
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
            onSubmitReport = { _, _, _, _, _, _ -> }
        )
    }
}
