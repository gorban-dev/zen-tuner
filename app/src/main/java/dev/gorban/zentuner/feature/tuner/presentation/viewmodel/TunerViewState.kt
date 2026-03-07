package dev.gorban.zentuner.feature.tuner.presentation.viewmodel

import dev.gorban.zentuner.feature.tuner.domain.model.Note

data class TunerViewState(
    val detectedNote: Note? = null,
    val amplitude: Double = 0.0,
    val isListening: Boolean = false,
    val amplitudeThreshold: Double = 0.003,
    val hasPermission: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val permissionDenialCount: Int = 0,
    val sentToSettings: Boolean = false
)
