package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OffProductWrapper(
    val code: String,
    val status: Int,
    val product: OffProductInfo? = null
)

@Serializable
data class OffProductInfo(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("quantity") val quantity: String? = null,
    val nutriments: OffNutriments? = null,
    val ingredients: List<OffIngredient>? = null,
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal") val energyKcal: Float? = null,
    @SerialName("carbohydrates") val carbs: Float? = null,
    @SerialName("fat") val fats: Float? = null,
    @SerialName("proteins") val proteins: Float? = null,
)

@Serializable
data class OffIngredient(
    val text: String,
    val vegan: String? = null,
    val vegetarian: String? = null
)