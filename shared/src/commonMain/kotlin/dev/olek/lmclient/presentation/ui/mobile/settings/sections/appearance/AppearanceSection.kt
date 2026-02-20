@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.settings.sections.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.olek.lmclient.data.models.AppearanceMode
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.appearance_mode_dark
import lm_client.shared.generated.resources.appearance_mode_light
import lm_client.shared.generated.resources.appearance_mode_system
import lm_client.shared.generated.resources.ic_appearance_dark
import lm_client.shared.generated.resources.ic_appearance_light
import lm_client.shared.generated.resources.ic_appearance_system
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppearanceSection(
    selectedAppearanceMode: AppearanceMode,
    onModeChange: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AppearanceMode.entries.fastForEach { mode ->
            AppearanceButton(
                modifier = Modifier.weight(1f),
                isSelected = selectedAppearanceMode == mode,
                mode = mode,
                onClick = onModeChange,
            )
        }
    }
}

@Composable
private fun AppearanceButton(
    isSelected: Boolean,
    mode: AppearanceMode,
    onClick: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val title = stringResource(mode.toDisplayStringRes())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(74.dp)
                .clip(AppTheme.shapes.selectableItem)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.stroke,
                    shape = AppTheme.shapes.selectableItem,
                ).then(
                    if (isSelected) {
                        Modifier.background(
                            color = AppTheme.colors.stroke,
                            shape = AppTheme.shapes.selectableItem,
                        )
                    } else {
                        Modifier
                    },
                ).selectable(
                    selected = isSelected,
                    onClick = { onClick(mode) },
                ).semantics {
                    contentDescription = title
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(mode.toIconRes()),
                tint = AppTheme.colors.icon,
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.semantics { hideFromAccessibility() },
            text = title,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.text,
        )
    }
}

@Composable
private fun AppearanceMode.toDisplayStringRes(): StringResource = when (this) {
    AppearanceMode.SYSTEM -> Res.string.appearance_mode_system
    AppearanceMode.LIGHT -> Res.string.appearance_mode_light
    AppearanceMode.DARK -> Res.string.appearance_mode_dark
}

@Composable
private fun AppearanceMode.toIconRes(): DrawableResource = when (this) {
    AppearanceMode.SYSTEM -> Res.drawable.ic_appearance_system
    AppearanceMode.LIGHT -> Res.drawable.ic_appearance_light
    AppearanceMode.DARK -> Res.drawable.ic_appearance_dark
}
