package dev.skymansandy.kurlclient.util.curlparser

import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry

internal fun buildCurlCommand(
    url: String,
    method: HttpMethod,
    headers: List<KeyValueEntry>,
    params: List<KeyValueEntry>,
    body: String,
): String = buildString {
    append("curl")

    if (method != HttpMethod.GET) {
        append(" -X ${method.name}")
    }

    // Append active query params to URL
    val activeParams = params.filter { it.enabled && it.key.isNotBlank() }
    val effectiveUrl = if (activeParams.isEmpty()) {
        url
    } else {
        val qs = activeParams.joinToString("&") { "${it.key.urlEncode()}=${it.value.urlEncode()}" }
        val sep = if ('?' in url) "&" else "?"
        "$url$sep$qs"
    }
    append(" '${effectiveUrl.escapeSingleQuotes()}'")

    headers.filter { it.enabled && it.key.isNotBlank() }.forEach { h ->
        append(" \\\n  -H '${h.key}: ${h.value.escapeSingleQuotes()}'")
    }

    if (body.isNotBlank()) {
        append(" \\\n  --data-raw '${body.escapeSingleQuotes()}'")
    }
}

internal fun parseCurlCommand(raw: String): ParsedCurlRequest? {
    // Normalise: strip line continuation, collapse whitespace
    val command = raw.trim()
        .replace(Regex("\\\\\r?\n\\s*"), " ")
        .trim()

    val tokens = tokenize(command)
    if (tokens.isEmpty() || tokens[0].lowercase() != "curl") return null

    var url: String? = null
    var method: String? = null
    val headers = mutableListOf<Pair<String, String>>()
    var body: String? = null
    var i = 1

    while (i < tokens.size) {
        when (tokens[i]) {
            "-X", "--request" -> {
                method = tokens.getOrNull(++i)
                i++
            }

            "-H", "--header" -> {
                tokens.getOrNull(++i)?.let { hdr ->
                    val colon = hdr.indexOf(':')
                    if (colon > 0) {
                        headers += hdr.substring(0, colon).trim() to hdr.substring(colon + 1).trim()
                    }
                }
                i++
            }

            "-d", "--data", "--data-raw", "--data-binary" -> {
                body = tokens.getOrNull(++i)
                i++
            }

            "--data-urlencode" -> {
                // treat as body for simplicity
                body = tokens.getOrNull(++i)
                i++
            }

            "--url" -> {
                url = tokens.getOrNull(++i)
                i++
            }
            // Flags that take a value we don't use
            "-u", "--user", "-A", "--user-agent", "--proxy",
            "--max-time", "--connect-timeout", "-e", "--referer",
            "-o", "--output", "--cacert", "--cert", "--key",
            -> {
                i += 2
            }
            // Boolean flags to skip
            "--compressed", "--insecure", "-k", "-L", "--location",
            "-v", "--verbose", "-s", "--silent", "-i", "--include",
            "-G", "--get",
            -> {
                i++
            }

            else -> {
                val t = tokens[i]
                // Positional URL (not a flag, and no URL captured yet)
                if (!t.startsWith("-") && url == null) url = t
                i++
            }
        }
    }

    if (url == null) return null

    // Split URL into base + query params
    val base = url.substringBefore('?')
    val qs = url.substringAfter('?', "")
    val params = if (qs.isNotBlank()) {
        qs.split('&').mapNotNull { part ->
            val eq = part.indexOf('=')
            if (eq >= 0) part.substring(0, eq).urlDecode() to part.substring(eq + 1).urlDecode()
            else if (part.isNotBlank()) part.urlDecode() to ""
            else null
        }
    } else emptyList()

    val inferredMethod = when {
        method != null -> method.uppercase()
        body != null -> "POST"
        else -> "GET"
    }

    return ParsedCurlRequest(
        url = base,
        method = inferredMethod,
        headers = headers,
        params = params,
        body = body,
    )
}

// ── Tokenizer ─────────────────────────────────────────────────────────────────

private fun tokenize(s: String): List<String> {
    val tokens = mutableListOf<String>()
    val buf = StringBuilder()
    var i = 0

    while (i < s.length) {
        when (val c = s[i]) {
            ' ', '\t' -> {
                if (buf.isNotEmpty()) {
                    tokens += buf.toString(); buf.clear()
                }
                i++
            }

            '\'' -> {
                // Single-quoted: literal content, handle bash '\'' idiom
                i++
                while (i < s.length && s[i] != '\'') {
                    buf.append(s[i++])
                }
                i++ // skip closing '
                // Handle '\'' (end quote, escaped single quote, reopen quote)
                if (i < s.length && s[i] == '\\' && i + 2 < s.length && s[i + 1] == '\'' && s[i + 2] == '\'') {
                    buf.append('\'')
                    i += 3
                }
            }

            '"' -> {
                i++
                while (i < s.length && s[i] != '"') {
                    if (s[i] == '\\' && i + 1 < s.length) {
                        i++
                        buf.append(
                            when (s[i]) {
                                'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> s[i]
                            },
                        )
                    } else {
                        buf.append(s[i])
                    }
                    i++
                }
                i++ // skip closing "
            }

            '\\' -> {
                if (i + 1 < s.length) {
                    i++; buf.append(s[i])
                }
                i++
            }

            else -> {
                buf.append(c); i++
            }
        }
    }
    if (buf.isNotEmpty()) tokens += buf.toString()
    return tokens
}
