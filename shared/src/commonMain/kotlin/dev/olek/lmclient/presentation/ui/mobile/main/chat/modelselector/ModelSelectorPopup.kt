@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.main.chat.modelselector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.presentation.components.main.ModelSelectorComponent
import dev.olek.lmclient.presentation.components.main.ModelSelectorComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.AppLoadingIndicator
import dev.olek.lmclient.presentation.ui.mobile.common.popup.AppPopup
import dev.olek.lmclient.presentation.ui.mobile.common.popup.ErrorPopup
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import kotlinx.coroutines.flow.MutableStateFlow
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_check
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview


@Composable
internal fun ModelSelectorPopup(
    component: ModelSelectorComponent,
) {
    val state by component.state.collectAsStateMultiplatform()
    val error = state.error

    if (error != null) {
        ErrorPopup(
            title = "Couldn't load models",
            error = error,
            onDismissRequest = component.onDismissRequest,
        )
    } else {
        AppPopup(
            title = "Select model",
            onDismissRequest = component.onDismissRequest,
            content = {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    modelsItems(
                        state = state,
                        onModelSelected = component::onModelSelected,
                    )
                }
            },
        )
    }
}

private fun LazyListScope.modelsItems(
    state: ModelSelectorComponent.State,
    onModelSelected: (Model) -> Unit
) {
    if (state.isLoading) {
        item("loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LoadingItemHeight)
                    .animateItem(),
                contentAlignment = Alignment.Center,
            ) {
                AppLoadingIndicator()
            }
        }
    } else {
        items(items = state.models, key = { it.id }) { model ->
            ModelItem(
                modifier = Modifier.animateItem(),
                model = model,
                selected = model == state.selectedModel,
                onSelect = onModelSelected,
            )
        }
    }
}

@Composable
private fun ModelItem(
    model: Model,
    selected: Boolean,
    onSelect: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth()
            .clip(AppTheme.shapes.selectableItem)
            .toggleable(
                value = selected,
                onValueChange = { onSelect(model) },
                role = Role.RadioButton,
            ).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = model.name,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text,
        )
        if (selected) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = AppTheme.colors.icon,
            )
        } else {
            Spacer(Modifier.size(24.dp))
        }
    }
}

@Preview
@Composable
private fun ModelSelectorModalPreview() {
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            ModelSelectorPopup(
                component = ModelSelectorComponentPreview(),
            )
        }
    }
}

@Preview
@Composable
private fun ModelSelectorModalLoadingPreview() {
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            ModelSelectorPopup(
                component = ModelSelectorComponentPreview(
                    state = MutableStateFlow(
                        ModelSelectorComponent.State(isLoading = true),
                    )
                ),
            )
        }
    }
}

private val LoadingItemHeight = 250.dp
