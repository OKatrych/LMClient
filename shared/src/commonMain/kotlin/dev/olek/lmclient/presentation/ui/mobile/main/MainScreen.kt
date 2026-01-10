package dev.olek.lmclient.presentation.ui.mobile.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.components.main.MainScreenComponent
import dev.olek.lmclient.presentation.ui.mobile.common.drawer.rememberNavigationDrawerState
import dev.olek.lmclient.presentation.ui.mobile.main.chat.ChatScreen
import dev.olek.lmclient.presentation.ui.mobile.main.history.ChatHistoryScreen

@Composable
internal fun MainScreen(component: MainScreenComponent) {
    val drawerState = rememberNavigationDrawerState(
        drawer = component.historyDrawer,
        onStateChange = component::setDrawerState,
    )

    BoxWithConstraints {
        val drawerWidth = MaxDrawerWidth.coerceAtMost(maxWidth)
        val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }

        DismissibleNavigationDrawer(
            drawerContent = {
                ChatHistoryScreen(
                    modifier = Modifier.width(drawerWidth),
                    component = drawerState.instance,
                )
            },
            modifier = Modifier.fillMaxSize(),
            drawerState = drawerState.drawerState,
        ) {
            Box {
                ChatScreen(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = drawerWidthPx + drawerState.drawerState.currentOffset
                            val progress = (offset / drawerWidthPx).coerceIn(0f, 1f)
                            val blurRadius = (progress * MaxBlurRadius).coerceIn(0f, MaxBlurRadius)
                            if (blurRadius > 0f) {
                                renderEffect = BlurEffect(
                                    radiusX = blurRadius,
                                    radiusY = blurRadius,
                                    edgeTreatment = TileMode.Clamp,
                                )
                            }
                        },
                    component = component.chatScreenComponent,
                )

                // Block interactions when drawer is open
                if (!drawerState.drawerState.isClosed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                    }
                                }
                            }
                    )
                }
            }
        }
    }

    // Close keyboard when state drawer is changed
    val focusManager = LocalFocusManager.current
    LaunchedEffect(drawerState.drawerState.isClosed) {
        focusManager.clearFocus()
    }
}

private val MaxDrawerWidth = 300.dp
private const val MaxBlurRadius = 15f
