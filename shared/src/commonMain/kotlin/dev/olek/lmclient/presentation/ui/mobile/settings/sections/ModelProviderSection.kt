package dev.olek.lmclient.presentation.ui.mobile.settings.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.list.ListItem
import dev.olek.lmclient.presentation.ui.mobile.extensions.getIconRes
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_configuration
import lm_client.shared.generated.resources.settings_section_model_provider_empty_provider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ModelProviderSection(
    selectedProvider: ModelProvider?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {},
    ) {
        ListItem(
            title = selectedProvider?.name
                ?: stringResource(Res.string.settings_section_model_provider_empty_provider),
            icon = {
                Icon(
                    painter = painterResource(
                        selectedProvider?.getIconRes() ?: Res.drawable.ic_configuration,
                    ),
                    tint = AppTheme.colors.icon,
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
    }
}
