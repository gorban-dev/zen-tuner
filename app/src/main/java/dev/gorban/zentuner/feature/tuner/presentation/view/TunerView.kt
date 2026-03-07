package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gorban.zentuner.feature.tuner.domain.model.SymbolSegment
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewEvent
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewState
import androidx.compose.ui.tooling.preview.Preview
import dev.gorban.zentuner.feature.tuner.domain.model.Note
import dev.gorban.zentuner.ui.theme.ZenTunerTheme

@Composable
fun TunerView(
    viewState: TunerViewState,
    eventHandler: (TunerViewEvent) -> Unit
) {
    val density = LocalDensity.current
    val screenWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val tunerSize = screenWidthDp - 40.dp

    val note = viewState.detectedNote
    val symbol = note?.symbolSegment ?: SymbolSegment.NONE
    val isInTune = note?.isInTune == true
    val isSharpSymbol = note?.isSharpSymbol == true

    val symbolAlpha by animateFloatAsState(
        targetValue = if (viewState.isListening) 1f else 0f,
        animationSpec = tween(100),
        label = "symbol_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            ArcTunerView(
                note = note,
                tunerSize = tunerSize,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = tunerSize * 0.35f)
                    .alpha(symbolAlpha),
                contentAlignment = Alignment.Center
            ) {
                SegmentedSymbol(
                    symbol = symbol,
                    color = Color.White,
                    isInTune = isInTune,
                    symbolWidth = 180.dp,
                    symbolHeight = 240.dp
                )

                if (isSharpSymbol) {
                    Text(
                        text = "#",
                        style = TextStyle(
                            color = if (isInTune) Color(0xFF4CAF50) else Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        modifier = Modifier.padding(start = 240.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MicGainKnob(
                threshold = viewState.amplitudeThreshold,
                amplitude = viewState.amplitude,
                isListening = viewState.isListening,
                onThresholdChange = { threshold ->
                    eventHandler(TunerViewEvent.UpdateThreshold(threshold))
                }
            )

            Spacer(modifier = Modifier.width(64.dp))

            PowerButton(
                isListening = viewState.isListening,
                onClick = {
                    eventHandler(TunerViewEvent.ToggleListening)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TunerViewPreviewLight() {
    ZenTunerTheme {
        TunerView(
            viewState = TunerViewState(
                isListening = true,
                detectedNote = Note.from(440.0),
                amplitude = 0.05
            ),
            eventHandler = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TunerViewPreviewDark() {
    ZenTunerTheme {
        TunerView(
            viewState = TunerViewState(
                isListening = true,
                detectedNote = Note.from(440.0),
                amplitude = 0.05
            ),
            eventHandler = {}
        )
    }
}
