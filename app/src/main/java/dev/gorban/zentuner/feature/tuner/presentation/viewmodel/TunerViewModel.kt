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

    override fun obtainEvent(viewEvent: TunerViewEvent) {
        when (viewEvent) {
            is TunerViewEvent.ToggleListening -> toggleListening()
            is TunerViewEvent.UpdateThreshold -> updateThreshold(viewEvent.threshold)
            is TunerViewEvent.PermissionGranted -> onPermissionGranted()
            is TunerViewEvent.PermissionDenied -> onPermissionDenied()
        }
    }

    private fun toggleListening() {
        if (!viewState.hasPermission) return

        if (viewState.isListening) {
            stopListening()
        } else {
            startListening()
        }
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
        viewState = viewState.copy(hasPermission = true)
    }

    private fun onPermissionDenied() {
        viewState = viewState.copy(hasPermission = false, isListening = false)
        pitchJob?.cancel()
        pitchJob = null
        viewModelScope.launch {
            stopTunerUseCase.execute(Unit)
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
