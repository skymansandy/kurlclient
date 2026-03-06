package dev.skymansandy.kurl.core.model

data class KurlResponse(
    val statusCode: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val body: String,
    val timeMs: Long,
    val sizeBytes: Long
)