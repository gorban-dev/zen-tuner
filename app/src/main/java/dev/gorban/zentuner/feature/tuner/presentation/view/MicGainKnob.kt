package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val MIN_THRESHOLD = 0.001
private const val MAX_THRESHOLD = 0.08
private const val MIN_ROTATION = -135f
private const val MAX_ROTATION = 135f
private const val SENSITIVITY = 0.5f
private const val DOT_COUNT = 13

private fun thresholdToRotation(threshold: Double): Float {
    val normalizedGain = 1f - ((threshold - MIN_THRESHOLD) / (MAX_THRESHOLD - MIN_THRESHOLD)).toFloat().coerceIn(0f, 1f)
    return MIN_ROTATION + normalizedGain * (MAX_ROTATION - MIN_ROTATION)
}

private fun rotationToThreshold(rotation: Float): Double {
    val normalizedRotation = (rotation - MIN_ROTATION) / (MAX_ROTATION - MIN_ROTATION)
    return (MAX_THRESHOLD - normalizedRotation * (MAX_THRESHOLD - MIN_THRESHOLD)).coerceIn(MIN_THRESHOLD, MAX_THRESHOLD)
}

@Composable
fun MicGainKnob(
    threshold: Double,
    amplitude: Double,
    isListening: Boolean,
    onThresholdChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rotation = thresholdToRotation(threshold)

    val currentRotation by rememberUpdatedState(rotation)
    val currentOnThresholdChange by rememberUpdatedState(onThresholdChange)

    val gainAdjustedAmplitude = amplitude / threshold
    val normalizedAmplitude = min(gainAdjustedAmplitude / 4.0, 1.0).toFloat()
    val activeDots = if (isListening) (normalizedAmplitude * DOT_COUNT).toInt().coerceIn(0, DOT_COUNT) else 0

    var lastDotPosition by remember { mutableIntStateOf(0) }

    val totalSize = 120.dp
    val knobSize = 64.dp
    val dotRadius = 40.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(totalSize)
                .pointerInput(Unit) {
                    var dragStartRotation = 0f
                    detectDragGestures(
                        onDragStart = {
                            dragStartRotation = currentRotation
                            lastDotPosition = ((currentRotation - MIN_ROTATION) / (MAX_ROTATION - MIN_ROTATION) * (DOT_COUNT - 1)).toInt().coerceIn(0, DOT_COUNT - 1)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragDistance = -dragAmount.y * SENSITIVITY
                            val newRotation = (dragStartRotation + dragDistance).coerceIn(MIN_ROTATION, MAX_ROTATION)
                            dragStartRotation = newRotation

                            currentOnThresholdChange(rotationToThreshold(newRotation))

                            val normalizedRotation = (newRotation - MIN_ROTATION) / (MAX_ROTATION - MIN_ROTATION)
                            val currentDotPosition = (normalizedRotation * (DOT_COUNT - 1)).toInt().coerceIn(0, DOT_COUNT - 1)
                            if (currentDotPosition != lastDotPosition) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastDotPosition = currentDotPosition
                            }
                        }
                    )
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val knobRadius = knobSize.toPx() / 2f
            val dotOrbitRadius = dotRadius.toPx()

            drawCircle(
                color = Color.White,
                radius = knobRadius,
                center = Offset(centerX, centerY)
            )

            val indicatorAngleRad = ((rotation - 90f) * PI / 180.0)
            val indicatorDistance = knobRadius - 8.dp.toPx()
            val indicatorX = (centerX + indicatorDistance * cos(indicatorAngleRad)).toFloat()
            val indicatorY = (centerY + indicatorDistance * sin(indicatorAngleRad)).toFloat()

            drawCircle(
                color = Color.Black,
                radius = 3.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
            )

            val startAngle = -135.0
            val sweepTotal = 270.0
            val angleStep = sweepTotal / (DOT_COUNT - 1)

            for (i in 0 until DOT_COUNT) {
                val dotAngleDeg = startAngle + i * angleStep
                val dotAngleRad = ((dotAngleDeg - 90.0) * PI / 180.0)
                val dotX = (centerX + dotOrbitRadius * cos(dotAngleRad)).toFloat()
                val dotY = (centerY + dotOrbitRadius * sin(dotAngleRad)).toFloat()

                val dotColor = when {
                    i >= activeDots -> Color.White.copy(alpha = 0.2f)
                    i >= 11 -> Color(0xFFF44336)
                    i >= 9 -> Color(0xFFFFEB3B)
                    else -> Color(0xFF4CAF50)
                }

                drawCircle(
                    color = dotColor,
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "MIC GAIN",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        )
    }
}
