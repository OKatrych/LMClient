package dev.olek.lmclient.presentation.ui.mobile.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme

@Suppress("ModifierComposed")
fun Modifier.animatedBorder(
    borderColors: List<Color> = listOf(Color.Blue, Color.Red),
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(24.dp),
    shadowRadius: Dp = 8.dp,
    backgroundColor: Color? = null,
) = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val density = LocalDensity.current
    val shadowRadiusPx = remember(shadowRadius, density) {
        with(density) { shadowRadius.toPx() }
    }
    val bgColor = backgroundColor ?: AppTheme.colors.surface

    // Precalculate a set of shader brushes for different positions
    val gradientBrushes = remember(borderColors) {
        val steps = 20
        (0..steps).map { step ->
            val offset = step.toFloat() / steps
            object : ShaderBrush() {
                override fun createShader(size: Size): Shader {
                    val widthOffset = size.width * offset
                    return LinearGradientShader(
                        colors = borderColors,
                        from = Offset(widthOffset, 0f),
                        to = Offset(widthOffset + size.width, size.height),
                        tileMode = TileMode.Mirror,
                    )
                }
            }
        }
    }

    // Select current brush based on animation progress
    val currentBrushIndex = (animationProgress * (gradientBrushes.size - 1)).toInt()
    val currentBrush = gradientBrushes[currentBrushIndex]

    this
        .dropShadow(shape = shape) {
            brush = currentBrush
            radius = shadowRadiusPx
        }.drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)

            // Draw background
            drawOutline(
                outline = outline,
                color = bgColor,
            )

            // Draw animated border
            drawOutline(
                outline = outline,
                brush = currentBrush,
                style = Stroke(width = borderWidth.toPx()),
            )
        }
}
