package dev.skymansandy.kurlclient.util.curlparser

internal data class ParsedCurlRequest(
    val url: String,
    val method: String,
    val headers: List<Pair<String, String>>,
    val params: List<Pair<String, String>>,
    val body: String?
)
