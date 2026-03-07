package dev.gorban.zentuner.feature.tuner.presentation.viewmodel

sealed class TunerViewAction {
    class RequestPermission : TunerViewAction()
    class ShowSettingsDialog : TunerViewAction()
}
