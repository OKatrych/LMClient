@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.settings.providerconfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.ModelProvider.ModelProviderConfig
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent.Event
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent.State
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.AppButton
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreview
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.ui.mobile.common.list.ListSection
import dev.olek.lmclient.presentation.ui.mobile.common.list.TextFieldListItem
import dev.olek.lmclient.presentation.ui.mobile.common.popup.ErrorPopup
import dev.olek.lmclient.presentation.ui.mobile.common.popup.SuccessPopup
import dev.olek.lmclient.presentation.ui.mobile.common.topbar.CenteredTopBar
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.config_api_key
import lm_client.shared.generated.resources.config_api_key_hint
import lm_client.shared.generated.resources.config_base_url
import lm_client.shared.generated.resources.config_base_url_hint
import lm_client.shared.generated.resources.error_popup_title_connection
import lm_client.shared.generated.resources.ic_key
import lm_client.shared.generated.resources.ic_url
import lm_client.shared.generated.resources.model_provider_button_activate
import lm_client.shared.generated.resources.model_provider_button_save
import lm_client.shared.generated.resources.success_popup_message_connection
import lm_client.shared.generated.resources.success_popup_title_connection
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ModelProviderConfigurationScreen(
    component: ModelProviderConfigurationComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()
    var errorEvent by remember { mutableStateOf<LMClientError?>(null) }
    var successEvent by remember { mutableStateOf<Unit?>(null) }
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    val listTopPaddingPx = LocalDensity.current.run { ProviderConfigListPadding.toPx() }

    LaunchedEffect(component) {
        component.event.collect { event ->
            when (event) {
                is Event.ConfigSaveSuccess -> successEvent = Unit
                is Event.ConfigSaveError -> errorEvent = event.error
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        contentColor = Color.Unspecified,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenteredTopBar(
                modifier = Modifier
                    .lazyListHazeEffect(
                        hazeState = hazeState,
                        hazeStyle = HazeMaterials.thin(AppTheme.colors.surface),
                        listState = listState,
                        listTopPaddingPx = listTopPaddingPx,
                    ),
                title = state.providerName,
                onBackClick = component::navigateBack,
            )
        },
        content = {
            ProviderConfigScreenContent(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                paddingValues = it + PaddingValues(top = ProviderConfigListPadding),
                state = state,
                listState = listState,
                onSaveClick = component::saveAndActivate,
                onConfigChange = component::onConfigChange,
            )
        },
    )

    errorEvent?.let { error ->
        ErrorPopup(
            title = stringResource(Res.string.error_popup_title_connection),
            error = error,
            onDismissRequest = { errorEvent = null },
        )
    }
    successEvent?.let {
        SuccessPopup(
            title = stringResource(Res.string.success_popup_title_connection),
            message = stringResource(Res.string.success_popup_message_connection),
            onDismissRequest = { successEvent = null },
        )
    }
}

@Composable
private fun ProviderConfigScreenContent(
    onConfigChange: (ModelProviderConfig) -> Unit,
    onSaveClick: () -> Unit,
    paddingValues: PaddingValues,
    listState: LazyListState,
    state: State,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(bottom = paddingValues.calculateBottomPadding()),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state.config) {
                is ModelProviderConfig.StandardConfig -> {
                    standardConfigItems(
                        config = state.config,
                        onConfigChange = onConfigChange,
                        isConfigSaveInProgress = state.isConfigSaveInProgress,
                    )
                }
                is ModelProviderConfig.LocalConfig -> {
                    localConfigItems(
                        config = state.config,
                        onConfigChange = onConfigChange,
                        isConfigSaveInProgress = state.isConfigSaveInProgress,
                    )
                }

                else -> Unit
            }
        }

        AnimatedVisibility(
            visible = !state.isProviderActive || state.isConfigModified,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            AppButton(
                modifier = Modifier
                    .padding(16.dp),
                onClick = onSaveClick,
                isLoading = state.isConfigSaveInProgress,
                text = when {
                    state.isConfigModified -> {
                        stringResource(Res.string.model_provider_button_save)
                    }
                    !state.isProviderActive -> {
                        stringResource(Res.string.model_provider_button_activate)
                    }
                    else -> ""
                },
            )
        }
    }
}

private fun LazyListScope.standardConfigItems(
    config: ModelProviderConfig.StandardConfig,
    onConfigChange: (ModelProviderConfig) -> Unit,
    isConfigSaveInProgress: Boolean,
) {
    item(key = "base_url") {
        ListSection(title = stringResource(Res.string.config_base_url)) {
            TextFieldListItem(
                hint = stringResource(Res.string.config_base_url_hint),
                value = config.apiUrl,
                icon = Res.drawable.ic_url,
                isError = config.apiUrl.isBlank(),
                isEnabled = !isConfigSaveInProgress,
                onValueChange = { onConfigChange(config.copy(apiUrl = it)) },
            )
        }
    }
    item(key = "api_key") {
        ListSection(title = stringResource(Res.string.config_api_key)) {
            TextFieldListItem(
                hint = stringResource(Res.string.config_api_key_hint),
                value = config.apiKey ?: "",
                icon = Res.drawable.ic_key,
                isError = config.apiKey.isNullOrBlank(),
                isSecret = true,
                isEnabled = !isConfigSaveInProgress,
                onValueChange = { onConfigChange(config.copy(apiKey = it)) },
            )
        }
    }
}

private fun LazyListScope.localConfigItems(
    config: ModelProviderConfig.LocalConfig,
    onConfigChange: (ModelProviderConfig) -> Unit,
    isConfigSaveInProgress: Boolean,
) {
    item(key = "base_url") {
        ListSection(title = stringResource(Res.string.config_base_url)) {
            TextFieldListItem(
                hint = stringResource(Res.string.config_base_url_hint),
                value = config.apiUrl,
                icon = Res.drawable.ic_url,
                isError = config.apiUrl.isBlank(),
                isEnabled = !isConfigSaveInProgress,
                onValueChange = { onConfigChange(config.copy(apiUrl = it)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun ModelProviderConfigurationScreenPreview() = ScreenPreviewWrapper {
    ModelProviderConfigurationScreen(
        ModelProviderConfigurationComponentPreview(
            customState = State(
                providerName = "OpenAI",
                config = ModelProviderConfig.StandardConfig(
                    providerId = "id",
                    apiUrl = "https://api.openai.com",
                    apiKey = "something_that_looks_like_key",
                ),
                isConfigModified = false,
                isConfigSaveInProgress = false,
            ),
        ),
    )
}

@ScreenPreview
@Composable
private fun ModelProviderConfigurationScreenModifiedPreview() = ScreenPreviewWrapper {
    ModelProviderConfigurationScreen(
        ModelProviderConfigurationComponentPreview(
            customState = State(
                providerName = "OpenAI",
                config = ModelProviderConfig.StandardConfig(
                    providerId = "id",
                    apiUrl = "https://api.openai.com",
                    apiKey = "something_that_looks_like_key",
                ),
                isConfigModified = true,
                isConfigSaveInProgress = false,
            ),
        ),
    )
}

@ScreenPreview
@Composable
private fun ModelProviderConfigurationScreenLocalConfigPreview() = ScreenPreviewWrapper {
    ModelProviderConfigurationScreen(
        ModelProviderConfigurationComponentPreview(
            customState = State(
                providerName = "Ollama",
                config = ModelProviderConfig.LocalConfig(
                    providerId = "id",
                    apiUrl = "https://localhost:8080",
                ),
                isConfigModified = true,
                isConfigSaveInProgress = false,
            ),
        ),
    )
}

private val ProviderConfigListPadding = 16.dp
