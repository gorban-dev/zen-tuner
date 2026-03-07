package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PowerButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale.value)
            .drawBehind {
                val radius = size.minDimension / 2f
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )

                val iconColor = if (isListening) Color(0xFF4CAF50) else Color.Black
                drawPowerIcon(
                    centerX = centerX,
                    centerY = centerY,
                    iconSize = 40.dp.toPx(),
                    color = iconColor
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scope.launch {
                            scale.animateTo(0.85f, spring(stiffness = Spring.StiffnessMedium))
                        }
                        tryAwaitRelease()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                        scope.launch {
                            scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {}
}

private fun DrawScope.drawPowerIcon(
    centerX: Float,
    centerY: Float,
    iconSize: Float,
    color: Color
) {
    val strokeWidth = iconSize * 0.1f
    val arcRadius = iconSize * 0.38f
    val gapHalfAngle = 35.0
    val startAngle = (-90.0 + gapHalfAngle) * PI / 180.0
    val endAngle = (-90.0 - gapHalfAngle + 360.0) * PI / 180.0

    val arcPath = Path()
    val step = PI / 60
    var angle = startAngle
    var firstPoint = true
    while (angle <= endAngle) {
        val x = (centerX + arcRadius * cos(angle)).toFloat()
        val y = (centerY + arcRadius * sin(angle)).toFloat()
        if (firstPoint) {
            arcPath.moveTo(x, y)
            firstPoint = false
        } else {
            arcPath.lineTo(x, y)
        }
        angle += step
    }

    drawPath(
        path = arcPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    drawLine(
        color = color,
        start = Offset(centerX, centerY - arcRadius - iconSize * 0.08f),
        end = Offset(centerX, centerY - arcRadius + iconSize * 0.14f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
