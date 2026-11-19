package com.pro.aifitnesstracker.data.foodscan

import com.pro.aifitnesstracker.data.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class FoodScanApi(
    private val client: OkHttpClient = defaultClient()
) {
    suspend fun analyze(imageFile: File, clientId: String): FoodScanAnalyzeResult =
        withContext(Dispatchers.IO) {
            val mediaType = guessImageMediaType(imageFile.name)
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/food-scans/analyze")
                .header("X-API-Key", ApiConfig.API_KEY)
                .header("X-Client-Id", clientId)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException(parseErrorMessage(raw, response.code))
                }
                val draft = parseAnalyzeResponse(JSONObject(raw))
                enrichNutritionIfMissing(draft)
            }
        }

    suspend fun listLogs(
        clientId: String,
        limit: Int? = null,
        offset: Int = 0
    ): FoodScanLogList = withContext(Dispatchers.IO) {
        val query = buildString {
            append("client_id=").append(encode(clientId))
            append("&offset=").append(offset)
            if (limit != null) append("&limit=").append(limit)
        }
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/food-scans/logs?$query")
            .header("X-API-Key", ApiConfig.API_KEY)
            .header("X-Client-Id", clientId)
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(parseErrorMessage(raw, response.code))
            }
            parseLogList(JSONObject(raw))
        }
    }

    suspend fun deleteLogs(clientId: String, ids: Collection<Int>): Unit =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("client_id", clientId)
                put("ids", JSONArray().apply { ids.forEach { put(it) } })
            }
            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/food-scans/logs")
                .header("X-API-Key", ApiConfig.API_KEY)
                .header("X-Client-Id", clientId)
                .header("Content-Type", "application/json")
                .delete(payload.toString().toRequestBody(JsonMediaType))
                .build()
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException(parseErrorMessage(raw, response.code))
                }
            }
        }

    suspend fun addToLog(result: FoodScanAnalyzeResult, clientId: String): Unit =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("client_id", clientId)
                put("title", result.title)
                if (!result.tag.isNullOrBlank()) put("tag", result.tag)
                put("meal_type", result.suggestedMealType.ifBlank { "snack" })
                put("servings", result.servings)
                put("total_grams", result.totalGrams)
                put("total_kcal", result.totalKcal)
                put(
                    "macros",
                    JSONObject().apply {
                        result.macros.proteinG?.let { put("protein_g", it) }
                        result.macros.carbsG?.let { put("carbs_g", it) }
                        result.macros.fatG?.let { put("fat_g", it) }
                        result.macros.fiberG?.let { put("fiber_g", it) }
                        result.macros.sugarG?.let { put("sugar_g", it) }
                    }
                )
                put(
                    "items",
                    JSONArray().apply {
                        result.items.forEach { item ->
                            put(
                                JSONObject().apply {
                                    put("name", item.name)
                                    item.foodId?.let { put("food_id", it) }
                                    item.label?.let { put("label", it) }
                                    item.confidence?.let { put("confidence", it) }
                                    put("grams", item.grams)
                                    put("kcal", item.kcal)
                                    item.proteinG?.let { put("protein_g", it) }
                                    item.carbsG?.let { put("carbs_g", it) }
                                    item.fatG?.let { put("fat_g", it) }
                                    item.fiberG?.let { put("fiber_g", it) }
                                }
                            )
                        }
                    }
                )
                result.predictedLabel?.let { put("predicted_label", it) }
                result.confidence?.let { put("confidence", it) }
            }

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/food-scans/logs")
                .header("X-API-Key", ApiConfig.API_KEY)
                .header("X-Client-Id", clientId)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException(parseErrorMessage(raw, response.code))
                }
            }
        }

    /**
     * Backend often returns title/label with food_id=null and macros/kcal all null/0
     * when the classifier label is not mapped in DB. Enrich via foods/search + nutrition/calculate.
     */
    private fun enrichNutritionIfMissing(draft: FoodScanAnalyzeResult): FoodScanAnalyzeResult {
        val macrosMissing = draft.macros.proteinG == null &&
            draft.macros.carbsG == null &&
            draft.macros.fatG == null &&
            draft.macros.fiberG == null
        val kcalMissing = draft.totalKcal <= 0.0
        if (!macrosMissing && !kcalMissing) return draft

        val querySeeds = buildList {
            draft.predictedLabel
                ?.replace('_', ' ')
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { add(it) }
            draft.title.trim().takeIf { it.isNotBlank() }?.let { add(it) }
            draft.items.firstOrNull()?.label
                ?.replace('_', ' ')
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { add(it) }
            draft.items.firstOrNull()?.name?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
        }.distinctBy { it.lowercase() }

        if (querySeeds.isEmpty()) return draft

        val grams = draft.totalGrams.takeIf { it > 0 } ?: 300.0
        var bestMatch: FoodSearchHit? = null
        var bestScore = 0
        for (seed in querySeeds) {
            for (variant in queryVariants(seed)) {
                val hits = searchFoods(variant)
                val ranked = hits
                    .filter { it.caloriesPer100g != null && it.caloriesPer100g > 0 }
                    .map { it to scoreFoodMatch(seed, it.name) }
                    .filter { it.second > 0 }
                    .sortedWith(
                        compareByDescending<Pair<FoodSearchHit, Int>> { it.second }
                            .thenBy { it.first.name.length }
                    )
                val top = ranked.firstOrNull() ?: continue
                if (top.second > bestScore) {
                    bestScore = top.second
                    bestMatch = top.first
                }
            }
            if (bestScore >= 300) break
        }

        val match = bestMatch ?: return draft
        val nutrition = calculateNutrition(
            foodId = match.id,
            servingGrams = grams,
            predictedLabel = draft.predictedLabel,
            confidence = draft.confidence
        ) ?: return draft

        val enrichedItem = (draft.items.firstOrNull() ?: FoodScanItem(
            name = nutrition.foodName,
            foodId = nutrition.foodId,
            label = draft.predictedLabel,
            confidence = draft.confidence,
            grams = grams,
            kcal = 0.0,
            proteinG = null,
            carbsG = null,
            fatG = null,
            fiberG = null
        )).copy(
            name = nutrition.foodName.ifBlank { draft.title },
            foodId = nutrition.foodId,
            grams = grams,
            kcal = nutrition.caloriesKcal ?: 0.0,
            proteinG = nutrition.proteinG,
            carbsG = nutrition.carbsG,
            fatG = nutrition.fatG,
            fiberG = nutrition.fiberG
        )

        val tag = draft.tag?.takeIf { it.isNotBlank() }
            ?: if (nutrition.isApproximate) {
                "Approximate; estimated from food database"
            } else {
                "Estimated from food database"
            }

        return draft.copy(
            title = draft.title.ifBlank { nutrition.foodName },
            tag = tag,
            totalGrams = grams,
            totalKcal = nutrition.caloriesKcal ?: 0.0,
            macros = FoodScanMacros(
                proteinG = nutrition.proteinG,
                carbsG = nutrition.carbsG,
                fatG = nutrition.fatG,
                fiberG = nutrition.fiberG,
                sugarG = nutrition.sugarG
            ),
            items = listOf(enrichedItem)
        )
    }

    private fun searchFoods(query: String): List<FoodSearchHit> {
        val url = "${ApiConfig.BASE_URL}/api/foods/search?q=${encode(query)}&limit=20"
        val request = Request.Builder()
            .url(url)
            .header("X-API-Key", ApiConfig.API_KEY)
            .get()
            .build()
        return client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) return emptyList()
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        FoodSearchHit(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            caloriesPer100g = obj.optDoubleOrNull("calories_per_100g")
                        )
                    )
                }
            }
        }
    }

    private fun calculateNutrition(
        foodId: Int,
        servingGrams: Double,
        predictedLabel: String?,
        confidence: Double?
    ): NutritionCalcHit? {
        val payload = JSONObject().apply {
            put("food_id", foodId)
            put("serving_weight_grams", servingGrams)
            put("serving_count", 1.0)
            put("save_history", false)
            predictedLabel?.let { put("predicted_label", it) }
            confidence?.let { put("confidence", it) }
        }
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/nutrition/calculate")
            .header("X-API-Key", ApiConfig.API_KEY)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JsonMediaType))
            .build()
        return client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) return null
            val json = JSONObject(raw)
            NutritionCalcHit(
                foodId = json.getInt("food_id"),
                foodName = json.getString("food_name"),
                isApproximate = json.optBoolean("is_approximate", false),
                caloriesKcal = json.optDoubleOrNull("calories_kcal"),
                proteinG = json.optDoubleOrNull("protein_g"),
                carbsG = json.optDoubleOrNull("carbohydrates_g"),
                fatG = json.optDoubleOrNull("fat_g"),
                fiberG = json.optDoubleOrNull("fiber_g"),
                sugarG = json.optDoubleOrNull("sugar_g")
            )
        }
    }

    private fun queryVariants(seed: String): List<String> {
        val base = seed.trim()
        if (base.isBlank()) return emptyList()
        val variants = linkedSetOf(base)
        if (!base.contains(' ')) {
            variants += "$base cheese"
            variants += "cheese $base"
            variants += "$base chicken"
            variants += "homemade $base"
        }
        return variants.toList()
    }

    private fun scoreFoodMatch(query: String, name: String): Int {
        val q = query.lowercase().trim()
        val n = name.lowercase().trim()
        if (q.isBlank() || n.isBlank()) return 0

        val junk = listOf(
            "flour", "mix", "cracker", "sauce", "seasoning", "spice",
            "dough", "crust mix", "cheese blend", "supplement"
        )
        if (junk.any { n.contains(it) } && !q.split(' ').any { it.length > 3 && n == it }) {
            // Heavily penalize obvious non-meals unless query itself is that word
            if (junk.any { q == it || q.endsWith(" $it") }) {
                // allow
            } else {
                return 0
            }
        }

        var score = 0
        if (n == q) score += 1000
        if (n.startsWith(q)) score += 400
        if (Regex("""\b${Regex.escape(q)}\b""").containsMatchIn(n)) score += 300
        if (n.contains(q)) score += 120

        // Prefer compact product names
        score += (120 - n.length).coerceIn(-80, 80)

        // Prefer names whose tokens overlap query tokens
        val qTokens = q.split(Regex("\\s+")).filter { it.length > 2 }
        val nTokens = n.split(Regex("[^a-z0-9]+")).filter { it.length > 2 }
        score += qTokens.count { token -> nTokens.any { it == token || it.startsWith(token) } } * 60

        // Tiny preference for closer calorie density to "normal meal" range is not needed
        return score
    }

    private fun parseLogList(json: JSONObject): FoodScanLogList {
        val itemsJson = json.optJSONArray("items") ?: JSONArray()
        val items = buildList {
            for (i in 0 until itemsJson.length()) {
                val item = itemsJson.getJSONObject(i)
                add(
                    FoodScanLogSummary(
                        id = item.getInt("id"),
                        title = item.getString("title"),
                        mealType = item.optString("meal_type", "snack"),
                        mealTypeLabel = item.optString("meal_type_label", "Snack"),
                        loggedAt = item.optString("logged_at", ""),
                        timeDisplay = item.optString("time_display", ""),
                        totalKcal = item.optDouble("total_kcal", 0.0),
                        thumbnailUrl = item.optStringOrNull("thumbnail_url")
                    )
                )
            }
        }
        return FoodScanLogList(
            total = json.optInt("total", items.size),
            items = items
        )
    }

    private fun parseAnalyzeResponse(json: JSONObject): FoodScanAnalyzeResult {
        val macrosJson = json.optJSONObject("macros") ?: JSONObject()
        val itemsJson = json.optJSONArray("items") ?: JSONArray()
        val items = buildList {
            for (i in 0 until itemsJson.length()) {
                val item = itemsJson.getJSONObject(i)
                add(
                    FoodScanItem(
                        name = item.getString("name"),
                        foodId = item.optIntOrNull("food_id"),
                        label = item.optStringOrNull("label"),
                        confidence = item.optDoubleOrNull("confidence"),
                        grams = item.getDouble("grams"),
                        kcal = item.getDouble("kcal"),
                        proteinG = item.optDoubleOrNull("protein_g"),
                        carbsG = item.optDoubleOrNull("carbs_g"),
                        fatG = item.optDoubleOrNull("fat_g"),
                        fiberG = item.optDoubleOrNull("fiber_g")
                    )
                )
            }
        }
        return FoodScanAnalyzeResult(
            title = json.getString("title"),
            tag = json.optStringOrNull("tag"),
            servings = json.optDouble("servings", 1.0),
            totalGrams = json.getDouble("total_grams"),
            totalKcal = json.getDouble("total_kcal"),
            macros = FoodScanMacros(
                proteinG = macrosJson.optDoubleOrNull("protein_g"),
                carbsG = macrosJson.optDoubleOrNull("carbs_g"),
                fatG = macrosJson.optDoubleOrNull("fat_g"),
                fiberG = macrosJson.optDoubleOrNull("fiber_g"),
                sugarG = macrosJson.optDoubleOrNull("sugar_g")
            ),
            items = items,
            predictedLabel = json.optStringOrNull("predicted_label"),
            confidence = json.optDoubleOrNull("confidence"),
            suggestedMealType = json.optString("suggested_meal_type", "snack"),
            disclaimer = json.optStringOrNull("disclaimer")
        )
    }

    private fun parseErrorMessage(raw: String, code: Int): String {
        if (raw.isBlank()) return "Request failed ($code)"
        return try {
            val json = JSONObject(raw)
            when {
                json.has("detail") -> {
                    val detail = json.get("detail")
                    when (detail) {
                        is JSONArray -> detail.optJSONObject(0)?.optString("msg")
                            ?: detail.toString()
                        else -> detail.toString()
                    }
                }
                json.has("message") -> json.getString("message")
                else -> raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun guessImageMediaType(name: String) = when {
        name.endsWith(".png", true) -> "image/png".toMediaType()
        name.endsWith(".webp", true) -> "image/webp".toMediaType()
        else -> "image/jpeg".toMediaType()
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) return null
        val value = optString(key, "").trim()
        return value.ifBlank { null }
    }

    private fun JSONObject.optDoubleOrNull(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        val value = optDouble(key)
        if (value.isNaN()) return null
        return value
    }

    private fun JSONObject.optIntOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        return optInt(key)
    }

    private data class FoodSearchHit(
        val id: Int,
        val name: String,
        val caloriesPer100g: Double?
    )

    private data class NutritionCalcHit(
        val foodId: Int,
        val foodName: String,
        val isApproximate: Boolean,
        val caloriesKcal: Double?,
        val proteinG: Double?,
        val carbsG: Double?,
        val fatG: Double?,
        val fiberG: Double?,
        val sugarG: Double?
    )

    companion object {
        private val JsonMediaType = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
        }
    }
}
