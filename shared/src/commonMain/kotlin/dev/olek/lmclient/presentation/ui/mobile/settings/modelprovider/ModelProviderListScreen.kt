@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.settings.modelprovider

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.models.ModelProvider.ModelProviderConfig
import dev.olek.lmclient.data.util.LMClientModelProvider
import dev.olek.lmclient.presentation.components.settings.ModelProviderListComponent
import dev.olek.lmclient.presentation.components.settings.ModelProviderListComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreview
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.ui.mobile.common.list.ListItem
import dev.olek.lmclient.presentation.ui.mobile.common.topbar.CenteredTopBar
import dev.olek.lmclient.presentation.ui.mobile.extensions.getIconColorFilter
import dev.olek.lmclient.presentation.ui.mobile.extensions.getIconRes
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.model_provider_section_active
import lm_client.shared.generated.resources.model_provider_section_inactive
import lm_client.shared.generated.resources.model_provider_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModelProviderListScreen(component: ModelProviderListComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateMultiplatform()
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    val listTopPaddingPx = LocalDensity.current.run { ModelProviderListPadding.toPx() }

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
                title = stringResource(Res.string.model_provider_title),
                onBackClick = component::navigateBack,
            )
        },
        content = {
            ScreenContent(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                paddingValues = it + PaddingValues(vertical = ModelProviderListPadding),
                state = state,
                listState = listState,
                onProviderClick = component::navigateToProviderConfiguration,
            )
        },
    )
}

@Composable
private fun ScreenContent(
    paddingValues: PaddingValues,
    state: ModelProviderListComponent.State,
    listState: LazyListState,
    onProviderClick: (ModelProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeProvider = state.providers.find { it.isActive }
    val inactiveProviders = state.providers.filter { !it.isActive }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = paddingValues,
    ) {
        activeProvider?.let { provider ->
            item("active_provider_section") {
                Column {
                    SectionTitle(title = stringResource(Res.string.model_provider_section_active))
                    ProviderItem(
                        modifier = Modifier.padding(bottom = 8.dp),
                        provider = provider,
                        onProviderClick = onProviderClick,
                    )
                }
            }
        }
        item("inactive_provider_section") {
            SectionTitle(
                title = stringResource(Res.string.model_provider_section_inactive),
            )
        }
        items(items = inactiveProviders, key = { it.id }) { provider ->
            ProviderItem(
                provider = provider,
                onProviderClick = onProviderClick,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(start = 16.dp, bottom = 16.dp),
        text = title,
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textSecondary,
    )
}

@Composable
private fun ProviderItem(
    provider: ModelProvider,
    onProviderClick: (ModelProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        title = provider.name,
        icon = {
            Image(
                painter = painterResource(provider.getIconRes()),
                colorFilter = provider.getIconColorFilter(),
                contentDescription = null,
            )
        },
        onClick = { onProviderClick(provider) },
    )
}

@ScreenPreview
@Composable
private fun ModelProviderListScreenPreview() = ScreenPreviewWrapper {
    ModelProviderListScreen(
        component = ModelProviderListComponentPreview(
            customState = ModelProviderListComponent.State(
                providers = listOf(
                    LMClientModelProvider.OpenAI,
                    LMClientModelProvider.Claude,
                    LMClientModelProvider.Google,
                    LMClientModelProvider.NexosAI,
                    LMClientModelProvider.OpenRouter,
                    LMClientModelProvider.DeepSeek,
                    LMClientModelProvider.Ollama,
                ).map {
                    ModelProvider(
                        id = it.id,
                        name = it.display,
                        config = ModelProviderConfig.StandardConfig(it.id, "", ""),
                        isActive = it == LMClientModelProvider.OpenAI,
                    )
                },
            ),
        ),
    )
}

private val ModelProviderListPadding = 16.dp
