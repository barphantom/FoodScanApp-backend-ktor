package com.example

import io.ktor.server.application.*
import com.example.utils.FirebaseAdmin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    FirebaseAdmin.init()

    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()
}

