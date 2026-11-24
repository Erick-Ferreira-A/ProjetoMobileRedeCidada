package com.example.projetorevisao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.theme.RcTheme
import java.util.Calendar

/** Destinos da barra inferior (TELAS §1). As rotas antigas foram mantidas para não quebrar o NavHost. */
enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Início", CitizenIcons.Home),
    MOBILITY("metro", "Mobilidade", CitizenIcons.Train),
    MEDIA("reels", "Vídeos", CitizenIcons.Reels),
    PROFILE("profile", "Perfil", CitizenIcons.Person)
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Bom dia"
    in 12..17 -> "Boa tarde"
    else -> "Boa noite"
}

/**
 * Barra superior (TELAS §1): contexto à esquerda e o escudo de SOS à direita, em todas as abas.
 * No Início mostra saudação + área; nas outras abas, o nome da aba. Na tela de SOS vira "Voltar".
 */
@Composable
fun CitizenAppTopBar(
    currentScreen: String,
    areaLabel: String = "São Paulo",
    onBack: () -> Unit = {},
    onSosClick: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentScreen == "sos") {
                IconButton(onClick = onBack) {
                    Icon(CitizenIcons.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }

            Column(Modifier.weight(1f)) {
                when (currentScreen) {
                    "home" -> {
                        Text(
                            greeting(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            areaLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    else -> Text(
                        text = when (currentScreen) {
                            "metro" -> "Mobilidade"
                            "reels" -> "Vídeos"
                            "sos" -> "Emergência"
                            "profile" -> "Perfil"
                            else -> "Rede Cidadã"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }

            if (currentScreen != "sos") SosShieldButton(onClick = onSosClick)
        }
    }
}

/** Escudo de SOS: pequeno, sempre no mesmo lugar, 48 dp de toque. O vermelho grande só dentro da tela. */
@Composable
private fun SosShieldButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = RcTheme.colors.criticalContainer,
        contentColor = RcTheme.colors.critical,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Emergência, abrir tela de SOS"
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(CitizenIcons.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("SOS", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Barra inferior (TELAS §1): Início · Mobilidade · [Relatar] · Vídeos · Perfil.
 * Rótulos sempre visíveis; Relatar no centro, no alcance do polegar (padrão Instagram/TikTok).
 */
@Composable
fun CitizenBottomNavBar(
    currentScreen: String,
    onSelectScreen: (String) -> Unit,
    onOpenReportSheet: () -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            TabItem(MainTab.HOME, currentScreen, onSelectScreen)
            TabItem(MainTab.MOBILITY, currentScreen, onSelectScreen)
            ReportAction(onOpenReportSheet)
            TabItem(MainTab.MEDIA, currentScreen, onSelectScreen)
            TabItem(MainTab.PROFILE, currentScreen, onSelectScreen)
        }
    }
}

@Composable
private fun RowScope.TabItem(tab: MainTab, currentScreen: String, onSelect: (String) -> Unit) {
    val selected = currentScreen == tab.route
    NavigationBarItem(
        selected = selected,
        onClick = { onSelect(tab.route) },
        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(24.dp)) },
        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/** Ação central: não é uma aba, abre a sheet de relato. Laranja com ícone marinho (nunca branco). */
@Composable
private fun RowScope.ReportAction(onClick: () -> Unit) {
    // A coluna inteira é o alvo de toque (maior que 48 dp), e as medidas do desenho repetem as das
    // abas (indicador 56×32, 12 dp do topo, 4 dp até o rótulo) para o rótulo alinhar com os outros.
    Column(
        modifier = Modifier
            .weight(1f)
            .height(80.dp) // altura da NavigationBar do M3
            .clickable(role = Role.Button, onClickLabel = "Relatar", onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "Relatar o que está acontecendo" }
            .padding(top = 10.5.dp), // medido no Pixel 8: alinha com o indicador das abas
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .background(RcTheme.colors.accent, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(CitizenIcons.Add, contentDescription = null, tint = RcTheme.colors.onAccent, modifier = Modifier.size(24.dp))
        }
        Text(
            "Relatar",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
