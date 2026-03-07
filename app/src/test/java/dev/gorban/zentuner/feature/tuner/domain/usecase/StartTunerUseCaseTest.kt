package dev.gorban.zentuner.feature.tuner.domain.usecase

import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class StartTunerUseCaseTest {

    private val repository = mockk<ITunerRepository>(relaxed = true)
    private val useCase = StartTunerUseCase(repository)

    @Test
    fun `execute calls repository start and returns success`() = runTest {
        val result = useCase.execute(Unit)

        assertTrue(result.isSuccess)
        verify { repository.start() }
    }

    @Test
    fun `execute returns failure when repository throws`() = runTest {
        every { repository.start() } throws RuntimeException("Mic error")

        val result = useCase.execute(Unit)

        assertTrue(result.isFailure)
    }
}
