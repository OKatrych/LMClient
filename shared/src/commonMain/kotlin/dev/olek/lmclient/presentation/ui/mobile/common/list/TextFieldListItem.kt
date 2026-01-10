package dev.olek.lmclient.presentation.ui.mobile.common.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.PreviewWrapper
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.config_base_url
import lm_client.shared.generated.resources.ic_url
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun TextFieldListItem(
    value: String,
    onValueChange: (String) -> Unit,
    icon: DrawableResource,
    hint: String,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
    isError: Boolean = false,
    isEnabled: Boolean = true,
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        value = value,
        onValueChange = onValueChange,
        enabled = isEnabled,
        placeholder = {
            Text(
                text = hint,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
            )
        },
        singleLine = true,
        isError = isError,
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
        },
        visualTransformation = if (isSecret) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = false,
            keyboardType = if (isSecret) KeyboardType.Password else KeyboardType.Text,
        ),
        textStyle = AppTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.text,
            unfocusedTextColor = AppTheme.colors.text,
            errorTextColor = AppTheme.colors.error,
            disabledTextColor = AppTheme.colors.text,
            focusedLeadingIconColor = AppTheme.colors.icon,
            unfocusedLeadingIconColor = AppTheme.colors.icon,
            errorLeadingIconColor = AppTheme.colors.error,
            disabledLeadingIconColor = AppTheme.colors.icon,
            focusedPlaceholderColor = AppTheme.colors.textSecondary,
            unfocusedPlaceholderColor = AppTheme.colors.textSecondary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = AppTheme.colors.text,
            errorCursorColor = AppTheme.colors.error,
        ),
    )
}

@Preview
@Composable
private fun TextConfigItemPreview() = PreviewWrapper {
    TextFieldListItem(
        hint = stringResource(Res.string.config_base_url),
        value = "https://api.openai.com",
        icon = Res.drawable.ic_url,
        onValueChange = {},
    )
}

@Preview
@Composable
private fun TextConfigItemPreviewEmpty() = PreviewWrapper {
    TextFieldListItem(
        hint = stringResource(Res.string.config_base_url),
        value = "",
        icon = Res.drawable.ic_url,
        onValueChange = {},
    )
}

@Preview
@Composable
private fun TextConfigItemPreviewDisabled() = PreviewWrapper {
    TextFieldListItem(
        hint = stringResource(Res.string.config_base_url),
        value = "https://api.openai.com",
        isEnabled = false,
        icon = Res.drawable.ic_url,
        onValueChange = {},
    )
}

@Preview
@Composable
private fun TextConfigItemPreviewError() = PreviewWrapper {
    TextFieldListItem(
        hint = stringResource(Res.string.config_base_url),
        value = "api.opendi.error",
        isError = true,
        icon = Res.drawable.ic_url,
        onValueChange = {},
    )
}
