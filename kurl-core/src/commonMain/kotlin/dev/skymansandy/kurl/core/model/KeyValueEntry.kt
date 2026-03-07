package dev.skymansandy.kurl.core.model

data class KeyValueEntry(
    val id: Long,
    val key: String = "",
    val value: String = "",
    val enabled: Boolean = true,
)
