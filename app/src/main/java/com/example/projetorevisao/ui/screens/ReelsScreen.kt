package com.example.projetorevisao.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.RemoteFullImage
import com.example.projetorevisao.ui.designsystem.RcAccentButton
import com.example.projetorevisao.ui.home.streetOf
import com.example.projetorevisao.ui.theme.DarkToneOnBlack
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.IncidentMedia
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

/** Uma página do feed: uma foto ou vídeo de um relato. */
private data class ReelItem(val incident: IncidentAlert, val media: IncidentMedia, val index: Int, val total: Int) {
    val key: String get() = "${incident.id}#$index"
}

private val OnMedia = Color.White
private val OnMediaMuted = Color.White.copy(alpha = 0.78f)

/**
 * Vídeos — feed vertical no modelo Reels/TikTok (docs/design/TELAS.md §2.5).
 *
 * Um item por página, tela cheia, sempre escuro. Toque pausa; toque duplo é "Também vi" (confirmação,
 * não "curtir"). Começa sem som. O feed termina: sem rolagem infinita para prender a pessoa.
 */
@Composable
fun CitizenReelsScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onOpenReportSheet: () -> Unit,
    onOpenIncident: (IncidentAlert) -> Unit = {},
    onSosClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val items = viewModel.incidents
        .filter { it.media.isNotEmpty() }
        .sortedByDescending { it.createdAtEpochMs }
        .flatMap { incident -> incident.media.mapIndexed { i, m -> ReelItem(incident, m, i, incident.media.size) } }
    var muted by rememberSaveable { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (items.isEmpty()) {
            ReelsEmpty(onOpenReportSheet)
        } else {
            val pager = rememberPagerState { items.size + 1 } // +1: página de fim do feed
            VerticalPager(
                state = pager,
                beyondViewportPageCount = 1,
                key = { page -> if (page < items.size) items[page].key else "fim" },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page >= items.size) {
                    ReelsEnd(onOpenReportSheet)
                } else {
                    val item = items[page]
                    ReelPage(
                        item = item,
                        active = pager.currentPage == page,
                        muted = muted,
                        onConfirm = { viewModel.confirmIncident(item.incident.id) },
                        onShare = { context.shareReel(item.incident) },
                        onOpen = { onOpenIncident(item.incident) }
                    )
                }
            }

            val current = items.getOrNull(pager.currentPage)
            ReelsTopBar(
                showSound = current?.media?.isVideo == true,
                muted = muted,
                onToggleSound = { muted = !muted },
                onSosClick = onSosClick
            )
        }
    }
}

@Composable
private fun ReelsTopBar(showSound: Boolean, muted: Boolean, onToggleSound: () -> Unit, onSosClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("Vídeos", style = MaterialTheme.typography.titleLarge, color = OnMedia)
            Text("Perto de você", style = MaterialTheme.typography.bodySmall, color = OnMediaMuted)
        }
        if (showSound) {
            GlassPill(
                text = if (muted) "Sem som" else "Com som",
                description = if (muted) "Ativar som" else "Desativar som",
                onClick = onToggleSound
            )
        }
        GlassPill(text = "SOS", description = "Emergência, abrir tela de SOS", icon = CitizenIcons.Shield, onClick = onSosClick)
    }
}

