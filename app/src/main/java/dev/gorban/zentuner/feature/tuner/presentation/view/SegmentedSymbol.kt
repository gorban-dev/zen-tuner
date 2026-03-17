package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gorban.zentuner.feature.tuner.domain.model.SymbolSegment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


private val segmentMapping = mapOf(
    SymbolSegment.C to setOf(1, 3, 4, 5, 6, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 31, 32, 33, 34, 35, 52, 53, 54),
    SymbolSegment.D to setOf(1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 52, 53, 54),
    SymbolSegment.E to setOf(1, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 46, 50, 51, 52, 53, 54, 55),
    SymbolSegment.F to setOf(1, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23, 24, 28, 29, 30, 37, 38, 39, 40, 46, 50, 51, 52, 53, 55),
    SymbolSegment.G to setOf(1, 3, 4, 5, 6, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 31, 32, 33, 34, 35, 39, 40, 46, 52, 53, 54, 55),
    SymbolSegment.A to setOf(1, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 28, 29, 30, 35, 36, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55),
    SymbolSegment.B to setOf(1, 2, 3, 4, 5, 6, 8, 10, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 37, 38, 39, 40, 46, 50, 51, 52, 53, 54, 55)
)

private val loadingSegments = listOf(38, 39, 55, 51)

@Composable
fun SegmentedSymbol(
    symbol: SymbolSegment,
    color: Color,
    isInTune: Boolean,
    modifier: Modifier = Modifier,
    symbolWidth: Dp = 112.5.dp,
    symbolHeight: Dp = 149.5.dp
) {

    val activeSegments = segmentMapping[symbol] ?: emptySet()
    val segmentAlphas = remember { Array(56) { Animatable(0f) } }
    val loadingAlphas = remember { Array(loadingSegments.size) { Animatable(0f) } }

    var loadingIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(symbol) {
        if (symbol == SymbolSegment.NONE) {
            // Staggered fade-out with random delays (like iOS)
            for (i in 1..55) {
                val delayMs = Random.nextInt(120).toLong()
                launch {
                    delay(delayMs)
                    segmentAlphas[i].animateTo(0f, tween(100))
                }
            }
            return@LaunchedEffect
        }

        // Staggered fade-in for new symbol
        for (i in 1..55) {
            val isActive = i in activeSegments
            val targetAlpha = if (isActive) 1f else 0f
            val delayMs = Random.nextInt(120).toLong()
            launch {
                delay(delayMs)
                segmentAlphas[i].animateTo(targetAlpha, tween(80))
            }
        }
    }

    LaunchedEffect(symbol == SymbolSegment.NONE) {
        if (symbol == SymbolSegment.NONE) {
            while (true) {
                for (idx in loadingSegments.indices) {
                    loadingIndex = idx
                    // Animate previous segment out and current segment in (like iOS easeInOut 0.4)
                    for (j in loadingAlphas.indices) {
                        launch {
                            loadingAlphas[j].animateTo(
                                if (j == idx) 1f else 0f,
                                tween(400)
                            )
                        }
                    }
                    delay(200)
                }
            }
        } else {
            // Fade out all loading segments when leaving NONE
            for (j in loadingAlphas.indices) {
                launch {
                    loadingAlphas[j].animateTo(0f, tween(100))
                }
            }
        }
    }

    val segmentColor = if (isInTune && symbol != SymbolSegment.NONE) Color(0xFF4CAF50) else color

    Canvas(modifier = modifier.size(symbolWidth, symbolHeight)) {
        val w = size.width
        val h = size.height

        for (i in 1..55) {
            val alpha = if (symbol == SymbolSegment.NONE) {
                val loadingIdx = loadingSegments.indexOf(i)
                if (loadingIdx >= 0) loadingAlphas[loadingIdx].value else 0f
            } else {
                segmentAlphas[i].value
            }
            val fillColor = segmentColor.copy(alpha = alpha)
            val path = buildSegmentPath(i, w, h)
            drawPath(path, fillColor, style = Fill)
            drawPath(path, Color.Black, style = Stroke(width = 2f))
        }
    }
}

