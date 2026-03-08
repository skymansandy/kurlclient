package dev.skymansandy.kurl.core.engine.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        configureSession {
            // HTTP/2 is enabled by default in NSURLSession
        }
    }
}
