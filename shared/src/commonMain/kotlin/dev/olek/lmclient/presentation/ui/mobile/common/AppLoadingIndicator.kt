package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.icon,
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        strokeWidth = 2.5.dp,
        trackColor = Color.Unspecified,
        color = color,
    )
}

@Preview
@Composable
private fun AppLoadingIndicatorPreview() = PreviewWrapper {
    AppLoadingIndicator()
}
