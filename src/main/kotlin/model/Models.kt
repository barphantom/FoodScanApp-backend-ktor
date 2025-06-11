package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    val barcode: String,
    val name: String,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val weight: Double
)

@Serializable
data class ProductRequest(
    val barcode: String,
    val name: String,
    val grams: Double,
    val macrosPer100g: Macros,
    val ingredients: List<String>
)

@Serializable
data class ProductResponse(
    val name: String,
    val macros: Macros,
    val ingredients: List<Ingredient>
)

@Serializable
data class Macros(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

@Serializable
data class Ingredient(
    val name: String,
    val tag: String // "green", "red", "neutral"
)