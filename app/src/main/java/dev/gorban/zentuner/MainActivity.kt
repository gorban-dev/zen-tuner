package dev.gorban.zentuner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.gorban.zentuner.feature.tuner.presentation.screen.TunerScreen
import dev.gorban.zentuner.ui.theme.ZenTunerTheme
import dev.gorban.zentuner.util.InAppReviewDelegate
import dev.gorban.zentuner.util.InAppUpdateDelegate

class MainActivity : ComponentActivity() {

    private val inAppUpdateDelegate = InAppUpdateDelegate(this)
    private val inAppReviewDelegate = InAppReviewDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(inAppUpdateDelegate)
        lifecycle.addObserver(inAppReviewDelegate)
        enableEdgeToEdge()
        setContent {
            ZenTunerTheme {
                TunerScreen()
            }
        }
    }
}
