package dev.olek.lmclient.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
class AppColorScheme(
    val surface: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundError: Color,
    val text: Color,
    val textSecondary: Color,
    val primary: Color,
    val onPrimary: Color,
    val icon: Color,
    val stroke: Color,
    val success: Color,
    val caution: Color,
    val error: Color,
    val ripple: Color,
    val dragHandle: Color,
    val strokeGradient: List<Color>,
)

fun darkAppColorScheme(
    surface: Color = DarkColors.surface,
    background: Color = DarkColors.background,
    backgroundSecondary: Color = DarkColors.backgroundSecondary,
    backgroundError: Color = DarkColors.backgroundError,
    text: Color = DarkColors.text,
    textSecondary: Color = DarkColors.textSecondary,
    primary: Color = DarkColors.primary,
    onPrimary: Color = DarkColors.onPrimary,
    icon: Color = DarkColors.icon,
    stroke: Color = DarkColors.stroke,
    success: Color = DarkColors.success,
    caution: Color = DarkColors.caution,
    error: Color = DarkColors.error,
    ripple: Color = DarkColors.ripple,
    dragHandle: Color = DarkColors.dragHandle,
    strokeGradient: List<Color> = DarkColors.strokeGradient,
) = AppColorScheme(
    surface = surface,
    background = background,
    backgroundSecondary = backgroundSecondary,
    backgroundError = backgroundError,
    text = text,
    textSecondary = textSecondary,
    primary = primary,
    onPrimary = onPrimary,
    icon = icon,
    stroke = stroke,
    success = success,
    caution = caution,
    error = error,
    ripple = ripple,
    dragHandle = dragHandle,
    strokeGradient = strokeGradient,
)

fun lightAppColorScheme(
    surface: Color = LightColors.surface,
    background: Color = LightColors.background,
    backgroundSecondary: Color = LightColors.backgroundSecondary,
    backgroundError: Color = LightColors.backgroundError,
    text: Color = LightColors.text,
    textSecondary: Color = LightColors.textSecondary,
    primary: Color = LightColors.primary,
    onPrimary: Color = LightColors.onPrimary,
    icon: Color = LightColors.icon,
    stroke: Color = LightColors.stroke,
    success: Color = LightColors.success,
    caution: Color = LightColors.caution,
    error: Color = LightColors.error,
    ripple: Color = LightColors.ripple,
    dragHandle: Color = LightColors.dragHandle,
    strokeGradient: List<Color> = LightColors.strokeGradient,
) = AppColorScheme(
    surface = surface,
    background = background,
    backgroundSecondary = backgroundSecondary,
    backgroundError = backgroundError,
    text = text,
    textSecondary = textSecondary,
    primary = primary,
    onPrimary = onPrimary,
    icon = icon,
    stroke = stroke,
    success = success,
    caution = caution,
    error = error,
    ripple = ripple,
    dragHandle = dragHandle,
    strokeGradient = strokeGradient,
)

private object LightColors {
    val surface = Color(0xFFEBE9EC)
    val background = Color(0xFFF6F6F6)
    val backgroundSecondary = Color(0xFFFFFFFF)
    val backgroundError = Color(0x26FF3B30)

    val text = Color(0xFF090909)
    val textSecondary = Color(0xFF4D4B4E)

    val primary = Color(0xFF252525)
    val onPrimary = Color(0xFFF5F5F5)

    val icon = Color(0xFF090909)
    val stroke = Color(0xFFDBDBDB)
    val success = Color(0xFF34C759)
    val caution = Color(0xFFFFCC00)
    val error = Color(0xFFFF3B30)
    val ripple = Color(0x1A000000)
    val dragHandle = Color(0xFFE5E5EA)
    val strokeGradient = listOf(
        Color(0xFF337ADC),
        Color(0xFF337FD3),
        Color(0xFF3484CB),
        Color(0xFF3489C2),
        Color(0xFF348EB9),
        Color(0xFF3493B0),
        Color(0xFF3498A8),
        Color(0xFF349D9F),
        Color(0xFF34A296),
        Color(0xFF34A78D),
        Color(0xFF34AC85),
        Color(0xFF34B17C),
        Color(0xFF34B673),
        Color(0xFF34BB6A),
        Color(0xFF34C062),
        Color(0xFF34C759),
    )
}

private object DarkColors {
    val surface = Color(0xFF0F0F0F)
    val background = Color(0xFF161616)
    val backgroundSecondary = Color(0xFF1C1C1E)
    val backgroundError = Color(0x26FF3B30)

    val text = Color(0xFFFDFDFD)
    val textSecondary = Color(0xFF909090)

    val primary = Color(0xFFFDFDFD)
    val onPrimary = Color(0xFF0F0F0F)

    val icon = Color(0xFFFDFDFD)
    val stroke = Color(0xFF1E1E1E)
    val success = Color(0xFF11a481)
    val caution = Color(0xFFDB7F34)
    val error = Color(0xFFD12F26)
    val ripple = Color(0x1AFFFFFF)
    val dragHandle = Color(0xFF3B3B3B)
    val strokeGradient = listOf(
        Color(0xFF337ADC),
        Color(0xFF337FD3),
        Color(0xFF3484CB),
        Color(0xFF3489C2),
        Color(0xFF348EB9),
        Color(0xFF3493B0),
        Color(0xFF3498A8),
        Color(0xFF349D9F),
        Color(0xFF34A296),
        Color(0xFF34A78D),
        Color(0xFF34AC85),
        Color(0xFF34B17C),
        Color(0xFF34B673),
        Color(0xFF34BB6A),
        Color(0xFF34C062),
        Color(0xFF34C759),
    )
}

internal val LocalAppColorScheme = staticCompositionLocalOf<AppColorScheme> {
    error("AppTheme must be part of the call hierarchy to provide colors")
}
