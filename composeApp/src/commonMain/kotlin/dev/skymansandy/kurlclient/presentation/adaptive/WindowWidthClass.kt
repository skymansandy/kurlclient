package dev.skymansandy.kurlclient.presentation.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass { Compact, Medium, Expanded }

fun Dp.toWindowWidthClass(): WindowWidthClass = when {
    this < 600.dp -> WindowWidthClass.Compact
    this < 840.dp -> WindowWidthClass.Medium
    else -> WindowWidthClass.Expanded
}