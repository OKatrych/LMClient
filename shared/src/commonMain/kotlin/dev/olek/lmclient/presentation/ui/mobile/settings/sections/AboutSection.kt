package dev.olek.lmclient.presentation.ui.mobile.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.list.ListItem
import dev.olek.lmclient.presentation.util.clipEntryOf
import kotlinx.coroutines.launch
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_github
import lm_client.shared.generated.resources.ic_license
import lm_client.shared.generated.resources.ic_versions
import lm_client.shared.generated.resources.settings_section_about_github
import lm_client.shared.generated.resources.settings_section_about_licenses
import lm_client.shared.generated.resources.settings_section_about_version
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutSection(
    version: String,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val copyVersion = remember(coroutineScope, clipboard, version) {
        {
            coroutineScope.launch { clipboard.setClipEntry(clipEntryOf(version)) }
        }
    }

    Column(modifier = modifier) {
        ListItem(
            title = stringResource(Res.string.settings_section_about_version),
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_versions),
                    tint = AppTheme.colors.icon,
                    contentDescription = null,
                )
            },
            subtitle = version,
            onClick = { copyVersion() },
        )
        ListItem(
            title = stringResource(Res.string.settings_section_about_licenses),
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_license),
                    tint = AppTheme.colors.icon,
                    contentDescription = null,
                )
            },
            onClick = { onLicensesClick() },
        )
        ListItem(
            title = stringResource(Res.string.settings_section_about_github),
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_github),
                    tint = AppTheme.colors.icon,
                    contentDescription = null,
                )
            },
            onClick = {
                // TODO("Implement browser navigation")
            },
        )
    }
}
