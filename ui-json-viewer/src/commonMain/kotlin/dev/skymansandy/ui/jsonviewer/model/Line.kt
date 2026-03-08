package dev.skymansandy.ui.jsonviewer.model

import androidx.compose.ui.text.AnnotatedString

internal data class Line(
    val indent: Int,
    val text: AnnotatedString,
    val foldId: Int = -1,
    val folded: Boolean = false,
)
