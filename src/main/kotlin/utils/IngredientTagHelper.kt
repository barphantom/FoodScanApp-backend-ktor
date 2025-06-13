package com.example.utils

object IngredientTagHelper {
    private val ingredientTagMap = mapOf(
        // Szkodliwe składniki
        "cukier" to "red",
        "sugar" to "red",
        "zucker" to "red",
        "glukoza" to "red",
        "glucose" to "red",
        "sirup" to "red",
        "syrop glukozowy" to "red",
        "syrop fruktozowy" to "red",
        "fructose syrup" to "red",
        "fruktose" to "red",
        "olej palmowy" to "red",
        "palm oil" to "red",
        "palmenöl" to "red",


        // Zdrowe składniki
        "woda" to "green",
        "water" to "green",
        "eau" to "green",
        "marchew" to "green",
        "carrot" to "green",
        "karotte" to "green",
        "pomidor" to "green",
        "tomato" to "green",
        "tomate" to "green",
        "szpinak" to "green",
        "spinach" to "green",
        "spinat" to "green",
        "ocet" to "green",
        "vinegar" to "green",

        // Neutralne składniki
        "sól" to "neutral",
        "salt" to "neutral",
        "sel" to "neutral",
        "skrobia" to "neutral",
        "starch" to "neutral",
        "stärke" to "neutral",
        "mąka" to "neutral",
        "flour" to "neutral",

    )

    fun getIngredientTag(name: String): String {
        val cleanedName = name.lowercase().trim()

        for ((key, tag) in ingredientTagMap) {
            if (cleanedName.contains(key)) {
                return tag
            }
        }
        return "neutral"
    }
}
