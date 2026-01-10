@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.common.popup

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.extensions.toLocalizedMessage
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_error
import lm_client.shared.generated.resources.popup_cta
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ErrorPopup(
    title: String,
    error: LMClientError,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppPopup(
        modifier = modifier,
        title = title,
        onDismissRequest = onDismissRequest,
        dismissButtonText = stringResource(Res.string.popup_cta),
        icon = {
            Icon(
                painter = painterResource(Res.drawable.ic_error),
                tint = AppTheme.colors.error,
                contentDescription = null,
            )
        },
    ) {
        Text(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .padding(top = 24.dp, start = 32.dp, end = 32.dp),
            text = error.toLocalizedMessage(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun ErrorPopupPreviewLight() {
    AppTheme(darkTheme = false) {
        ErrorPopup(
            title = "Connection failed",
            error = LMClientError.Authentication,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ErrorPopupPreviewDark() {
    AppTheme(darkTheme = true) {
        ErrorPopup(
            title = "Connection failed",
            error = LMClientError.ConnectionIssue,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ErrorPopupPreviewLongText() {
    AppTheme(darkTheme = false) {
        ErrorPopup(
            title = "Connection failed",
            error = LMClientError.UnknownError(
                message = "java.io.IOException: unexpected" +
                        " end of stream onConnection{comenius-" +
                        "api.sabacloud.com:443, proxy=DIRECT" +
                        " hostAddress=12.130.57.1 cipherSuite=" +
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM" +
                        "_SHA384 protocol=http/1.1} " +
                        "(recycle count=0)" +
                        "java.io.IOException: unexpected" +
                        " end of stream onConnection{comenius-" +
                        "api.sabacloud.com:443, proxy=DIRECT" +
                        " hostAddress=12.130.57.1 cipherSuite=" +
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM" +
                        "_SHA384 protocol=http/1.1} " +
                        "(recycle count=0)" +
                        "java.io.IOException: unexpected" +
                        " end of stream onConnection{comenius-" +
                        "api.sabacloud.com:443, proxy=DIRECT" +
                        " hostAddress=12.130.57.1 cipherSuite=" +
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM" +
                        "_SHA384 protocol=http/1.1} " +
                        "(recycle count=0)" +
                        "java.io.IOException: unexpected" +
                        " end of stream onConnection{comenius-" +
                        "api.sabacloud.com:443, proxy=DIRECT" +
                        " hostAddress=12.130.57.1 cipherSuite=" +
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM" +
                        "_SHA384 protocol=http/1.1} " +
                        "(recycle count=0)",
            ),
            onDismissRequest = {},
        )
    }
}
