@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.common.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.button_back
import lm_client.shared.generated.resources.ic_arrow_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CenteredTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredTopBar(
        modifier = modifier,
        centerContent = {
            Text(
                text = title,
                style = AppTheme.typography.titleBold,
                color = AppTheme.colors.text,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left),
                    tint = AppTheme.colors.icon,
                    contentDescription = stringResource(Res.string.button_back),
                )
            }
        },
    )
}

@Composable
internal fun CenteredTopBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    navigationIcon: @Composable () -> Unit = {},
    centerContent: @Composable () -> Unit = {},
    actionIcon: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .windowInsetsPadding(windowInsets)
            .heightIn(min = 56.dp)
            .clipToBounds(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            navigationIcon()
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            centerContent()
        }
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            actionIcon()
        }
    }
}
