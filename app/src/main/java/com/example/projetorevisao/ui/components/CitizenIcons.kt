package com.example.projetorevisao.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandOrange

/**
 * Logo Oficial "Tutela Cidadã / Rede Cidadã" (fiel ao Figma PROJETO UX - IHC V2):
 * Escudo azul-marinho com contorno interno e símbolo central laranja de rede comunitária (3 cidadãos conectados).
 */
@Composable
fun RedeCidadaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    shieldColor: Color = BrandBlue,
    coreColor: Color = BrandOrange,
    innerSurfaceColor: Color = Color.White
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer Shield Path
            val outerShield = Path().apply {
                moveTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.88f, h * 0.20f)
                lineTo(w * 0.88f, h * 0.48f)
                cubicTo(
                    w * 0.88f, h * 0.72f,
                    w * 0.71f, h * 0.88f,
                    w * 0.50f, h * 0.95f
                )
                cubicTo(
                    w * 0.29f, h * 0.88f,
                    w * 0.12f, h * 0.72f,
                    w * 0.12f, h * 0.48f
                )
                lineTo(w * 0.12f, h * 0.20f)
                close()
            }

            // Inner Shield Path
            val innerShield = Path().apply {
                moveTo(w * 0.50f, h * 0.15f)
                lineTo(w * 0.79f, h * 0.26f)
                lineTo(w * 0.79f, h * 0.47f)
                cubicTo(
                    w * 0.79f, h * 0.66f,
                    w * 0.66f, h * 0.79f,
                    w * 0.50f, h * 0.85f
                )
                cubicTo(
                    w * 0.34f, h * 0.79f,
                    w * 0.21f, h * 0.66f,
                    w * 0.21f, h * 0.47f
                )
                lineTo(w * 0.21f, h * 0.26f)
                close()
            }

            drawPath(path = outerShield, color = shieldColor)
            drawPath(path = innerShield, color = innerSurfaceColor)
            drawPath(
                path = innerShield,
                color = shieldColor,
                style = Stroke(width = w * 0.035f, join = StrokeJoin.Round)
            )

            // Orange Community Network Core (3 connected nodes/people inside the shield)
            val strokeW = w * 0.055f
            val center = Offset(w * 0.50f, h * 0.50f)
            val topNode = Offset(w * 0.50f, h * 0.33f)
            val leftNode = Offset(w * 0.36f, h * 0.58f)
            val rightNode = Offset(w * 0.64f, h * 0.58f)

            // Connecting arcs / arms
            drawLine(
                color = coreColor,
                start = topNode,
                end = leftNode,
                strokeWidth = strokeW * 0.75f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = coreColor,
                start = leftNode,
                end = rightNode,
                strokeWidth = strokeW * 0.75f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = coreColor,
                start = rightNode,
                end = topNode,
                strokeWidth = strokeW * 0.75f,
                cap = StrokeCap.Round
            )

            // Central pulse hub
            drawCircle(
                color = innerSurfaceColor,
                radius = w * 0.09f,
                center = center
            )
            drawCircle(
                color = coreColor,
                radius = w * 0.055f,
                center = center
            )

            // 3 Citizen Heads/Nodes
            drawCircle(color = coreColor, radius = w * 0.065f, center = topNode)
            drawCircle(color = coreColor, radius = w * 0.065f, center = leftNode)
            drawCircle(color = coreColor, radius = w * 0.065f, center = rightNode)
        }
    }
}

/**
 * Ícones Vetoriais baseados no Google Material Symbols (https://fonts.google.com/icons)
 * Viewport padrão 24x24dp para nitidez em qualquer densidade de tela Android.
 */
object CitizenIcons {

    private fun materialSymbol(name: String, block: androidx.compose.ui.graphics.vector.ImageVector.Builder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
    }

