package com.example

import com.example.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/product") {
            get("/{barcode}") {
                val barcode = call.parameters["barcode"]

                if (barcode == null) {
                    call.respondText("Brakuje barcode", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val productWithIngredients = transaction {
                    val productRow = Products.selectAll().where { Products.barcode eq barcode }.singleOrNull()
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

                if (productWithIngredients == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(productWithIngredients)
                }
            }

            post("/save") {
                val product = call.receive<ProductRequest>()
                transaction {
                    val productId = Products.insertAndGetId {
                        it[barcode] = product.barcode
                        it[name] = product.name
                        it[calories] = product.macrosPer100g.calories
                        it[protein] = product.macrosPer100g.protein
                        it[fat] = product.macrosPer100g.fat
                        it[carbs] = product.macrosPer100g.carbs
                        it[weight] = product.grams
                    }

                    product.ingredients.forEach { ingredientName ->
                        IngredientsTable.insert {
                            it[IngredientsTable.product] = productId
                            it[name] = ingredientName
                            it[tag] = getTagForIngredient(ingredientName)
                        }
                    }
                }
                call.respondText("Produkt i składniki zapisane")
            }

            get("/all") {
                val products = transaction {
                    Products.selectAll().map {
                        ProductDTO(
                            barcode = it[Products.barcode],
                            name = it[Products.name],
                            calories = it[Products.calories],
                            protein = it[Products.protein],
                            fat = it[Products.fat],
                            carbs = it[Products.carbs],
                            weight = it[Products.weight]
                        )
                    }
                }
                call.respond(products)
            }
        }
    }
}

fun getTagForIngredient(ingredientName: String): String {
    return "neutral"
}
