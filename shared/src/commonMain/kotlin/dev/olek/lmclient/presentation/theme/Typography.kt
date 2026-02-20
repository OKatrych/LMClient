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
    val displayMedium: TextStyle,
    val title: TextStyle,
    val titleBold: TextStyle,
    val body: TextStyle,
    val bodyMedium: TextStyle,
    val bodySemiBold: TextStyle,
    val caption: TextStyle,
    val captionMedium: TextStyle,
    val footnote: TextStyle,
    val labelSemiBold: TextStyle,
)

internal val DefaultAppTypography: AppTypography
    @Composable
    get() {
        return AppTypography(
            displayMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
            ),
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
            body = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
            bodySemiBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
            caption = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
            captionMedium = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            footnote = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            ),
            labelSemiBold = TextStyle(
                fontFamily = DefaultFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
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
