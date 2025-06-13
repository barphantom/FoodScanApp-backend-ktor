package com.example

import com.example.model.*
import com.example.repository.ProductRepository
import com.example.repository.UserRepository
import com.example.services.OpenFoodFactsService
import com.example.utils.IngredientTagHelper

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureRouting() {
    val productRepository = ProductRepository()
    val userRepository = UserRepository()

    routing {
        get("/") {
            call.respondText("Hello World!")
            call.principal<UserIdPrincipal>()?.name
        }

        authenticate {
            route("/product") {
                get("/{barcode}") {
                    val barcode = call.parameters["barcode"]

                    if (barcode == null) {
                        call.respondText("Brakuje barcode",
                            status = HttpStatusCode.BadRequest)
                        return@get
                    }

                    val principal = call.principal<UserIdPrincipal>()
                    val uid = principal?.name
                    println("Zalogowany UID: $uid")

                    val product = productRepository.getProductFromDatabase(barcode)
                    if (product != null) {
                        call.respond(product)
                    } else {
                        val fetched = OpenFoodFactsService.fetchProduct(barcode)
                        if (fetched != null) {
                            call.respond(fetched)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Nie znaleziono produktu.")
                        }
                    }
                }

                delete("/{barcode}") {
                    val currentUserUID = call.principal<UserIdPrincipal>()?.name
                    if (currentUserUID == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                        return@delete
                    }

                    val barcode = call.parameters["barcode"]
                    if (barcode == null) {
                        call.respond(HttpStatusCode.BadRequest, "Brakuje barcode")
                        return@delete
                    }

                    val user = userRepository.findByUid(currentUserUID)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, "Użytkownik nie istnieje")
                        return@delete
                    }

                    val deleted = transaction {
                        val product = Products
                            .selectAll()
                            .where { Products.barcode eq barcode }
                            .andWhere { Products.user eq user.id }
                            .singleOrNull()

                        if (product == null) {
                            false
                        } else {
                            val productId = product[Products.id]
                            IngredientsTable
                                .deleteWhere { IngredientsTable.product eq productId }
                            Products
                                .deleteWhere { Products.id eq productId }
                            true
                        }
                    }

                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "Produkt został usunięty")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Nie znaleziono produktu użytkownika")
                    }
                }

                post("/save") {
                    val product = call.receive<ProductRequest>()

                    val currentUserUID = call.principal<UserIdPrincipal>()?.name
                    if (currentUserUID == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                        return@post
                    }

                    val user = userRepository.findByUid(currentUserUID)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    val alreadyExists = transaction {
                        val p = Products
                            .selectAll()
                            .where { Products.barcode eq product.barcode }
                            .andWhere { Products.user eq user.id }
                            .singleOrNull()

                        p != null
                    }

                    if (alreadyExists) {
                        call.respond(HttpStatusCode.BadRequest,
                            "Produkt o tym kodzie kreskowym już istnieje")
                        return@post
                    }

                    transaction {
                        val productId = Products.insertAndGetId {
                            it[barcode] = product.barcode
                            it[name] = product.name
                            it[calories] = productRepository.calculateMacroForProduct(product.macrosPer100g.calories, product.grams)
                            it[protein] = productRepository.calculateMacroForProduct(product.macrosPer100g.protein, product.grams)
                            it[fat] = productRepository.calculateMacroForProduct(product.macrosPer100g.fat, product.grams)
                            it[carbs] = productRepository.calculateMacroForProduct(product.macrosPer100g.carbs, product.grams)
                            it[weight] = product.grams
                            it[Products.user] = user.id
                        }

                        product.ingredients.forEach { ingredientName ->
                            IngredientsTable.insert {
                                it[IngredientsTable.product] = productId
                                it[name] = ingredientName
                                it[tag] = IngredientTagHelper.getIngredientTag(ingredientName)
                            }
                        }
                    }
                    call.respondText("Produkt i składniki zapisane")
                }


                get("/all") {
                    val currentUserUID = call.principal<UserIdPrincipal>()?.name
                    if (currentUserUID == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                        return@get
                    }

                    val findUser = userRepository.findByUid(currentUserUID)
                    if (findUser == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    val products = transaction {
                        Products.selectAll().where { Products.user eq findUser.id }
                            .map {
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
}
