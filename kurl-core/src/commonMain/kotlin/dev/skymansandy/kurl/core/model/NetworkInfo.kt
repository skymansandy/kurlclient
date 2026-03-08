package dev.skymansandy.kurl.core.model

data class NetworkInfo(
    val httpVersion: String? = null,
    val localAddress: String? = null,
    val remoteAddress: String? = null,
    val tlsProtocol: String? = null,
    val cipherName: String? = null,
    val certificateCN: String? = null,
    val issuerCN: String? = null,
    val validUntil: String? = null,
)
