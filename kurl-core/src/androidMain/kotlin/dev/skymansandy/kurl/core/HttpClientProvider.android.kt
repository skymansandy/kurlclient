package dev.skymansandy.kurl.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

internal actual fun createHttpClient(): HttpClient = HttpClient(Android)