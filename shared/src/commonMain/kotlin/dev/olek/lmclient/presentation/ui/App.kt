@file:OptIn(ExperimentalDecomposeApi::class)

package dev.olek.lmclient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV2
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.olek.lmclient.data.models.AppearanceMode
import dev.olek.lmclient.presentation.components.AppComponent
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.main.MainScreen
import dev.olek.lmclient.presentation.ui.mobile.settings.SettingsScreen
import dev.olek.lmclient.presentation.ui.mobile.settings.licenses.LicensesScreen
import dev.olek.lmclient.presentation.ui.mobile.settings.modelprovider.ModelProviderListScreen
import dev.olek.lmclient.presentation.ui.mobile.settings.providerconfig.ModelProviderConfigurationScreen
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform

@Composable
fun App(
    component: AppComponent,
    modifier: Modifier = Modifier,
    onThemeChange: ((isDarkTheme: Boolean) -> Unit)? = null,
) {
    val state by component.state.collectAsStateMultiplatform()
    val isDarkTheme = when (state.appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    if (onThemeChange != null) {
        LaunchedEffect(isDarkTheme, onThemeChange) { onThemeChange(isDarkTheme) }
    }

    AppTheme(
        darkTheme = isDarkTheme,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Children(
                stack = component.stack,
                animation = predictiveBackAnimation(
                    backHandler = component.backHandler,
                    fallbackAnimation = stackAnimation(fade() + scale()),
                    selector = { backEvent, _, _ -> androidPredictiveBackAnimatableV2(backEvent) },
                    onBack = component::navigateBack,
                ),
            ) {
                when (val child = it.instance) {
                    is AppComponent.Child.MainScreen -> MainScreen(child.component)
                    is AppComponent.Child.SettingsScreen -> SettingsScreen(child.component)
                    is AppComponent.Child.ModelProviderListScreen -> ModelProviderListScreen(child.component)
                    is AppComponent.Child.ModelProviderConfigurationScreen ->
                        ModelProviderConfigurationScreen(child.component)
                    is AppComponent.Child.LicensesComponentScreen -> LicensesScreen(child.component)
                }
            }
        }
    }
}
