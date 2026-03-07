package dev.gorban.zentuner.feature.tuner.domain.usecase

import dev.gorban.zentuner.core.UseCase
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import dev.gorban.zentuner.feature.tuner.domain.model.PitchResult
import kotlinx.coroutines.flow.Flow

class ObservePitchUseCase(
    private val repository: ITunerRepository
) : UseCase<() -> Double, Result<Flow<PitchResult>>> {

    override suspend fun execute(params: () -> Double): Result<Flow<PitchResult>> =
        try {
            Result.success(repository.observePitch(repository.sampleRate, params))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