    val Shield: ImageVector by lazy {
        materialSymbol("Shield") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                lineTo(4f, 5f)
                verticalLineToRelative(6.09f)
                curveToRelative(0f, 5.05f, 3.41f, 9.76f, 8f, 10.91f)
                curveToRelative(4.59f, -1.15f, 8f, -5.86f, 8f, -10.91f)
                verticalLineTo(5f)
                lineTo(12f, 2f)
                close()
                moveTo(18f, 11.09f)
                curveToRelative(0f, 4f, -2.55f, 7.7f, -6f, 8.83f)
                curveToRelative(-3.45f, -1.13f, -6f, -4.82f, -6f, -8.83f)
                verticalLineTo(6.31f)
                lineToRelative(6f, -2.12f)
                lineToRelative(6f, 2.12f)
                verticalLineToRelative(4.78f)
                close()
            }
        }
    }

    val Home: ImageVector by lazy {
        materialSymbol("Home") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                lineTo(4f, 9f)
                verticalLineToRelative(12f)
                horizontalLineToRelative(6f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(6f)
                verticalLineTo(9f)
                lineTo(12f, 3f)
                close()
            }
        }
    }

    val Map: ImageVector by lazy {
        materialSymbol("Map") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.5f, 3f)
                lineToRelative(-0.16f, 0.03f)
                lineTo(15f, 5.1f)
                lineTo(9f, 3f)
                lineTo(3.36f, 4.9f)
                curveTo(3.15f, 4.97f, 3f, 5.15f, 3f, 5.38f)
                verticalLineTo(20.5f)
                curveTo(3f, 20.78f, 3.22f, 21f, 3.5f, 21f)
                lineToRelative(0.16f, -0.03f)
                lineTo(9f, 18.9f)
                lineToRelative(6f, 2.1f)
                lineToRelative(5.64f, -1.9f)
                curveToRelative(0.21f, -0.07f, 0.36f, -0.25f, 0.36f, -0.48f)
                verticalLineTo(3.5f)
                curveTo(21f, 3.22f, 20.78f, 3f, 20.5f, 3f)
                close()
                moveTo(15f, 19f)
                lineToRelative(-6f, -2.11f)
                verticalLineTo(5f)
                lineToRelative(6f, 2.11f)
                verticalLineTo(19f)
                close()
            }
        }
    }

    val Reels: ImageVector by lazy {
        materialSymbol("Reels") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 4f)
                lineToRelative(2f, 4f)
                horizontalLineToRelative(-3f)
                lineToRelative(-2f, -4f)
                horizontalLineToRelative(-2f)
                lineToRelative(2f, 4f)
                horizontalLineToRelative(-3f)
                lineToRelative(-2f, -4f)
                horizontalLineTo(8f)
                lineToRelative(2f, 4f)
                horizontalLineTo(7f)
                lineTo(5f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(4f)
                horizontalLineToRelative(-4f)
                close()
                moveTo(10f, 11f)
                lineToRelative(5f, 3f)
                lineToRelative(-5f, 3f)
                verticalLineToRelative(-6f)
                close()
            }
        }
    }

    val Groups: ImageVector by lazy {
        materialSymbol("Groups") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 11f)
                curveToRelative(1.66f, 0f, 2.99f, -1.34f, 2.99f, -3f)
                reflectiveCurveTo(17.66f, 5f, 16f, 5f)
                curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                close()
                moveTo(8f, 11f)
                curveToRelative(1.66f, 0f, 2.99f, -1.34f, 2.99f, -3f)
                reflectiveCurveTo(9.66f, 5f, 8f, 5f)
                curveTo(6.34f, 5f, 5f, 6.34f, 5f, 8f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                close()
                moveTo(8f, 13f)
                curveToRelative(-2.33f, 0f, -7f, 1.17f, -7f, 3.5f)
                verticalLineTo(19f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0f, -2.33f, -4.67f, -3.5f, -7f, -3.5f)
                close()
                moveTo(16f, 13f)
                curveToRelative(-0.29f, 0f, -0.62f, 0.02f, -0.97f, 0.05f)
                curveToRelative(1.16f, 0.84f, 1.97f, 1.97f, 1.97f, 3.45f)
                verticalLineTo(19f)
                horizontalLineToRelative(6f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0f, -2.33f, -4.67f, -3.5f, -7f, -3.5f)
                close()
            }
        }
    }

    val Train: ImageVector by lazy {
        materialSymbol("Train") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveToRelative(-4f, 0f, -8f, 0.5f, -8f, 4f)
                verticalLineToRelative(9.5f)
                curveTo(4f, 17.43f, 5.57f, 19f, 7.5f, 19f)
                lineTo(6f, 20.5f)
                verticalLineToRelative(0.5f)
                horizontalLineToRelative(2.23f)
                lineToRelative(2f, -2f)
                horizontalLineTo(14f)
                lineToRelative(2f, 2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-0.5f)
                lineTo(16.5f, 19f)
                curveToRelative(1.93f, 0f, 3.5f, -1.57f, 3.5f, -3.5f)
                verticalLineTo(6f)
                curveToRelative(0f, -3.5f, -3.58f, -4f, -8f, -4f)
                close()
                moveTo(7.5f, 17f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 14f, 7.5f, 14f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 17f, 7.5f, 17f)
                close()
                moveTo(11f, 10f)
                horizontalLineTo(6f)
                verticalLineTo(6f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(4f)
                close()
                moveTo(13f, 10f)
                verticalLineTo(6f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(-5f)
                close()
                moveTo(16.5f, 17f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
    }

    val SosEmergency: ImageVector by lazy {
        materialSymbol("SosEmergency") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                lineTo(1f, 21f)
                horizontalLineToRelative(22f)
                lineTo(12f, 2f)
                close()
                moveTo(13f, 18f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                close()
                moveTo(13f, 14f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(4f)
                close()
            }
        }
    }

    val Person: ImageVector by lazy {
        materialSymbol("Person") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f)
                curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
                reflectiveCurveToRelative(-1.79f, -4f, -4f, -4f)
                reflectiveCurveToRelative(-4f, 1.79f, -4f, 4f)
                reflectiveCurveToRelative(1.79f, 4f, 4f, 4f)
                close()
                moveTo(12f, 14f)
                curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(-2f)
                curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f)
                close()
            }
        }
    }

    val Notifications: ImageVector by lazy {
        materialSymbol("Notifications") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 22f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                horizontalLineToRelative(-4f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
                close()
                moveTo(18f, 16f)
                verticalLineToRelative(-5f)
                curveToRelative(0f, -3.07f, -1.64f, -5.64f, -4.5f, -6.32f)
                verticalLineTo(4f)
                curveToRelative(0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
                verticalLineToRelative(0.68f)
                curveTo(7.63f, 5.36f, 6f, 7.92f, 6f, 11f)
                verticalLineToRelative(5f)
                lineToRelative(-2f, 2f)
                verticalLineToRelative(1f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(-1f)
                lineToRelative(-2f, -2f)
                close()
            }
        }
    }

    val Search: ImageVector by lazy {
        materialSymbol("Search") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f)
                horizontalLineToRelative(-0.79f)
                lineToRelative(-0.28f, -0.27f)
                curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                reflectiveCurveTo(3f, 5.91f, 3f, 9.5f)
                reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
                curveToRelative(1.61f, 0f, 3.09f, -0.59f, 4.23f, -1.57f)
                lineToRelative(0.27f, 0.28f)
                verticalLineToRelative(0.79f)
                lineToRelative(5f, 4.99f)
                lineTo(20.49f, 19f)
                lineToRelative(-4.99f, -5f)
                close()
                moveTo(9.5f, 14f)
                curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
                reflectiveCurveTo(14f, 7.01f, 14f, 9.5f)
                reflectiveCurveTo(11.99f, 14f, 9.5f, 14f)
                close()
            }
        }
    }

    val LocationPin: ImageVector by lazy {
        materialSymbol("LocationPin") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
                curveToRelative(0f, 5.25f, 7f, 13f, 7f, 13f)
                reflectiveCurveToRelative(7f, -7.75f, 7f, -13f)
                curveToRelative(0f, -3.87f, -3.13f, -7f, -7f, -7f)
                close()
                moveTo(12f, 11.5f)
                curveToRelative(-1.38f, 0f, -2.5f, -1.12f, -2.5f, -2.5f)
                reflectiveCurveToRelative(1.12f, -2.5f, 2.5f, -2.5f)
                reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f)
                reflectiveCurveToRelative(-1.12f, 2.5f, -2.5f, 2.5f)
                close()
            }
        }
    }

    val MyLocation: ImageVector by lazy {
        materialSymbol("MyLocation") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 8f)
                curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f)
                reflectiveCurveToRelative(1.79f, 4f, 4f, 4f)
                reflectiveCurveToRelative(4f, -1.79f, 4f, -4f)
                reflectiveCurveToRelative(-1.79f, -4f, -4f, -4f)
                close()
                moveTo(20.94f, 11f)
                curveToRelative(-0.46f, -4.17f, -3.77f, -7.48f, -7.94f, -7.94f)
                verticalLineTo(1f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(2.06f)
                curveTo(6.83f, 3.52f, 3.52f, 6.83f, 3.06f, 11f)
                horizontalLineTo(1f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2.06f)
                curveToRelative(0.46f, 4.17f, 3.77f, 7.48f, 7.94f, 7.94f)
                verticalLineTo(23f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-2.06f)
                curveToRelative(4.17f, -0.46f, 7.48f, -3.77f, 7.94f, -7.94f)
                horizontalLineTo(23f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-2.06f)
                close()
                moveTo(12f, 19f)
                curveToRelative(-3.87f, 0f, -7f, -3.13f, -7f, -7f)
                reflectiveCurveToRelative(3.13f, -7f, 7f, -7f)
                reflectiveCurveToRelative(7f, 3.13f, 7f, 7f)
                reflectiveCurveToRelative(-3.13f, 7f, -7f, 7f)
                close()
            }
        }
    }

    val Layers: ImageVector by lazy {
        materialSymbol("Layers") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.99f, 18.54f)
                lineToRelative(-7.37f, -5.73f)
                lineTo(3f, 14.07f)
                lineToRelative(9f, 7f)
                lineToRelative(9f, -7f)
                lineToRelative(-1.63f, -1.27f)
                lineToRelative(-7.38f, 5.74f)
                close()
                moveTo(12f, 16f)
                lineToRelative(7.36f, -5.73f)
                lineTo(21f, 9f)
                lineToRelative(-9f, -7f)
                lineToRelative(-9f, 7f)
                lineToRelative(1.63f, 1.27f)
                lineTo(12f, 16f)
                close()
            }
        }
    }

    val VerifiedCheck: ImageVector by lazy {
        materialSymbol("VerifiedCheck") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineToRelative(-1.42f, 1.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
    }

    val Fire: ImageVector by lazy {
        materialSymbol("Fire") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.5f, 0.67f)
                reflectiveCurveToRelative(0.74f, 2.65f, 0.74f, 4.8f)
                curveToRelative(0f, 2.06f, -1.35f, 3.73f, -3.41f, 3.73f)
                curveToRelative(-2.07f, 0f, -3.63f, -1.67f, -3.63f, -3.73f)
                lineToRelative(0.03f, -0.36f)
                curveTo(5.21f, 7.51f, 4f, 10.62f, 4f, 14f)
                curveToRelative(0f, 4.42f, 3.58f, 8f, 8f, 8f)
                reflectiveCurveToRelative(8f, -3.58f, 8f, -8f)
                curveTo(20f, 8.61f, 17.41f, 3.8f, 13.5f, 0.67f)
                close()
                moveTo(11.71f, 19f)
                curveToRelative(-1.78f, 0f, -3.22f, -1.4f, -3.22f, -3.14f)
                curveToRelative(0f, -1.62f, 1.05f, -2.76f, 2.81f, -3.12f)
                curveToRelative(1.77f, -0.36f, 3.6f, -1.21f, 4.62f, -2.58f)
                curveToRelative(0.39f, 1.29f, 0.59f, 2.65f, 0.59f, 4.04f)
                curveToRelative(0f, 2.65f, -2.15f, 4.8f, -4.8f, 4.8f)
                close()
            }
        }
    }

    val FloodWater: ImageVector by lazy {
        materialSymbol("FloodWater") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveToRelative(-5.33f, 4.55f, -8f, 8.48f, -8f, 11.8f)
                curveToRelative(0f, 4.98f, 3.8f, 8.2f, 8f, 8.2f)
                reflectiveCurveToRelative(8f, -3.22f, 8f, -8.2f)
                curveToRelative(0f, -3.32f, -2.67f, -7.25f, -8f, -11.8f)
                close()
                moveTo(7.83f, 14f)
                curveToRelative(0.37f, 0f, 0.67f, 0.26f, 0.74f, 0.62f)
                curveToRelative(0.41f, 2.22f, 2.28f, 2.98f, 3.64f, 2.87f)
                curveToRelative(0.43f, -0.02f, 0.79f, 0.32f, 0.79f, 0.75f)
                curveToRelative(0f, 0.4f, -0.32f, 0.73f, -0.72f, 0.75f)
                curveToRelative(-2.13f, 0.13f, -4.62f, -1.09f, -5.19f, -4.12f)
                curveTo(7.01f, 14.42f, 7.37f, 14f, 7.83f, 14f)
                close()
            }
        }
    }

    val Storm: ImageVector by lazy {
        materialSymbol("Storm") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 2f)
                verticalLineToRelative(11f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(9f)
                lineToRelative(7f, -12f)
                horizontalLineToRelative(-4f)
                lineToRelative(4f, -8f)
                close()
            }
        }
    }

    val CarIncident: ImageVector by lazy {
        materialSymbol("CarIncident") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5f, 17.5f, 5f)
                horizontalLineToRelative(-11f)
                curveToRelative(-0.66f, 0f, -1.21f, 0.42f, -1.42f, 1.01f)
                lineTo(3f, 12f)
                verticalLineToRelative(8f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(1f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                verticalLineToRelative(-1f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(1f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(1f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                verticalLineToRelative(-8f)
                lineToRelative(-2.08f, -5.99f)
                close()
                moveTo(6.5f, 16f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 13f, 6.5f, 13f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(7.33f, 16f, 6.5f, 16f)
                close()
                moveTo(17.5f, 16f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(5f, 11f)
                lineToRelative(1.5f, -4.5f)
                horizontalLineToRelative(11f)
                lineTo(19f, 11f)
                horizontalLineTo(5f)
                close()
            }
        }
    }

    val PoliceShield: ImageVector by lazy {
        materialSymbol("PoliceShield") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                lineTo(3f, 5f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 5.55f, 3.84f, 10.74f, 9f, 12f)
                curveToRelative(5.16f, -1.26f, 9f, -6.45f, 9f, -12f)
                verticalLineTo(5f)
                lineToRelative(-9f, -4f)
                close()
                moveTo(14.5f, 12.59f)
                lineToRelative(0.9f, 3.88f)
                lineTo(12f, 14.42f)
                lineToRelative(-3.4f, 2.05f)
                lineToRelative(0.9f, -3.87f)
                lineToRelative(-3f, -2.59f)
                lineToRelative(3.96f, -0.34f)
                lineTo(12f, 6.02f)
                lineToRelative(1.54f, 3.65f)
                lineTo(17.5f, 10f)
                lineToRelative(-3f, 2.59f)
                close()
            }
        }
    }

    val PhoneCall: ImageVector by lazy {
        materialSymbol("PhoneCall") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.62f, 10.79f)
                curveToRelative(1.44f, 2.83f, 3.76f, 5.14f, 6.59f, 6.59f)
                lineToRelative(2.2f, -2.2f)
                curveToRelative(0.27f, -0.27f, 0.67f, -0.36f, 1.02f, -0.24f)
                curveToRelative(1.12f, 0.37f, 2.33f, 0.57f, 3.57f, 0.57f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                verticalLineTo(20f)
                curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
                curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
                curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                curveToRelative(0f, 1.25f, 0.2f, 2.45f, 0.57f, 3.57f)
                curveToRelative(0.11f, 0.35f, 0.03f, 0.74f, -0.25f, 1.02f)
                lineToRelative(-2.2f, 2.2f)
                close()
            }
        }
    }

    val Lock: ImageVector by lazy {
        materialSymbol("Lock") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 8f)
                horizontalLineToRelative(-1f)
                verticalLineTo(6f)
                curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
                reflectiveCurveTo(7f, 3.24f, 7f, 6f)
                verticalLineToRelative(2f)
                horizontalLineTo(6f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(12f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(10f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(12f, 17f)
                curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
                reflectiveCurveToRelative(0.9f, -2f, 2f, -2f)
                reflectiveCurveToRelative(2f, 0.9f, 2f, 2f)
                reflectiveCurveToRelative(-0.9f, 2f, -2f, 2f)
                close()
                moveTo(15.1f, 8f)
                horizontalLineTo(8.9f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                curveToRelative(1.71f, 0f, 3.1f, 1.39f, 3.1f, 3.1f)
                verticalLineToRelative(2f)
                close()
            }
        }
    }

    val VisibilityOff: ImageVector by lazy {
        materialSymbol("VisibilityOff") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f)
                curveToRelative(0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
                lineToRelative(2.92f, 2.92f)
                curveToRelative(1.51f, -1.26f, 2.7f, -2.89f, 3.43f, -4.75f)
                curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
                curveToRelative(-1.4f, 0f, -2.74f, 0.25f, -3.98f, 0.7f)
                lineToRelative(2.16f, 2.16f)
                curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
                close()
                moveTo(2f, 4.27f)
                lineToRelative(2.28f, 2.28f)
                lineToRelative(0.46f, 0.46f)
                curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
                curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
                curveToRelative(1.55f, 0f, 3.03f, -0.3f, 4.38f, -0.84f)
                lineToRelative(0.42f, 0.42f)
                lineTo(19.73f, 22f)
                lineTo(21f, 20.73f)
                lineTo(3.27f, 3f)
                lineTo(2f, 4.27f)
                close()
                moveTo(7.53f, 9.8f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                curveToRelative(0.22f, 0f, 0.44f, -0.03f, 0.65f, -0.08f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.67f, 0.33f, -1.41f, 0.53f, -2.2f, 0.53f)
                curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
                curveToRelative(0f, -0.79f, 0.2f, -1.53f, 0.53f, -2.2f)
                close()
            }
        }
    }

    val SparklesAi: ImageVector by lazy {
        materialSymbol("SparklesAi") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23f, 5f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19f, 1f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15f, 5f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19f, 9f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9f, 4f)
                lineTo(6.5f, 9.5f)
                lineTo(1f, 12f)
                lineToRelative(5.5f, 2.5f)
                lineTo(9f, 20f)
                lineToRelative(2.5f, -5.5f)
                lineTo(17f, 12f)
                lineToRelative(-5.5f, -2.5f)
                close()
                moveTo(19f, 15f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15f, 19f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19f, 23f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23f, 19f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19f, 15f)
                close()
            }
        }
    }

    val ThumbUp: ImageVector by lazy {
        materialSymbol("ThumbUp") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(1f, 21f)
                horizontalLineToRelative(4f)
                verticalLineTo(9f)
                horizontalLineTo(1f)
                verticalLineToRelative(12f)
                close()
                moveTo(23f, 10f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-6.31f)
                lineToRelative(0.95f, -4.57f)
                lineToRelative(0.03f, -0.32f)
                curveToRelative(0f, -0.41f, -0.17f, -0.79f, -0.44f, -1.06f)
                lineTo(14.17f, 1f)
                lineTo(7.59f, 7.59f)
                curveTo(7.22f, 7.95f, 7f, 8.45f, 7f, 9f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(9f)
                curveToRelative(0.83f, 0f, 1.54f, -0.5f, 1.84f, -1.22f)
                lineToRelative(3.02f, -7.05f)
                curveToRelative(0.09f, -0.23f, 0.14f, -0.47f, 0.14f, -0.73f)
                verticalLineToRelative(-2f)
                close()
            }
        }
    }

    val Favorite: ImageVector by lazy {
        materialSymbol("Favorite") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 21.35f)
                lineToRelative(-1.45f, -1.32f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveToRelative(1.74f, 0f, 3.41f, 0.81f, 4.5f, 2.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveToRelative(0f, 3.78f, -3.4f, 6.86f, -8.55f, 11.54f)
                lineTo(12f, 21.35f)
                close()
            }
        }
    }

    val Comment: ImageVector by lazy {
        materialSymbol("Comment") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 2f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(18f)
                lineToRelative(4f, -4f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
            }
        }
    }

    val Share: ImageVector by lazy {
        materialSymbol("Share") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 16.08f)
                curveToRelative(-0.76f, 0f, -1.44f, 0.3f, -1.96f, 0.77f)
                lineTo(8.91f, 12.7f)
                curveToRelative(0.05f, -0.23f, 0.09f, -0.46f, 0.09f, -0.7f)
                reflectiveCurveToRelative(-0.04f, -0.47f, -0.09f, -0.7f)
                lineToRelative(7.05f, -4.11f)
                curveToRelative(0.54f, 0.5f, 1.25f, 0.81f, 2.04f, 0.81f)
                curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
                reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f)
                reflectiveCurveToRelative(-3f, 1.34f, -3f, 3f)
                curveToRelative(0f, 0.24f, 0.04f, 0.47f, 0.09f, 0.7f)
                lineTo(8.04f, 9.81f)
                curveTo(7.5f, 9.31f, 6.79f, 9f, 6f, 9f)
                curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                curveToRelative(0.79f, 0f, 1.5f, -0.31f, 2.04f, -0.81f)
                lineToRelative(7.12f, 4.16f)
                curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                curveToRelative(0f, 1.61f, 1.31f, 2.92f, 2.92f, 2.92f)
                reflectiveCurveToRelative(2.92f, -1.31f, 2.92f, -2.92f)
                reflectiveCurveToRelative(-1.31f, -2.92f, -2.92f, -2.92f)
                close()
            }
        }
    }

    val Bookmark: ImageVector by lazy {
        materialSymbol("Bookmark") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 3f)
                horizontalLineTo(7f)
                curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
                lineTo(5f, 21f)
                lineToRelative(7f, -3f)
                lineToRelative(7f, 3f)
                verticalLineTo(5f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
            }
        }
    }

    val Add: ImageVector by lazy {
        materialSymbol("Add") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineToRelative(-6f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-6f)
                horizontalLineTo(5f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(6f)
                verticalLineTo(5f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(6f)
                verticalLineToRelative(2f)
                close()
            }
        }
    }

    val ArrowBack: ImageVector by lazy {
        materialSymbol("ArrowBack") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineToRelative(5.59f, -5.59f)
                lineTo(12f, 4f)
                lineToRelative(-8f, 8f)
                lineToRelative(8f, 8f)
                lineToRelative(1.41f, -1.41f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                verticalLineToRelative(-2f)
                close()
            }
        }
    }

    val ArrowForward: ImageVector by lazy {
        materialSymbol("ArrowForward") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(16.17f, 11f)
                horizontalLineTo(4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(12.17f)
                lineToRelative(-5.58f, 5.59f)
                lineTo(12f, 20f)
                lineToRelative(8f, -8f)
                close()
            }
        }
    }

    val Play: ImageVector by lazy {
        materialSymbol("Play") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 5f)
                verticalLineToRelative(14f)
                lineToRelative(11f, -7f)
                close()
            }
        }
    }

    val RouteSafe: ImageVector by lazy {
        materialSymbol("RouteSafe") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 15.18f)
                verticalLineTo(7f)
                curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f)
                reflectiveCurveToRelative(-4f, 1.79f, -4f, 4f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
                reflectiveCurveToRelative(-2f, -0.9f, -2f, -2f)
                verticalLineTo(8.82f)
                curveTo(8.16f, 8.4f, 9f, 7.3f, 9f, 6f)
                curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
                reflectiveCurveTo(3f, 4.34f, 3f, 6f)
                curveToRelative(0f, 1.3f, 0.84f, 2.4f, 2f, 2.82f)
                verticalLineTo(17f)
                curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f)
                reflectiveCurveToRelative(4f, -1.79f, 4f, -4f)
                verticalLineTo(7f)
                curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
                reflectiveCurveToRelative(2f, 0.9f, 2f, 2f)
                verticalLineToRelative(8.18f)
                curveToRelative(-1.16f, 0.41f, -2f, 1.51f, -2f, 2.82f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                curveToRelative(0f, -1.3f, -0.84f, -2.4f, -2f, -2.82f)
                close()
            }
        }
    }

    val Camera: ImageVector by lazy {
        materialSymbol("Camera") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 15.2f)
                curveToRelative(1.77f, 0f, 3.2f, -1.43f, 3.2f, -3.2f)
                reflectiveCurveTo(13.77f, 8.8f, 12f, 8.8f)
                reflectiveCurveTo(8.8f, 10.23f, 8.8f, 12f)
                reflectiveCurveToRelative(1.43f, 3.2f, 3.2f, 3.2f)
                close()
                moveTo(9f, 2f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(12f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-3.17f)
                lineTo(15f, 2f)
                horizontalLineTo(9f)
                close()
            }
        }
    }

    val CloudSync: ImageVector by lazy {
        materialSymbol("CloudSync") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
                curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
                curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
                curveToRelative(0f, 3.31f, 2.69f, 6f, 6f, 6f)
                horizontalLineToRelative(13f)
                curveToRelative(2.76f, 0f, 5f, -2.24f, 5f, -5f)
                curveToRelative(0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
                moveTo(10f, 17f)
                lineToRelative(-3.5f, -3.5f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10f, 14.17f)
                lineToRelative(4.59f, -4.59f)
                lineTo(16f, 11f)
                lineToRelative(-6f, 6f)
                close()
            }
        }
    }

    val WarningTriangle: ImageVector by lazy {
        materialSymbol("WarningTriangle") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(1f, 21f)
                horizontalLineToRelative(22f)
                lineTo(12f, 2f)
                lineTo(1f, 21f)
                close()
                moveTo(13f, 18f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                close()
                moveTo(13f, 14f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(4f)
                close()
            }
        }
    }
}

