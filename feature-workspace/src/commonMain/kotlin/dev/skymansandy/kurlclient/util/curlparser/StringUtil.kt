package dev.skymansandy.kurlclient.util.curlparser

internal fun String.escapeSingleQuotes(): String = replace("'", "'\\''")

internal fun String.urlEncode(): String = buildString {
    this@urlEncode.forEach { c ->
        when {
            c.isLetterOrDigit() || c in "-._~" -> append(c)
            else -> append('%').append(c.code.toString(16).padStart(2, '0').uppercase())
        }
    }
}

internal fun String.urlDecode(): String = buildString {
    var i = 0
    while (i < this@urlDecode.length) {
        if (this@urlDecode[i] == '%' && i + 2 < this@urlDecode.length) {
            val hex = this@urlDecode.substring(i + 1, i + 3)
            hex.toIntOrNull(16)?.let { append(it.toChar()) } ?: append('%')
            i += 3
        } else if (this@urlDecode[i] == '+') {
            append(' '); i++
        } else {
            append(this@urlDecode[i++])
        }
    }
}