private fun buildSegmentPath(index: Int, w: Float, h: Float): Path {
    return when (index) {
        1 -> Path().apply {
            moveTo(0.33333f * w, 0.25f * h)
            lineTo(0.33333f * w, 0f)
            cubicTo(0f, 0.02335f * h, 0f, 0.25f * h, 0f, 0.25f * h)
            lineTo(0.33333f * w, 0.25f * h)
            close()
        }
        2 -> Path().apply {
            moveTo(0.33333f * w, 0f)
            lineTo(0f, 0f)
            lineTo(0f, 0.25f * h)
            cubicTo(0f, 0.25f * h, 0f, 0.02335f * h, 0.33333f * w, 0f)
            close()
        }
        3 -> Path().apply {
            moveTo(0.33333f * w, 0f)
            lineTo(0.33333f * w, 0.25f * h)
            lineTo(0.5f * w, 0.25f * h)
            lineTo(0.33333f * w, 0f)
            close()
        }
        4 -> Path().apply {
            moveTo(0.66667f * w, 0f)
            lineTo(0.33333f * w, 0f)
            lineTo(0.5f * w, 0.25f * h)
            lineTo(0.66667f * w, 0f)
            close()
        }
        5 -> Path().apply {
            moveTo(0.66667f * w, 0.25f * h)
            lineTo(0.66667f * w, 0f)
            lineTo(0.5f * w, 0.25f * h)
            lineTo(0.66667f * w, 0.25f * h)
            close()
        }
        6 -> Path().apply {
            moveTo(0.66667f * w, 0f)
            lineTo(0.66667f * w, 0.25f * h)
            lineTo(w, 0.25f * h)
            cubicTo(w, 0.25f * h, w, 0.02335f * h, 0.66667f * w, 0f)
            close()
        }
        7 -> Path().apply {
            moveTo(w, 0f)
            lineTo(0.66667f * w, 0f)
            cubicTo(w, 0.02335f * h, w, 0.25f * h, w, 0.25f * h)
            lineTo(w, 0f)
            close()
        }
        8 -> Path().apply {
            moveTo(w, 0.25f * h)
            lineTo(0.66667f * w, 0.25f * h)
            lineTo(0.66667f * w, 0.375f * h)
            cubicTo(0.75319f * w, 0.37822f * h, 0.81752f * w, 0.40518f * h, 0.86498f * w, 0.44331f * h)
            cubicTo(w, 0.36798f * h, w, 0.25f * h, w, 0.25f * h)
            close()
        }
        9 -> Path().apply {
            moveTo(w, 0.25f * h)
            cubicTo(w, 0.25f * h, w, 0.38948f * h, 0.91787f * w, 0.49849f * h)
            cubicTo(0.90333f * w, 0.47867f * h, 0.88577f * w, 0.45993f * h, 0.86498f * w, 0.44331f * h)
            cubicTo(w, 0.36798f * h, w, 0.25f * h, w, 0.25f * h)
            close()
        }
        10 -> Path().apply {
            moveTo(0.66667f * w, 0.375f * h)
            lineTo(0.66667f * w, 0.5f * h)
            cubicTo(0.75319f * w, 0.49106f * h, 0.81752f * w, 0.46983f * h, 0.86498f * w, 0.44331f * h)
            cubicTo(0.81752f * w, 0.40518f * h, 0.75319f * w, 0.37822f * h, 0.66667f * w, 0.375f * h)
            close()
        }
        11 -> Path().apply {
            moveTo(0.86498f * w, 0.44331f * h)
            cubicTo(0.81752f * w, 0.46983f * h, 0.75319f * w, 0.49106f * h, 0.66667f * w, 0.5f * h)
            cubicTo(0.75398f * w, 0.50613f * h, 0.81859f * w, 0.5263f * h, 0.86627f * w, 0.55271f * h)
            cubicTo(0.88627f * w, 0.53623f * h, 0.90365f * w, 0.51784f * h, 0.91787f * w, 0.49849f * h)
            cubicTo(0.90333f * w, 0.47867f * h, 0.88577f * w, 0.45993f * h, 0.86498f * w, 0.44331f * h)
            close()
        }
        12 -> Path().apply {
            moveTo(w, 0.75f * h)
            cubicTo(w, 0.75f * h, w, 0.6268f * h, 0.86627f * w, 0.55271f * h)
            cubicTo(0.88627f * w, 0.53623f * h, 0.90365f * w, 0.51784f * h, 0.91787f * w, 0.49849f * h)
            cubicTo(w, 0.60783f * h, w, 0.75f * h, w, 0.75f * h)
            close()
        }
        13 -> Path().apply {
            moveTo(w, 0.75f * h)
            lineTo(w, 0.25f * h)
            cubicTo(w, 0.25f * h, w, 0.38948f * h, 0.91787f * w, 0.49849f * h)
            cubicTo(w, 0.60783f * h, w, 0.75f * h, w, 0.75f * h)
            close()
        }
        14 -> Path().apply {
            moveTo(0.66667f * w, 0.5f * h)
            lineTo(0.66667f * w, 0.625f * h)
            cubicTo(0.75398f * w, 0.61995f * h, 0.81859f * w, 0.59167f * h, 0.86627f * w, 0.55271f * h)
            cubicTo(0.81859f * w, 0.5263f * h, 0.75398f * w, 0.50613f * h, 0.66667f * w, 0.5f * h)
            close()
        }
        15 -> Path().apply {
            moveTo(0.66667f * w, 0.75f * h)
            lineTo(w, 0.75f * h)
            cubicTo(w, 0.75f * h, w, 0.6268f * h, 0.86627f * w, 0.55271f * h)
            cubicTo(0.81859f * w, 0.59167f * h, 0.75398f * w, 0.61995f * h, 0.66667f * w, 0.625f * h)
            lineTo(0.66667f * w, 0.75f * h)
            close()
        }
        16 -> Path().apply {
            moveTo(0.33333f * w, 0.25f * h)
            lineTo(0f, 0.25f * h)
            cubicTo(0f, 0.25f * h, 0f, 0.36798f * h, 0.13484f * w, 0.44331f * h)
            cubicTo(0.18235f * w, 0.40518f * h, 0.24648f * w, 0.37822f * h, 0.33333f * w, 0.375f * h)
            lineTo(0.33333f * w, 0.25f * h)
            close()
        }
        17 -> Path().apply {
            moveTo(0.33333f * w, 0.5f * h)
            lineTo(0.33333f * w, 0.375f * h)
            cubicTo(0.24648f * w, 0.37822f * h, 0.18235f * w, 0.40518f * h, 0.13484f * w, 0.44331f * h)
            cubicTo(0.18235f * w, 0.46983f * h, 0.24648f * w, 0.49106f * h, 0.33333f * w, 0.5f * h)
            close()
        }
        18 -> Path().apply {
            moveTo(0.33333f * w, 0.5f * h)
            cubicTo(0.24648f * w, 0.49106f * h, 0.18235f * w, 0.46983f * h, 0.13484f * w, 0.44331f * h)
            cubicTo(0.11418f * w, 0.45993f * h, 0.09663f * w, 0.47867f * h, 0.08179f * w, 0.49849f * h)
            cubicTo(0.09635f * w, 0.51784f * h, 0.11343f * w, 0.53623f * h, 0.13355f * w, 0.55271f * h)
            cubicTo(0.18117f * w, 0.5263f * h, 0.24571f * w, 0.50613f * h, 0.33333f * w, 0.5f * h)
            close()
        }
        19 -> Path().apply {
            moveTo(0.13484f * w, 0.44331f * h)
            cubicTo(0f, 0.36798f * h, 0f, 0.25f * h, 0f, 0.25f * h)
            cubicTo(0f, 0.25f * h, 0f, 0.38948f * h, 0.08179f * w, 0.49849f * h)
            cubicTo(0.09663f * w, 0.47867f * h, 0.11418f * w, 0.45993f * h, 0.13484f * w, 0.44331f * h)
            close()
        }
        20 -> Path().apply {
            moveTo(0.13355f * w, 0.55271f * h)
            cubicTo(0.11343f * w, 0.53623f * h, 0.09635f * w, 0.51784f * h, 0.08179f * w, 0.49849f * h)
            cubicTo(0.05141f * w, 0.53912f * h, 0.03232f * w, 0.58428f * h, 0.02031f * w, 0.625f * h)
            lineTo(0.04807f * w, 0.625f * h)
            cubicTo(0.06759f * w, 0.59958f * h, 0.09501f * w, 0.57409f * h, 0.13355f * w, 0.55271f * h)
            close()
        }
        21 -> Path().apply {
            moveTo(0f, 0.25f * h)
            lineTo(0f, 0.625f * h)
            lineTo(0.02031f * w, 0.625f * h)
            cubicTo(0.03232f * w, 0.58428f * h, 0.05141f * w, 0.53912f * h, 0.08179f * w, 0.49849f * h)
            cubicTo(0f, 0.38948f * h, 0f, 0.25f * h, 0f, 0.25f * h)
            close()
        }
        22 -> Path().apply {
            moveTo(0f, 0.625f * h)
            lineTo(0f, 0.75f * h)
            cubicTo(0f, 0.75f * h, 0f, 0.69386f * h, 0.02031f * w, 0.625f * h)
            lineTo(0f, 0.625f * h)
            close()
        }
        23 -> Path().apply {
            moveTo(0.02031f * w, 0.625f * h)
            cubicTo(0f, 0.69386f * h, 0f, 0.75f * h, 0f, 0.75f * h)
            cubicTo(0f, 0.75f * h, 0f, 0.68766f * h, 0.04807f * w, 0.625f * h)
            lineTo(0.02031f * w, 0.625f * h)
            close()
        }
        24 -> Path().apply {
            moveTo(0f, 0.75f * h)
            lineTo(0.33333f * w, 0.75f * h)
            lineTo(0.33333f * w, 0.625f * h)
            lineTo(0.04807f * w, 0.625f * h)
            cubicTo(0f, 0.68766f * h, 0f, 0.75f * h, 0f, 0.75f * h)
            close()
        }
        25 -> Path().apply {
            moveTo(0.66667f * w, 0.75f * h)
            lineTo(0.5f * w, 0.75f * h)
            lineTo(0.62473f * w, 0.9375f * h)
            lineTo(0.66667f * w, 0.91667f * h)
            lineTo(0.66667f * w, 0.75f * h)
            close()
        }
        26 -> Path().apply {
            moveTo(0.37527f * w, 0.9375f * h)
            lineTo(0.5f * w, h)
            lineTo(0.62473f * w, 0.9375f * h)
            lineTo(0.5f * w, 0.75f * h)
            lineTo(0.37527f * w, 0.9375f * h)
            close()
        }
        27 -> Path().apply {
            moveTo(0.5f * w, 0.75f * h)
            lineTo(0.33333f * w, 0.75f * h)
            lineTo(0.33333f * w, 0.91667f * h)
            lineTo(0.37527f * w, 0.9375f * h)
            lineTo(0.5f * w, 0.75f * h)
            close()
        }
        28 -> Path().apply {
            moveTo(0.33333f * w, 0.75f * h)
            lineTo(0f, 0.75f * h)
            lineTo(0.33333f * w, 0.91667f * h)
            lineTo(0.33333f * w, 0.75f * h)
            close()
        }
        29 -> Path().apply {
            moveTo(0.33333f * w, 0.91667f * h)
            lineTo(0f, 0.75f * h)
            cubicTo(0f, 0.75f * h, 0f, 0.97665f * h, 0.33333f * w, h)
            lineTo(0.33333f * w, 0.91667f * h)
            close()
        }
        30 -> Path().apply {
            moveTo(0f, h)
            lineTo(0.33333f * w, h)
            cubicTo(0f, 0.97665f * h, 0f, 0.75f * h, 0f, 0.75f * h)
            lineTo(0f, h)
            close()
        }
        31 -> Path().apply {
            moveTo(0.33333f * w, 0.91667f * h)
            lineTo(0.33333f * w, h)
            lineTo(0.37527f * w, 0.9375f * h)
            lineTo(0.33333f * w, 0.91667f * h)
            close()
        }
        32 -> Path().apply {
            moveTo(0.33333f * w, h)
            lineTo(0.5f * w, h)
            lineTo(0.37527f * w, 0.9375f * h)
            lineTo(0.33333f * w, h)
            close()
        }
        33 -> Path().apply {
            moveTo(0.5f * w, h)
            lineTo(0.66667f * w, h)
            lineTo(0.62473f * w, 0.9375f * h)
            lineTo(0.5f * w, h)
            close()
        }
        34 -> Path().apply {
            moveTo(0.62473f * w, 0.9375f * h)
            lineTo(0.66667f * w, h)
            lineTo(0.66667f * w, 0.91667f * h)
            lineTo(0.62473f * w, 0.9375f * h)
            close()
        }
        35 -> Path().apply {
            moveTo(w, 0.75f * h)
            lineTo(0.66667f * w, 0.91667f * h)
            lineTo(0.66667f * w, h)
            cubicTo(w, 0.97665f * h, w, 0.75f * h, w, 0.75f * h)
            close()
        }
        36 -> Path().apply {
            moveTo(0.66667f * w, h)
            lineTo(w, h)
            lineTo(w, 0.75f * h)
            cubicTo(w, 0.75f * h, w, 0.97665f * h, 0.66667f * w, h)
            close()
        }
        37 -> Path().apply {
            moveTo(0.33333f * w, 0.5f * h)
            lineTo(0.5f * w, 0.375f * h)
            lineTo(0.33333f * w, 0.375f * h)
            lineTo(0.33333f * w, 0.5f * h)
            close()
        }
        38 -> Path().apply {
            moveTo(0.33333f * w, 0.5f * h)
            lineTo(0.5f * w, 0.5f * h)
            lineTo(0.5f * w, 0.375f * h)
            lineTo(0.33333f * w, 0.5f * h)
            close()
        }
        39 -> Path().apply {
            moveTo(0.5f * w, 0.5f * h)
            lineTo(0.66667f * w, 0.5f * h)
            lineTo(0.5f * w, 0.375f * h)
            lineTo(0.5f * w, 0.5f * h)
            close()
        }
        40 -> Path().apply {
            moveTo(0.5f * w, 0.375f * h)
            lineTo(0.66667f * w, 0.5f * h)
            lineTo(0.66667f * w, 0.375f * h)
            lineTo(0.5f * w, 0.375f * h)
            close()
        }
        41 -> Path().apply {
            moveTo(0.5f * w, 0.375f * h)
            lineTo(0.33333f * w, 0.25f * h)
            lineTo(0.33333f * w, 0.375f * h)
            lineTo(0.5f * w, 0.375f * h)
            close()
        }
        42 -> Path().apply {
            moveTo(0.33333f * w, 0.25f * h)
            lineTo(0.5f * w, 0.375f * h)
            lineTo(0.58323f * w, 0.3125f * h)
            lineTo(0.5f * w, 0.25f * h)
            lineTo(0.33333f * w, 0.25f * h)
            close()
        }
        43 -> Path().apply {
            moveTo(0.5f * w, 0.375f * h)
            lineTo(0.66667f * w, 0.375f * h)
            lineTo(0.58323f * w, 0.3125f * h)
            lineTo(0.5f * w, 0.375f * h)
            close()
        }
        44 -> Path().apply {
            moveTo(0.66667f * w, 0.25f * h)
            lineTo(0.5f * w, 0.25f * h)
            lineTo(0.58323f * w, 0.3125f * h)
            lineTo(0.66667f * w, 0.25f * h)
            close()
        }
        45 -> Path().apply {
            moveTo(0.66667f * w, 0.375f * h)
            lineTo(0.66667f * w, 0.25f * h)
            lineTo(0.58323f * w, 0.3125f * h)
            lineTo(0.66667f * w, 0.375f * h)
            close()
        }
        46 -> Path().apply {
            moveTo(0.66667f * w, 0.5f * h)
            lineTo(0.5f * w, 0.625f * h)
            lineTo(0.66667f * w, 0.625f * h)
            lineTo(0.66667f * w, 0.5f * h)
            close()
        }
        47 -> Path().apply {
            moveTo(0.5f * w, 0.625f * h)
            lineTo(0.66667f * w, 0.75f * h)
            lineTo(0.66667f * w, 0.625f * h)
            lineTo(0.5f * w, 0.625f * h)
            close()
        }
        48 -> Path().apply {
            moveTo(0.66667f * w, 0.75f * h)
            lineTo(0.5f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.75f * h)
            lineTo(0.5f * w, 0.75f * h)
            lineTo(0.66667f * w, 0.75f * h)
            close()
        }
        49 -> Path().apply {
            moveTo(0.33333f * w, 0.75f * h)
            lineTo(0.5f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.75f * h)
            close()
        }
        50 -> Path().apply {
            moveTo(0.5f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.5f * h)
            lineTo(0.33333f * w, 0.625f * h)
            lineTo(0.5f * w, 0.625f * h)
            close()
        }
        51 -> Path().apply {
            moveTo(0.33333f * w, 0.5f * h)
            lineTo(0.5f * w, 0.625f * h)
            lineTo(0.5f * w, 0.5f * h)
            lineTo(0.33333f * w, 0.5f * h)
            close()
        }
        52 -> Path().apply {
            moveTo(0.33333f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.5f * h)
            cubicTo(0.24571f * w, 0.50613f * h, 0.18117f * w, 0.5263f * h, 0.13355f * w, 0.55271f * h)
            cubicTo(0.18117f * w, 0.59167f * h, 0.24571f * w, 0.61995f * h, 0.33333f * w, 0.625f * h)
            close()
        }
        53 -> Path().apply {
            moveTo(0.33333f * w, 0.625f * h)
            cubicTo(0.24571f * w, 0.61995f * h, 0.18117f * w, 0.59167f * h, 0.13355f * w, 0.55271f * h)
            cubicTo(0.09501f * w, 0.57409f * h, 0.06759f * w, 0.59958f * h, 0.04807f * w, 0.625f * h)
            lineTo(0.33333f * w, 0.625f * h)
            close()
        }
        54 -> Path().apply {
            moveTo(w, 0.75f * h)
            lineTo(0.66667f * w, 0.75f * h)
            lineTo(0.66667f * w, 0.91667f * h)
            lineTo(w, 0.75f * h)
            close()
        }
        55 -> Path().apply {
            moveTo(0.5f * w, 0.625f * h)
            lineTo(0.66667f * w, 0.5f * h)
            lineTo(0.5f * w, 0.5f * h)
            lineTo(0.5f * w, 0.625f * h)
            close()
        }
        else -> Path()
    }
}
