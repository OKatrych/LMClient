@file:Suppress("MatchingDeclarationName")

package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview annotation for full-screen mobile layouts.
 * Displays both light and dark themes side by side at realistic phone dimensions.
 */
@Preview(
    heightDp = PhoneScreenHeightDp,
    widthDp = PhoneScreenWidthDp * 2 + SpacingBetweenPreviewsDp,
)
annotation class ScreenPreview

/**
 * Preview wrapper for full-screen mobile layouts.
 * Renders content side by side in both light and dark themes at phone screen dimensions.
 * Use with [ScreenPreview] annotation for optimal preview sizing.
 * @param background Optional background color override
 * @param content The composable content to preview
 */
@Composable
fun ScreenPreviewWrapper(modifier: Modifier = Modifier, background: Color? = null, content: @Composable () -> Unit) {
    Row(modifier = modifier) {
        AppTheme(darkTheme = false) {
            PreviewContent(
                content = content,
                modifier = Modifier
                    .width(PhoneScreenWidthDp.dp)
                    .background(background ?: AppTheme.colors.background),
            )
        }
        Spacer(modifier = Modifier.width(SpacingBetweenPreviewsDp.dp))
        AppTheme(darkTheme = true) {
            PreviewContent(
                content = content,
                modifier = Modifier
                    .width(PhoneScreenWidthDp.dp)
                    .background(background ?: AppTheme.colors.background),
            )
        }
    }
}

/**
 * Preview wrapper for UI components.
 * Renders content in both light and dark themes stacked vertically.
 * Ideal for previewing individual components or small UI sections.
 *
 * @param content The composable content to preview
 */
@Composable
fun PreviewWrapper(
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        AppTheme(darkTheme = false) {
            PreviewContent(
                modifier = if (showBackground) {
                    Modifier.background(AppTheme.colors.surface)
                } else {
                    Modifier
                },
                content = content,
            )
        }
        Spacer(modifier = Modifier.height(SpacingBetweenPreviewsDp.dp))
        AppTheme(darkTheme = true) {
            PreviewContent(
                modifier = if (showBackground) {
                    Modifier.background(AppTheme.colors.surface)
                } else {
                    Modifier
                },
                content = content,
            )
        }
    }
}

@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }
}

private const val PhoneScreenWidthDp = 412
private const val PhoneScreenHeightDp = 892
private const val SpacingBetweenPreviewsDp = 8
