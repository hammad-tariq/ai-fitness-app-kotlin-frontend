package com.pro.aifitnesstracker.data.foodscan

data class FoodScanAnalyzeResult(
    val title: String,
    val tag: String?,
    val servings: Double,
    val totalGrams: Double,
    val totalKcal: Double,
    val macros: FoodScanMacros,
    val items: List<FoodScanItem>,
    val predictedLabel: String?,
    val confidence: Double?,
    val suggestedMealType: String,
    val disclaimer: String?
)

data class FoodScanMacros(
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?,
    val fiberG: Double?,
    val sugarG: Double? = null
)

data class FoodScanItem(
    val name: String,
    val foodId: Int?,
    val label: String?,
    val confidence: Double?,
    val grams: Double,
    val kcal: Double,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?,
    val fiberG: Double?
)

data class FoodScanLogSummary(
    val id: Int,
    val title: String,
    val mealType: String,
    val mealTypeLabel: String,
    val loggedAt: String,
    val timeDisplay: String,
    val totalKcal: Double,
    val thumbnailUrl: String?
)

data class FoodScanLogList(
    val total: Int,
    val items: List<FoodScanLogSummary>
)
