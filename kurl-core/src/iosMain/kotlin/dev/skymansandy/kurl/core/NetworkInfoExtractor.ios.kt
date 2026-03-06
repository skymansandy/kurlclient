package dev.skymansandy.kurl.core

import dev.skymansandy.kurl.core.model.NetworkInfo

internal actual fun buildNetworkInfo(requestUrl: String, httpVersion: String): NetworkInfo {
    val isHttps = requestUrl.startsWith("https://", ignoreCase = true)
    val host = requestUrl
        .removePrefix("https://").removePrefix("http://")
        .substringBefore("/").substringBefore("?")

    return NetworkInfo(
        httpVersion = httpVersion,
        remoteAddress = host,
        tlsProtocol = if (isHttps) "TLS" else null
    )
}