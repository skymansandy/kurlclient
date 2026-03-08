package dev.skymansandy.kurl.core.engine.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import java.net.http.HttpClient.Version

internal actual fun createHttpClient(): HttpClient = HttpClient(Java) {
    engine {
        config {
            version(Version.HTTP_2)
            sslContext(createCapturingSslContext())
        }
    }
}
