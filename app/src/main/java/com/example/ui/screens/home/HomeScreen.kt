package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.TodayNutritionSummary
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.StepsGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    profile: UserProfileEntity?,
    nutritionSummary: TodayNutritionSummary,
    meals: List<MealPlanEntity>,
    onPlanCardClick: () -> Unit,
    onFridgeCardClick: () -> Unit,
    onMealClick: (MealPlanEntity) -> Unit,
    onNotificationClick: () -> Unit,
    onSeeAllMealsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val userName = profile?.name ?: "Rahul"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: "Good morning, Rahul 🟢" + Notification Bell
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning,",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = userName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(LimePrimaryDark)
                        )
                    }
                }

                // Glass Notification Bell button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, GlassBorderLight, CircleShape)
                        .clickable(onClick = onNotificationClick)
                        .testTag("home_notification_bell"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // "Today's Progress" Card (Screenshot 2)
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_today_progress_card"),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Goal Ring
                        CircularGoalRing(
                            percentage = nutritionSummary.overallGoalProgressPercent,
                            size = 125.dp,
                            strokeWidth = 11.dp
                        )

                        // Three Metrics (Calories, Protein, Steps)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Calories
                            MetricRowItem(
                                icon = Icons.Rounded.LocalFireDepartment,
                                iconColor = CalorieOrange,
                                iconBg = Color(0xFFFFEDE6),
                                label = "Calories",
                                value = "${nutritionSummary.totalCalories} / ${nutritionSummary.targetCalories} kcal"
                            )

                            // Protein
                            MetricRowItem(
                                icon = Icons.Rounded.WaterDrop,
                                iconColor = ProteinBlue,
                                iconBg = Color(0xFFE0F2FE),
                                label = "Protein",
                                value = "${nutritionSummary.totalProtein} / ${nutritionSummary.targetProtein} g"
                            )

                            // Steps
                            MetricRowItem(
                                icon = Icons.Rounded.DirectionsWalk,
                                iconColor = StepsGreen,
                                iconBg = Color(0xFFDCFCE7),
                                label = "Steps",
                                value = "${String.format("%,d", nutritionSummary.totalSteps)} / ${String.format("%,d", nutritionSummary.targetSteps)}"
                            )
                        }
                    }
                }
            }

            // "AI Plan for You" Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_ai_plan_card"),
                backgroundColor = Color(0xFFF9FDF5),
                borderColor = Color(0xFFD4F878),
                onClick = onPlanCardClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEBFCD2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = LimePrimaryDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "${profile?.goal ?: "Fat Loss"} Plan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${profile?.targetCalories ?: 1800} kcal • ${profile?.dietaryPreference ?: "High Protein"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "View Plan",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // "Today's Meals" Section (Screenshot 2)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Meals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LimePrimaryDark,
                        modifier = Modifier
                            .clickable(onClick = onSeeAllMealsClick)
                            .testTag("home_see_all_meals")
                    )
                }

                // Horizontal Carousel of Meals
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    meals.forEach { meal ->
                        HomeMealCard(
                            meal = meal,
                            onClick = { onMealClick(meal) }
                        )
                    }
                }
            }

            // "What's in Your Fridge?" Card (Screenshot 2)
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_fridge_card"),
                backgroundColor = Color(0xFFF9FDF5),
                borderColor = Color(0xFFD4F878),
                onClick = onFridgeCardClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "What\'s in Your Fridge?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Add ingredients and let AI suggest meals",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fridge_illustration),
                            contentDescription = "Fridge",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Open Fridge",
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(17.dp)
            )
        }

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun HomeMealCard(
    meal: MealPlanEntity,
    onClick: () -> Unit
) {
    val mealImage = when (meal.mealType.lowercase()) {
        "breakfast" -> R.drawable.meal_paneer_bowl
        "lunch" -> R.drawable.meal_paneer_bowl
        "dinner" -> R.drawable.meal_moong_chilla
        else -> R.drawable.ic_fitlit_logo
    }

    LiquidGlassCard(
        modifier = Modifier
            .width(115.dp)
            .clickable(onClick = onClick)
            .testTag("home_meal_card_${meal.mealType}"),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(id = mealImage),
                contentDescription = meal.title,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = meal.mealType,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = meal.title.take(15) + if (meal.title.length > 15) "..." else "",
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )

            Text(
                text = "${meal.calories} kcal",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = LimePrimaryDark
            )
        }
    }
}
