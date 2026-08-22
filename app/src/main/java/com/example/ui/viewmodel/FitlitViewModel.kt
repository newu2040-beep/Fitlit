package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.local.entity.LoggedFoodEntity
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WaterLogEntity
import com.example.data.local.entity.WeightLogEntity
import com.example.data.remote.GeneratedMeal
import com.example.data.remote.GeneratedPlanResponse
import com.example.data.repository.FitlitRepository
import com.example.data.repository.TodayNutritionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FitlitViewModel(
    private val repository: FitlitRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val mealPlans: StateFlow<List<MealPlanEntity>> = repository.mealPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLoggedFood: StateFlow<List<LoggedFoodEntity>> = repository.getLoggedFoodForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWater: StateFlow<List<WaterLogEntity>> = repository.getTodayWater()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayActivity: StateFlow<List<ActivityLogEntity>> = repository.getTodayActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLogEntity>> = repository.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fridgeItems: StateFlow<List<FridgeItemEntity>> = repository.fridgeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined live nutrition summary
    val nutritionSummary: StateFlow<TodayNutritionSummary> = combine(
        userProfile,
        todayLoggedFood,
        todayActivity,
        todayWater
    ) { profile, loggedFood, activities, waterLogs ->
        val p = profile ?: UserProfileEntity()
        val cals = loggedFood.sumOf { it.calories }
        val protein = loggedFood.sumOf { it.proteinG }
        val carbs = loggedFood.sumOf { it.carbsG }
        val fats = loggedFood.sumOf { it.fatG }
        val steps = activities.sumOf { it.stepsCount }
        val water = waterLogs.sumOf { it.amountMl }

        // Calculate weighted score for overall daily goal progress (Calories + Protein + Steps)
        val calProgress = if (p.targetCalories > 0) (cals.toFloat() / p.targetCalories).coerceIn(0f, 1f) else 0f
        val proteinProgress = if (p.targetProtein > 0) (protein.toFloat() / p.targetProtein).coerceIn(0f, 1f) else 0f
        val stepProgress = if (p.targetSteps > 0) (steps.toFloat() / p.targetSteps).coerceIn(0f, 1f) else 0f

        val overallPercent = ((calProgress * 0.45f + proteinProgress * 0.35f + stepProgress * 0.20f) * 100).toInt()

        TodayNutritionSummary(
            totalCalories = cals,
            targetCalories = p.targetCalories,
            totalProtein = protein,
            targetProtein = p.targetProtein,
            totalCarbs = carbs,
            targetCarbs = p.targetCarbs,
            totalFats = fats,
            targetFats = p.targetFats,
            totalSteps = steps,
            targetSteps = p.targetSteps,
            totalWaterMl = water,
            targetWaterMl = p.targetWaterMl,
            overallGoalProgressPercent = overallPercent.coerceIn(0, 100)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayNutritionSummary())

    // UI Loading and Interaction States
    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    private val _generatedFridgeMeals = MutableStateFlow<List<GeneratedMeal>>(emptyList())
    val generatedFridgeMeals: StateFlow<List<GeneratedMeal>> = _generatedFridgeMeals.asStateFlow()

    private val _selectedMealDetail = MutableStateFlow<MealPlanEntity?>(null)
    val selectedMealDetail: StateFlow<MealPlanEntity?> = _selectedMealDetail.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val _timeRangeFilter = MutableStateFlow("Day")
    val timeRangeFilter: StateFlow<String> = _timeRangeFilter.asStateFlow()

    fun setTimeRangeFilter(filter: String) {
        _timeRangeFilter.value = filter
    }

    fun setSelectedMealDetail(meal: MealPlanEntity?) {
        _selectedMealDetail.value = meal
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun showNotification(msg: String) {
        _notificationMessage.value = msg
    }

    // Profile & Onboarding Actions
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            showNotification("Profile updated successfully")
        }
    }

    fun completeOnboarding(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(hasCompletedOnboarding = true, safetyDisclaimerAccepted = true))
            regenerateFullPlan()
        }
    }

    // Meal Plan Actions
    fun regenerateFullPlan() {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val fridgeNames = fridgeItems.value.map { it.name }
            val result = repository.regenerateFullPlan(fridgeNames)
            _isGeneratingPlan.value = false
            if (result.isSuccess) {
                showNotification("✨ Personalized AI Meal Plan Created!")
            } else {
                showNotification("Plan updated using smart targets")
            }
        }
    }

    fun swapMeal(mealId: Long, mealType: String) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val result = repository.swapSingleMeal(mealId, mealType)
            _isGeneratingPlan.value = false
            if (result.isSuccess) {
                showNotification("✨ Swapped to ${result.getOrNull()?.title}")
            }
        }
    }

    fun toggleMealFavorite(meal: MealPlanEntity) {
        viewModelScope.launch {
            repository.toggleMealFavorite(meal.id, meal.isFavorite)
        }
    }

    fun logMealAsEaten(meal: MealPlanEntity) {
        viewModelScope.launch {
            repository.logMealAsEaten(meal)
            showNotification("Logged ${meal.title} (+${meal.calories} kcal)")
        }
    }

    // Fridge Actions & AI Vision Analysis
    fun addFridgeItem(name: String, category: String = "Produce", quantity: String = "1 portion") {
        viewModelScope.launch {
            repository.addFridgeItem(name, category, quantity)
            showNotification("Added $name to your fridge")
        }
    }

    fun deleteFridgeItem(id: Long) {
        viewModelScope.launch {
            repository.deleteFridgeItem(id)
        }
    }

    fun analyzeFridgePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            val result = repository.analyzeFridgePhoto(bitmap)
            _isAnalyzingPhoto.value = false
            result.onSuccess { detectedItems ->
                if (detectedItems.isNotEmpty()) {
                    repository.addMultipleFridgeItems(detectedItems)
                    showNotification("Detected ${detectedItems.size} ingredients from photo!")
                    generateFridgeMealIdeas()
                } else {
                    showNotification("No clear food items detected in photo")
                }
            }.onFailure {
                showNotification("Could not analyze photo. Added default ingredients.")
            }
        }
    }

    fun generateFridgeMealIdeas() {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val available = fridgeItems.value.map { it.name }
            val result = repository.getQuickFridgeRecipes(available)
            _isGeneratingPlan.value = false
            result.onSuccess { meals ->
                _generatedFridgeMeals.value = meals
            }
        }
    }

    fun analyzeFoodPlatePhoto(bitmap: Bitmap, onResult: (GeneratedMeal) -> Unit) {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            val result = repository.analyzeFoodPlate(bitmap)
            _isAnalyzingPhoto.value = false
            result.onSuccess { meal ->
                onResult(meal)
            }
        }
    }

    // Fast Logging Actions
    fun logCustomFoodItem(name: String, mealType: String, calories: Int, protein: Int, carbs: Int, fats: Int) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            repository.logCustomFood(
                LoggedFoodEntity(
                    dateStr = todayStr,
                    mealType = mealType,
                    name = name,
                    calories = calories,
                    proteinG = protein,
                    carbsG = carbs,
                    fatG = fats
                )
            )
            showNotification("Logged $name")
        }
    }

    fun parseAndLogNaturalFoodText(text: String, mealType: String) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val result = repository.parseFoodText(text)
            _isGeneratingPlan.value = false
            result.onSuccess { meal ->
                logCustomFoodItem(
                    name = meal.title,
                    mealType = mealType,
                    calories = meal.calories,
                    protein = meal.proteinG,
                    carbs = meal.carbsG,
                    fats = meal.fatG
                )
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(amountMl)
            showNotification("+$amountMl ml Water logged")
        }
    }

    fun logActivity(name: String, minutes: Int, calories: Int, steps: Int = 0) {
        viewModelScope.launch {
            repository.logActivity(name, minutes, calories, steps)
            showNotification("Logged $name activity")
        }
    }

    fun logWeight(weight: Float, bodyFat: Float = 18.6f, muscle: Float = 56.2f, note: String = "") {
        viewModelScope.launch {
            repository.logWeight(weight, bodyFat, muscle, note)
            showNotification("Logged weight: $weight kg")
        }
    }
}

class FitlitViewModelFactory(
    private val repository: FitlitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitlitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitlitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
