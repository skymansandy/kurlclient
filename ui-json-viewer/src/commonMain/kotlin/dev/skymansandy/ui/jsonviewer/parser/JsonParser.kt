package dev.skymansandy.ui.jsonviewer.parser

import dev.skymansandy.ui.jsonviewer.model.JNode

internal class JsonParser(private val src: String) {

    private var pos = 0
    private var nextId = 0

    fun parse(): JNode { ws(); return value() }

    private fun value(): JNode = when {
        cur() == '{'                 -> parseObj()
        cur() == '['                 -> parseArr()
        cur() == '"'                 -> JNode.Str(strVal())
        src.startsWith("true",  pos) -> { pos += 4; JNode.Bool(true) }
        src.startsWith("false", pos) -> { pos += 5; JNode.Bool(false) }
        src.startsWith("null",  pos) -> { pos += 4; JNode.Null }
        else                         -> parseNum()
    }

    private fun parseObj(): JNode.Obj {
        val id = nextId++
        eat('{'); ws()
        val entries = mutableListOf<Pair<String, JNode>>()
        if (cur() != '}') {
            while (true) {
                ws()
                val k = strVal(); ws(); eat(':'); ws()
                entries += k to value()
                ws()
                if (cur() == ',') { pos++; ws() } else break
            }
        }
        eat('}')
        return JNode.Obj(id, entries)
    }

    private fun parseArr(): JNode.Arr {
        val id = nextId++
        eat('['); ws()
        val items = mutableListOf<JNode>()
        if (cur() != ']') {
            while (true) {
                ws(); items += value(); ws()
                if (cur() == ',') { pos++; ws() } else break
            }
        }
        eat(']')
        return JNode.Arr(id, items)
    }

    private fun strVal(): String {
        eat('"')
        val sb = StringBuilder()
        while (pos < src.length && src[pos] != '"') {
            if (src[pos] == '\\' && pos + 1 < src.length) {
                pos++
                when (src[pos]) {
                    '"'  -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/'  -> sb.append('/')
                    'n'  -> sb.append('\n')
                    'r'  -> sb.append('\r')
                    't'  -> sb.append('\t')
                    'b'  -> sb.append('\b')
                    'f'  -> sb.append('\u000C')
                    'u'  -> {
                        val h = src.substring(pos + 1, minOf(pos + 5, src.length))
                        sb.append(h.toInt(16).toChar())
                        pos += 4
                    }
                    else -> sb.append(src[pos])
                }
            } else {
                sb.append(src[pos])
            }
            pos++
        }
        eat('"')
        return sb.toString()
    }

    private fun parseNum(): JNode.Num {
        val s = pos
        if (cur() == '-') pos++
        while (pos < src.length && src[pos] in "0123456789.eE+-") pos++
        return JNode.Num(src.substring(s, pos))
    }

    private fun cur() = if (pos < src.length) src[pos] else '\u0000'
    private fun eat(c: Char) { if (cur() == c) pos++ }
    private fun ws() { while (pos < src.length && src[pos].isWhitespace()) pos++ }
}