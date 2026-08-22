package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Rahul",
    val goal: String = "Fat Loss", // "Fat Loss", "Build Muscle", "Maintain Weight"
    val age: Int = 26,
    val gender: String = "Male",
    val heightCm: Float = 175f,
    val currentWeightKg: Float = 72.4f,
    val targetWeightKg: Float = 68.0f,
    val targetTimelineWeeks: Int = 4,
    val activityLevel: String = "Moderately Active", // "Sedentary", "Lightly Active", "Moderately Active", "Very Active"
    val targetCalories: Int = 1900,
    val targetProtein: Int = 140,
    val targetCarbs: Int = 220,
    val targetFats: Int = 65,
    val targetWaterMl: Int = 2500,
    val targetSteps: Int = 10000,
    val budgetLevel: String = "Low", // "Low", "Moderate", "Flexible"
    val budgetAmountDaily: Double = 12.0,
    val dietaryPreference: String = "High Protein", // "Vegetarian", "Non-Vegetarian", "Vegan", "High Protein", "Keto"
    val allergies: String = "None",
    val hasCompletedOnboarding: Boolean = true,
    val safetyDisclaimerAccepted: Boolean = true
)

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int = 0, // 0 for Today / Monday, etc.
    val mealType: String = "Breakfast", // "Breakfast", "Lunch", "Dinner", "Snack"
    val title: String = "",
    val description: String = "",
    val calories: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val prepTimeMins: Int = 15,
    val estimatedCost: Double = 2.50,
    val isBudgetFriendly: Boolean = true,
    val ingredients: String = "", // Comma separated or JSON
    val preparationInstructions: String = "",
    val whyItFitsGoal: String = "",
    val isFavorite: Boolean = false,
    val isEaten: Boolean = false,
    val dateStr: String = ""
)

@Entity(tableName = "logged_food")
data class LoggedFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val mealType: String,
    val name: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fridge_items")
data class FridgeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Produce", // "Produce", "Protein", "Dairy", "Grains", "Condiments"
    val quantity: String = "1 portion",
    val isAvailable: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val weightKg: Float,
    val bodyFatPercent: Float = 18.6f,
    val muscleMassKg: Float = 56.2f,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val activityName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val stepsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
