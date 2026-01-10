package dev.olek.lmclient.presentation.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

object AppTheme {
    val colors: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorScheme.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current
}

// Exposes custom theme value to Compose resources, https://youtrack.jetbrains.com/issue/CMP-4197
expect object LocalAppTheme {
    val current: Boolean @Composable get

    @Composable
    infix fun provides(value: Boolean?): ProvidedValue<*>
}

@Composable
internal fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme =
        remember(darkTheme) { if (darkTheme) darkAppColorScheme() else lightAppColorScheme() }
    val typography = DefaultAppTypography
    val shapes = remember { defaultAppShapes() }

    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalAppTypography provides typography,
        LocalAppShapes provides shapes,
        LocalIndication provides ripple(color = colorScheme.ripple),
        LocalAppTheme provides darkTheme,
    ) {
        content()
    }
}
