package dev.olek.lmclient.presentation.ui.mobile.common.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme

@Composable
internal fun ListItem(
    title: String,
    icon: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .heightIn(48.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .sizeIn(48.dp, 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.body,
                color = AppTheme.colors.text,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = AppTheme.colors.textSecondary,
                    style = AppTheme.typography.caption,
                )
            }
        }
    }
}
