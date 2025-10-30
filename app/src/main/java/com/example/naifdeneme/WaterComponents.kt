package com.example.naifdeneme.ui.screens.water.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.ui.theme.accentSky
import com.example.naifdeneme.ui.theme.neonAqua

/**
 * Canvas ile çizilmiş, animasyonlu Progress Ring
 * Light mode: Pastel renkler
 * Dark mode: Neon glow efektli
 */
@Composable
fun AnimatedWaterProgressRing(
    currentAmount: Int,
    targetAmount: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val progress = (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
    val isDark = isSystemInDarkTheme()

    // Renkleri direkt import ettik, artık çakışma yok
    val neonAquaColor = neonAqua
    val accentSkyColor = accentSky
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Animasyon
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) progress else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas - Progress Ring
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.minDimension
            val strokeWidth = 12.dp.toPx()
            val radius = (canvasSize - strokeWidth) / 2
            val centerOffset = Offset(size.width / 2, size.height / 2)

            // Background circle (gri halka)
            drawCircle(
                color = if (isDark)
                    Color(0xFF2C2C2E)
                else
                    Color(0xFFF0F0F0),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc (renkli halka)
            val sweepAngle = 360f * animatedProgress
            val startAngle = -90f // 12 saat yönünden başla

            if (isDark) {
                // Dark mode: Neon glow effect
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            neonAquaColor,
                            neonAquaColor.copy(alpha = 0.8f),
                            neonAquaColor
                        ),
                        center = centerOffset
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                // Outer glow (blur effect simulation)
                drawArc(
                    color = neonAquaColor.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth + 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            } else {
                // Light mode: Pastel gradient
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentSkyColor,
                            accentSkyColor.copy(alpha = 0.7f),
                            accentSkyColor
                        ),
                        center = centerOffset
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Center text (miktar + hedef)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${currentAmount}ml",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "/ ${targetAmount}ml",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Basit Progress Ring (Canvas ile kendi implementasyonumuz)
 * Performans için alternatif
 */
@Composable
fun SimpleWaterProgressRing(
    currentAmount: Int,
    targetAmount: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "simple_progress"
    )

    // Theme renklerini composable context'te al
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Kendi canvas implementasyonumuz
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val strokeWidth = 12.dp.toPx()
            val radius = (canvasSize - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentAmount}ml",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = textColor
            )
            Text(
                text = "/ ${targetAmount}ml",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )
        }
    }
}

/**
 * Bar Chart Component (Günlük istatistik için)
 */
@Composable
fun WaterBarChart(
    data: List<Pair<String, Float>>, // (label, percentage)
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    // MaterialTheme.colorScheme extension'ları cross-module çalışmıyor, direkt import edilmiş renkleri kullan
    val primaryColor = if (isDark) neonAqua else accentSky
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, percentage) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .fillMaxHeight(percentage.coerceIn(0f, 1f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                x = 8.dp.toPx(),
                                y = 8.dp.toPx()
                            )
                        )

                        // Glow effect (dark mode)
                        if (isDark && percentage > 0.1f) {
                            drawRoundRect(
                                color = primaryColor.copy(alpha = 0.3f),
                                topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                                size = Size(
                                    size.width + 4.dp.toPx(),
                                    size.height + 4.dp.toPx()
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    x = 10.dp.toPx(),
                                    y = 10.dp.toPx()
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Horizontal Progress Bar (Küçük, basit)
 */
@Composable
fun WaterProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    // MaterialTheme.colorScheme extension'ları cross-module çalışmıyor, direkt import edilmiş renkleri kullan
    val primaryColor = if (isDark) neonAqua else accentSky

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        // Background
        drawRoundRect(
            color = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF0F0F0),
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                x = 4.dp.toPx(),
                y = 4.dp.toPx()
            )
        )

        // Progress fill
        val progressWidth = size.width * progress.coerceIn(0f, 1f)
        if (progressWidth > 0) {
            drawRoundRect(
                color = primaryColor,
                size = Size(progressWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    x = 4.dp.toPx(),
                    y = 4.dp.toPx()
                )
            )

            // Glow (dark mode)
            if (isDark) {
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.3f),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                    size = Size(
                        progressWidth + 4.dp.toPx(),
                        size.height + 4.dp.toPx()
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        x = 6.dp.toPx(),
                        y = 6.dp.toPx()
                    )
                )
            }
        }
    }
}

// ============================================
// PREVIEW - GÜNCELLENDİ (naifdeneme kullanacak)
// ============================================

@Preview(name = "Progress Ring - Light", showBackground = true)
@Composable
fun ProgressRingLightPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedWaterProgressRing(
                currentAmount = 1500,
                targetAmount = 2500
            )
        }
    }
}

@Preview(name = "Progress Ring - Dark", showBackground = true)
@Composable
fun ProgressRingDarkPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedWaterProgressRing(
                currentAmount = 1800,
                targetAmount = 2500
            )
        }
    }
}

@Preview(name = "Simple Ring - Light", showBackground = true)
@Composable
fun SimpleRingLightPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            SimpleWaterProgressRing(
                currentAmount = 1200,
                targetAmount = 2500
            )
        }
    }
}

@Preview(name = "Bar Chart - Light", showBackground = true)
@Composable
fun BarChartLightPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            WaterBarChart(
                data = listOf(
                    "6-9" to 0.4f,
                    "9-12" to 0.1f,
                    "12-15" to 0.8f,
                    "15-18" to 0.3f,
                    "18-21" to 0f
                )
            )
        }
    }
}

@Preview(name = "Bar Chart - Dark", showBackground = true)
@Composable
fun BarChartDarkPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            WaterBarChart(
                data = listOf(
                    "6-9" to 0.4f,
                    "9-12" to 0.1f,
                    "12-15" to 0.95f,
                    "15-18" to 0.3f,
                    "18-21" to 0.6f
                )
            )
        }
    }
}

@Preview(name = "Progress Bar", showBackground = true)
@Composable
fun ProgressBarPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WaterProgressBar(progress = 0.25f)
            WaterProgressBar(progress = 0.50f)
            WaterProgressBar(progress = 0.75f)
            WaterProgressBar(progress = 1.0f)
        }
    }
}