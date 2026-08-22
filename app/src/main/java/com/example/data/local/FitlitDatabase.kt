package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FitlitDao
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.local.entity.LoggedFoodEntity
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WaterLogEntity
import com.example.data.local.entity.WeightLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        MealPlanEntity::class,
        LoggedFoodEntity::class,
        FridgeItemEntity::class,
        WeightLogEntity::class,
        ActivityLogEntity::class,
        WaterLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitlitDatabase : RoomDatabase() {
    abstract fun fitlitDao(): FitlitDao

    companion object {
        @Volatile
        private var INSTANCE: FitlitDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FitlitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitlitDatabase::class.java,
                    "fitlit_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.fitlitDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: FitlitDao) {
            // Seed initial default user profile matching user mockup
            val profile = UserProfileEntity(
                id = 1,
                name = "Rahul",
                goal = "Fat Loss",
                age = 26,
                gender = "Male",
                heightCm = 176f,
                currentWeightKg = 72.4f,
                targetWeightKg = 68.0f,
                targetTimelineWeeks = 4,
                activityLevel = "Moderately Active",
                targetCalories = 1900,
                targetProtein = 140,
                targetCarbs = 220,
                targetFats = 65,
                targetWaterMl = 3000,
                targetSteps = 10000,
                budgetLevel = "Low",
                budgetAmountDaily = 12.0,
                dietaryPreference = "High Protein",
                hasCompletedOnboarding = true,
                safetyDisclaimerAccepted = true
            )
            dao.saveUserProfile(profile)

            // Seed initial today's meal plan exactly as in mockup
            val initialMeals = listOf(
                MealPlanEntity(
                    dayOfWeek = 0,
                    mealType = "Breakfast",
                    title = "Oats & Fresh Berries",
                    description = "Rolled oats cooked in almond milk topped with chia seeds, blueberries and banana slices.",
                    calories = 420,
                    proteinG = 22,
                    carbsG = 65,
                    fatG = 8,
                    prepTimeMins = 10,
                    estimatedCost = 1.80,
                    isBudgetFriendly = true,
                    ingredients = "Rolled oats (60g), Almond milk (200ml), Chia seeds (10g), Mixed Berries (50g), 1 Banana, Whey scoop (15g)",
                    preparationInstructions = "1. Cook oats with almond milk over medium heat for 5 mins.\n2. Stir in chia seeds and protein.\n3. Top with fresh blueberries and sliced banana.",
                    whyItFitsGoal = "Slow digesting complex carbs with antioxidant rich fruits provide sustained morning fat-burning energy.",
                    isFavorite = true,
                    isEaten = true
                ),
                MealPlanEntity(
                    dayOfWeek = 0,
                    mealType = "Lunch",
                    title = "High Protein Paneer Bowl",
                    description = "Pan-seared low-fat paneer cubes tossed with steamed brown rice, crunchy bell peppers, diced onions, and herbs.",
                    calories = 520,
                    proteinG = 38,
                    carbsG = 55,
                    fatG = 16,
                    prepTimeMins = 20,
                    estimatedCost = 2.90,
                    isBudgetFriendly = true,
                    ingredients = "Low-fat Paneer (150g), Brown Rice (70g dry), Green Capsicum (50g), Carrot (40g), Onion (30g), Olive Oil (1 tsp), Spices (Cumin, Turmeric, Black pepper)",
                    preparationInstructions = "1. Cook brown rice in boiling water.\n2. Cube paneer and sauté in 1 tsp olive oil with turmeric and black pepper until golden.\n3. Stir fry capsicum and onion lightly.\n4. Combine all in a bowl and garnish with cilantro.",
                    whyItFitsGoal = "Dense vegetarian protein with high leucine content to preserve lean muscle while maintaining caloric deficit.",
                    isFavorite = true,
                    isEaten = true
                ),
                MealPlanEntity(
                    dayOfWeek = 0,
                    mealType = "Dinner",
                    title = "Moong Dal Chilla & Mint Dip",
                    description = "Crispy golden yellow lentil savory pancakes with freshly grated cottage cheese and cool cilantro-mint chutney.",
                    calories = 480,
                    proteinG = 38,
                    carbsG = 45,
                    fatG = 12,
                    prepTimeMins = 15,
                    estimatedCost = 1.50,
                    isBudgetFriendly = true,
                    ingredients = "Yellow Moong Dal (100g soaked), Onion (1 small), Tomato (1 small), Ginger-chili paste, Fresh Mint & Coriander (1 cup), Paneer garnish (30g)",
                    preparationInstructions = "1. Blend soaked moong dal into smooth batter.\n2. Add diced onions, tomatoes, and ginger paste.\n3. Pour onto a hot non-stick pan and cook until crisp on both sides.\n4. Serve with fresh mint-coriander chutney.",
                    whyItFitsGoal = "Extremely light on digestion before sleep, rich in dietary fiber and plant proteins.",
                    isFavorite = false,
                    isEaten = false
                ),
                MealPlanEntity(
                    dayOfWeek = 0,
                    mealType = "Snack",
                    title = "Greek Yogurt & Almond Shake",
                    description = "Cold creamy Greek yogurt blended with chilled water, roasted almonds and a touch of cinnamon.",
                    calories = 200,
                    proteinG = 20,
                    carbsG = 12,
                    fatG = 6,
                    prepTimeMins = 5,
                    estimatedCost = 1.20,
                    isBudgetFriendly = true,
                    ingredients = "Greek Yogurt (150g), Roasted Almonds (8 pcs), Cinnamon powder (pinch), Stevia/Honey (optional)",
                    preparationInstructions = "1. Add greek yogurt and crushed almonds into blender.\n2. Blend with cold water and ice until frothy.\n3. Dust with cinnamon powder.",
                    whyItFitsGoal = "Quick protein boost that curbs late-afternoon sweet cravings without spiking insulin.",
                    isFavorite = false,
                    isEaten = false
                )
            )
            dao.insertMealPlans(initialMeals)

            // Seed initial fridge inventory
            val initialFridge = listOf(
                FridgeItemEntity(name = "Paneer (Cottage Cheese)", category = "Protein", quantity = "400g", isAvailable = true),
                FridgeItemEntity(name = "Eggs", category = "Protein", quantity = "12 pcs", isAvailable = true),
                FridgeItemEntity(name = "Greek Yogurt", category = "Dairy", quantity = "500g", isAvailable = true),
                FridgeItemEntity(name = "Rolled Oats", category = "Grains", quantity = "1 kg", isAvailable = true),
                FridgeItemEntity(name = "Brown Rice", category = "Grains", quantity = "1 kg", isAvailable = true),
                FridgeItemEntity(name = "Yellow Moong Dal", category = "Protein", quantity = "500g", isAvailable = true),
                FridgeItemEntity(name = "Bell Peppers", category = "Produce", quantity = "3 pcs", isAvailable = true),
                FridgeItemEntity(name = "Spinach", category = "Produce", quantity = "1 bunch", isAvailable = true),
                FridgeItemEntity(name = "Tomatoes", category = "Produce", quantity = "4 pcs", isAvailable = true),
                FridgeItemEntity(name = "Bananas", category = "Produce", quantity = "4 pcs", isAvailable = true),
                FridgeItemEntity(name = "Olive Oil", category = "Condiments", quantity = "500ml", isAvailable = true)
            )
            dao.insertFridgeItems(initialFridge)

            // Seed initial weight logs
            val weightLogs = listOf(
                WeightLogEntity(dateStr = "2026-08-01", weightKg = 73.6f, bodyFatPercent = 20.1f, muscleMassKg = 55.4f, timestamp = System.currentTimeMillis() - 86400000L * 21),
                WeightLogEntity(dateStr = "2026-08-08", weightKg = 73.1f, bodyFatPercent = 19.5f, muscleMassKg = 55.7f, timestamp = System.currentTimeMillis() - 86400000L * 14),
                WeightLogEntity(dateStr = "2026-08-15", weightKg = 72.8f, bodyFatPercent = 19.0f, muscleMassKg = 56.0f, timestamp = System.currentTimeMillis() - 86400000L * 7),
                WeightLogEntity(dateStr = "2026-08-22", weightKg = 72.4f, bodyFatPercent = 18.6f, muscleMassKg = 56.2f, timestamp = System.currentTimeMillis())
            )
            weightLogs.forEach { dao.insertWeightLog(it) }

            // Seed today's logged food matching mockup (1420 kcal, 98g protein, 165g carbs, 48g fats)
            val todayStr = java.time.LocalDate.now().toString()
            dao.insertLoggedFood(LoggedFoodEntity(dateStr = todayStr, mealType = "Breakfast", name = "Oats & Fresh Berries", calories = 420, proteinG = 22, carbsG = 65, fatG = 8))
            dao.insertLoggedFood(LoggedFoodEntity(dateStr = todayStr, mealType = "Lunch", name = "High Protein Paneer Bowl", calories = 520, proteinG = 38, carbsG = 55, fatG = 16))
            dao.insertLoggedFood(LoggedFoodEntity(dateStr = todayStr, mealType = "Snack", name = "Moong Dal Chilla (1/2)", calories = 280, proteinG = 20, carbsG = 30, fatG = 8))
            dao.insertLoggedFood(LoggedFoodEntity(dateStr = todayStr, mealType = "Snack", name = "Greek Yogurt Shake", calories = 200, proteinG = 18, carbsG = 15, fatG = 16))

            // Seed today's activity (8,246 steps)
            dao.insertActivityLog(ActivityLogEntity(dateStr = todayStr, activityName = "Morning Brisk Walk", durationMinutes = 45, caloriesBurned = 280, stepsCount = 5420))
            dao.insertActivityLog(ActivityLogEntity(dateStr = todayStr, activityName = "Daily Commute & Errands", durationMinutes = 30, caloriesBurned = 140, stepsCount = 2826))

            // Seed water (2,250 ml)
            dao.insertWaterLog(WaterLogEntity(dateStr = todayStr, amountMl = 750))
            dao.insertWaterLog(WaterLogEntity(dateStr = todayStr, amountMl = 750))
            dao.insertWaterLog(WaterLogEntity(dateStr = todayStr, amountMl = 750))
        }
    }
}
