package dev.olek.lmclient.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.inter_bold
import lm_client.shared.generated.resources.inter_extra_bold
import lm_client.shared.generated.resources.inter_light
import lm_client.shared.generated.resources.inter_medium
import lm_client.shared.generated.resources.inter_regular
import lm_client.shared.generated.resources.inter_thin
import org.jetbrains.compose.resources.Font

@Immutable
class AppTypography(
    val title: TextStyle,
    val titleBold: TextStyle,
    val titleLarge: TextStyle,
    val titleLargeMedium: TextStyle,
    val titleLargeBold: TextStyle,
    val body: TextStyle,
    val bodyBold: TextStyle,
    val bodyMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyLargeMedium: TextStyle,
    val bodyLargeBold: TextStyle,
    val bodyLargeSemiBold: TextStyle,
)

internal val DefaultAppTypography: AppTypography
    @Composable
    get() {
        return AppTypography(
            title = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
            ),
            titleBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
            ),
            titleLargeMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
            ),
            titleLargeBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            body = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
            bodyBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
            bodyLargeMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
            bodyLargeBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            bodyLargeSemiBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
        )
    }

private val DefaultFontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.inter_thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.inter_light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.inter_extra_bold, FontWeight.ExtraBold, FontStyle.Normal),
    )

val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("AppTheme must be part of the call hierarchy to provide typography")
}
