package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = mapOf()
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 2048,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    @Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @Json(name = "groundingChunks") val groundingChunks: List<GroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class GroundingChunk(
    @Json(name = "web") val web: GroundingWeb? = null
)

@JsonClass(generateAdapter = true)
data class GroundingWeb(
    @Json(name = "uri") val uri: String? = null,
    @Json(name = "title") val title: String? = null
)

// Data class parsed from structured AI Plan response
@JsonClass(generateAdapter = true)
data class GeneratedPlanResponse(
    @Json(name = "dailyCalorieTarget") val dailyCalorieTarget: Int = 1900,
    @Json(name = "proteinTargetGrams") val proteinTargetGrams: Int = 140,
    @Json(name = "carbsTargetGrams") val carbsTargetGrams: Int = 220,
    @Json(name = "fatTargetGrams") val fatTargetGrams: Int = 65,
    @Json(name = "dailyBudgetUsd") val dailyBudgetUsd: Double = 12.0,
    @Json(name = "planSummary") val planSummary: String = "",
    @Json(name = "meals") val meals: List<GeneratedMeal> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedMeal(
    @Json(name = "mealType") val mealType: String = "Lunch",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "calories") val calories: Int = 0,
    @Json(name = "proteinG") val proteinG: Int = 0,
    @Json(name = "carbsG") val carbsG: Int = 0,
    @Json(name = "fatG") val fatG: Int = 0,
    @Json(name = "prepTimeMins") val prepTimeMins: Int = 15,
    @Json(name = "estimatedCost") val estimatedCost: Double = 3.0,
    @Json(name = "isBudgetFriendly") val isBudgetFriendly: Boolean = true,
    @Json(name = "ingredients") val ingredients: List<String> = emptyList(),
    @Json(name = "preparationInstructions") val preparationInstructions: String = "",
    @Json(name = "whyItFitsGoal") val whyItFitsGoal: String = "",
    @Json(name = "missingIngredients") val missingIngredients: List<String> = emptyList()
)
