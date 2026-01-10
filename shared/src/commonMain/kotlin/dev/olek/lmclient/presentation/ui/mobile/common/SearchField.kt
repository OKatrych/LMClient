package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Icon
import com.composeunstyled.Text
import com.composeunstyled.TextField
import com.composeunstyled.TextInput
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_search
import lm_client.shared.generated.resources.search_bar_hint
import lm_client.shared.generated.resources.search_icon_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchField(
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = stringResource(Res.string.search_bar_hint),
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        val state = rememberTextFieldState()

        DisposableEffect(state.text) {
            currentOnValueChange(state.text.toString())
            onDispose {}
        }

        TextField(
            state = state,
            textStyle = AppTheme.typography.bodyMedium,
            singleLine = true,
            textColor = AppTheme.colors.text,
            cursorBrush = SolidColor(AppTheme.colors.primary),
        ) {
            TextInput(
                shape = AppTheme.shapes.searchBar,
                backgroundColor = AppTheme.colors.backgroundSecondary,
                contentPadding = PaddingValues(12.dp),
                leading = {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp),
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = stringResource(Res.string.search_icon_desc),
                        tint = AppTheme.colors.textSecondary,
                    )
                },
                placeholder = {
                    Text(
                        text = hint,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun SearchFieldPreview() = PreviewWrapper {
    SearchField(
        onValueChange = {},
    )
}
