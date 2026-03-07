package dev.gorban.zentuner.feature.tuner.presentation.viewmodel

sealed class TunerViewEvent {
    object ToggleListening : TunerViewEvent()
    data class UpdateThreshold(val threshold: Double) : TunerViewEvent()
    object PermissionGranted : TunerViewEvent()
    object PermissionDenied : TunerViewEvent()
}
