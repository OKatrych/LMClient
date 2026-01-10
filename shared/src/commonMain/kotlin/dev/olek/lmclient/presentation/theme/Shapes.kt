package dev.olek.lmclient.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
class AppShapes(
    val searchBar: Shape,
    val button: Shape,
    val popup: Shape,
    val queryInput: Shape,
    val card: Shape,
    val selectableItem: Shape,
)

internal fun defaultAppShapes() = AppShapes(
    searchBar = RoundedCornerShape(percent = 50),
    button = RoundedCornerShape(percent = 50),
    popup = RoundedCornerShape(24.dp),
    queryInput = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    card = RoundedCornerShape(12.dp),
    selectableItem = RoundedCornerShape(8.dp),
)

internal val LocalAppShapes = staticCompositionLocalOf<AppShapes> {
    error("AppTheme must be part of the call hierarchy to provide shapes")
}
