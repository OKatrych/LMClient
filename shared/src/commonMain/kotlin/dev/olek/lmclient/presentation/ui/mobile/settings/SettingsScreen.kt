@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.olek.lmclient.data.models.AppearanceMode
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.models.ModelProvider.ModelProviderConfig
import dev.olek.lmclient.presentation.components.settings.SettingsScreenComponent
import dev.olek.lmclient.presentation.components.settings.SettingsScreenComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreview
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.ui.mobile.common.list.ListSection
import dev.olek.lmclient.presentation.ui.mobile.common.topbar.CenteredTopBar
import dev.olek.lmclient.presentation.ui.mobile.settings.sections.AboutSection
import dev.olek.lmclient.presentation.ui.mobile.settings.sections.ModelProviderSection
import dev.olek.lmclient.presentation.ui.mobile.settings.sections.appearance.AppearanceSection
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.settings_section_about
import lm_client.shared.generated.resources.settings_section_appearance
import lm_client.shared.generated.resources.settings_section_model_provider
import lm_client.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    component: SettingsScreenComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    val listTopPaddingPx = LocalDensity.current.run { SettingsListPadding.toPx() }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        contentColor = Color.Unspecified,
        topBar = {
            CenteredTopBar(
                modifier = Modifier
                    .lazyListHazeEffect(
                        hazeState = hazeState,
                        hazeStyle = HazeMaterials.thin(AppTheme.colors.surface),
                        listState = listState,
                        listTopPaddingPx = listTopPaddingPx,
                    ),
                title = stringResource(Res.string.settings_title),
                onBackClick = component::navigateBack,
            )
        },
        content = {
            ScreenContent(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                paddingValues = it + PaddingValues(vertical = SettingsListPadding),
                state = state,
                listState = listState,
                onProviderClick = component::navigateToModelProviders,
                onLicensesClick = component::navigateToLicenses,
                onAppearanceModeClick = component::onAppearanceModeClicked,
            )
        },
    )
}

@Composable
private fun ScreenContent(
    paddingValues: PaddingValues,
    state: SettingsScreenComponent.State,
    listState: LazyListState,
    onProviderClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onAppearanceModeClick: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ListSection(
                title = stringResource(Res.string.settings_section_model_provider),
            ) {
                ModelProviderSection(
                    selectedProvider = state.selectedProvider,
                    onClick = onProviderClick,
                )
            }
        }
        item {
            ListSection(
                title = stringResource(Res.string.settings_section_appearance),
            ) {
                AppearanceSection(
                    selectedAppearanceMode = state.appearanceMode,
                    onModeChange = onAppearanceModeClick,
                )
            }
        }
        item {
            ListSection(
                title = stringResource(Res.string.settings_section_about),
            ) {
                AboutSection(
                    version = state.appVersion,
                    onLicensesClick = onLicensesClick,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingsScreenPreview() = ScreenPreviewWrapper {
    SettingsScreen(
        component = SettingsScreenComponentPreview(
            customState = SettingsScreenComponent.State(
                selectedProvider = null,
                appVersion = "1.0.0-preview",
            ),
        ),
    )
}

@ScreenPreview
@Composable
private fun SettingsScreenPreviewWithProvider() = ScreenPreviewWrapper {
    SettingsScreen(
        component = SettingsScreenComponentPreview(
            customState = SettingsScreenComponent.State(
                selectedProvider = ModelProvider(
                    id = "id1",
                    name = "Nexos.ai",
                    config = ModelProviderConfig.StandardConfig(
                        providerId = "id1",
                        apiKey = "",
                        apiUrl = "",
                    ),
                    isActive = true,
                ),
                appVersion = "1.0.0-preview",
            ),
        ),
    )
}

private val SettingsListPadding = 16.dp
