package dev.gorban.zentuner.feature.tuner.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.gorban.zentuner.feature.tuner.presentation.view.TunerView
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewEvent
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TunerScreen() {
    val viewModel: TunerViewModel = koinViewModel()
    val viewState by viewModel.viewStates.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        } else {
            viewModel.obtainEvent(TunerViewEvent.PermissionDenied)
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    TunerView(
        viewState = viewState,
        eventHandler = viewModel::obtainEvent
    )
}
