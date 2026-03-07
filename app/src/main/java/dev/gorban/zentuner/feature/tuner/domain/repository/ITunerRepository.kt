package dev.gorban.zentuner.feature.tuner.domain.repository

import dev.gorban.zentuner.feature.tuner.domain.model.PitchResult
import kotlinx.coroutines.flow.Flow

interface ITunerRepository {
    val sampleRate: Int
    fun observePitch(sampleRate: Int, threshold: () -> Double): Flow<PitchResult>
    fun start()
    fun stop()
}
