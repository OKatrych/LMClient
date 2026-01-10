package dev.olek.lmclient.presentation.components.settings

import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.BuildKonfig
import dev.olek.lmclient.data.models.AppearanceMode
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.SettingsRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsScreenComponent {
    val state: StateFlow<State>

    fun navigateToModelProviders()

    fun navigateToLicenses()

    fun navigateBack()

    fun onAppearanceModeClicked(appearanceMode: AppearanceMode)

    data class State(
        val selectedProvider: ModelProvider? = null,
        val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
        val appVersion: String = "",
    )
}

internal class SettingsScreenComponentImpl(
    context: ComponentContext,
    private val onBackPressed: () -> Unit,
    private val onModelProviderClicked: () -> Unit,
    private val onLicensesClicked: () -> Unit,
) : SettingsScreenComponent,
    KoinComponent,
    ComponentContext by context {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val modelProviderRepository: ModelProviderRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    override val state: StateFlow<SettingsScreenComponent.State> =
        combine(
            modelProviderRepository.observeActiveProvider(),
            settingsRepository.observeAppearanceMode(),
            flow { emit(BuildKonfig.appVersionName) },
        ) { activeProvider, appearanceMode, appVersion ->
            SettingsScreenComponent.State(
                selectedProvider = activeProvider,
                appearanceMode = appearanceMode,
                appVersion = appVersion,
            )
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = SettingsScreenComponent.State(),
        )

    override fun navigateToModelProviders() {
        onModelProviderClicked()
    }

    override fun navigateBack() {
        onBackPressed()
    }

    override fun navigateToLicenses() {
        onLicensesClicked()
    }

    override fun onAppearanceModeClicked(appearanceMode: AppearanceMode) {
        if (state.value.appearanceMode == appearanceMode) return
        coroutineScope.launch { settingsRepository.setAppearanceMode(appearanceMode) }
    }
}

class SettingsScreenComponentPreview(customState: SettingsScreenComponent.State) : SettingsScreenComponent {
    override val state: StateFlow<SettingsScreenComponent.State> = MutableStateFlow(customState)

    override fun navigateToModelProviders() = Unit

    override fun navigateBack() = Unit

    override fun onAppearanceModeClicked(appearanceMode: AppearanceMode) = Unit

    override fun navigateToLicenses() = Unit
}
