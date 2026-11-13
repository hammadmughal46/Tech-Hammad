package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen

@Composable
fun PakAdsBrandedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Subtle Radial Glow in top-right and bottom-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonGreen.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(width * 0.9f, height * 0.1f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.9f, height * 0.1f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(width * 0.1f, height * 0.75f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.1f, height * 0.75f)
            )

            // 2. Subtle Watermark Grid of PAK ADS motifs: Coins, Play Icons, Dollar/PKR symbols
            val gridStep = 140f
            var y = 60f
            var row = 0
            while (y < height) {
                var x = if (row % 2 == 0) 40f else 110f
                var col = 0
                while (x < width) {
                    val motif = (row + col) % 4
                    when (motif) {
                        0 -> {
                            // Play Icon (Triangle)
                            val path = Path().apply {
                                moveTo(x - 12f, y - 14f)
                                lineTo(x + 14f, y)
                                lineTo(x - 12f, y + 14f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = NeonGreen.copy(alpha = 0.035f),
                                style = Stroke(width = 2.5f)
                            )
                        }
                        1 -> {
                            // Coin Circle with 'Rs' or '$'
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.035f),
                                radius = 16f,
                                center = Offset(x, y),
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.02f),
                                radius = 9f,
                                center = Offset(x, y)
                            )
                        }
                        2 -> {
                            // Sparkle Star / Ad Star
                            val path = Path().apply {
                                moveTo(x, y - 16f)
                                lineTo(x + 5f, y - 5f)
                                lineTo(x + 16f, y)
                                lineTo(x + 5f, y + 5f)
                                lineTo(x, y + 16f)
                                lineTo(x - 5f, y + 5f)
                                lineTo(x - 16f, y)
                                lineTo(x - 5f, y - 5f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = NeonGreen.copy(alpha = 0.03f)
                            )
                        }
                        3 -> {
                            // Digital screen / Ad monitor outline
                            drawRoundRect(
                                color = NeonCyan.copy(alpha = 0.03f),
                                topLeft = Offset(x - 16f, y - 11f),
                                size = androidx.compose.ui.geometry.Size(32f, 22f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                    x += gridStep
                    col++
                }
                y += gridStep
                row++
            }
        }

        content()
    }
}
