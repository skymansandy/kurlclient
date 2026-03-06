package dev.skymansandy.kurl.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

internal actual fun createHttpClient(): HttpClient = HttpClient(Java) {
    engine {
        config {
            sslContext(createCapturingSslContext())
        }
    }
}