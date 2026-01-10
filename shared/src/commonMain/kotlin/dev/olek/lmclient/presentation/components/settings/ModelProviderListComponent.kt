package dev.olek.lmclient.presentation.components.settings

import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ModelProviderListComponent {
    val state: StateFlow<State>

    fun navigateToProviderConfiguration(provider: ModelProvider)

    fun navigateBack()

    data class State(val isLoading: Boolean = true, val providers: List<ModelProvider> = emptyList())
}

internal class ModelProviderListComponentImpl(
    context: ComponentContext,
    private val onProviderConfigClicked: (providerId: String) -> Unit,
    private val onBackPressed: () -> Unit,
) : ModelProviderListComponent,
    ComponentContext by context,
    KoinComponent {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val modelProviderRepository: ModelProviderRepository by inject()

    override val state: StateFlow<ModelProviderListComponent.State> = modelProviderRepository
        .observeProviders()
        .map { providers ->
            ModelProviderListComponent.State(
                isLoading = false,
                providers = providers,
            )
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = ModelProviderListComponent.State(),
        )

    override fun navigateToProviderConfiguration(provider: ModelProvider) {
        onProviderConfigClicked(provider.id)
    }

    override fun navigateBack() {
        onBackPressed()
    }
}

class ModelProviderListComponentPreview(customState: ModelProviderListComponent.State) : ModelProviderListComponent {
    override val state: StateFlow<ModelProviderListComponent.State> = MutableStateFlow(customState)

    override fun navigateToProviderConfiguration(provider: ModelProvider) = Unit

    override fun navigateBack() = Unit
}
