package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.dao.FitlitDao
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.local.entity.LoggedFoodEntity
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WaterLogEntity
import com.example.data.local.entity.WeightLogEntity
import com.example.data.remote.GeneratedMeal
import com.example.data.remote.GeneratedPlanResponse
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class TodayNutritionSummary(
    val totalCalories: Int = 0,
    val targetCalories: Int = 1900,
    val totalProtein: Int = 0,
    val targetProtein: Int = 140,
    val totalCarbs: Int = 0,
    val targetCarbs: Int = 220,
    val totalFats: Int = 0,
    val targetFats: Int = 65,
    val totalSteps: Int = 0,
    val targetSteps: Int = 10000,
    val totalWaterMl: Int = 0,
    val targetWaterMl: Int = 2500,
    val overallGoalProgressPercent: Int = 0
)

data class GroceryItem(
    val name: String,
    val category: String,
    val estimatedCost: Double,
    val isBought: Boolean = false,
    val isInFridge: Boolean = false
)

class FitlitRepository(
    private val dao: FitlitDao,
    private val aiRepository: GeminiAiRepository = GeminiAiRepository()
) {
    // User Profile
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.saveUserProfile(profile)
    }

    suspend fun getProfileOnce(): UserProfileEntity? {
        return dao.getUserProfileOnce()
    }

    // Meal Plans
    val mealPlans: Flow<List<MealPlanEntity>> = dao.getAllMealPlans()

    fun getMealPlansForDay(dayOfWeek: Int): Flow<List<MealPlanEntity>> =
        dao.getMealPlansForDay(dayOfWeek)

    suspend fun toggleMealFavorite(mealId: Long, currentFavorite: Boolean) {
        dao.setMealFavorite(mealId, !currentFavorite)
    }

    suspend fun logMealAsEaten(meal: MealPlanEntity) {
        dao.setMealEaten(meal.id, true)
        val todayStr = LocalDate.now().toString()
        dao.insertLoggedFood(
            LoggedFoodEntity(
                dateStr = todayStr,
                mealType = meal.mealType,
                name = meal.title,
                calories = meal.calories,
                proteinG = meal.proteinG,
                carbsG = meal.carbsG,
                fatG = meal.fatG
            )
        )
    }

    suspend fun unlogMeal(mealId: Long) {
        dao.setMealEaten(mealId, false)
    }

    // Food Logs
    fun getLoggedFoodForToday(): Flow<List<LoggedFoodEntity>> {
        val today = LocalDate.now().toString()
        return dao.getLoggedFoodForDate(today)
    }

    val allLoggedFood: Flow<List<LoggedFoodEntity>> = dao.getAllLoggedFood()

    suspend fun logCustomFood(food: LoggedFoodEntity) {
        dao.insertLoggedFood(food)
    }

    suspend fun deleteLoggedFood(food: LoggedFoodEntity) {
        dao.deleteLoggedFood(food)
    }

    // Water & Activity Logs
    fun getTodayWater(): Flow<List<WaterLogEntity>> {
        val today = LocalDate.now().toString()
        return dao.getWaterLogsForDate(today)
    }

    suspend fun addWater(amountMl: Int) {
        val today = LocalDate.now().toString()
        dao.insertWaterLog(WaterLogEntity(dateStr = today, amountMl = amountMl))
    }

    fun getTodayActivity(): Flow<List<ActivityLogEntity>> {
        val today = LocalDate.now().toString()
        return dao.getActivityLogsForDate(today)
    }

    suspend fun logActivity(name: String, durationMinutes: Int, calories: Int, steps: Int = 0) {
        val today = LocalDate.now().toString()
        dao.insertActivityLog(
            ActivityLogEntity(
                dateStr = today,
                activityName = name,
                durationMinutes = durationMinutes,
                caloriesBurned = calories,
                stepsCount = steps
            )
        )
    }

    // Weight & Body Stats
    val weightLogs: Flow<List<WeightLogEntity>> = dao.getAllWeightLogs()

    suspend fun logWeight(weightKg: Float, bodyFat: Float, muscleMass: Float, note: String = "") {
        val today = LocalDate.now().toString()
        dao.insertWeightLog(
            WeightLogEntity(
                dateStr = today,
                weightKg = weightKg,
                bodyFatPercent = bodyFat,
                muscleMassKg = muscleMass,
                note = note
            )
        )
        // Also update user profile current weight
        val currentProfile = dao.getUserProfileOnce()
        if (currentProfile != null) {
            dao.saveUserProfile(currentProfile.copy(currentWeightKg = weightKg))
        }
    }

    // Fridge Items
    val fridgeItems: Flow<List<FridgeItemEntity>> = dao.getAllFridgeItems()

    suspend fun addFridgeItem(name: String, category: String = "Produce", quantity: String = "1 portion") {
        dao.insertFridgeItem(FridgeItemEntity(name = name, category = category, quantity = quantity))
    }

    suspend fun addMultipleFridgeItems(items: List<String>) {
        val entities = items.map { name ->
            val cat = when {
                name.contains("egg", true) || name.contains("chicken", true) || name.contains("paneer", true) || name.contains("tofu", true) || name.contains("dal", true) -> "Protein"
                name.contains("milk", true) || name.contains("yogurt", true) || name.contains("cheese", true) -> "Dairy"
                name.contains("rice", true) || name.contains("oat", true) || name.contains("bread", true) -> "Grains"
                name.contains("oil", true) || name.contains("sauce", true) || name.contains("spice", true) -> "Condiments"
                else -> "Produce"
            }
            FridgeItemEntity(name = name, category = cat)
        }
        dao.insertFridgeItems(entities)
    }

    suspend fun deleteFridgeItem(id: Long) {
        dao.deleteFridgeItem(id)
    }

    // AI Generation Actions
    suspend fun regenerateFullPlan(availableFridgeItems: List<String> = emptyList()): Result<GeneratedPlanResponse> {
        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        val result = aiRepository.generatePersonalizedMealPlan(profile, availableFridgeItems)
        result.onSuccess { generated ->
            dao.clearMealPlans()
            val entities = generated.meals.mapIndexed { index, meal ->
                MealPlanEntity(
                    dayOfWeek = 0,
                    mealType = meal.mealType,
                    title = meal.title,
                    description = meal.description,
                    calories = meal.calories,
                    proteinG = meal.proteinG,
                    carbsG = meal.carbsG,
                    fatG = meal.fatG,
                    prepTimeMins = meal.prepTimeMins,
                    estimatedCost = meal.estimatedCost,
                    isBudgetFriendly = meal.isBudgetFriendly,
                    ingredients = meal.ingredients.joinToString(", "),
                    preparationInstructions = meal.preparationInstructions,
                    whyItFitsGoal = meal.whyItFitsGoal,
                    isFavorite = false,
                    isEaten = false
                )
            }
            dao.insertMealPlans(entities)
        }
        return result
    }

    suspend fun swapSingleMeal(mealId: Long, mealType: String): Result<GeneratedMeal> {
        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        val targetCal = when (mealType.lowercase()) {
            "breakfast" -> (profile.targetCalories * 0.25).toInt()
            "lunch" -> (profile.targetCalories * 0.35).toInt()
            "dinner" -> (profile.targetCalories * 0.25).toInt()
            else -> (profile.targetCalories * 0.15).toInt()
        }
        val targetProtein = when (mealType.lowercase()) {
            "breakfast" -> (profile.targetProtein * 0.25).toInt()
            "lunch" -> (profile.targetProtein * 0.35).toInt()
            "dinner" -> (profile.targetProtein * 0.25).toInt()
            else -> (profile.targetProtein * 0.15).toInt()
        }

        val result = aiRepository.swapMealFast(
            mealType = mealType,
            targetCalories = targetCal,
            targetProtein = targetProtein,
            preference = profile.dietaryPreference,
            budget = profile.budgetLevel
        )
        result.onSuccess { meal ->
            val updated = MealPlanEntity(
                id = mealId,
                dayOfWeek = 0,
                mealType = meal.mealType,
                title = meal.title,
                description = meal.description,
                calories = meal.calories,
                proteinG = meal.proteinG,
                carbsG = meal.carbsG,
                fatG = meal.fatG,
                prepTimeMins = meal.prepTimeMins,
                estimatedCost = meal.estimatedCost,
                isBudgetFriendly = meal.isBudgetFriendly,
                ingredients = meal.ingredients.joinToString(", "),
                preparationInstructions = meal.preparationInstructions,
                whyItFitsGoal = meal.whyItFitsGoal,
                isFavorite = false,
                isEaten = false
            )
            dao.updateMealPlan(updated)
        }
        return result
    }

    suspend fun analyzeFridgePhoto(bitmap: Bitmap): Result<List<String>> {
        return aiRepository.analyzeFridgePhoto(bitmap)
    }

    suspend fun analyzeFoodPlate(bitmap: Bitmap): Result<GeneratedMeal> {
        return aiRepository.analyzeFoodPlate(bitmap)
    }

    suspend fun getQuickFridgeRecipes(ingredients: List<String>): Result<List<GeneratedMeal>> {
        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        return aiRepository.generateQuickFridgeRecipes(ingredients, profile.goal, profile.budgetLevel)
    }

    suspend fun parseFoodText(text: String): Result<GeneratedMeal> {
        return aiRepository.parseFoodTextToMacros(text)
    }
}
