package dev.gorban.zentuner.feature.tuner.presentation.view

import android.widget.Space
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.gorban.zentuner.R
import dev.gorban.zentuner.ui.theme.ZenTunerTheme
import kotlinx.coroutines.launch

@Composable
fun PowerButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val iconColor = if (isListening) Color(0xFF4CAF50) else Color.Black

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale.value)
            .clip(CircleShape)
            .background(Color.White)
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
                            scale.animateTo(
                                1f,
                                spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_power_settings_new_24),
            contentDescription = "Power",
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PowerButtonOn_Preview() {
    ZenTunerTheme {
        Column(
            modifier = Modifier.size(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PowerButton(isListening = true, onClick = {})

            Spacer(modifier = Modifier.size(16.dp))

            PowerButton(isListening = false, onClick = {})
        }
    }
}
