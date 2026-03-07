package dev.gorban.zentuner.feature.tuner.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dev.gorban.zentuner.core.BaseSharedViewModel
import dev.gorban.zentuner.feature.tuner.domain.usecase.ObservePitchUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StartTunerUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StopTunerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TunerViewModel(
    private val observePitchUseCase: ObservePitchUseCase,
    private val startTunerUseCase: StartTunerUseCase,
    private val stopTunerUseCase: StopTunerUseCase
) : BaseSharedViewModel<TunerViewState, TunerViewAction, TunerViewEvent>(
    initialState = TunerViewState()
) {

    private var pitchJob: Job? = null
    private var pendingStart = false

    override fun obtainEvent(viewEvent: TunerViewEvent) {
        when (viewEvent) {
            is TunerViewEvent.ToggleListening -> toggleListening()
            is TunerViewEvent.UpdateThreshold -> updateThreshold(viewEvent.threshold)
            is TunerViewEvent.PermissionGranted -> onPermissionGranted()
            is TunerViewEvent.PermissionDenied -> onPermissionDenied()
            is TunerViewEvent.SettingsDialogDismissed -> onSettingsDialogDismissed()
            is TunerViewEvent.OpenedSettings -> onOpenedSettings()
            is TunerViewEvent.PermissionRecheck -> onPermissionRecheck(viewEvent.hasPermission)
        }
    }

    private fun toggleListening() {
        if (viewState.isListening) {
            stopListening()
            return
        }

        if (!viewState.hasPermission) {
            if (viewState.permissionDenialCount >= 2) {
                viewState = viewState.copy(showSettingsDialog = true)
            } else {
                pendingStart = true
                viewAction = TunerViewAction.RequestPermission()
            }
            return
        }

        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            startTunerUseCase.execute(Unit).onSuccess {
                viewState = viewState.copy(isListening = true)

                pitchJob = viewModelScope.launch {
                    observePitchUseCase.execute { viewState.amplitudeThreshold }
                        .onSuccess { flow ->
                            flow.collect { pitchResult ->
                                viewState = viewState.copy(
                                    detectedNote = pitchResult.detectedNote,
                                    amplitude = pitchResult.amplitude
                                )
                            }
                        }
                }
            }
        }
    }

    private fun stopListening() {
        pitchJob?.cancel()
        pitchJob = null
        viewModelScope.launch {
            stopTunerUseCase.execute(Unit)
        }
        viewState = viewState.copy(isListening = false, detectedNote = null, amplitude = 0.0)
    }

    private fun updateThreshold(threshold: Double) {
        viewState = viewState.copy(amplitudeThreshold = threshold)
    }

    private fun onPermissionGranted() {
        viewState = viewState.copy(hasPermission = true, permissionDenialCount = 0)
        if (pendingStart) {
            pendingStart = false
            startListening()
        }
    }

    private fun onPermissionDenied() {
        val newCount = viewState.permissionDenialCount + 1
        viewState = viewState.copy(
            hasPermission = false,
            isListening = false,
            permissionDenialCount = newCount
        )
        pendingStart = false
        pitchJob?.cancel()
        pitchJob = null
        viewModelScope.launch {
            stopTunerUseCase.execute(Unit)
        }
    }

    private fun onSettingsDialogDismissed() {
        viewState = viewState.copy(showSettingsDialog = false)
    }

    private fun onOpenedSettings() {
        viewState = viewState.copy(sentToSettings = true)
    }

    private fun onPermissionRecheck(hasPermission: Boolean) {
        if (hasPermission) {
            viewState = viewState.copy(hasPermission = true, permissionDenialCount = 0, sentToSettings = false)
        } else if (viewState.sentToSettings) {
            viewState = viewState.copy(permissionDenialCount = 0, sentToSettings = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pitchJob?.cancel()
        viewModelScope.launch {
            stopTunerUseCase.execute(Unit)
        }
    }
}
