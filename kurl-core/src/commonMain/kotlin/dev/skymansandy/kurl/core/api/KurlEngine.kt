package dev.skymansandy.kurl.core.api

import dev.skymansandy.kurl.core.model.KurlRequest
import dev.skymansandy.kurl.core.model.KurlResponse

interface KurlEngine {

    suspend fun execute(request: KurlRequest): KurlResponse

    fun close()
}
