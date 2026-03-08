package dev.skymansandy.kurl.core.engine.network

import dev.skymansandy.kurl.core.model.NetworkInfo

internal expect fun buildNetworkInfo(requestUrl: String, httpVersion: String): NetworkInfo