package dev.gorban.zentuner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.gorban.zentuner.feature.tuner.presentation.screen.TunerScreen
import dev.gorban.zentuner.ui.theme.ZenTunerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenTunerTheme {
                TunerScreen()
            }
        }
    }
}
