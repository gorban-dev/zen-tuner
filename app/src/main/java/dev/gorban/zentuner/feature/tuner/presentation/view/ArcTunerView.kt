package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gorban.zentuner.feature.tuner.domain.model.Note
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun ArcTunerView(
    note: Note?,
    tunerSize: Dp,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Arc range: ±50 cents mapped to angle range
    val arcStartAngle = 240f // degrees
    val arcEndAngle = 300f   // degrees
    val middleAngle = 270f
    val arcRange = 50f       // ±50 cents

    val targetAngle = if (note != null) {
        val clampedCents = note.centsOffset.toFloat().coerceIn(-arcRange, arcRange)
        val normalizedOffset = clampedCents / arcRange // -1 to 1
        val angleRange = arcEndAngle - arcStartAngle
        if (note.isInTune) middleAngle else middleAngle + normalizedOffset * angleRange / 2f
    } else {
        middleAngle
    }

    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tuner_angle"
    )

    val isInTune = note?.isInTune == true

    val indicatorScale by animateFloatAsState(
        targetValue = if (isInTune) 2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicator_scale"
    )

    val dotSize = 20.dp
    val centerDotSize = 48.dp
    val lineWidth = 2.dp

    Canvas(modifier = modifier.size(tunerSize)) {
        val w = size.width
        val h = size.height

        // iOS: center = (tunerSize/2, tunerSize), radius = tunerSize
        // The arc center is at the BOTTOM of the view, radius = full width
        val centerX = w / 2f
        val centerY = h // center at bottom
        val arcRadius = w // radius = full width of the view

        // Draw arc: from 240° to 300° (with 2° inset on each side)
        // In Android drawArc, angles: 0° = 3 o'clock, goes clockwise
        // iOS 240° maps to Android: startAngle = 240° (measured from center going clockwise)
        // But the arc center is offset, so we use the arc bounding box approach
        val arcLeft = centerX - arcRadius
        val arcTop = centerY - arcRadius
        val arcDiameter = arcRadius * 2

        drawArc(
            color = Color.White,
            startAngle = arcStartAngle + 2f,
            sweepAngle = (arcEndAngle - 2f) - (arcStartAngle + 2f),
            useCenter = false,
            topLeft = Offset(arcLeft, arcTop + dotSize.toPx() / 2f + lineWidth.toPx()),
            size = Size(arcDiameter, arcDiameter),
            style = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Round)
        )

        // Center dot at top of arc (270° from arc center)
        val centerDotAngleRad = Math.toRadians(270.0)
        val centerDotX = (centerX + arcRadius * cos(centerDotAngleRad)).toFloat()
        val centerDotY = (centerY + arcRadius * sin(centerDotAngleRad)).toFloat() + dotSize.toPx() / 2f

        val centerDotRadius = centerDotSize.toPx() / 2f
        drawCircle(
            color = Color.Black,
            radius = centerDotRadius,
            center = Offset(centerDotX, centerDotY)
        )
        drawCircle(
            color = Color.White,
            radius = centerDotRadius,
            center = Offset(centerDotX, centerDotY),
            style = Stroke(width = lineWidth.toPx())
        )

        // Indicator dot
        if (note != null) {
            val indicatorAngleRad = Math.toRadians(animatedAngle.toDouble())
            val indicatorX = (centerX + arcRadius * cos(indicatorAngleRad)).toFloat()
            val indicatorY = (centerY + arcRadius * sin(indicatorAngleRad)).toFloat() + dotSize.toPx() / 2f

            val baseRadius = dotSize.toPx() / 2f
            val indicatorRadius = baseRadius * indicatorScale
            val indicatorColor = if (isInTune) Color(0xFF4CAF50) else Color.White

            drawCircle(
                color = indicatorColor,
                radius = indicatorRadius,
                center = Offset(indicatorX, indicatorY)
            )
        }

        // Labels
        val labelStyle = TextStyle(color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

        // "0" label above center dot
        val centerText = textMeasurer.measure("0", labelStyle)
        drawText(
            textLayoutResult = centerText,
            topLeft = Offset(
                centerDotX - centerText.size.width / 2f,
                centerDotY - centerDotRadius - centerText.size.height - 4.dp.toPx()
            )
        )

        // "-50" label
        val leftAngleRad = Math.toRadians(arcStartAngle.toDouble())
        val leftX = (centerX + arcRadius * cos(leftAngleRad)).toFloat()
        val leftY = (centerY + arcRadius * sin(leftAngleRad)).toFloat() + dotSize.toPx() / 2f
        val leftText = textMeasurer.measure("-50", labelStyle)
        drawText(
            textLayoutResult = leftText,
            topLeft = Offset(
                leftX - leftText.size.width / 2f,
                leftY + 4.dp.toPx()
            )
        )

        // "+50" label
        val rightAngleRad = Math.toRadians(arcEndAngle.toDouble())
        val rightX = (centerX + arcRadius * cos(rightAngleRad)).toFloat()
        val rightY = (centerY + arcRadius * sin(rightAngleRad)).toFloat() + dotSize.toPx() / 2f
        val rightText = textMeasurer.measure("+50", labelStyle)
        drawText(
            textLayoutResult = rightText,
            topLeft = Offset(
                rightX - rightText.size.width / 2f,
                rightY + 4.dp.toPx()
            )
        )
    }
}
