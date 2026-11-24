package com.example.projetorevisao.ui.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.theme.RcTone
import kotlin.math.cos
import kotlin.math.sin

/** Condição do tempo como a pessoa fala: ensolarado, nublado, chuva, tempestade. */
enum class RcWeatherKind(val label: String) {
    SUNNY("Ensolarado"),
    CLEAR_NIGHT("Céu limpo"),
    PARTLY_CLOUDY("Parcialmente nublado"),
    CLOUDY("Nublado"),
    DRIZZLE("Garoa"),
    RAIN("Chuva"),
    STORM("Tempestade")
}

/**
 * Converte o rótulo vindo do serviço de clima (Open-Meteo/WMO) para [RcWeatherKind].
 * À noite, "céu limpo" vira lua em vez de sol.
 */
fun weatherKindOf(conditionLabel: String, hourOfDay: Int): RcWeatherKind {
    val c = conditionLabel.lowercase()
    val night = hourOfDay < 6 || hourOfDay >= 18
    return when {
        "tempestade" in c || "raio" in c -> RcWeatherKind.STORM
        "chuva" in c || "pancada" in c -> RcWeatherKind.RAIN
        "garoa" in c || "chuvisco" in c -> RcWeatherKind.DRIZZLE
        "parcialmente" in c -> RcWeatherKind.PARTLY_CLOUDY
        "nublado" in c || "encoberto" in c || "nevoeiro" in c -> RcWeatherKind.CLOUDY
        else -> if (night) RcWeatherKind.CLEAR_NIGHT else RcWeatherKind.SUNNY
    }
}

/** Ícone de clima desenhado no traço do app (sem pacote extra de ícones). */
@Composable
fun RcWeatherGlyph(kind: RcWeatherKind, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    val cloud = MaterialTheme.colorScheme.onSurfaceVariant
    val sun = RcTone.WARNING.color
    val water = RcTone.INFO.color
    Canvas(modifier.size(size)) {
        val w = this.size.width
        when (kind) {
            RcWeatherKind.SUNNY -> drawSun(Offset(w * .5f, w * .5f), w * .2f, sun)
            RcWeatherKind.CLEAR_NIGHT -> drawMoon(w, sun)
            RcWeatherKind.PARTLY_CLOUDY -> {
                drawSun(Offset(w * .36f, w * .34f), w * .14f, sun)
                drawCloud(w, cloud, top = .40f)
            }
            RcWeatherKind.CLOUDY -> drawCloud(w, cloud, top = .28f)
            RcWeatherKind.DRIZZLE -> {
                drawCloud(w, cloud, top = .14f)
                drawDrops(w, water, count = 2)
            }
            RcWeatherKind.RAIN -> {
                drawCloud(w, cloud, top = .14f)
                drawDrops(w, water, count = 3)
            }
            RcWeatherKind.STORM -> {
                drawCloud(w, cloud, top = .14f)
                val bolt = Path().apply {
                    moveTo(w * .52f, w * .58f); lineTo(w * .38f, w * .80f); lineTo(w * .50f, w * .80f)
                    lineTo(w * .42f, w * .98f); lineTo(w * .64f, w * .72f); lineTo(w * .52f, w * .72f); close()
                }
                drawPath(bolt, sun)
            }
        }
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float, color: androidx.compose.ui.graphics.Color) {
    drawCircle(color, radius, center)
    val stroke = radius * .32f
    repeat(8) { i ->
        val a = Math.toRadians(i * 45.0)
        val start = Offset(center.x + cos(a).toFloat() * radius * 1.45f, center.y + sin(a).toFloat() * radius * 1.45f)
        val end = Offset(center.x + cos(a).toFloat() * radius * 2.0f, center.y + sin(a).toFloat() * radius * 2.0f)
        drawLine(color, start, end, strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawMoon(w: Float, color: androidx.compose.ui.graphics.Color) {
    val full = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(w * .5f, w * .5f), w * .34f)) }
    val bite = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(w * .66f, w * .36f), w * .30f)) }
    drawPath(Path.combine(PathOperation.Difference, full, bite), color)
}

/** Nuvem: três círculos e uma base, na mesma cor (sem contorno). [top] é a fração da altura onde ela começa. */
private fun DrawScope.drawCloud(w: Float, color: androidx.compose.ui.graphics.Color, top: Float) {
    val base = w * (top + .34f)
    drawCircle(color, w * .17f, Offset(w * .32f, base - w * .02f))
    drawCircle(color, w * .22f, Offset(w * .52f, base - w * .10f))
    drawCircle(color, w * .15f, Offset(w * .72f, base))
    drawRoundRect(
        color,
        topLeft = Offset(w * .16f, base - w * .04f),
        size = androidx.compose.ui.geometry.Size(w * .70f, w * .19f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .1f)
    )
}

private fun DrawScope.drawDrops(w: Float, color: androidx.compose.ui.graphics.Color, count: Int) {
    val xs = if (count == 2) listOf(.40f, .60f) else listOf(.32f, .50f, .68f)
    xs.forEach { x ->
        drawLine(
            color,
            Offset(w * x, w * .70f),
            Offset(w * (x - .06f), w * .90f),
            strokeWidth = w * .08f,
            cap = StrokeCap.Round
        )
    }
}
