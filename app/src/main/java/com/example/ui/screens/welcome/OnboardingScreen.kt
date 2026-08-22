package com.example.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SafetyDisclaimerCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    initialProfile: UserProfileEntity?,
    onComplete: (UserProfileEntity) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Goals & Body, 2: Budget & Diet, 3: Review & Safety

    var name by remember { mutableStateOf(initialProfile?.name ?: "Rahul") }
    var selectedGoal by remember { mutableStateOf(initialProfile?.goal ?: "Fat Loss") }
    var age by remember { mutableStateOf((initialProfile?.age ?: 26).toString()) }
    var gender by remember { mutableStateOf(initialProfile?.gender ?: "Male") }
    var heightCm by remember { mutableStateOf((initialProfile?.heightCm ?: 175f).toInt().toString()) }
    var weightKg by remember { mutableStateOf((initialProfile?.currentWeightKg ?: 72.4f).toString()) }
    var targetWeightKg by remember { mutableStateOf((initialProfile?.targetWeightKg ?: 68.0f).toString()) }
    var timelineWeeks by remember { mutableFloatStateOf(initialProfile?.targetTimelineWeeks?.toFloat() ?: 4f) }
    var activityLevel by remember { mutableStateOf(initialProfile?.activityLevel ?: "Moderately Active") }

    var budgetLevel by remember { mutableStateOf(initialProfile?.budgetLevel ?: "Low") }
    var dailyBudget by remember { mutableStateOf((initialProfile?.budgetAmountDaily ?: 12.0).toString()) }
    var dietaryPreference by remember { mutableStateOf(initialProfile?.dietaryPreference ?: "High Protein") }

    var safetyAccepted by remember { mutableStateOf(initialProfile?.safetyDisclaimerAccepted ?: true) }

    val goals = listOf(
        Triple("Fat Loss", "Caloric deficit & lean tone", Icons.Rounded.LocalFireDepartment),
        Triple("Build Muscle", "Hypertrophy & high protein", Icons.Rounded.FitnessCenter),
        Triple("Maintain Weight", "Body recomp & vitality", Icons.Rounded.Speed)
    )

    val activities = listOf(
        "Sedentary (Office/Desk)",
        "Lightly Active (1-2 workouts/wk)",
        "Moderately Active (3-5 workouts/wk)",
        "Very Active (6+ intense workouts/wk)"
    )

    val dietOptions = listOf(
        "High Protein",
        "Vegetarian",
        "Non-Vegetarian",
        "Vegan",
        "Keto / Low-Carb"
    )

    val budgetTiers = listOf(
        Pair("Low", "$8 - $14 / day • Smart savings"),
        Pair("Moderate", "$15 - $25 / day • Balanced"),
        Pair("Flexible", "$25+ / day • Premium whole foods")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (step > 1) step-- else onBack()
                    },
                    modifier = Modifier.testTag("onboarding_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Step $step of 3",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LimePrimaryDark
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step == 1) {
                    item {
                        Text(
                            text = "Your Fitness Blueprint",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tell Fitlit AI about your body and targets.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_name"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        Text(
                            text = "Select Primary Goal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(goals.size) { index ->
                        val (goalTitle, goalDesc, icon) = goals[index]
                        val isSelected = selectedGoal == goalTitle
                        val border = if (isSelected) Color(0xFFA6E324) else GlassBorderLight
                        val bg = if (isSelected) Color(0xFFF6FDE8) else Color.White

                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGoal = goalTitle }
                                .testTag("goal_card_$goalTitle"),
                            backgroundColor = bg,
                            borderColor = border,
                            borderWidth = if (isSelected) 2.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFEBFCD2) else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) LimePrimaryDark else TextSecondary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goalTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = goalDesc,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).testTag("input_age"),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).testTag("input_height"),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                label = { Text("Current Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).testTag("input_weight"),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            OutlinedTextField(
                                value = targetWeightKg,
                                onValueChange = { targetWeightKg = it },
                                label = { Text("Target Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).testTag("input_target_weight"),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Target Timeline: ${timelineWeeks.toInt()} Weeks",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Slider(
                            value = timelineWeeks,
                            onValueChange = { timelineWeeks = it },
                            valueRange = 2f..16f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = LimePrimaryDark,
                                activeTrackColor = Color(0xFFA6E324)
                            )
                        )
                    }

                    item {
                        Text(
                            text = "Daily Activity Level",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activities.forEach { act ->
                                val selected = activityLevel == act
                                LiquidGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activityLevel = act },
                                    backgroundColor = if (selected) Color(0xFFF6FDE8) else Color.White,
                                    borderColor = if (selected) Color(0xFFA6E324) else GlassBorderLight
                                ) {
                                    Text(
                                        text = act,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (step == 2) {
                    item {
                        Text(
                            text = "Diet & Smart Budget",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Fitlit plans delicious high-protein meals within your budget.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Dietary Preference",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            dietOptions.forEach { opt ->
                                val isSelected = dietaryPreference == opt
                                LiquidGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { dietaryPreference = opt },
                                    backgroundColor = if (isSelected) Color(0xFFF6FDE8) else Color.White,
                                    borderColor = if (isSelected) Color(0xFFA6E324) else GlassBorderLight
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Food Budget Tier",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            budgetTiers.forEach { (tier, desc) ->
                                val isSelected = budgetLevel == tier
                                LiquidGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            budgetLevel = tier
                                            dailyBudget = when (tier) {
                                                "Low" -> "12.0"
                                                "Moderate" -> "20.0"
                                                else -> "32.0"
                                            }
                                        },
                                    backgroundColor = if (isSelected) Color(0xFFF6FDE8) else Color.White,
                                    borderColor = if (isSelected) Color(0xFFA6E324) else GlassBorderLight
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = tier,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = dailyBudget,
                            onValueChange = { dailyBudget = it },
                            label = { Text("Daily Food Budget ($ USD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_daily_budget"),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )
                    }
                } else if (step == 3) {
                    item {
                        Text(
                            text = "Review & Safety Check",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Fitlit AI will formulate your personalized nutrition and macros.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                    }

                    // Calculated Targets Preview Card
                    item {
                        val weight = weightKg.toFloatOrNull() ?: 72f
                        val height = heightCm.toFloatOrNull() ?: 175f
                        val ageInt = age.toIntOrNull() ?: 26

                        // Harris-Benedict BMR calculation
                        val bmr = 10 * weight + 6.25f * height - 5 * ageInt + 5
                        val multiplier = when {
                            activityLevel.contains("Sedentary") -> 1.2f
                            activityLevel.contains("Lightly") -> 1.375f
                            activityLevel.contains("Moderately") -> 1.55f
                            else -> 1.725f
                        }
                        val tdee = (bmr * multiplier).toInt()
                        val targetCal = when (selectedGoal) {
                            "Fat Loss" -> (tdee - 450).coerceAtLeast(1500)
                            "Build Muscle" -> tdee + 300
                            else -> tdee
                        }
                        val targetProtein = (weight * 2.0f).toInt() // 2.0g per kg

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFFF4FDE8),
                            borderColor = Color(0xFFD4F878)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = LimePrimaryDark
                                    )
                                    Text(
                                        text = "Fitlit AI Targets for $name",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Daily Target", fontSize = 12.sp, color = TextSecondary)
                                        Text("$targetCal kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Column {
                                        Text("Protein", fontSize = 12.sp, color = TextSecondary)
                                        Text("${targetProtein}g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Column {
                                        Text("Daily Budget", fontSize = 12.sp, color = TextSecondary)
                                        Text("$$dailyBudget", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Mandatory Safety Disclaimer Card
                    item {
                        SafetyDisclaimerCard(
                            isAcknowledged = safetyAccepted,
                            onAcknowledgedChange = { safetyAccepted = it }
                        )
                    }
                }
            }

            // Bottom Continue Action Button
            LiquidGlassButton(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        val weight = weightKg.toFloatOrNull() ?: 72.4f
                        val height = heightCm.toFloatOrNull() ?: 175f
                        val ageInt = age.toIntOrNull() ?: 26
                        val targetW = targetWeightKg.toFloatOrNull() ?: (weight - 4f)
                        val bmr = 10 * weight + 6.25f * height - 5 * ageInt + 5
                        val multiplier = when {
                            activityLevel.contains("Sedentary") -> 1.2f
                            activityLevel.contains("Lightly") -> 1.375f
                            activityLevel.contains("Moderately") -> 1.55f
                            else -> 1.725f
                        }
                        val tdee = (bmr * multiplier).toInt()
                        val targetCal = when (selectedGoal) {
                            "Fat Loss" -> (tdee - 450).coerceAtLeast(1500)
                            "Build Muscle" -> tdee + 300
                            else -> tdee
                        }
                        val targetProtein = (weight * 2.0f).toInt()
                        val targetFats = (targetCal * 0.25f / 9f).toInt()
                        val targetCarbs = ((targetCal - targetProtein * 4 - targetFats * 9) / 4f).toInt()

                        val updated = UserProfileEntity(
                            id = 1,
                            name = name.ifBlank { "Rahul" },
                            goal = selectedGoal,
                            age = ageInt,
                            gender = gender,
                            heightCm = height,
                            currentWeightKg = weight,
                            targetWeightKg = targetW,
                            targetTimelineWeeks = timelineWeeks.toInt(),
                            activityLevel = activityLevel,
                            targetCalories = targetCal,
                            targetProtein = targetProtein,
                            targetCarbs = targetCarbs,
                            targetFats = targetFats,
                            targetWaterMl = 3000,
                            targetSteps = 10000,
                            budgetLevel = budgetLevel,
                            budgetAmountDaily = dailyBudget.toDoubleOrNull() ?: 12.0,
                            dietaryPreference = dietaryPreference,
                            hasCompletedOnboarding = true,
                            safetyDisclaimerAccepted = safetyAccepted
                        )
                        onComplete(updated)
                    }
                },
                enabled = step != 3 || safetyAccepted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("onboarding_continue_btn")
            ) {
                Text(
                    text = if (step < 3) "Continue" else "Generate My AI Plan ✨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnLime
                )
            }
        }
    }
}
