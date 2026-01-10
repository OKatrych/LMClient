package dev.olek.lmclient.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import dev.olek.lmclient.data.models.AppearanceMode
import dev.olek.lmclient.data.repositories.SettingsRepository
import dev.olek.lmclient.presentation.components.AppComponent.Child
import dev.olek.lmclient.presentation.components.AppComponent.Child.LicensesComponentScreen
import dev.olek.lmclient.presentation.components.AppComponent.Child.MainScreen
import dev.olek.lmclient.presentation.components.AppComponent.Child.ModelProviderConfigurationScreen
import dev.olek.lmclient.presentation.components.AppComponent.Child.ModelProviderListScreen
import dev.olek.lmclient.presentation.components.AppComponent.Child.SettingsScreen
import dev.olek.lmclient.presentation.components.main.MainScreenComponent
import dev.olek.lmclient.presentation.components.main.MainScreenComponentImpl
import dev.olek.lmclient.presentation.components.settings.LicensesComponent
import dev.olek.lmclient.presentation.components.settings.LicensesComponentImpl
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponentImpl
import dev.olek.lmclient.presentation.components.settings.ModelProviderListComponent
import dev.olek.lmclient.presentation.components.settings.ModelProviderListComponentImpl
import dev.olek.lmclient.presentation.components.settings.SettingsScreenComponent
import dev.olek.lmclient.presentation.components.settings.SettingsScreenComponentImpl
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AppComponent : BackHandlerOwner {
    val stack: Value<ChildStack<*, Child>>

    val state: StateFlow<State>

    fun navigateBack()

    sealed class Child {
        class MainScreen(val component: MainScreenComponent) : Child()
        class SettingsScreen(val component: SettingsScreenComponent) : Child()
        class ModelProviderListScreen(val component: ModelProviderListComponent) : Child()
        class ModelProviderConfigurationScreen(
            val component: ModelProviderConfigurationComponent,
        ) : Child()

        class LicensesComponentScreen(val component: LicensesComponent) : Child()
    }

    data class State(
        val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    )
}

class AppComponentImpl(
    componentContext: ComponentContext
) : AppComponent, KoinComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val settingsRepository: SettingsRepository by inject()

    private val navigation = StackNavigation<Config>()
    override val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.ChatScreenConfig,
        handleBackButton = true, // Pop the back stack on back button press
    ) { config, context ->
        when (config) {
            is Config.ChatScreenConfig -> MainScreen(
                component = MainScreenComponentImpl(
                    context,
                    onSettingsClick = { navigation.pushNew(Config.SettingsScreenConfig) },
                    onConfigureProviderClick = {
                        navigation.navigate { stack ->
                            stack + Config.SettingsScreenConfig + Config.ModelProviderListConfig
                        }
                    },
                ),
            )

            is Config.SettingsScreenConfig -> SettingsScreen(
                component = SettingsScreenComponentImpl(
                    context = context,
                    onBackPressed = { navigation.pop() },
                    onModelProviderClicked = {
                        navigation.pushNew(Config.ModelProviderListConfig)
                    },
                    onLicensesClicked = {
                        navigation.pushNew(Config.LicensesComponentConfig)
                    },
                ),
            )

            is Config.ProviderConfigScreenConfig -> ModelProviderConfigurationScreen(
                ModelProviderConfigurationComponentImpl(
                    context = context,
                    providerId = config.providerId,
                    onBackPressed = { navigation.pop() },
                ),
            )

            is Config.ModelProviderListConfig -> ModelProviderListScreen(
                ModelProviderListComponentImpl(
                    context = context,
                    onProviderConfigClicked = {
                        navigation.pushNew(Config.ProviderConfigScreenConfig(it))
                    },
                    onBackPressed = { navigation.pop() },
                ),
            )

            is Config.LicensesComponentConfig -> LicensesComponentScreen(
                LicensesComponentImpl(
                    context = context,
                    onBackPressed = { navigation.pop() },
                ),
            )
        }
    }

    override val state: StateFlow<AppComponent.State> = settingsRepository
        .observeAppearanceMode()
        .map { AppComponent.State(appearanceMode = it) }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
            initialValue = AppComponent.State(),
        )

    override fun navigateBack() {
        navigation.pop()
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object ChatScreenConfig : Config()

        @Serializable
        data object SettingsScreenConfig : Config()

        @Serializable
        data object ModelProviderListConfig : Config()

        @Serializable
        data object LicensesComponentConfig : Config()

        @Serializable
        data class ProviderConfigScreenConfig(val providerId: String) : Config()
    }
}
