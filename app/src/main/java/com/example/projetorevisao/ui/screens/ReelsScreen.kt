package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.theme.BrandNavyDark
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.example.projetorevisao.ui.viewmodel.SafetyReel
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

@Composable
fun CitizenReelsScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onOpenReportSheet: (() -> Unit)? = null
) {
    val activeReel = viewModel.selectedReelForPlayer

    if (activeReel != null) {
        CitizenReelFullScreenPlayer(
            reel = activeReel,
            allReels = viewModel.safetyReels,
            onClose = { viewModel.openReelPlayer(null) },
            onToggleLike = { viewModel.toggleReelLike(activeReel.id) },
            onToggleBookmark = { viewModel.toggleReelBookmark(activeReel.id) },
            onNextReel = { viewModel.navigateReel(1) },
            onPrevReel = { viewModel.navigateReel(-1) }
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vídeos de Segurança",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Relatos visuais verificados da sua região • Toque para assistir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (onOpenReportSheet != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandOrange.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { onOpenReportSheet() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = CitizenIcons.Camera,
                                        contentDescription = null,
                                        tint = BrandOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Enviar Vídeo",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BrandOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(viewModel.safetyReels, key = { it.id }) { reel ->
                ReelGridThumbnailCard(
                    reel = reel,
                    onClick = { viewModel.openReelPlayer(reel) }
                )
            }
        }
    }
}

@Composable
private fun ReelGridThumbnailCard(
    reel: SafetyReel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic atmospheric background representing the incident video feed
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            reel.category.badgeColor.copy(alpha = 0.55f),
                            BrandNavyDark,
                            Color(0xFF070B14)
                        )
                    )
                )
                drawCircle(
                    color = reel.category.badgeColor.copy(alpha = 0.28f),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.7f, size.height * 0.3f)
                )
            }

            // Top category & duration chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = reel.category.badgeColor
                ) {
                    Text(
                        text = reel.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = "0:${reel.durationSeconds}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Center Play Button (Figma 192:288)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CitizenIcons.Play,
                    contentDescription = "Assistir ${reel.title}",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Bottom Metadata Overlay (Figma 192:299 - Title + Views)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reel.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (reel.isOfficialVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = CitizenIcons.VerifiedCheck,
                            contentDescription = "Canal Oficial Verificado",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reel.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CitizenIcons.Play,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reel.viewsLabel} views",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = reel.timeAgo.replace("Há ", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenReelFullScreenPlayer(
    reel: SafetyReel,
    allReels: List<SafetyReel>,
    onClose: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNextReel: () -> Unit,
    onPrevReel: () -> Unit
) {
    val activeIndex = allReels.indexOfFirst { it.id == reel.id }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060A12))
    ) {
        // Simulated vertical 9:16 video feed backdrop (Figma 32:1100)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrandNavyDark,
                        reel.category.badgeColor.copy(alpha = 0.35f),
                        Color(0xFF050811)
                    )
                )
            )
            drawCircle(
                color = reel.category.badgeColor.copy(alpha = 0.22f),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.5f, size.height * 0.38f)
            )
        }

        // Top Progress Bars + Back Controls (Figma 32:1174 & 32:1182)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allReels.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= activeIndex) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.clickable(onClick = onClose)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.ArrowBack,
                            contentDescription = "Voltar para grade",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Grade de Vídeos",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CitizenStatusColors.safe.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, CitizenStatusColors.safe)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.VerifiedCheck,
                            contentDescription = null,
                            tint = CitizenStatusColors.safe,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MÍDIA VERIFICADA IA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Center Visual Indicator of Active Stream
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(reel.category.badgeColor.copy(alpha = 0.26f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (reel.category) {
                        IncidentCategory.INCENDIO -> CitizenIcons.Fire
                        IncidentCategory.ALAGAMENTO -> CitizenIcons.FloodWater
                        IncidentCategory.TEMPESTADE -> CitizenIcons.Storm
                        IncidentCategory.TRANSITO -> CitizenIcons.CarIncident
                        IncidentCategory.OPERACAO_POLICIAL -> CitizenIcons.PoliceShield
                        else -> CitizenIcons.Shield
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "TRANSMISSÃO COMUNITÁRIA • ${reel.viewsLabel} VISUALIZAÇÕES",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.78f),
                letterSpacing = 1.sp
            )
        }

        // Right-side Interaction Column (Figma 32:1147)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ReelActionIconButton(
                icon = CitizenIcons.Favorite,
                label = formatCount(reel.likesCount),
                tint = if (reel.isLiked) Color(0xFFFF4D6D) else Color.White,
                onClick = onToggleLike
            )
            ReelActionIconButton(
                icon = CitizenIcons.Comment,
                label = reel.commentsCount.toString(),
                tint = Color.White,
                onClick = {}
            )
            ReelActionIconButton(
                icon = CitizenIcons.Share,
                label = "Compartilhar",
                tint = Color.White,
                onClick = {}
            )
            ReelActionIconButton(
                icon = CitizenIcons.Bookmark,
                label = if (reel.isBookmarked) "Salvo" else "Salvar",
                tint = if (reel.isBookmarked) BrandOrange else Color.White,
                onClick = onToggleBookmark
            )
        }

        // Bottom Incident Details & Navigation (Figma 32:1114)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                    )
                )
                .padding(start = 16.dp, end = 76.dp, bottom = 18.dp, top = 28.dp)
        ) {
            // Category Badge (Figma 32:1116)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = reel.category.badgeColor
            ) {
                Text(
                    text = reel.category.badgeTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Author Row (Figma 32:1120)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reel.authorName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reel.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (reel.isOfficialVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = CitizenIcons.VerifiedCheck,
                                contentDescription = "Verificado",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${reel.authorRole} • ${reel.timeAgo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reel.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location Pin Row (Figma 32:1138)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = CitizenIcons.LocationPin,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reel.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prev / Next Controls (Figma 32:1197 "↓ Deslize para próximo")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPrevReel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("↑ Anterior", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onNextReel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("↓ Próximo Vídeo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReelActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCount(count: Int): String {
    return if (count >= 1000) String.format("%.1fk", count / 1000f) else count.toString()
}

@Preview(showBackground = true, name = "Reels Grid - Light")
@Composable
fun PreviewCitizenReelsGridLight() {
    CitizenSPTheme(darkTheme = false) {
        CitizenReelsScreen()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Reels Player - Dark"
)
@Composable
fun PreviewCitizenReelsPlayerDark() {
    val vm = rememberCitizenViewModel()
    CitizenSPTheme(darkTheme = true) {
        CitizenReelFullScreenPlayer(
            reel = vm.safetyReels.first(),
            allReels = vm.safetyReels,
            onClose = {},
            onToggleLike = {},
            onToggleBookmark = {},
            onNextReel = {},
            onPrevReel = {}
        )
    }
}
