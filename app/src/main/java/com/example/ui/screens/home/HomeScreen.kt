package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.TodoEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.TodayNutritionSummary
import com.example.ui.components.CircularGoalRing
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RealtimeStepsHUD
import com.example.ui.components.UserAvatarView
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.StepsGreen
import com.example.util.ApiKeyStatus
import com.example.util.LiveStepState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    profile: UserProfileEntity?,
    nutritionSummary: TodayNutritionSummary,
    stepState: LiveStepState,
    meals: List<MealPlanEntity>,
    todos: List<TodoEntity>,
    geminiKeyStatus: ApiKeyStatus,
    onPlanCardClick: () -> Unit,
    onFridgeCardClick: () -> Unit,
    onMealClick: (MealPlanEntity) -> Unit,
    onNotificationClick: () -> Unit,
    onThemePickerClick: () -> Unit,
    onManageApiKeyClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onToggleLiveWalk: () -> Unit,
    onAddQuickSteps: (Int) -> Unit,
    onSeeAllMealsClick: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onToggleTodo: (TodoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val userName = profile?.name ?: "Rahul"

    var currentEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentEpochMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val now = LocalDateTime.now()
    val liveFormattedTime = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
    val liveDateStr = now.format(DateTimeFormatter.ofPattern("EEE, MMM d"))

    val todayStr = LocalDate.now().toString()
    val todayTodos = remember(todos) {
        todos.filter { it.dueDateStr == todayStr }
    }
    val pendingTodos = remember(todayTodos) {
        todayTodos.filter { !it.isCompleted }.take(3)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: User Avatar + "Good morning, Rahul" + Live Date/Time + Quick Theme & Key actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatarView(
                        photoUriOrBase64 = profile?.profilePhotoUri,
                        userName = userName,
                        size = 46.dp,
                        onClick = onAvatarClick
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Good day,",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$liveDateStr • $liveFormattedTime",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = userName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                // Action Buttons: API Key, Theme Switcher & Notification Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // API Key Config Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClick = onManageApiKeyClick)
                            .testTag("home_api_key_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "Gemini Key",
                            tint = if (geminiKeyStatus == ApiKeyStatus.QUOTA_EXCEEDED) CalorieOrange else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Theme Switcher Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClick = onThemePickerClick)
                            .testTag("home_theme_picker_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Themes & AMOLED",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Glass Notification Bell button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClick = onNotificationClick)
                            .testTag("home_notification_bell"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Realtime Hardware Step Counter HUD Card
            RealtimeStepsHUD(
                stepState = stepState,
                targetSteps = profile?.targetSteps ?: 10000,
                onToggleLiveWalk = onToggleLiveWalk,
                onAddQuickSteps = onAddQuickSteps
            )

            // "Today's Progress" Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_today_progress_card"),
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Today's Progress & Fuel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Goal Ring
                        CircularGoalRing(
                            percentage = nutritionSummary.overallGoalProgressPercent,
                            size = 120.dp,
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
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                label = "Calories",
                                value = "${nutritionSummary.totalCalories} / ${nutritionSummary.targetCalories} kcal"
                            )

                            // Protein
                            MetricRowItem(
                                icon = Icons.Rounded.WaterDrop,
                                iconColor = ProteinBlue,
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                label = "Protein",
                                value = "${nutritionSummary.totalProtein} / ${nutritionSummary.targetProtein} g"
                            )

                            // Steps
                            MetricRowItem(
                                icon = Icons.Rounded.DirectionsWalk,
                                iconColor = StepsGreen,
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                label = "Steps",
                                value = "${String.format("%,d", nutritionSummary.totalSteps)} / ${String.format("%,d", nutritionSummary.targetSteps)}"
                            )
                        }
                    }
                }
            }

            // "Today's Schedule & To-Dos" Section Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_schedule_section_card"),
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
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
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Today's Schedule & Tasks",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "View all (${todayTodos.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToTasks)
                                .testTag("home_view_all_tasks")
                        )
                    }

                    if (pendingTodos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StepsGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (todayTodos.isNotEmpty()) "All today's tasks completed! 🎉" else "No tasks scheduled for today",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        pendingTodos.forEach { todo ->
                            HomeTodoRow(
                                todo = todo,
                                currentEpochMillis = currentEpochMillis,
                                onToggle = { onToggleTodo(todo) }
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
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "${profile?.goal ?: "Fat Loss"} AI Plan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${profile?.targetCalories ?: 1800} kcal • ${profile?.dietaryPreference ?: "High Protein"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "View Plan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // "Today's Meals" Section
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
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

            // "What's in Your Fridge?" Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_fridge_card"),
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline,
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
                            text = "What's in Your Fridge?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scan ingredients and generate smart recipes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Open Fridge",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTodoRow(
    todo: TodoEntity,
    currentEpochMillis: Long,
    onToggle: () -> Unit
) {
    val minuteText = remember(todo.dueTimestamp, currentEpochMillis) {
        if (todo.dueTimestamp > 0) {
            val diffMins = ((todo.dueTimestamp - currentEpochMillis) / 60000L).toInt()
            when {
                diffMins > 60 -> "in ${diffMins / 60}h ${diffMins % 60}m"
                diffMins in 1..60 -> "in $diffMins mins"
                diffMins == 0 -> "now"
                diffMins in -60..-1 -> "${-diffMins}m late"
                else -> "${-diffMins / 60}h late"
            }
        } else {
            ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (todo.isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = StepsGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${todo.dueTimeStr} ${if (minuteText.isNotBlank()) "• $minuteText" else ""}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = todo.category,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
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
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline,
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
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = meal.title.take(15) + if (meal.title.length > 15) "..." else "",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Text(
                text = "${meal.calories} kcal",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
