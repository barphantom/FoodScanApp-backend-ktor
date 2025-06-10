package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.slf4j.event.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/produkt") {
            get("/{barcode}") {
                val barcode = call.parameters["barcode"]
                if (barcode == null) {
                    call.respondText("Brakuje barcode", status = HttpStatusCode.BadRequest)
                }

                call.respond(
                    ProductResponse(
                        name = "Test produkt",
                        macros = Macros(22.0, 30.0, 5.0, 400.0),
                        ingredients = listOf(
                            Ingredient("olej palmowy", "red"),
                            Ingredient("kakao", "green"),
                            Ingredient("woda", "neutral")
                        ),
                    )
                )
            }

            post {
                val product = call.receive<ProductRequest>()
                call.respond(product)
            }
        }
    }
}

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
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double
)

@Serializable
data class Ingredient(
    val name: String,
    val tag: String // "green", "red", "neutral"
)