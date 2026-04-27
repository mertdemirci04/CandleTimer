package com.example.candletime.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun CandleComponent(progress: Float) {

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    val animatedReveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
        label = "candleReveal"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1500, easing = FastOutLinearInEasing),
        label = "candleProgress"
    )

    val infinite = rememberInfiniteTransition(label = "candleInfinite")

    val flicker1 by infinite.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(110, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker1"
    )
    val flicker2 by infinite.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(370, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "flicker2"
    )
    val flickerHeight = (flicker1 + flicker2) / 2f

    val sway1 by infinite.animateFloat(
        initialValue = -0.8f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway1"
    )
    val sway2 by infinite.animateFloat(
        initialValue = -0.4f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2110, easing = LinearEasing), RepeatMode.Reverse),
        label = "sway2"
    )
    val sway = sway1 + sway2

    val glowPulse by infinite.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )

    val waxDrip by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Restart),
        label = "waxDrip"
    )

    val roomDarkColor = Color(0xFF080504)
    val waxColorLight = Color(0xFFF0E5CC)
    val waxColorBase = Color(0xFFD4C1A0)
    val waxColorDark = Color(0xFF967E5E)

    val brassDark = Color(0xFF3B2F1D)
    val brassMid = Color(0xFF7A6031)
    val brassLight = Color(0xFFC7A65A)
    val brassHighlight = Color(0xFFFCEBAE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(roomDarkColor),
        contentAlignment = Alignment.BottomCenter
    ) {
        val maxCandleHeight = 420.dp
        val slideOffsetPx = (1f - animatedReveal) * 800f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxCandleHeight)
                .graphicsLayer {
                    alpha = animatedReveal
                    translationY = slideOffsetPx
                }
        ) {
            val cx = size.width / 2f
            val candleW = size.width * 0.26f

            val minCandleH = 12f
            val rawCandleH = size.height * animatedProgress
            val candleH = max(minCandleH, rawCandleH)

            val burnOutFactor = (animatedProgress / 0.05f).coerceIn(0f, 1f)

            val holderBaseY = size.height - 40f
            val candleBaseY = holderBaseY - 15f
            val topY = candleBaseY - candleH
            val leftX = cx - candleW / 2f
            val rightX = cx + candleW / 2f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF883300).copy(alpha = 0.35f * glowPulse * burnOutFactor), Color.Transparent),
                    center = Offset(cx, holderBaseY), radius = candleW * 3.5f
                ),
                topLeft = Offset(cx - candleW * 3.5f, holderBaseY - 20f),
                size = Size(candleW * 7f, 60f)
            )

            val plateW = candleW * 2.4f
            val plateH = 35f
            val plateLeft = cx - plateW / 2f

            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(brassDark, brassMid, brassDark),
                    startY = holderBaseY - plateH * 0.5f, endY = holderBaseY + plateH * 0.5f
                ),
                topLeft = Offset(plateLeft, holderBaseY - plateH / 2f),
                size = Size(plateW, plateH * 1.2f)
            )

            drawOval(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to brassDark,
                        0.2f to brassMid,
                        0.5f to brassLight,
                        0.55f to brassHighlight,
                        0.65f to brassMid,
                        1.0f to brassDark
                    ),
                    startX = plateLeft, endX = plateLeft + plateW
                ),
                topLeft = Offset(plateLeft, holderBaseY - plateH / 2f),
                size = Size(plateW, plateH)
            )

            val innerPlateW = candleW * 1.3f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(brassDark.copy(alpha = 0.9f), brassMid.copy(alpha = 0.4f)),
                    center = Offset(cx, candleBaseY), radius = innerPlateW / 2f
                ),
                topLeft = Offset(cx - innerPlateW / 2f, candleBaseY - 10f),
                size = Size(innerPlateW, 20f)
            )


            val t1 = min(20f, candleH * 0.4f)
            val t2 = min(30f, candleH * 0.6f)
            val t3 = min(25f, candleH * 0.5f)
            val t4 = min(15f, candleH * 0.3f)

            val bodyPath = Path().apply {
                moveTo(leftX, topY + t1)
                cubicTo(leftX, topY + t2, rightX, topY + t3, rightX, topY + t4)
                lineTo(rightX - 3f, candleBaseY)
                cubicTo(rightX, candleBaseY + 5f, leftX, candleBaseY + 5f, leftX + 3f, candleBaseY)
                close()
            }

            drawPath(
                path = bodyPath,
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to waxColorDark,
                        0.12f to waxColorBase,
                        0.35f to waxColorLight,
                        0.75f to waxColorBase,
                        0.95f to waxColorDark.copy(alpha = 0.9f),
                        1.0f to roomDarkColor.copy(alpha = 0.5f)
                    ),
                    startX = leftX, endX = rightX
                )
            )

            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF7A00).copy(alpha = 0.85f * glowPulse * burnOutFactor),
                        Color(0xFFFF5500).copy(alpha = 0.35f * glowPulse * burnOutFactor),
                        Color.Transparent
                    ),
                    startY = topY, endY = topY + candleH * 0.45f
                )
            )

            val poolH = min(22f, candleH * 0.8f)
            val poolW = candleW * 0.92f
            val poolLeft = cx - poolW / 2f
            val poolTop = topY + min(8f, candleH * 0.2f)

            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFDCA66A), Color(0xFFB07F43)),
                    startY = poolTop - 5f, endY = poolTop + poolH
                ),
                topLeft = Offset(poolLeft, poolTop - 5f),
                size = Size(poolW, poolH)
            )

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFCC44).copy(alpha = 0.9f * burnOutFactor.coerceAtLeast(0.3f)),
                        Color(0xFFD6943C).copy(alpha = 0.8f),
                        Color(0xFF906532).copy(alpha = 0.9f)
                    ),
                    center = Offset(cx, poolTop + poolH * 0.6f),
                    radius = poolW * 0.6f
                ),
                topLeft = Offset(poolLeft, poolTop),
                size = Size(poolW, poolH)
            )

            drawOval(
                color = Color.White.copy(alpha = 0.5f * glowPulse * burnOutFactor),
                topLeft = Offset(poolLeft + poolW * 0.65f, poolTop + poolH * 0.25f),
                size = Size(poolW * 0.15f, poolH * 0.2f)
            )

            val currentDripY = topY + 25f + (candleH * 0.45f * waxDrip)
            val dripAlpha = (1f - waxDrip).coerceAtLeast(0f) * burnOutFactor // Ateş sönerken damlama da kesilir
            val dripX = cx - candleW * 0.25f

            if (dripAlpha > 0.05f) {
                val dripPath = Path().apply {
                    moveTo(dripX, topY + 15f)
                    lineTo(dripX + 10f, topY + 15f)
                    cubicTo(dripX + 10f, currentDripY - 15f, dripX + 14f, currentDripY, dripX + 5f, currentDripY + 10f)
                    cubicTo(dripX - 4f, currentDripY, dripX, currentDripY - 15f, dripX, topY + 15f)
                    close()
                }
                drawPath(
                    path = dripPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(waxColorBase, waxColorLight, waxColorDark),
                        startX = dripX - 2f, endX = dripX + 12f
                    ),
                    alpha = dripAlpha
                )
            }

            val wickBaseY = poolTop + poolH / 2f + 2f
            val wickTopX = cx + (sway * 5f * burnOutFactor)
            val wickTopY = wickBaseY - (26f * burnOutFactor.coerceAtLeast(0.4f))

            val wickPath = Path().apply {
                moveTo(cx, wickBaseY)
                cubicTo(cx - 3f, wickBaseY - 10f, wickTopX + 6f, wickTopY + 10f, wickTopX, wickTopY)
            }

            drawPath(
                path = wickPath,
                color = Color(0xFF110B08).copy(alpha = burnOutFactor.coerceAtLeast(0.6f)),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            if (burnOutFactor > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFFF3300), Color.Transparent),
                        center = Offset(wickTopX, wickTopY), radius = 6f * burnOutFactor
                    ),
                    radius = 6f * burnOutFactor, center = Offset(wickTopX, wickTopY)
                )
            }

            if (burnOutFactor > 0.01f) {
                val flameBaseY = wickTopY + 4f
                val baseFlameW = 32f * burnOutFactor
                val baseFlameH = 95f * flickerHeight * burnOutFactor
                val swayOffset = sway * 28f * burnOutFactor

                val glowRadius = max(1f, 250f * glowPulse * burnOutFactor)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9900).copy(alpha = 0.18f * glowPulse * burnOutFactor),
                            Color(0xFFFF4400).copy(alpha = 0.06f * glowPulse * burnOutFactor),
                            Color.Transparent
                        ),
                        center = Offset(wickTopX, flameBaseY - 35f),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(wickTopX, flameBaseY - 35f)
                )

                fun drawFlameLayer(width: Float, heightMultiplier: Float, color: Color, yOffsetAdjustment: Float = 0f) {
                    val topYOffset = flameBaseY - (baseFlameH * heightMultiplier) + yOffsetAdjustment
                    val topXOffset = wickTopX + (swayOffset * heightMultiplier)

                    val fPath = Path().apply {
                        moveTo(wickTopX, flameBaseY)
                        cubicTo(
                            wickTopX + width, flameBaseY - heightMultiplier * 10f,
                            topXOffset + width * 0.2f, topYOffset + baseFlameH * 0.4f,
                            topXOffset, topYOffset
                        )
                        cubicTo(
                            topXOffset - width * 0.2f, topYOffset + baseFlameH * 0.4f,
                            wickTopX - width, flameBaseY - heightMultiplier * 10f,
                            wickTopX, flameBaseY
                        )
                        close()
                    }

                    drawPath(
                        path = fPath,
                        brush = Brush.radialGradient(
                            colors = listOf(color, color.copy(alpha = 0f)),
                            center = Offset(wickTopX, flameBaseY - (baseFlameH * heightMultiplier * 0.3f)),
                            radius = max(1f, width * 2.5f)
                        )
                    )
                }

                drawFlameLayer(width = baseFlameW * 1.4f, heightMultiplier = 1.1f, color = Color(0xFFFF2200).copy(alpha = 0.5f))
                drawFlameLayer(width = baseFlameW * 1.0f, heightMultiplier = 0.85f, color = Color(0xFFFFaa00).copy(alpha = 0.8f))
                drawFlameLayer(width = baseFlameW * 0.5f, heightMultiplier = 0.5f, color = Color(0xFFFFFFDD), yOffsetAdjustment = 5f)

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0088FF).copy(alpha = 0.9f), Color(0xFF0033CC).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(wickTopX, flameBaseY),
                        radius = max(1f, baseFlameW * 0.6f)
                    ),
                    topLeft = Offset(wickTopX - baseFlameW * 0.6f, flameBaseY - 8f),
                    size = Size(baseFlameW * 1.2f, 16f)
                )
            }
        }
    }
}