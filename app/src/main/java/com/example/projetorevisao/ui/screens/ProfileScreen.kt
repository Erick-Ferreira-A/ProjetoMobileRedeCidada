package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.ExternalApiConnectionStatus
import com.example.projetorevisao.data.session.LocalProfileStore
import com.example.projetorevisao.data.session.SessionManager
import com.example.projetorevisao.data.session.ageFromBirthDate
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.LegalLinks
import com.example.projetorevisao.ui.designsystem.RcPrimaryButton
import com.example.projetorevisao.ui.designsystem.RcSecondaryButton
import com.example.projetorevisao.ui.designsystem.RcSource
import com.example.projetorevisao.ui.designsystem.RcSourceBadge
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.RcSpacing
import com.example.projetorevisao.ui.theme.RcTheme
import com.example.projetorevisao.ui.theme.RcTone
import com.example.projetorevisao.ui.viewmodel.AuthViewModel
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

/** Faixas do score de confiabilidade (0 a 1000). */
internal data class TrustLevel(val label: String, val tone: RcTone, val explanation: String)

internal fun trustLevelOf(score: Int): TrustLevel = when {
    score >= 900 -> TrustLevel("Muito confiável", RcTone.SAFE, "Seus relatos aparecem primeiro para a vizinhança.")
    score >= 750 -> TrustLevel("Confiável", RcTone.SAFE, "Seus relatos aparecem normalmente.")
    score >= 500 -> TrustLevel("Em construção", RcTone.WARNING, "Relatos confirmados por outras pessoas aumentam seu score.")
    else -> TrustLevel("Em revisão", RcTone.CRITICAL, "Seus relatos passam por revisão antes de aparecer para todos.")
}

/**
 * Perfil (docs/design/TELAS.md §2.8): quem sou, minha confiabilidade e o que o app sabe de mim.
 * Visitante vê o convite para entrar; com conta, editar perfil, score, preferências e conta.
 */
@Composable
fun CitizenProfileScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    authViewModel: AuthViewModel = remember { AuthViewModel() },
    onRequestLogin: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenGuardians: () -> Unit = {},
    onComingSoon: (String) -> Unit = {}
) {
    val context = LocalContext.current
    LocalProfileStore.init(context)
    val local by LocalProfileStore.profile.collectAsState()
    val session by SessionManager.session.collectAsState()
    val loggedIn = viewModel.isLoggedIn
    var showSources by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val name = local.displayName.ifBlank { viewModel.currentUserName?.ifBlank { null } ?: if (loggedIn) "Sua conta" else "Visitante" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = RcSpacing.md, end = RcSpacing.md, top = RcSpacing.xs, bottom = RcSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(RcSpacing.md)
    ) {
        // 1. Quem sou.
        item(key = "header") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Avatar(name = name, avatarPath = local.avatarPath, size = 72)
                Spacer(Modifier.width(RcSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val subtitle = when {
                        !loggedIn -> "Entre para relatar, confirmar relatos e ter guardiões."
                        else -> listOfNotNull(
                            ageFromBirthDate(local.birthDate)?.let { "$it anos" },
                            local.district.ifBlank { session?.neighborhood?.substringBefore(",") }?.ifBlank { null },
                            viewModel.currentUserEmail
                        ).joinToString(" · ")
                    }
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        item(key = "account-action") {
            if (loggedIn) {
                RcSecondaryButton("Editar perfil", onClick = onEditProfile, modifier = Modifier.fillMaxWidth())
            } else {
                RcPrimaryButton("Entrar ou criar conta", onClick = onRequestLogin, modifier = Modifier.fillMaxWidth())
            }
        }

        // 2. Confiabilidade (estilo nota do Uber): o número, a faixa e como funciona.
        item(key = "trust") {
            TrustScoreCard(score = session?.trustScore, loggedIn = loggedIn, onRequestLogin = onRequestLogin)
        }

        // 3. Atalhos.
        item(key = "shortcuts") {
            SectionCard {
                ProfileRow(CitizenIcons.Notifications, "Meus relatos", "O que você publicou e quantos confirmaram", badge = RcSource.ComingSoon) { onComingSoon("Meus relatos") }
                RowDivider()
                ProfileRow(CitizenIcons.Groups, "Guardiões", "Quem recebe seu aviso de SOS") { onOpenGuardians() }
                RowDivider()
                ProfileRow(CitizenIcons.ThumbUp, "Avaliações", "Como a comunidade avaliou seus relatos", badge = RcSource.ComingSoon) { onComingSoon("Avaliações") }
            }
        }

        // 4. Preferências.
        item(key = "prefs") {
            SectionCard(title = "Preferências") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RcSpacing.md, vertical = RcSpacing.sm)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Publicar como anônimo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Já vem marcado ao relatar. Seu nome não aparece para outras pessoas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(RcSpacing.sm))
                    Switch(checked = viewModel.anonymousReportingDefault, onCheckedChange = { viewModel.toggleAnonymousDefault(it) })
                }
                RowDivider()
                ProfileRow(CitizenIcons.Notifications, "Notificações", "Alertas perto de você e do seu trajeto", badge = RcSource.ComingSoon) { onComingSoon("Notificações") }
                RowDivider()
                ProfileRow(CitizenIcons.Play, "Vídeos", "Tocar sozinho: sempre, só no Wi-Fi ou nunca", badge = RcSource.ComingSoon) { onComingSoon("Preferência de vídeos") }
            }
        }

        // 5. Privacidade e dados.
        item(key = "privacy") {
            SectionCard(title = "Privacidade e dados") {
                ProfileRow(CitizenIcons.CloudSync, "Fontes de dados", "De onde vêm as informações do app", trailing = if (showSources) "Ocultar" else "Ver") { showSources = !showSources }
            }
        }
        if (showSources) {
            items(viewModel.integratedApis, key = { it.id }) { api -> DataSourceRow(api) }
        }

        // 6. Conta.
        item(key = "account") {
            SectionCard(title = "Conta") {
                if (loggedIn) {
                    ProfileRow(CitizenIcons.ArrowBack, "Sair da conta", "Seus dados pessoais deste aparelho são apagados") {
                        LocalProfileStore.clear(context)
                        authViewModel.logout(onLoggedOut)
                    }
                    RowDivider()
                    ProfileRow(CitizenIcons.Lock, "Excluir minha conta", "Apaga conta, guardiões e alertas SOS do servidor", danger = true) {
                        authViewModel.clearError()
                        showDeleteDialog = true
                    }
                } else {
                    ProfileRow(CitizenIcons.Person, "Entrar ou criar conta", "Você está usando o app sem conta") { onRequestLogin() }
                }
                Box(Modifier.padding(horizontal = RcSpacing.sm)) { LegalLinks(showDeletion = loggedIn) }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            authViewModel = authViewModel,
            onDismiss = { showDeleteDialog = false },
            onDeleted = {
                showDeleteDialog = false
                LocalProfileStore.clear(context)
                onLoggedOut()
            }
        )
    }
}

