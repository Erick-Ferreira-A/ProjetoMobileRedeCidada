package com.example.projetorevisao.ui.designsystem

import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.theme.RcAppTheme
import com.example.projetorevisao.ui.theme.RcTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Botão de SOS que só aciona depois de **segurar por [holdMillis]** (guia v1.0 §6, TELAS §2.7).
 *
 * - Evita acionamento no bolso ou por toque acidental.
 * - O anel enche enquanto a pessoa segura; soltar antes recolhe o anel e não faz nada.
 * - Vibração curta ao começar e longa ao completar (exceto em [discreet]).
 * - Com "remover animações" ligado no Android, o anel vira contagem em texto ("2", "1").
 * - TalkBack: tocar duas vezes e segurar (ação de toque longo) aciona direto.
 */
@Composable
fun RcSosHoldButton(
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
    holdMillis: Int = 2_000,
    busy: Boolean = false,
    discreet: Boolean = false,
    size: Dp = 176.dp
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val reduceMotion = rememberReducedMotion()
    val critical = RcTheme.colors.critical
    val criticalContainer = RcTheme.colors.criticalContainer
    val track = MaterialTheme.colorScheme.outlineVariant

    val secondsLeft = ceil((1f - progress.value) * holdMillis / 1000f).toInt().coerceAtLeast(1)
    val holding = progress.value > 0f

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                role = Role.Button
                contentDescription = "SOS. Toque duas vezes e segure para avisar seus guardiões"
                stateDescription = if (busy) "Preparando aviso" else "Pronto"
                onLongClick(label = "Acionar SOS") { if (!busy) onActivate(); true }
            }
            .pointerInput(busy) {
                if (busy) return@pointerInput
                detectTapGestures(
                    onPress = {
                        if (!discreet) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        holdJob = scope.launch {
                            progress.animateTo(1f, tween(durationMillis = holdMillis, easing = LinearEasing))
                            if (!discreet) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onActivate()
                            progress.snapTo(0f)
                        }
                        val released = tryAwaitRelease()
                        if (progress.value < 1f) {
                            holdJob?.cancel()
                            scope.launch {
                                if (reduceMotion) progress.snapTo(0f) else progress.animateTo(0f, tween(180))
                            }
                        }
                        if (!released) holdJob?.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Anel de progresso (some com "remover animações": a contagem em texto substitui).
        if (!reduceMotion) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 8.dp.toPx()
                val inset = stroke / 2
                val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                drawArc(critical, -90f, 360f * progress.value, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Surface(
            shape = CircleShape,
            color = if (holding) critical else criticalContainer,
            contentColor = if (holding) MaterialTheme.colorScheme.surface else critical,
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp, color = critical)
                    Text("Preparando…", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text(
                        text = if (holding && reduceMotion) "$secondsLeft" else "SOS",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = if (holding) "continue segurando" else "segure 2 s",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** true quando "Remover animações" está ligado (escala de animação 0). */
@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

@Preview(name = "SOS · claro")
@Composable
private fun SosHoldLightPreview() = RcAppTheme(darkTheme = false) {
    Surface(color = MaterialTheme.colorScheme.background) { RcSosHoldButton(onActivate = {}, modifier = Modifier.padding(24.dp)) }
}

@Preview(name = "SOS · escuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SosHoldDarkPreview() = RcAppTheme(darkTheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) { RcSosHoldButton(onActivate = {}, modifier = Modifier.padding(24.dp)) }
}
