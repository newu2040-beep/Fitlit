package com.example.ui.screens.home

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ImageSourcePickerDialog
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.FatRose
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.StepsGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaterCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onLogFood: (name: String, mealType: String, cals: Int, p: Int, c: Int, f: Int) -> Unit,
    onLogNaturalFood: (String, String) -> Unit,
    onLogActivity: (name: String, mins: Int, cals: Int, steps: Int) -> Unit,
    onLogWeight: (Float, Float, Float) -> Unit,
    onAddWater: (Int) -> Unit,
    onAnalyzePlate: (Bitmap) -> Unit,
    isAnalyzing: Boolean
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Food") } // Food, Activity, Weight, Water
    var showPlatePhotoPicker by remember { mutableStateOf(false) }

    // Food fields
    var foodName by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Lunch") }
    var calories by remember { mutableStateOf("450") }
    var protein by remember { mutableStateOf("30") }
    var carbs by remember { mutableStateOf("45") }
    var fat by remember { mutableStateOf("12") }

    // Activity fields
    var activityName by remember { mutableStateOf("Weight Training") }
    var durationMins by remember { mutableStateOf("45") }
    var calsBurned by remember { mutableStateOf("320") }
    var stepsCount by remember { mutableStateOf("2500") }

    // Weight fields
    var weightValue by remember { mutableStateOf("72.0") }
    var bodyFatValue by remember { mutableStateOf("18.2") }
    var muscleMassValue by remember { mutableStateOf("56.5") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Log",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Category selector tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Food", Icons.Rounded.Restaurant),
                    Pair("Workout", Icons.Rounded.FitnessCenter),
                    Pair("Weight", Icons.Rounded.MonitorWeight),
                    Pair("Water", Icons.Rounded.WaterDrop)
                ).forEach { (cat, icon) ->
                    val isSelected = selectedSection == cat
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSection = cat }
                            .testTag("quick_add_tab_$cat"),
                        backgroundColor = if (isSelected) Color(0xFFF6FDE8) else Color(0xFFF8FAFC),
                        borderColor = if (isSelected) Color(0xFFA6E324) else Color(0xFFE2E8F0)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = cat,
                                tint = if (isSelected) LimePrimaryDark else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            when (selectedSection) {
                "Food" -> {
                    // AI Photo Plate scan banner
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPlatePhotoPicker = true }
                            .testTag("quick_add_scan_plate"),
                        backgroundColor = Color(0xFFF9FDF5),
                        borderColor = Color(0xFFD4F878)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = LimePrimaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scan Meal Plate with AI Vision",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Auto-calculate calories and macros from photo",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LimePrimaryDark)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food Description or Natural Text") },
                        placeholder = { Text("e.g. 2 whole boiled eggs and whole wheat toast") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("quick_add_food_input"),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            if (foodName.isNotBlank()) {
                                onLogFood(
                                    foodName.trim(),
                                    mealType,
                                    calories.toIntOrNull() ?: 350,
                                    protein.toIntOrNull() ?: 25,
                                    carbs.toIntOrNull() ?: 40,
                                    fat.toIntOrNull() ?: 10
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimaryDark),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("quick_add_submit_food")
                    ) {
                        Text("Log Food", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                "Workout" -> {
                    OutlinedTextField(
                        value = activityName,
                        onValueChange = { activityName = it },
                        label = { Text("Activity Type") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationMins,
                            onValueChange = { durationMins = it },
                            label = { Text("Duration (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = calsBurned,
                            onValueChange = { calsBurned = it },
                            label = { Text("Calories Burned") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            onLogActivity(
                                activityName,
                                durationMins.toIntOrNull() ?: 30,
                                calsBurned.toIntOrNull() ?: 200,
                                stepsCount.toIntOrNull() ?: 1000
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimaryDark),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Log Workout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                "Weight" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightValue,
                            onValueChange = { weightValue = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bodyFatValue,
                            onValueChange = { bodyFatValue = it },
                            label = { Text("Body Fat %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val w = weightValue.toFloatOrNull() ?: 72f
                            val bf = bodyFatValue.toFloatOrNull() ?: 18.2f
                            val mm = muscleMassValue.toFloatOrNull() ?: 56.5f
                            onLogWeight(w, bf, mm)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimaryDark),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Save Weight Entry", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                "Water" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(250, 500, 750, 1000).forEach { ml ->
                            Button(
                                onClick = {
                                    onAddWater(ml)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$ml", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WaterCyan)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPlatePhotoPicker) {
        ImageSourcePickerDialog(
            title = "Scan Meal Plate",
            subtitle = "Take a photo of your meal or select from gallery",
            onDismiss = { showPlatePhotoPicker = false },
            onImageSelected = { bitmap ->
                onAnalyzePlate(bitmap)
            }
        )
    }
}
