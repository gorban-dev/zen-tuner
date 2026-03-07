package dev.gorban.zentuner.feature.tuner.domain.usecase

import dev.gorban.zentuner.core.UseCase
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository

class StopTunerUseCase(
    private val repository: ITunerRepository
) : UseCase<Unit, Result<Unit>> {

    override suspend fun execute(params: Unit): Result<Unit> =
        try {
            repository.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
