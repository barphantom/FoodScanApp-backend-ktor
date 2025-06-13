package com.example.repository

import com.example.IngredientsTable
import com.example.model.*
import com.example.Products
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.round


class ProductRepository {

    fun getProductFromDatabase(barcode: String): ProductResponse? {
        return transaction {
            val productRow = Products.selectAll()
                .where { Products.barcode eq barcode }.singleOrNull()
            if (productRow == null) {
                return@transaction null
            }

            val productId = productRow[Products.id]

            val ingredients = IngredientsTable
                .selectAll()
                .where { IngredientsTable.product eq productId }
                .map {
                    Ingredient(
                        name = it[IngredientsTable.name],
                        tag = it[IngredientsTable.tag]
                    )
                }

            ProductResponse(
                name = productRow[Products.name],
                macros = Macros(
                    calories = productRow[Products.calories],
                    protein = productRow[Products.protein],
                    fat = productRow[Products.fat],
                    carbs = productRow[Products.carbs],
                ),
                ingredients = ingredients,
            )
        }
    }

    fun calculateMacroForProduct(macroPer100g: Double, productWeight: Double): Double {
        val multiplier = round(productWeight / 100.0)
        return macroPer100g * multiplier
    }
}
