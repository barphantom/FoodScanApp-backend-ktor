package com.example.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseAdmin {

    fun init() {
        val serviceAccount = this::class.java.classLoader
            .getResourceAsStream("firebase-service-account.json")
            ?: throw IllegalStateException("Brak pliku firebase-service-account.json w resources!")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }
}

