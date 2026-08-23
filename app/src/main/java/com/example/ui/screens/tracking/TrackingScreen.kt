package com.example.ui.screens.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WeightLogEntity
import com.example.data.repository.TodayNutritionSummary
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SplineTrackingChart
import com.example.ui.theme.CarbsAmber
import com.example.ui.theme.FatRose
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.WaterCyan

@Composable
fun TrackingScreen(
    profile: UserProfileEntity?,
    summary: TodayNutritionSummary,
    weightLogs: List<WeightLogEntity>,
    selectedTimeRange: String,
    onTimeRangeChange: (String) -> Unit,
    onAddWater: (Int) -> Unit,
    onLogWeightClick: () -> Unit,
    onLogActivityClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onResetDataClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentWeight = weightLogs.lastOrNull()?.weightKg ?: profile?.currentWeightKg ?: 72.4f
    val bodyFat = weightLogs.lastOrNull()?.bodyFatPercent ?: 18.6f
    val muscle = weightLogs.lastOrNull()?.muscleMassKg ?: 56.2f

    val heightM = (profile?.heightCm ?: 175f) / 100f
    val bmi = if (heightM > 0) String.format("%.1f", currentWeight / (heightM * heightM)) else "22.3"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Header: "Tracking", Reset Icon, Calendar Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracking & Body Metrics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onResetDataClick != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable(onClick = onResetDataClick)
                                .testTag("tracking_reset_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = "Reset Tracking Data",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(onClick = onCalendarClick)
                            .testTag("tracking_calendar_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segmented Time Filter Tabs: [ Day | Week | Month | Year ]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Day", "Week", "Month", "Year").forEach { tab ->
                            GlassFilterChip(
                                selected = selectedTimeRange == tab,
                                onClick = { onTimeRangeChange(tab) },
                                label = tab,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_tab_$tab")
                            )
                        }
                    }
                }

                // Calories Chart Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracking_calories_chart_card"),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.outline
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Calories Intake & Deficit",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${summary.totalCalories} / ${summary.targetCalories} kcal",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = "On Track",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Spline curve wave
                            SplineTrackingChart(
                                points = if (summary.totalCalories > 0) {
                                    val progress = (summary.totalCalories.toFloat() / summary.targetCalories).coerceIn(0.1f, 1f)
                                    listOf(0.15f, 0.25f, 0.22f, 0.48f, progress * 0.9f, progress, progress * 0.95f)
                                } else listOf(0.1f, 0.2f, 0.15f, 0.35f, 0.5f, 0.65f, 0.75f)
                            )
                        }
                    }
                }

                // 3 Macro Pills Row (Protein, Carbs, Fats)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroSummaryPill(
                            icon = Icons.Rounded.WaterDrop,
                            iconColor = ProteinBlue,
                            iconBg = ProteinBlue.copy(alpha = 0.15f),
                            label = "Protein",
                            value = "${summary.totalProtein} / ${summary.targetProtein}g",
                            modifier = Modifier.weight(1f)
                        )
                        MacroSummaryPill(
                            icon = Icons.Rounded.Grain,
                            iconColor = CarbsAmber,
                            iconBg = CarbsAmber.copy(alpha = 0.15f),
                            label = "Carbs",
                            value = "${summary.totalCarbs} / ${summary.targetCarbs}g",
                            modifier = Modifier.weight(1f)
                        )
                        MacroSummaryPill(
                            icon = Icons.Rounded.Opacity,
                            iconColor = FatRose,
                            iconBg = FatRose.copy(alpha = 0.15f),
                            label = "Fats",
                            value = "${summary.totalFats} / ${summary.targetFats}g",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Body Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Body Stats",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+ Log Weight",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable(onClick = onLogWeightClick)
                                    .testTag("tracking_log_weight_btn")
                            )
                        }

                        // 2x2 Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BodyStatCard(
                                title = "Weight",
                                value = "$currentWeight kg",
                                changeText = "▼ 1.2 kg",
                                changePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                            BodyStatCard(
                                title = "Body Fat",
                                value = "$bodyFat %",
                                changeText = "▼ 1.5 %",
                                changePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BodyStatCard(
                                title = "Muscle Mass",
                                value = "$muscle kg",
                                changeText = "▲ 0.8 kg",
                                changePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                            BodyStatCard(
                                title = "BMI",
                                value = bmi,
                                changeText = "Healthy",
                                changePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Water Hydration Tracker Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracking_water_card"),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = WaterCyan.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = WaterCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Water Hydration Intake",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${summary.totalWaterMl} / ${summary.targetWaterMl} ml",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WaterCyan
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickWaterButton(amount = 250, onClick = { onAddWater(250) }, modifier = Modifier.weight(1f))
                                QuickWaterButton(amount = 500, onClick = { onAddWater(500) }, modifier = Modifier.weight(1f))
                                QuickWaterButton(amount = 750, onClick = { onAddWater(750) }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun MacroSummaryPill(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BodyStatCard(
    title: String,
    value: String,
    changeText: String,
    changePositive: Boolean,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = changeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QuickWaterButton(
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$amount ml",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = WaterCyan
        )
    }
}
