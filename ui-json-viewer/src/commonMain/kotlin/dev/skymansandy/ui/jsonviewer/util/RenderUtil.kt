package dev.skymansandy.ui.jsonviewer.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import dev.skymansandy.ui.jsonviewer.constants.cBracket
import dev.skymansandy.ui.jsonviewer.constants.cKey
import dev.skymansandy.ui.jsonviewer.constants.cKeyword
import dev.skymansandy.ui.jsonviewer.constants.cNumber
import dev.skymansandy.ui.jsonviewer.constants.cPunct
import dev.skymansandy.ui.jsonviewer.constants.cString
import dev.skymansandy.ui.jsonviewer.model.JNode
import dev.skymansandy.ui.jsonviewer.model.Line

internal fun sp(color: Color) =
    SpanStyle(color = color, fontFamily = FontFamily.Monospace, fontSize = 13.sp)

internal fun buildLines(root: JNode, folded: Set<Int>): List<Line> {
    val out = mutableListOf<Line>()
    emit(root, 0, out, folded, prefix = null, suffix = null)
    return out
}

private fun String.escaped() = replace("\\", "\\\\").replace("\"", "\\\"")
    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

private fun emit(
    node: JNode,
    indent: Int,
    out: MutableList<Line>,
    folded: Set<Int>,
    prefix: AnnotatedString?,
    suffix: String?
) {
    val pre = prefix ?: AnnotatedString("")
    fun sfx() = if (suffix != null)
        buildAnnotatedString { pushStyle(sp(cPunct)); append(suffix); pop() }
    else AnnotatedString("")

    when (node) {
        is JNode.Obj -> {
            val isFolded = node.id in folded
            out += Line(
                indent = indent,
                text = buildAnnotatedString {
                    append(pre)
                    pushStyle(sp(cBracket)); append("{"); pop()
                    if (isFolded) {
                        pushStyle(sp(cPunct)); append("...}"); pop(); append(sfx())
                    }
                },
                foldId = node.id,
                folded = isFolded
            )
            if (!isFolded) {
                node.entries.forEachIndexed { i, (k, v) ->
                    val keyPre = buildAnnotatedString {
                        pushStyle(sp(cKey)); append("\"$k\""); pop()
                        pushStyle(sp(cPunct)); append(": "); pop()
                    }
                    emit(
                        v,
                        indent + 1,
                        out,
                        folded,
                        keyPre,
                        if (i < node.entries.lastIndex) "," else null
                    )
                }
                out += Line(indent, buildAnnotatedString {
                    pushStyle(sp(cBracket)); append("}"); pop(); append(sfx())
                })
            }
        }

        is JNode.Arr -> {
            val isFolded = node.id in folded
            out += Line(
                indent = indent,
                text = buildAnnotatedString {
                    append(pre)
                    pushStyle(sp(cBracket)); append("["); pop()
                    if (isFolded) {
                        pushStyle(sp(cPunct)); append("...]"); pop(); append(sfx())
                    }
                },
                foldId = node.id,
                folded = isFolded
            )
            if (!isFolded) {
                node.items.forEachIndexed { i, v ->
                    emit(
                        v,
                        indent + 1,
                        out,
                        folded,
                        null,
                        if (i < node.items.lastIndex) "," else null
                    )
                }
                out += Line(indent, buildAnnotatedString {
                    pushStyle(sp(cBracket)); append("]"); pop(); append(sfx())
                })
            }
        }

        is JNode.Str -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cString)); append("\"${node.value.escaped()}\""); pop(); append(
            sfx()
        )
        })

        is JNode.Num -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cNumber)); append(node.value); pop(); append(sfx())
        })

        is JNode.Bool -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cKeyword)); append(node.value.toString()); pop(); append(sfx())
        })

        JNode.Null -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cKeyword)); append("null"); pop(); append(sfx())
        })
    }
}
