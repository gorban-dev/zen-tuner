package dev.gorban.zentuner.feature.tuner.domain.usecase

import dev.gorban.zentuner.feature.tuner.domain.model.Note
import dev.gorban.zentuner.feature.tuner.domain.model.PitchResult
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservePitchUseCaseTest {

    private val repository = mockk<ITunerRepository>()
    private val useCase = ObservePitchUseCase(repository)

    @Test
    fun `execute returns success with flow`() = runTest {
        val pitchResult = PitchResult(detectedNote = Note.from(440.0), amplitude = 0.05)
        every { repository.sampleRate } returns 44100
        every { repository.observePitch(44100, any()) } returns flowOf(pitchResult)

        val result = useCase.execute { 0.003 }

        assertTrue(result.isSuccess)
        val flow = result.getOrNull()
        assertNotNull(flow)
        assertEquals(pitchResult, flow!!.first())
    }

    @Test
    fun `execute returns failure when repository throws`() = runTest {
        every { repository.sampleRate } returns 44100
        every { repository.observePitch(any(), any()) } throws RuntimeException("Error")

        val result = useCase.execute { 0.003 }

        assertTrue(result.isFailure)
    }
}
