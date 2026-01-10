package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * Applies a haze effect that reacts to the scroll state of a lazy list.
 *
 * For normal layout: The haze effect will be applied only when the list has been
 * scrolled past the inner padding of the first item. When the list is at the top,
 * the haze effect's alpha is set to 0f (fully transparent).
 *
 * For reverse layout: The haze effect will be applied when the list has been
 * scrolled up from the bottom, hiding the last item or scrolling past its inner padding.
 *
 * @param hazeState the state object controlling the haze effect.
 * @param hazeStyle the style or configuration of the haze blur effect.
 * @param listState the scroll state of the associated [LazyListState].
 * @param listTopPaddingPx the top padding (in pixels) that should be considered when determining
 *                         whether the list has been scrolled past the first item.
 */
internal fun Modifier.lazyListHazeEffect(
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    listState: LazyListState,
    listTopPaddingPx: Float,
) = hazeEffect(
    state = hazeState,
    style = hazeStyle,
) {
    // When list is not scrolled - do not apply blur to top bar
    val shouldBlur by derivedStateOf {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.totalItemsCount == 0) {
            return@derivedStateOf false
        }
        if (layoutInfo.reverseLayout) {
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                false
            } else {
                val lastItemIndex = layoutInfo.totalItemsCount - 1
                val topMostVisibleItem = visibleItems.last()

                if (topMostVisibleItem.index < lastItemIndex) {
                    true
                } else {
                    val itemTopEdge = topMostVisibleItem.offset + topMostVisibleItem.size
                    val scrollPastTop =
                        itemTopEdge - (layoutInfo.viewportEndOffset - listTopPaddingPx)
                    scrollPastTop > 0
                }
            }
        } else {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > listTopPaddingPx
        }
    }
    alpha = (if (shouldBlur) 1f else 0f)
}
