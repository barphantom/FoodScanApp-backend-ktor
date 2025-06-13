package com.example.services

import com.example.model.*
import com.example.utils.IngredientTagHelper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


object OpenFoodFactsService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchProduct(barcode: String): ProductResponse? {
        return try {
            val response: OffProductWrapper = client.get("https://world.openfoodfacts.org/api/v2/product/$barcode") {
                parameter("fields", "product_name,nutriments,ingredients_text,ingredients,quantity")
                parameter("lc", "pl")
            }.body()

            val info = response.product ?: return null
            val weight = parseWeight(info.quantity) ?: 100.0

            val macros = Macros(
                protein = (info.nutriments?.proteins?.toDouble() ?: 0.0) * (weight / 100),
                carbs = (info.nutriments?.carbs?.toDouble() ?: 0.0) * (weight / 100),
                fat = (info.nutriments?.fats?.toDouble() ?: 0.0) * (weight / 100),
                calories = (info.nutriments?.energyKcal?.toDouble() ?: 0.0) * (weight / 100)
            )

            val ingredients = info.ingredients?.map {
                Ingredient(name = it.text, tag = IngredientTagHelper.getIngredientTag(it.text))
            } ?: emptyList()

            ProductResponse(
                name = info.productName ?: "Nieznany produkt",
                macros = macros,
                ingredients = ingredients
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun determineTag(ingredient: OffIngredient): String {
        return when (ingredient.vegan?.lowercase()) {
            "no" -> "red"
            "maybe" -> "neutral"
            else -> "green"
        }
    }

    private fun parseWeight(quantity: String?): Double? {
        if (quantity == null) return null
        val regex = Regex("(\\d+(\\.\\d+)?)")
        val match = regex.find(quantity)
        return match?.value?.toDoubleOrNull()
    }

}
