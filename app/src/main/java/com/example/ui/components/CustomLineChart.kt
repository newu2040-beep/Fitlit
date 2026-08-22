package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextMuted

@Composable
fun SplineTrackingChart(
    points: List<Float> = listOf(0.15f, 0.25f, 0.20f, 0.45f, 0.75f, 0.70f, 0.85f),
    labels: List<String> = listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM"),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (points.size - 1)

                val strokePath = Path()
                val fillPath = Path()

                // Calculate coordinates
                val coordinates = points.mapIndexed { index, normalizedVal ->
                    val x = index * stepX
                    val y = height - (normalizedVal * (height * 0.75f) + height * 0.12f)
                    Pair(x, y)
                }

                strokePath.moveTo(coordinates[0].first, coordinates[0].second)
                fillPath.moveTo(0f, height)
                fillPath.lineTo(coordinates[0].first, coordinates[0].second)

                for (i in 0 until coordinates.size - 1) {
                    val p0 = coordinates[i]
                    val p1 = coordinates[i + 1]

                    val controlX1 = p0.first + (p1.first - p0.first) / 2
                    val controlY1 = p0.second
                    val controlX2 = p0.first + (p1.first - p0.first) / 2
                    val controlY2 = p1.second

                    strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.first, p1.second)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.first, p1.second)
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw translucent gradient under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x55C6F432),
                            Color(0x20C6F432),
                            Color(0x02FFFFFF)
                        )
                    )
                )

                // Draw main curve
                drawPath(
                    path = strokePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            LimeGradientStart,
                            LimeGradientEnd,
                            LimePrimaryDark
                        )
                    ),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw current progress indicator point on latest point
                val lastPoint = coordinates[coordinates.size - 2]
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(lastPoint.first, lastPoint.second)
                )
                drawCircle(
                    color = LimePrimaryDark,
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(lastPoint.first, lastPoint.second)
                )
            }
        }

        // Time axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
