package com.example.ui.screens.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.BudgetFriendlyBadge
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.LimeGradientCard
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MacroTag
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.FatRose
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MealPlanScreen(
    profile: UserProfileEntity?,
    meals: List<MealPlanEntity>,
    fridgeItems: List<FridgeItemEntity>,
    isGenerating: Boolean,
    onBack: () -> Unit,
    onRegenerateClick: () -> Unit,
    onMealClick: (MealPlanEntity) -> Unit,
    onToggleFavorite: (MealPlanEntity) -> Unit,
    onLogMeal: (MealPlanEntity) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Today") } // "Today", "Week", "Grocery List"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar: Back, "AI Meal Plan", Filter/Settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("plan_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "AI Meal Plan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.testTag("plan_filter_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Preferences",
                        tint = TextPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Plan Summary Card (Screenshot 3)
                item {
                    LimeGradientCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan_header_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Row 1: Goal, Daily Target, Protein
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryCol(title = "Goal", value = profile?.goal ?: "Fat Loss")
                                SummaryCol(title = "Daily Target", value = "${profile?.targetCalories ?: 1800} kcal")
                                SummaryCol(title = "Protein", value = "${profile?.targetProtein ?: 140} g")
                            }

                            // Row 2: Budget, Duration, Plan by
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryCol(title = "Budget", value = profile?.budgetLevel ?: "Low")
                                SummaryCol(title = "Duration", value = "${profile?.targetTimelineWeeks ?: 4} Weeks")
                                Column {
                                    Text("Plan by", fontSize = 11.sp, color = TextSecondary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Fitlit AI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = LimePrimaryDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter Tabs: [ Today | Week | Grocery List ]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassFilterChip(
                            selected = selectedTab == "Today",
                            onClick = { selectedTab = "Today" },
                            label = "Today",
                            modifier = Modifier.testTag("tab_today")
                        )
                        GlassFilterChip(
                            selected = selectedTab == "Week",
                            onClick = { selectedTab = "Week" },
                            label = "Week",
                            modifier = Modifier.testTag("tab_week")
                        )
                        GlassFilterChip(
                            selected = selectedTab == "Grocery List",
                            onClick = { selectedTab = "Grocery List" },
                            label = "Grocery List",
                            modifier = Modifier.testTag("tab_grocery")
                        )
                    }
                }

                if (selectedTab == "Today") {
                    item {
                        Text(
                            text = "Today's Plan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (meals.isEmpty() && isGenerating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(color = LimePrimaryDark)
                                    Text("Crafting optimized meal plan...", fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                        }
                    } else {
                        items(meals) { meal ->
                            MealPlanItemCard(
                                meal = meal,
                                onClick = { onMealClick(meal) },
                                onToggleFavorite = { onToggleFavorite(meal) },
                                onLogAsEaten = { onLogMeal(meal) }
                            )
                        }
                    }
                } else if (selectedTab == "Week") {
                    item {
                        Text(
                            text = "7-Day AI Meal Plan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    items(days.size) { index ->
                        val dayName = days[index]
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(dayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${profile?.targetCalories ?: 1900} kcal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LimePrimaryDark)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (index % 2 == 0) "• Oats Bowl  • Paneer Rice Bowl  • Moong Chilla" else "• Egg Scramble  • Lentil Bowl  • Grilled Tofu",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    // Grocery List Tab
                    item {
                        GroceryListSection(
                            meals = meals,
                            fridgeItems = fridgeItems,
                            budgetDaily = profile?.budgetAmountDaily ?: 12.0
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Bottom "✨ Regenerate Plan" Floating Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            LiquidGlassButton(
                onClick = onRegenerateClick,
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_regenerate_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = TextOnLime,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generating Plan...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnLime
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = TextOnLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regenerate Plan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnLime
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCol(title: String, value: String) {
    Column {
        Text(text = title, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun MealPlanItemCard(
    meal: MealPlanEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLogAsEaten: () -> Unit
) {
    val mealImage = when (meal.mealType.lowercase()) {
        "breakfast" -> R.drawable.meal_paneer_bowl
        "lunch" -> R.drawable.meal_paneer_bowl
        "dinner" -> R.drawable.meal_moong_chilla
        else -> R.drawable.ic_fitlit_logo
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("meal_plan_card_${meal.id}"),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = mealImage),
                    contentDescription = meal.title,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${meal.calories} kcal • ${meal.proteinG}g Protein",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = meal.ingredients.ifBlank { "Nutrient rich whole ingredients" },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (meal.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (meal.isFavorite) FatRose else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Badges & Macro Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetFriendlyBadge(label = "Budget Friendly • $${String.format("%.2f", meal.estimatedCost)}")

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MacroTag(type = "P", valueGrams = meal.proteinG)
                    MacroTag(type = "C", valueGrams = meal.carbsG)
                    MacroTag(type = "F", valueGrams = meal.fatG)
                }
            }
        }
    }
}

@Composable
private fun GroceryListSection(
    meals: List<MealPlanEntity>,
    fridgeItems: List<FridgeItemEntity>,
    budgetDaily: Double
) {
    var boughtItems by remember { mutableStateOf(setOf<String>()) }

    val rawIngredients = meals.flatMap { it.ingredients.split(",") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val totalEstCost = (budgetDaily * 7)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFF9FDF5),
            borderColor = Color(0xFFD4F878)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Weekly Food Budget", fontSize = 12.sp, color = TextSecondary)
                    Text("~$${totalEstCost.toInt()} / week", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBFCD2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ShoppingCart,
                        contentDescription = null,
                        tint = LimePrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = "Smart Grocery Checklist (${rawIngredients.size} items)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        rawIngredients.forEach { item ->
            val isInFridge = fridgeItems.any { it.name.contains(item, ignoreCase = true) }
            val isChecked = boughtItems.contains(item) || isInFridge

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        boughtItems = if (boughtItems.contains(item)) {
                            boughtItems - item
                        } else {
                            boughtItems + item
                        }
                    },
                backgroundColor = if (isChecked) Color(0xFFF0FDF4) else Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isChecked) LimePrimaryDark else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isChecked) TextPrimary else TextSecondary
                        )
                    }

                    if (isInFridge) {
                        Text(
                            text = "In Fridge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LimePrimaryDark
                        )
                    }
                }
            }
        }
    }
}
