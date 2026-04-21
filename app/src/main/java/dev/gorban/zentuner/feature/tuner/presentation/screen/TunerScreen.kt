package dev.gorban.zentuner.feature.tuner.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gorban.zentuner.feature.tuner.presentation.view.PermissionSettingsDialog
import dev.gorban.zentuner.feature.tuner.presentation.view.TunerView
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewAction
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewEvent
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TunerScreen() {
    val viewModel: TunerViewModel = koinViewModel()
    val viewState by viewModel.viewStates().collectAsStateWithLifecycle()
    val viewAction by viewModel.viewActions().collectAsStateWithLifecycle(null)
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

    LifecycleResumeEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.obtainEvent(TunerViewEvent.PermissionRecheck(hasPermission))

        onPauseOrDispose {}
    }

    LaunchedEffect(viewAction) {
        when (viewAction) {
            is TunerViewAction.RequestPermission -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            is TunerViewAction.ShowSettingsDialog -> {}
            else -> Unit
        }
    }

    DisposableEffect(viewState.isListening) {
        val window = (context as? Activity)?.window
        if (viewState.isListening) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (viewState.showSettingsDialog) {
        PermissionSettingsDialog(
            onDismiss = {
                viewModel.obtainEvent(TunerViewEvent.SettingsDialogDismissed)
            },
            onGoToSettings = {
                viewModel.obtainEvent(TunerViewEvent.SettingsDialogDismissed)
                viewModel.obtainEvent(TunerViewEvent.OpenedSettings)
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        )
    }

    TunerView(
        viewState = viewState,
        eventHandler = viewModel::obtainEvent
    )
}
