package dev.gorban.zentuner.feature.tuner.di

import dev.gorban.zentuner.feature.tuner.data.datasource.AudioRecorder
import dev.gorban.zentuner.feature.tuner.data.datasource.PitchDetector
import dev.gorban.zentuner.feature.tuner.data.repository.TunerRepository
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import dev.gorban.zentuner.feature.tuner.domain.usecase.ObservePitchUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StartTunerUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StopTunerUseCase
import dev.gorban.zentuner.feature.tuner.presentation.viewmodel.TunerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tunerModule = module {
    single { AudioRecorder() }
    single { PitchDetector() }
    single<ITunerRepository> { TunerRepository(get(), get()) }
    factory { ObservePitchUseCase(get()) }
    factory { StartTunerUseCase(get()) }
    factory { StopTunerUseCase(get()) }
    viewModel { TunerViewModel(get(), get(), get()) }
}