/** Botão translúcido sobre a mídia (legível sobre qualquer imagem). */
@Composable
private fun GlassPill(text: String, description: String, onClick: () -> Unit, icon: ImageVector? = null) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        contentColor = OnMedia,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .semantics { contentDescription = description }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ReelPage(
    item: ReelItem,
    active: Boolean,
    muted: Boolean,
    onConfirm: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var paused by remember(item.key) { mutableStateOf(false) }
    var expanded by remember(item.key) { mutableStateOf(false) }
    val burst = remember { Animatable(0f) }
    var burstKey by remember { mutableStateOf(0) }

    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        burst.snapTo(0.6f)
        burst.animateTo(1.15f, tween(160))
        burst.animateTo(1f, tween(120))
        kotlinx.coroutines.delay(350)
        burst.animateTo(0f, tween(180))
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(item.key) {
                detectTapGestures(
                    onTap = { if (item.media.isVideo) paused = !paused },
                    onDoubleTap = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!item.incident.userConfirmed) onConfirm()
                        burstKey++
                    }
                )
            }
    ) {
        if (item.media.isVideo) {
            ReelVideo(url = item.media.url, playing = active && !paused, muted = muted)
        } else {
            RemoteFullImage(item.media.url, Modifier.fillMaxSize())
        }

        // Gradientes só nas bordas, para o texto ler sobre qualquer imagem.
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(320.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
        )

        // Pausado: ícone grande no centro.
        AnimatedVisibility(
            visible = paused && item.media.isVideo,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(CitizenIcons.Play, contentDescription = "Pausado", tint = OnMedia, modifier = Modifier.size(40.dp)) }
        }

        // "Também vi" do toque duplo.
        if (burst.value > 0f) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .scale(burst.value)
                    .size(96.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(CitizenIcons.VerifiedCheck, contentDescription = null, tint = OnMedia, modifier = Modifier.size(40.dp))
                    Text("Também vi", style = MaterialTheme.typography.labelMedium, color = OnMedia)
                }
            }
        }

        // Trilho de ações à direita.
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RailAction(
                icon = CitizenIcons.VerifiedCheck,
                label = if (item.incident.confirmations > 0) item.incident.confirmations.toString() else "Vi",
                description = if (item.incident.userConfirmed) "Você confirmou. Toque para desfazer" else "Também vi",
                highlighted = item.incident.userConfirmed,
                onClick = onConfirm
            )
            RailAction(CitizenIcons.LocationPin, "Mapa", "Ver no mapa", onClick = onOpen)
            RailAction(CitizenIcons.Share, "Enviar", "Compartilhar", onClick = onShare)
            RailAction(CitizenIcons.Comment, "Mais", "Detalhes, denunciar ou bloquear", onClick = onOpen)
        }

        // Informações embaixo.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 84.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = MaterialTheme.shapes.small, color = Color.Black.copy(alpha = 0.45f)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(DarkToneOnBlack(item.incident.category.tone), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(item.incident.category.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnMedia)
                    }
                }
                Text(
                    "${item.incident.distanceLabel} · ${item.incident.timeAgo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnMediaMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                item.incident.title,
                style = MaterialTheme.typography.titleMedium,
                color = OnMedia,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClickLabel = "Abrir relato", onClick = onOpen)
            )
            if (item.incident.description.isNotBlank()) {
                Text(
                    item.incident.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnMediaMuted,
                    maxLines = if (expanded) 8 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClickLabel = if (expanded) "Mostrar menos" else "Mostrar mais") { expanded = !expanded }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Relato de morador · ${streetOf(item.incident.address)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnMediaMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (item.total > 1) {
                    Text("${item.index + 1}/${item.total}", style = MaterialTheme.typography.labelMedium, color = OnMediaMuted)
                }
            }
        }
    }
}

@Composable
private fun RailAction(
    icon: ImageVector,
    label: String,
    description: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 56.dp)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Box(
            Modifier
                .size(48.dp)
                .background(if (highlighted) OnMedia else Color.Black.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (highlighted) Color.Black else OnMedia, modifier = Modifier.size(24.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnMedia, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * Vídeo do feed: começa sem som, repete em loop e só toca na página visível.
 * INTEGRAR: trocar VideoView por Media3 ExoPlayer (pré-carregamento e economia de dados).
 */
@Composable
private fun ReelVideo(url: String, playing: Boolean, muted: Boolean) {
    var player by remember(url) { mutableStateOf<MediaPlayer?>(null) }
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    // Baixa para o cache e toca o arquivo local (o servidor ainda não aceita Range; ver VideoCache).
    val localFile by androidx.compose.runtime.produceState<java.io.File?>(null, url) {
        value = com.example.projetorevisao.data.media.VideoCache.get(context, url)
        if (value == null) failed = true
    }
    if (failed) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Não foi possível carregar este vídeo. Verifique a internet e tente de novo.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnMedia,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    val file = localFile
    if (file == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OnMedia, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
        }
        return
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        player = mp
                        loading = false
                    }
                    // Sem o diálogo do sistema: mostra a mensagem do app.
                    setOnErrorListener { _, _, _ -> failed = true; true }
                    setVideoPath(file.absolutePath)
                }
            },
            update = { view ->
                val volume = if (muted) 0f else 1f
                runCatching { player?.setVolume(volume, volume) }
                if (playing && !loading) {
                    if (!view.isPlaying) view.start()
                } else if (view.isPlaying) {
                    view.pause()
                }
            },
            onRelease = { it.stopPlayback() }
        )
        if (loading) CircularProgressIndicator(color = OnMedia, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ReelsEnd(onOpenReportSheet: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(CitizenIcons.VerifiedCheck, contentDescription = null, tint = OnMedia, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Você viu tudo perto de você.", style = MaterialTheme.typography.headlineSmall, color = OnMedia, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Quando alguém publicar uma foto ou vídeo por aqui, aparece neste feed.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnMediaMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        RcAccentButton("Relatar com foto ou vídeo", onClick = onOpenReportSheet)
    }
}

@Composable
private fun ReelsEmpty(onOpenReportSheet: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(CitizenIcons.Reels, contentDescription = null, tint = OnMedia, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Ainda não há fotos ou vídeos perto de você.", style = MaterialTheme.typography.headlineSmall, color = OnMedia, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Relatos com foto ou vídeo aparecem aqui, um por vez, em tela cheia.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnMediaMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        RcAccentButton("Relatar com foto ou vídeo", onClick = onOpenReportSheet)
    }
}

/** Compartilhar pelo seletor do Android; a pessoa escolhe o app e confirma. */
private fun android.content.Context.shareReel(incident: IncidentAlert) {
    val text = "${incident.title} — ${streetOf(incident.address)} (${incident.timeAgo}). " +
        "Relato de morador no app Rede Cidadã, não confirmado pela polícia."
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }, "Compartilhar relato"))
}
