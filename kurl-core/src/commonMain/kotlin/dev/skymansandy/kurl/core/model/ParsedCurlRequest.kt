package dev.skymansandy.kurl.core.model

data class ParsedCurlRequest(
    val url: String,
    val method: String,
    val headers: List<Pair<String, String>>,
    val params: List<Pair<String, String>>,
    val body: String?
)
