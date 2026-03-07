package dev.skymansandy.ui.jsonviewer.model

internal sealed interface JNode {
    data class Obj(val id: Int, val entries: List<Pair<String, JNode>>) : JNode
    data class Arr(val id: Int, val items: List<JNode>) : JNode
    data class Str(val value: String) : JNode
    data class Num(val value: String) : JNode
    data class Bool(val value: Boolean) : JNode
    data object Null : JNode
}
