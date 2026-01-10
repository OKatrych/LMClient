package dev.olek.lmclient.presentation.ui.mobile.common.dropdown

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.olek.lmclient.presentation.theme.AppTheme

/**
 * Copy of the [androidx.compose.material3.DropdownMenu] but with items in [LazyColumn] and
 * removed logic for dynamic popup resize (not needed in this app).
 */
@Composable
internal fun LazyDropdownMenu(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset.Zero,
    alignment: Alignment = Alignment.TopStart,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = AppTheme.shapes.popup,
    containerColor: Color = AppTheme.colors.backgroundSecondary,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: LazyListScope.() -> Unit,
) {
    val expandedState = remember { MutableTransitionState(false) }
    var dismissRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Expand popup automatically
        expandedState.targetState = true
    }

    LaunchedEffect(expandedState.currentState, dismissRequested, onDismissRequest) {
        // Let dismiss animation finish before calling [onDismissRequest] callback
        if (dismissRequested && !expandedState.currentState) {
            onDismissRequest()
        }
    }

    if (expandedState.currentState || expandedState.targetState) {
        Popup(
            onDismissRequest = {
                dismissRequested = true
                expandedState.targetState = false
            },
            offset = offset,
            alignment = alignment,
            properties = properties,
        ) {
            DropdownMenuContent(
                expandedState = expandedState,
                lazyListState = lazyListState,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                shape = shape,
                containerColor = containerColor,
                shadowElevation = shadowElevation,
                border = border,
                modifier = modifier,
                content = content,
            )
        }
    }
}

@Composable
internal fun DropdownMenuContent(
    expandedState: MutableTransitionState<Boolean>,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    shape: Shape,
    containerColor: Color,
    shadowElevation: Dp,
    border: BorderStroke?,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    // Menu open/close animation.
    val transition = rememberTransition(expandedState, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = InTransitionDuration, easing = LinearOutSlowInEasing)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        },
    ) { expanded ->
        if (expanded) ExpandedScaleTarget else ClosedScaleTarget
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        },
    ) { expanded ->
        if (expanded) ExpandedAlphaTarget else ClosedAlphaTarget
    }

    val isInspecting = LocalInspectionMode.current
    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX =
                if (!isInspecting) {
                    scale
                } else if (expandedState.targetState) {
                    ExpandedScaleTarget
                } else {
                    ClosedScaleTarget
                }
            scaleY =
                if (!isInspecting) {
                    scale
                } else if (expandedState.targetState) {
                    ExpandedScaleTarget
                } else {
                    ClosedScaleTarget
                }
            this.alpha =
                if (!isInspecting) {
                    alpha
                } else if (expandedState.targetState) {
                    ExpandedAlphaTarget
                } else {
                    ClosedAlphaTarget
                }
        },
        shape = shape,
        color = containerColor,
        shadowElevation = shadowElevation,
        border = border,
    ) {
        @Suppress("ModifierNotUsedAtRoot")
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            state = lazyListState,
            content = content,
        )
    }
}

// Menu open/close animation.
internal const val InTransitionDuration = 120
internal const val OutTransitionDuration = 120
internal const val ExpandedScaleTarget = 1f
internal const val ClosedScaleTarget = 0.8f
internal const val ExpandedAlphaTarget = 1f
internal const val ClosedAlphaTarget = 0f
