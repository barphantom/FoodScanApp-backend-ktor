package com.example

import com.example.services.FirebaseAuthService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureSecurity() {
    install(Authentication) {
        bearer("firebase") {
            authenticate { tokenCredential ->
                val token = FirebaseAuthService.verifyToken(tokenCredential.token)
                if (token != null) {
                    UserIdPrincipal(token.uid) // UID Firebase jako identyfikator
                } else null
            }
        }
    }
}