/** Foto do perfil ou iniciais. */
@Composable
internal fun Avatar(name: String, avatarPath: String?, size: Int) {
    val bitmap = remember(avatarPath) {
        avatarPath?.let { path -> runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
    }
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = "Foto do perfil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
            Text(initials, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun TrustScoreCard(score: Int?, loggedIn: Boolean, onRequestLogin: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
            Text("Sua confiabilidade", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() })
            if (!loggedIn || score == null) {
                Text(
                    "Entre para ter um score. Ele mostra à vizinhança o quanto seus relatos são confirmados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                RcSecondaryButton("Entrar", onClick = onRequestLogin)
                return@Column
            }
            val level = trustLevelOf(score)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$score", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
                Text(" / 1000", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = level.tone.container, contentColor = level.tone.color) {
                    Text(level.label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            LinearProgressIndicator(
                progress = { (score / 1000f).coerceIn(0f, 1f) },
                color = level.tone.color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )
            Text(level.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Ocultar como funciona" else "Como funciona", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.weight(1f))
                RcSourceBadge(RcSource.ComingSoon)
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "Sobe quando outras pessoas confirmam o que você relatou (\"Também vi\").",
                        "Sobe quando você confirma relatos que depois se mostram verdadeiros.",
                        "Desce quando um relato seu é denunciado e removido pela moderação.",
                        "Score baixo faz seus relatos passarem por revisão antes de aparecer — é o freio contra notícia falsa."
                    ).forEach { line ->
                        Row {
                            Text("•  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Text(
                        "Em integração: hoje todas as contas começam com 940 e o número ainda não muda sozinho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.semantics { heading() })
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) { Column { content() } }
    }
}

@Composable
private fun RowDivider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 56.dp))

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: RcSource? = null,
    trailing: String? = null,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (danger) RcTheme.colors.critical else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = RcSpacing.md, vertical = RcSpacing.sm)
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) color else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(RcSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (badge != null) RcSourceBadge(badge)
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DataSourceRow(api: ExternalApiConnectionStatus) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(api.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(api.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            RcSourceBadge(RcSource.Official(api.provider))
            Text(api.featuresProvided, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DeleteAccountDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!authViewModel.isLoading) onDismiss() },
        title = { Text("Excluir conta?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Sua conta, seus guardiões e seus alertas SOS serão apagados do servidor. Isso não pode ser desfeito. " +
                        "Relatos que você já publicou continuam visíveis, sem seu nome."
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Digite sua senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                authViewModel.errorMessage?.let {
                    Text(it, color = RcTheme.colors.critical, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !authViewModel.isLoading && password.isNotEmpty(),
                onClick = { authViewModel.deleteAccount(password, onDeleted) }
            ) {
                Text(if (authViewModel.isLoading) "Excluindo…" else "Excluir definitivamente", color = RcTheme.colors.critical)
            }
        },
        dismissButton = {
            TextButton(enabled = !authViewModel.isLoading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Preview(showBackground = true, name = "Perfil · claro")
@Composable
fun PreviewCitizenProfileLight() {
    CitizenSPTheme(darkTheme = false) { CitizenProfileScreen() }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Perfil · escuro")
@Composable
fun PreviewCitizenProfileDark() {
    CitizenSPTheme(darkTheme = true) { CitizenProfileScreen() }
}
