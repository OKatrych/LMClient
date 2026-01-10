package dev.olek.lmclient.presentation.ui.mobile.common.popup

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheetScope
import com.composables.core.DragIndication
import com.composables.core.ModalBottomSheet
import com.composables.core.ModalBottomSheetScope
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.SheetDetent.Companion.Hidden
import com.composables.core.rememberModalBottomSheetState
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.AppButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun AppPopup(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    dismissButtonText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentDismissCallback by rememberUpdatedState(onDismissRequest)
    val sheetState = rememberModalBottomSheetState(initialDetent = Hidden)

    LaunchedEffect(sheetState) {
        sheetState.targetDetent = FullyExpanded
    }

    val dismiss = remember(sheetState) {
        {
            sheetState.scope.launch {
                sheetState.animateTo(Hidden)
            }.invokeOnCompletion {
                currentDismissCallback()
            }
        }
    }
    val listenForDismiss: () -> Unit = remember(sheetState) {
        {
            sheetState.scope.launch {
                val animationComplete = 1f
                while (
                    sheetState.progress(from = FullyExpanded, to = Hidden) != animationComplete
                ) {
                    delay(AppPopupDefaults.DISMISS_POLL_INTERVAL_MS)
                }
            }.invokeOnCompletion {
                currentDismissCallback()
            }
        }
    }

    ModalBottomSheet(
        state = sheetState,
        onDismiss = listenForDismiss,
    ) {
        Scrim(
            enter = fadeIn(tween(durationMillis = AppPopupDefaults.SCRIM_ANIMATION_DURATION_MS)),
            exit = fadeOut(tween(durationMillis = AppPopupDefaults.SCRIM_ANIMATION_DURATION_MS)),
        )
        PopupSheet(
            modifier = modifier,
            title = title,
            icon = icon,
            dismissButtonText = dismissButtonText,
            onDismiss = { dismiss() },
            content = content,
        )
    }
}

@Composable
private fun ModalBottomSheetScope.PopupSheet(
    title: String,
    icon: (@Composable () -> Unit)?,
    dismissButtonText: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Sheet(
        modifier = modifier
            .widthIn(max = AppPopupDefaults.SheetMaxWidth)
            .fillMaxWidth()
            .padding(
                start = AppPopupDefaults.SheetHorizontalPadding,
                end = AppPopupDefaults.SheetHorizontalPadding,
                bottom = AppPopupDefaults.SheetBottomPadding,
            )
            .navigationBarsPadding()
            .imePadding(),
        backgroundColor = AppTheme.colors.backgroundSecondary,
        shape = AppTheme.shapes.popup,
    ) {
        BoxWithConstraints {
            val contentMaxHeight = remember(maxHeight) {
                maxHeight * AppPopupDefaults.CONTENT_MAX_HEIGHT_FRACTION
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PopupDragHandle()
                PopupIcon(icon)
                PopupTitle(title = title, hasIcon = icon != null)
                PopupBodyContent(maxHeight = contentMaxHeight, content = content)
                PopupFooter(dismissButtonText = dismissButtonText, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun BottomSheetScope.PopupDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .size(height = 5.dp, width = 54.dp)
            .background(
                color = AppTheme.colors.dragHandle,
                shape = AppTheme.shapes.popup,
            ),
    ) {
        DragIndication(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun PopupIcon(icon: (@Composable () -> Unit)?) {
    if (icon == null) return

    Box(
        modifier = Modifier
            .padding(top = AppPopupDefaults.IconTopPadding)
            .sizeIn(
                minWidth = AppPopupDefaults.IconMinSize,
                minHeight = AppPopupDefaults.IconMinSize,
            ),
        content = { icon() },
    )
}

@Composable
private fun PopupTitle(
    title: String,
    hasIcon: Boolean,
) {
    val topPadding = if (hasIcon) {
        AppPopupDefaults.TitleTopPaddingWithIcon
    } else {
        AppPopupDefaults.TitleTopPaddingWithoutIcon
    }

    Text(
        modifier = Modifier.padding(
            top = topPadding,
            start = AppPopupDefaults.TitleHorizontalPadding,
            end = AppPopupDefaults.TitleHorizontalPadding,
        ),
        text = title,
        style = AppTheme.typography.titleBold,
        color = AppTheme.colors.text,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ColumnScope.PopupBodyContent(
    maxHeight: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(weight = 1f, fill = false)
            .heightIn(max = maxHeight),
        content = content,
    )
}

@Composable
private fun PopupFooter(
    dismissButtonText: String?,
    onDismiss: () -> Unit,
) {
    if (dismissButtonText != null) {
        AppButton(
            modifier = Modifier.padding(
                vertical = AppPopupDefaults.ButtonVerticalPadding,
                horizontal = AppPopupDefaults.ButtonHorizontalPadding,
            ),
            text = dismissButtonText,
            onClick = onDismiss,
        )
    } else {
        Spacer(modifier = Modifier.height(AppPopupDefaults.BottomSpacerHeight))
    }
}

internal object AppPopupDefaults {
    // Animation
    const val SCRIM_ANIMATION_DURATION_MS = 200
    const val DISMISS_POLL_INTERVAL_MS = 100L

    // Layout constraints
    val SheetMaxWidth: Dp = 640.dp
    val SheetHorizontalPadding: Dp = 16.dp
    val SheetBottomPadding: Dp = 16.dp
    const val CONTENT_MAX_HEIGHT_FRACTION = 0.6f

    // Icon
    val IconTopPadding: Dp = 24.dp
    val IconMinSize: Dp = 36.dp

    // Title
    val TitleTopPaddingWithIcon: Dp = 8.dp
    val TitleTopPaddingWithoutIcon: Dp = 16.dp
    val TitleHorizontalPadding: Dp = 32.dp

    // Button
    val ButtonVerticalPadding: Dp = 24.dp
    val ButtonHorizontalPadding: Dp = 16.dp
    val BottomSpacerHeight: Dp = 16.dp
}
