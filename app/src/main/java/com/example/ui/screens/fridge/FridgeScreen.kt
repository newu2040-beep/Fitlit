package com.example.ui.screens.fridge

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.remote.GeneratedMeal
import com.example.ui.components.BudgetFriendlyBadge
import com.example.ui.components.ImageSourcePickerDialog
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MacroTag
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FridgeScreen(
    fridgeItems: List<FridgeItemEntity>,
    suggestedMeals: List<GeneratedMeal>,
    isAnalyzing: Boolean,
    isGenerating: Boolean,
    onBack: () -> Unit,
    onAddItem: (String, String) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onAnalyzePhoto: (Bitmap) -> Unit,
    onGenerateMeals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newIngredientName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Produce") }
    var showPhotoPicker by remember { mutableStateOf(false) }

    val categories = listOf("Produce", "Protein", "Dairy", "Grains", "Condiments")

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
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("fridge_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "What's in My Fridge?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = { showPhotoPicker = true },
                    modifier = Modifier.testTag("fridge_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Scan Fridge",
                        tint = LimePrimaryDark
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AI Scan Card Banner
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhotoPicker = true }
                            .testTag("fridge_scan_banner"),
                        backgroundColor = Color(0xFFF9FDF5),
                        borderColor = Color(0xFFD4F878)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.fridge_illustration),
                                contentDescription = "Fridge",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Scan Fridge with AI",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = LimePrimaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Take a photo of your shelves or pantry to auto-detect ingredients",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Add Ingredient Input
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Add Ingredients Manually",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newIngredientName,
                                    onValueChange = { newIngredientName = it },
                                    placeholder = { Text("e.g. Greek yogurt, eggs, spinach") },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fridge_ingredient_input"),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(LimePrimaryDark)
                                        .clickable {
                                            if (newIngredientName.isNotBlank()) {
                                                onAddItem(newIngredientName.trim(), selectedCategory)
                                                newIngredientName = ""
                                            }
                                        }
                                        .testTag("fridge_add_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Ingredients in Fridge Chip Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "In Your Kitchen (${fridgeItems.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            if (fridgeItems.isNotEmpty()) {
                                Text(
                                    text = "Generate Recipes ✨",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LimePrimaryDark,
                                    modifier = Modifier
                                        .clickable(onClick = onGenerateMeals)
                                        .testTag("fridge_generate_recipes_text")
                                )
                            }
                        }

                        if (fridgeItems.isEmpty()) {
                            Text(
                                text = "Your fridge is empty. Add ingredients above or take a photo to get AI meal ideas!",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                fridgeItems.forEach { item ->
                                    FridgeChip(
                                        item = item,
                                        onDelete = { onDeleteItem(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Suggested Meals from Fridge Section
                if (suggestedMeals.isNotEmpty() || isGenerating) {
                    item {
                        Text(
                            text = "AI Chef Recommendations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (isGenerating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = LimePrimaryDark)
                            }
                        }
                    } else {
                        items(suggestedMeals) { meal ->
                            SuggestedMealCard(meal = meal)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Bottom Generate Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            LiquidGlassButton(
                onClick = onGenerateMeals,
                enabled = !isGenerating && fridgeItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fridge_generate_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = TextOnLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cook from My Fridge ✨",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnLime
                    )
                }
            }
        }

        if (showPhotoPicker) {
            ImageSourcePickerDialog(
                title = "Scan Fridge Ingredients",
                subtitle = "Take a photo of your fridge or select from gallery",
                onDismiss = { showPhotoPicker = false },
                onImageSelected = { bitmap ->
                    onAnalyzePhoto(bitmap)
                }
            )
        }
    }
}

@Composable
private fun FridgeChip(
    item: FridgeItemEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove",
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SuggestedMealCard(meal: GeneratedMeal) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "${meal.calories} kcal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LimePrimaryDark
                )
            }

            Text(
                text = meal.description,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MacroTag(type = "P", valueGrams = meal.proteinG)
                    MacroTag(type = "C", valueGrams = meal.carbsG)
                    MacroTag(type = "F", valueGrams = meal.fatG)
                }

                BudgetFriendlyBadge(label = "Est. $${String.format("%.2f", meal.estimatedCost)}")
            }

            if (meal.missingIngredients.isNotEmpty()) {
                Text(
                    text = "Missing staples: ${meal.missingIngredients.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
