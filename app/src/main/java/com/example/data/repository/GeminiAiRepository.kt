package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.entity.TodoEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GeminiTool
import com.example.data.remote.GeneratedMeal
import com.example.data.remote.GeneratedPlanResponse
import com.example.util.GeminiKeyManager
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class GeminiAiRepository(
    private val apiService: GeminiApiService = GeminiClient.service,
    private val keyManager: GeminiKeyManager? = null
) {
    private val apiKey: String
        get() = keyManager?.getEffectiveApiKey()
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            ?: ""

    /**
     * Generate full daily / weekly meal plan based on user goals, body stats, and budget.
     * Uses gemini-1.5-flash with Search Grounding support.
     */
    suspend fun generatePersonalizedMealPlan(
        profile: UserProfileEntity,
        availableIngredients: List<String> = emptyList(),
        useSearchGrounding: Boolean = true
    ): Result<GeneratedPlanResponse> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(getFallbackPlan(profile))
            }

            val ingredientsNote = if (availableIngredients.isNotEmpty()) {
                "Available pantry & fridge ingredients: ${availableIngredients.joinToString(", ")}. Try to prioritize these."
            } else {
                "Use easily available, affordable whole foods."
            }

            val prompt = """
                You are Fitlit AI, an expert sports nutritionist, dietitian, and budget meal planner.
                Create a high-precision, personalized daily meal plan for:
                - Goal: ${profile.goal}
                - Age: ${profile.age}, Gender: ${profile.gender}, Height: ${profile.heightCm} cm, Weight: ${profile.currentWeightKg} kg
                - Target Weight: ${profile.targetWeightKg} kg in ${profile.targetTimelineWeeks} weeks
                - Activity Level: ${profile.activityLevel}
                - Calorie Target: ${profile.targetCalories} kcal, Protein Target: ${profile.targetProtein}g
                - Dietary Preference: ${profile.dietaryPreference}, Allergies/Restrictions: ${profile.allergies}
                - Budget Tier: ${profile.budgetLevel} ($${profile.budgetAmountDaily}/day)
                - $ingredientsNote

                Provide 4 meals: Breakfast, Lunch, Dinner, Snack.
                Ensure total calories and protein closely match the targets (approx ${profile.targetCalories} kcal and ${profile.targetProtein}g protein).
                Keep estimated cost within or near the daily budget of $${profile.budgetAmountDaily}.

                Respond ONLY with a valid JSON object matching this exact schema without markdown backticks:
                {
                  "dailyCalorieTarget": ${profile.targetCalories},
                  "proteinTargetGrams": ${profile.targetProtein},
                  "carbsTargetGrams": ${profile.targetCarbs},
                  "fatTargetGrams": ${profile.targetFats},
                  "dailyBudgetUsd": ${profile.budgetAmountDaily},
                  "planSummary": "Brief overview of how this plan optimizes ${profile.goal} on a ${profile.budgetLevel} budget",
                  "meals": [
                    {
                      "mealType": "Breakfast",
                      "title": "Meal Title",
                      "description": "Appetizing description",
                      "calories": 450,
                      "proteinG": 30,
                      "carbsG": 50,
                      "fatG": 12,
                      "prepTimeMins": 15,
                      "estimatedCost": 2.50,
                      "isBudgetFriendly": true,
                      "ingredients": ["1 cup oats", "2 eggs", "1 apple"],
                      "preparationInstructions": "Step 1... Step 2...",
                      "whyItFitsGoal": "High protein kickstarts metabolic rate...",
                      "missingIngredients": []
                    }
                  ]
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.5f,
                    responseMimeType = "application/json"
                ),
                tools = if (useSearchGrounding) listOf(GeminiTool()) else null
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val parsed = parsePlanJson(responseText)
                if (parsed != null && parsed.meals.isNotEmpty()) {
                    return@withContext Result.success(parsed)
                }
            }

            Result.success(getFallbackPlan(profile))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackPlan(profile))
        }
    }

    /**
     * Analyze a fridge or pantry photo to detect ingredients.
     * Uses gemini-1.5-pro for advanced vision understanding.
     */
    suspend fun analyzeFridgePhoto(bitmap: Bitmap): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(listOf("Eggs", "Paneer", "Milk", "Tomatoes", "Spinach", "Bell Peppers", "Oats"))
            }

            val base64Image = bitmap.toBase64()
            val prompt = """
                Analyze this photo of a refrigerator, pantry, or groceries.
                List every identifiable edible food item, vegetable, fruit, protein source, dairy, or condiment.
                Return ONLY a valid JSON array of strings containing clean ingredient names (e.g. ["Eggs", "Cheddar Cheese", "Tomatoes", "Broccoli", "Greek Yogurt"]).
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-pro:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val clean = responseText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val jsonArray = JSONArray(clean)
                val items = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    items.add(jsonArray.getString(i))
                }
                if (items.isNotEmpty()) {
                    return@withContext Result.success(items)
                }
            }

            Result.success(listOf("Eggs", "Paneer", "Spinach", "Tomatoes", "Bell Peppers", "Oats"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(listOf("Eggs", "Paneer", "Spinach", "Tomatoes", "Bell Peppers", "Oats"))
        }
    }

    /**
     * Analyze a photo of a meal plate to estimate food items and macros.
     * Uses gemini-1.5-pro.
     */
    suspend fun analyzeFoodPlate(bitmap: Bitmap): Result<GeneratedMeal> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(
                    GeneratedMeal(
                        title = "Detected Grilled Bowl",
                        description = "Cottage cheese / paneer with sauteed vegetables and rice",
                        calories = 480,
                        proteinG = 32,
                        carbsG = 48,
                        fatG = 14,
                        prepTimeMins = 15,
                        estimatedCost = 2.80,
                        ingredients = listOf("Paneer 120g", "Brown rice 100g", "Veggies 80g")
                    )
                )
            }

            val base64Image = bitmap.toBase64()
            val prompt = """
                Analyze this photo of a prepared meal or food dish.
                Identify the food items, estimate the portion sizes, calories, and macronutrients (protein, carbs, fats).
                Respond ONLY with a JSON object:
                {
                  "mealType": "Lunch",
                  "title": "Clear Dish Name",
                  "description": "Short description of items on plate",
                  "calories": 450,
                  "proteinG": 30,
                  "carbsG": 45,
                  "fatG": 12,
                  "prepTimeMins": 15,
                  "estimatedCost": 3.0,
                  "isBudgetFriendly": true,
                  "ingredients": ["Item 1 (amount)", "Item 2 (amount)"],
                  "preparationInstructions": "Quick summary",
                  "whyItFitsGoal": "Nutrition breakdown summary"
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-pro:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val meal = parseSingleMealJson(responseText)
                if (meal != null) return@withContext Result.success(meal)
            }

            Result.success(
                GeneratedMeal(
                    title = "Scanned Healthy Dish",
                    description = "Nutritious balanced meal with protein and complex carbs",
                    calories = 490,
                    proteinG = 34,
                    carbsG = 50,
                    fatG = 15
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(
                GeneratedMeal(
                    title = "Scanned Healthy Dish",
                    description = "Balanced meal with protein and carbs",
                    calories = 490,
                    proteinG = 34,
                    carbsG = 50,
                    fatG = 15
                )
            )
        }
    }

    /**
     * Ultra low-latency recipe suggestions based on fridge items.
     * Uses gemini-1.5-flash for instant response.
     */
    suspend fun generateQuickFridgeRecipes(
        availableIngredients: List<String>,
        goal: String,
        budget: String
    ): Result<List<GeneratedMeal>> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank() || availableIngredients.isEmpty()) {
                return@withContext Result.success(getFallbackFridgeMeals(availableIngredients))
            }

            val prompt = """
                You are a fast chef assistant. Using these ingredients in the fridge:
                ${availableIngredients.joinToString(", ")}
                
                Suggest 3 quick, affordable, high-protein recipes matching goal '$goal' and budget '$budget'.
                Highlight any small missing pantry staple if necessary (e.g. olive oil, salt).
                Respond ONLY with a JSON array of meals:
                [
                  {
                    "mealType": "Lunch",
                    "title": "Dish Name",
                    "description": "Tasty summary",
                    "calories": 480,
                    "proteinG": 35,
                    "carbsG": 40,
                    "fatG": 14,
                    "prepTimeMins": 15,
                    "estimatedCost": 2.20,
                    "isBudgetFriendly": true,
                    "ingredients": ["Ing 1", "Ing 2"],
                    "preparationInstructions": "1. Sauté... 2. Serve...",
                    "whyItFitsGoal": "Reason why it works",
                    "missingIngredients": ["Salt", "Pepper"]
                  }
                ]
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val meals = parseMealListJson(responseText)
                if (meals.isNotEmpty()) return@withContext Result.success(meals)
            }

            Result.success(getFallbackFridgeMeals(availableIngredients))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackFridgeMeals(availableIngredients))
        }
    }

    /**
     * Swap a single meal in seconds.
     * Uses gemini-1.5-flash.
     */
    suspend fun swapMealFast(
        mealType: String,
        targetCalories: Int,
        targetProtein: Int,
        preference: String,
        budget: String
    ): Result<GeneratedMeal> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(
                    GeneratedMeal(
                        mealType = mealType,
                        title = "Grilled Tofu & Vegetable Stir-fry",
                        description = "Crispy high protein tofu cubes wok-tossed with fresh greens and light soy sauce.",
                        calories = targetCalories,
                        proteinG = targetProtein,
                        carbsG = (targetCalories * 0.4 / 4).toInt(),
                        fatG = (targetCalories * 0.25 / 9).toInt(),
                        prepTimeMins = 15,
                        estimatedCost = 2.40,
                        isBudgetFriendly = true,
                        ingredients = listOf("Firm Tofu (180g)", "Broccoli & Capsicum (150g)", "Soy sauce (1 tbsp)", "Garlic"),
                        preparationInstructions = "1. Press and cube tofu.\n2. Pan-sear until crisp.\n3. Add veggies and sauce for 3 mins.",
                        whyItFitsGoal = "Clean plant protein powerhouse with minimal saturated fats."
                    )
                )
            }

            val prompt = """
                Fast replacement recipe for $mealType.
                Targets: $targetCalories calories, $targetProtein g protein.
                Dietary style: $preference, Budget: $budget.
                Respond ONLY with a JSON object:
                {
                  "mealType": "$mealType",
                  "title": "Creative Meal Name",
                  "description": "Tasty description",
                  "calories": $targetCalories,
                  "proteinG": $targetProtein,
                  "carbsG": 45,
                  "fatG": 12,
                  "prepTimeMins": 15,
                  "estimatedCost": 2.50,
                  "isBudgetFriendly": true,
                  "ingredients": ["Item 1", "Item 2"],
                  "preparationInstructions": "Step 1... Step 2...",
                  "whyItFitsGoal": "Why this meal is ideal"
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.6f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val meal = parseSingleMealJson(responseText)
                if (meal != null) return@withContext Result.success(meal)
            }

            Result.success(
                GeneratedMeal(
                    mealType = mealType,
                    title = "Egg White & Spinach Scramble",
                    description = "Fluffy egg whites with sautéed baby spinach and toasted whole wheat bread.",
                    calories = targetCalories,
                    proteinG = targetProtein,
                    carbsG = 35,
                    fatG = 8,
                    prepTimeMins = 10,
                    estimatedCost = 1.60
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(
                GeneratedMeal(
                    mealType = mealType,
                    title = "Quick High-Protein Wrap",
                    description = "Whole wheat tortilla filled with cottage cheese, crunchy lettuce and yogurt spread.",
                    calories = targetCalories,
                    proteinG = targetProtein,
                    carbsG = 40,
                    fatG = 10
                )
            )
        }
    }

    /**
     * Natural language food logger.
     * Uses gemini-1.5-flash to parse free text into exact calories and macros.
     */
    suspend fun parseFoodTextToMacros(text: String): Result<GeneratedMeal> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(
                    GeneratedMeal(
                        title = text.capitalizeWords(),
                        description = text,
                        calories = 350,
                        proteinG = 24,
                        carbsG = 38,
                        fatG = 10
                    )
                )
            }

            val prompt = """
                Parse this food text: "$text" into realistic nutritional estimates.
                Respond ONLY with JSON:
                {
                  "mealType": "Lunch",
                  "title": "Clean Short Food Title",
                  "description": "$text",
                  "calories": 350,
                  "proteinG": 25,
                  "carbsG": 40,
                  "fatG": 10
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val meal = parseSingleMealJson(responseText)
                if (meal != null) return@withContext Result.success(meal)
            }

            Result.success(
                GeneratedMeal(
                    title = text.capitalizeWords(),
                    description = text,
                    calories = 320,
                    proteinG = 22,
                    carbsG = 35,
                    fatG = 9
                )
            )
        } catch (e: Exception) {
            Result.success(
                GeneratedMeal(
                    title = text.capitalizeWords(),
                    description = text,
                    calories = 320,
                    proteinG = 22,
                    carbsG = 35,
                    fatG = 9
                )
            )
        }
    }

    suspend fun chatWithInsight(query: String, profile: UserProfileEntity?): String = withContext(Dispatchers.IO) {
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext "Please add your Gemini API Key in Settings to use Insight AI."
            }

            val contextStr = if (profile != null) {
                "User Profile: Goal is ${profile.goal}. Age ${profile.age}, ${profile.gender}, ${profile.currentWeightKg}kg."
            } else {
                "User profile not fully set up."
            }

            val prompt = """
                You are Insight AI, an expert fitness and nutrition assistant for the Fitlit app.
                $contextStr
                User query: $query
                Provide a helpful, accurate, and encouraging response. Format nicely.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.5f,
                    responseMimeType = "text/plain"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            return@withContext response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I'm having trouble thinking right now. Please try again later."
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "An error occurred while connecting to Insight AI. Make sure your API key is valid."
        }
    }

    // Helper JSON parsers
    private fun parsePlanJson(jsonStr: String): GeneratedPlanResponse? {
        return try {
            val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val adapter = GeminiClient.moshi.adapter(GeneratedPlanResponse::class.java)
            adapter.fromJson(clean)
        } catch (e: Exception) {
            // Manual fallback parsing if JSON keys vary slightly
            try {
                val obj = JSONObject(jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
                val mealsArray = obj.optJSONArray("meals") ?: JSONArray()
                val mealsList = mutableListOf<GeneratedMeal>()
                for (i in 0 until mealsArray.length()) {
                    val m = mealsArray.getJSONObject(i)
                    val ingList = mutableListOf<String>()
                    val ingArr = m.optJSONArray("ingredients")
                    if (ingArr != null) {
                        for (j in 0 until ingArr.length()) ingList.add(ingArr.getString(j))
                    }
                    mealsList.add(
                        GeneratedMeal(
                            mealType = m.optString("mealType", "Meal"),
                            title = m.optString("title", "Nutritious Dish"),
                            description = m.optString("description", ""),
                            calories = m.optInt("calories", 450),
                            proteinG = m.optInt("proteinG", 30),
                            carbsG = m.optInt("carbsG", 45),
                            fatG = m.optInt("fatG", 12),
                            prepTimeMins = m.optInt("prepTimeMins", 15),
                            estimatedCost = m.optDouble("estimatedCost", 2.5),
                            isBudgetFriendly = m.optBoolean("isBudgetFriendly", true),
                            ingredients = ingList,
                            preparationInstructions = m.optString("preparationInstructions", ""),
                            whyItFitsGoal = m.optString("whyItFitsGoal", "")
                        )
                    )
                }
                GeneratedPlanResponse(
                    dailyCalorieTarget = obj.optInt("dailyCalorieTarget", 1900),
                    proteinTargetGrams = obj.optInt("proteinTargetGrams", 140),
                    carbsTargetGrams = obj.optInt("carbsTargetGrams", 220),
                    fatTargetGrams = obj.optInt("fatTargetGrams", 65),
                    dailyBudgetUsd = obj.optDouble("dailyBudgetUsd", 12.0),
                    planSummary = obj.optString("planSummary", "Personalized fitness plan"),
                    meals = mealsList
                )
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parseSingleMealJson(jsonStr: String): GeneratedMeal? {
        return try {
            val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val adapter = GeminiClient.moshi.adapter(GeneratedMeal::class.java)
            adapter.fromJson(clean)
        } catch (e: Exception) {
            try {
                val m = JSONObject(jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
                val ingList = mutableListOf<String>()
                val ingArr = m.optJSONArray("ingredients")
                if (ingArr != null) {
                    for (j in 0 until ingArr.length()) ingList.add(ingArr.getString(j))
                }
                GeneratedMeal(
                    mealType = m.optString("mealType", "Meal"),
                    title = m.optString("title", "Custom Recipe"),
                    description = m.optString("description", ""),
                    calories = m.optInt("calories", 400),
                    proteinG = m.optInt("proteinG", 28),
                    carbsG = m.optInt("carbsG", 40),
                    fatG = m.optInt("fatG", 12),
                    prepTimeMins = m.optInt("prepTimeMins", 15),
                    estimatedCost = m.optDouble("estimatedCost", 2.5),
                    ingredients = ingList,
                    preparationInstructions = m.optString("preparationInstructions", ""),
                    whyItFitsGoal = m.optString("whyItFitsGoal", "")
                )
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parseMealListJson(jsonStr: String): List<GeneratedMeal> {
        return try {
            val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val type = Types.newParameterizedType(List::class.java, GeneratedMeal::class.java)
            val adapter = GeminiClient.moshi.adapter<List<GeneratedMeal>>(type)
            adapter.fromJson(clean) ?: emptyList()
        } catch (e: Exception) {
            try {
                val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val arr = JSONArray(clean)
                val list = mutableListOf<GeneratedMeal>()
                for (i in 0 until arr.length()) {
                    val m = arr.getJSONObject(i)
                    list.add(
                        GeneratedMeal(
                            mealType = m.optString("mealType", "Meal"),
                            title = m.optString("title", "Recipe Idea"),
                            description = m.optString("description", ""),
                            calories = m.optInt("calories", 450),
                            proteinG = m.optInt("proteinG", 32),
                            carbsG = m.optInt("carbsG", 42),
                            fatG = m.optInt("fatG", 12),
                            prepTimeMins = m.optInt("prepTimeMins", 15),
                            estimatedCost = m.optDouble("estimatedCost", 2.2),
                            preparationInstructions = m.optString("preparationInstructions", ""),
                            whyItFitsGoal = m.optString("whyItFitsGoal", "")
                        )
                    )
                }
                list
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    private fun getFallbackPlan(profile: UserProfileEntity): GeneratedPlanResponse {
        return GeneratedPlanResponse(
            dailyCalorieTarget = profile.targetCalories,
            proteinTargetGrams = profile.targetProtein,
            carbsTargetGrams = profile.targetCarbs,
            fatTargetGrams = profile.targetFats,
            dailyBudgetUsd = profile.budgetAmountDaily,
            planSummary = "High-protein ${profile.goal} plan designed with budget-friendly, whole ingredients.",
            meals = listOf(
                GeneratedMeal(
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
                    ingredients = listOf("Rolled oats (60g)", "Almond milk (200ml)", "Chia seeds (10g)", "Mixed Berries (50g)", "1 Banana"),
                    preparationInstructions = "1. Cook oats with almond milk over medium heat for 5 mins.\n2. Stir in chia seeds.\n3. Top with fresh blueberries and sliced banana.",
                    whyItFitsGoal = "Slow digesting complex carbs with antioxidant rich fruits provide sustained morning fat-burning energy."
                ),
                GeneratedMeal(
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
                    ingredients = listOf("Low-fat Paneer (150g)", "Brown Rice (70g dry)", "Green Capsicum (50g)", "Carrot (40g)", "Onion (30g)", "Olive Oil (1 tsp)", "Spices"),
                    preparationInstructions = "1. Cook brown rice in boiling water.\n2. Cube paneer and sauté in olive oil with turmeric until golden.\n3. Stir fry capsicum and onion lightly.\n4. Combine all in a bowl and garnish with cilantro.",
                    whyItFitsGoal = "Dense vegetarian protein with high leucine content to preserve lean muscle while maintaining caloric deficit."
                ),
                GeneratedMeal(
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
                    ingredients = listOf("Yellow Moong Dal (100g soaked)", "Onion (1 small)", "Tomato (1 small)", "Ginger-chili paste", "Fresh Mint & Coriander (1 cup)", "Paneer garnish (30g)"),
                    preparationInstructions = "1. Blend soaked moong dal into smooth batter.\n2. Add diced onions, tomatoes, and ginger paste.\n3. Pour onto a hot non-stick pan and cook until crisp on both sides.\n4. Serve with fresh mint-coriander chutney.",
                    whyItFitsGoal = "Extremely light on digestion before sleep, rich in dietary fiber and plant proteins."
                ),
                GeneratedMeal(
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
                    ingredients = listOf("Greek Yogurt (150g)", "Roasted Almonds (8 pcs)", "Cinnamon powder (pinch)"),
                    preparationInstructions = "1. Add greek yogurt and crushed almonds into blender.\n2. Blend with cold water and ice until frothy.\n3. Dust with cinnamon powder.",
                    whyItFitsGoal = "Quick protein boost that curbs late-afternoon sweet cravings without spiking insulin."
                )
            )
        )
    }

    private fun getFallbackFridgeMeals(ingredients: List<String>): List<GeneratedMeal> {
        val hasPaneer = ingredients.any { it.contains("paneer", ignoreCase = true) }
        val hasEggs = ingredients.any { it.contains("egg", ignoreCase = true) }
        val hasSpinach = ingredients.any { it.contains("spinach", ignoreCase = true) }

        return listOf(
            GeneratedMeal(
                mealType = "Lunch",
                title = if (hasPaneer) "Spiced Paneer & Pepper Scramble" else "Golden Egg & Herb Bhurji",
                description = "High-protein skillet with caramelized onions, bell peppers and mild spices.",
                calories = 460,
                proteinG = 36,
                carbsG = 22,
                fatG = 16,
                prepTimeMins = 12,
                estimatedCost = 1.80,
                isBudgetFriendly = true,
                ingredients = if (hasPaneer) listOf("Paneer 150g", "Bell Pepper 1", "Onion 1/2", "Spices") else listOf("3 Whole Eggs", "Spinach 50g", "Onion 1/2", "Olive oil 1 tsp"),
                preparationInstructions = "1. Heat oil in skillet.\n2. Sauté chopped onions and peppers for 3 mins.\n3. Add protein and spices, cook for 5 mins until aromatic.",
                whyItFitsGoal = "Made 100% from your current fridge items, packed with clean protein and zero waste."
            ),
            GeneratedMeal(
                mealType = "Dinner",
                title = "Savory Lentil & Spinach Crepes",
                description = "Crispy golden lentil batter cooked with shredded greens and tangy dipping sauce.",
                calories = 420,
                proteinG = 30,
                carbsG = 46,
                fatG = 10,
                prepTimeMins = 15,
                estimatedCost = 1.40,
                isBudgetFriendly = true,
                ingredients = listOf("Moong dal / Lentils (80g)", "Spinach (50g)", "Ginger & chili", "1 tsp Oil"),
                preparationInstructions = "1. Grind lentils with spinach into a fine batter.\n2. Spread on medium-hot pan.\n3. Flip and cook until golden crisp.",
                whyItFitsGoal = "Low cost per serving ($1.40) and easily digestible complex carbohydrates for recovery."
            ),
            GeneratedMeal(
                mealType = "Breakfast",
                title = "Protein Power Oatmeal Bowl",
                description = "Warm creamy oats topped with banana coins and crushed roasted nuts.",
                calories = 390,
                proteinG = 20,
                carbsG = 58,
                fatG = 9,
                prepTimeMins = 8,
                estimatedCost = 1.10,
                isBudgetFriendly = true,
                ingredients = listOf("Rolled Oats (50g)", "Milk/Water (200ml)", "1 Banana", "Almonds/Nuts (10g)"),
                preparationInstructions = "1. Simmer oats in milk for 4 mins.\n2. Top with sliced banana and crushed nuts.",
                whyItFitsGoal = "Sustained fiber release ensures full satiety throughout the morning."
            )
        )
    }

    /**
     * Generate an AI-tailored daily fitness, hydration, meal and recovery To-Do schedule.
     * Uses gemini-1.5-flash.
     */
    suspend fun generateDailySchedule(profile: UserProfileEntity): Result<List<TodoEntity>> = withContext(Dispatchers.IO) {
        val todayStr = LocalDate.now().toString()
        val currentBaseMillis = System.currentTimeMillis()
        try {
            val key = apiKey
            if (key.isBlank()) {
                return@withContext Result.success(getFallbackDailySchedule(profile, todayStr, currentBaseMillis))
            }

            val prompt = """
                You are Fitlit AI, a world-class fitness coach and habit architect.
                Generate a precision, science-backed daily schedule of actionable to-do tasks for:
                - Goal: ${profile.goal}
                - Calorie Target: ${profile.targetCalories} kcal, Protein: ${profile.targetProtein}g
                - Daily Steps Target: ${profile.targetSteps}
                - Water Target: ${profile.targetWaterMl} ml
                - Activity Level: ${profile.activityLevel}

                Create 6 to 8 structured tasks throughout the day spanning:
                - Categories: Workout, Nutrition, Hydration, Supplement, Habit
                - Specific Times: e.g. "07:00 AM", "07:30 AM", "08:30 AM", "01:00 PM", "05:30 PM", "07:30 PM", "08:30 PM", "10:30 PM"
                - Priorities: High, Medium, Low
                - Reminder minutes (e.g. 0, 15, 30)

                Respond ONLY with a valid JSON array of objects:
                [
                  {
                    "title": "Short punchy task name",
                    "description": "Clear actionable instruction on why and how to do it",
                    "category": "Workout",
                    "priority": "High",
                    "dueTimeStr": "07:30 AM",
                    "reminderMinutes": 15
                  }
                ]
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                val clean = responseText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val jsonArr = JSONArray(clean)
                val todoList = mutableListOf<TodoEntity>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val timeStr = obj.optString("dueTimeStr", "08:00 AM")
                    todoList.add(
                        TodoEntity(
                            title = obj.optString("title", "Daily Fitness Task"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Workout"),
                            priority = obj.optString("priority", "Medium"),
                            dueDateStr = todayStr,
                            dueTimeStr = timeStr,
                            dueTimestamp = parseTimeStrToEpochMillis(timeStr, currentBaseMillis),
                            reminderMinutes = obj.optInt("reminderMinutes", 15),
                            isCompleted = false,
                            isAiGenerated = true
                        )
                    )
                }
                if (todoList.isNotEmpty()) {
                    return@withContext Result.success(todoList)
                }
            }

            Result.success(getFallbackDailySchedule(profile, todayStr, currentBaseMillis))
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 429) {
                keyManager?.markQuotaExceeded()
            }
            Result.success(getFallbackDailySchedule(profile, todayStr, currentBaseMillis))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackDailySchedule(profile, todayStr, currentBaseMillis))
        }
    }

    private fun getFallbackDailySchedule(profile: UserProfileEntity, todayStr: String, currentBaseMillis: Long): List<TodoEntity> {
        return listOf(
            TodoEntity(
                title = "Morning Electrolyte & 500ml Water",
                description = "Drink 500ml lukewarm water with pinch of pink salt or lemon to optimize morning cortisol.",
                category = "Hydration",
                priority = "High",
                dueDateStr = todayStr,
                dueTimeStr = "07:00 AM",
                dueTimestamp = currentBaseMillis - 3600000L * 4,
                reminderMinutes = 15,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Fasted Morning Walk (${profile.targetSteps / 2} Steps)",
                description = "Brisk outdoor walk in natural sunlight for circadian alignment and metabolic priming.",
                category = "Workout",
                priority = "High",
                dueDateStr = todayStr,
                dueTimeStr = "07:30 AM",
                dueTimestamp = currentBaseMillis - 3600000L * 3,
                reminderMinutes = 15,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Fuel: High-Protein Breakfast (${(profile.targetProtein * 0.25).toInt()}g)",
                description = "Replenish amino acids with oats, berries, and egg whites/paneer.",
                category = "Nutrition",
                priority = "Medium",
                dueDateStr = todayStr,
                dueTimeStr = "08:30 AM",
                dueTimestamp = currentBaseMillis - 3600000L * 2,
                reminderMinutes = 10,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Midday Meal & 10m Postprandial Walk",
                description = "High Protein lunch with brown rice and leafy greens, followed by a 10 min stroll.",
                category = "Nutrition",
                priority = "High",
                dueDateStr = todayStr,
                dueTimeStr = "01:00 PM",
                dueTimestamp = currentBaseMillis + 3600000L * 1,
                reminderMinutes = 15,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Targeted Strength & Hypertrophy Session",
                description = "45 mins progressive resistance training targeting hypertrophy and core endurance.",
                category = "Workout",
                priority = "High",
                dueDateStr = todayStr,
                dueTimeStr = "05:30 PM",
                dueTimestamp = currentBaseMillis + 3600000L * 5,
                reminderMinutes = 30,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Hydration Milestone (${profile.targetWaterMl}ml Goal)",
                description = "Check your water intake logs to guarantee cellular hydration.",
                category = "Hydration",
                priority = "Medium",
                dueDateStr = todayStr,
                dueTimeStr = "07:30 PM",
                dueTimestamp = currentBaseMillis + 3600000L * 7,
                reminderMinutes = 0,
                isCompleted = false,
                isAiGenerated = false
            ),
            TodoEntity(
                title = "Light Recovery Dinner & Micronutrients",
                description = "Clean protein rich dinner with minerals to aid overnight tissue repair.",
                category = "Nutrition",
                priority = "Medium",
                dueDateStr = todayStr,
                dueTimeStr = "08:30 PM",
                dueTimestamp = currentBaseMillis + 3600000L * 8,
                reminderMinutes = 15,
                isCompleted = false,
                isAiGenerated = true
            ),
            TodoEntity(
                title = "Night Rest Protocol & Wind Down",
                description = "Turn off bright screens, sip herbal tea, ensure 8 hours uninterrupted sleep.",
                category = "Habit",
                priority = "Low",
                dueDateStr = todayStr,
                dueTimeStr = "10:30 PM",
                dueTimestamp = currentBaseMillis + 3600000L * 10,
                reminderMinutes = 30,
                isCompleted = false,
                isAiGenerated = true
            )
        )
    }

    private fun parseTimeStrToEpochMillis(timeStr: String, baseMillis: Long): Long {
        return try {
            val parts = timeStr.trim().split(" ")
            val time = parts[0].split(":")
            var hour = time[0].toInt()
            val min = time.getOrNull(1)?.toInt() ?: 0
            val isPm = parts.getOrNull(1)?.equals("PM", ignoreCase = true) == true
            if (isPm && hour < 12) hour += 12
            if (!isPm && hour == 12) hour = 0
            val now = java.time.LocalDateTime.now()
            val target = now.withHour(hour).withMinute(min).withSecond(0)
            target.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            baseMillis
        }
    }
}

// Extension helper to convert Bitmap to Base64
private fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
