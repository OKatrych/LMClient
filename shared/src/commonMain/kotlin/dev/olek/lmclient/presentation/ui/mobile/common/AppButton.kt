package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            modifier = Modifier
                .heightIn(54.dp)
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier),
            onClick = onClick,
            enabled = isEnabled && !isLoading,
            shape = AppTheme.shapes.button,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                contentColor = AppTheme.colors.onPrimary,
                disabledContainerColor = if (isLoading) {
                    AppTheme.colors.primary
                } else {
                    AppTheme.colors.surface
                        .copy(alpha = 0.6f)
                        .compositeOver(AppTheme.colors.primary)
                },
                disabledContentColor = if (isLoading) {
                    AppTheme.colors.onPrimary
                } else {
                    AppTheme.colors.surface
                        .copy(alpha = 0.7f)
                        .compositeOver(AppTheme.colors.onPrimary)
                },
            ),
        ) {
            if (isLoading) {
                AppLoadingIndicator(color = AppTheme.colors.onPrimary)
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = text,
                    style = AppTheme.typography.titleBold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppButtonPreview() = PreviewWrapper {
    AppButton(
        text = "Save",
        onClick = {},
    )
}

@Preview
@Composable
private fun AppButtonFullWidthPreview() = PreviewWrapper {
    AppButton(
        text = "Save",
        fillMaxWidth = true,
        onClick = {},
    )
}

@Preview
@Composable
private fun AppButtonDisabledPreview() = PreviewWrapper {
    AppButton(
        text = "Save",
        isEnabled = false,
        onClick = {},
    )
}

@Preview
@Composable
private fun AppButtonLoadingPreview() = PreviewWrapper {
    AppButton(
        text = "Save",
        isLoading = true,
        onClick = {},
    )
}
