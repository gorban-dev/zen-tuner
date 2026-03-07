package dev.gorban.zentuner.feature.tuner.presentation.viewmodel

import dev.gorban.zentuner.feature.tuner.domain.model.Note
import dev.gorban.zentuner.feature.tuner.domain.model.PitchResult
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import dev.gorban.zentuner.feature.tuner.domain.usecase.ObservePitchUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StartTunerUseCase
import dev.gorban.zentuner.feature.tuner.domain.usecase.StopTunerUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TunerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ITunerRepository>(relaxed = true)
    private lateinit var viewModel: TunerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.sampleRate } returns 44100
        viewModel = TunerViewModel(
            ObservePitchUseCase(repository),
            StartTunerUseCase(repository),
            StopTunerUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.viewStates.value
        assertFalse(state.isListening)
        assertFalse(state.hasPermission)
        assertNull(state.detectedNote)
        assertEquals(0.0, state.amplitude, 0.001)
    }

    @Test
    fun `permission granted updates state`() {
        viewModel.obtainEvent(TunerViewEvent.PermissionGranted)

        assertTrue(viewModel.viewStates.value.hasPermission)
    }

    @Test
    fun `permission denied stops listening and updates state`() = runTest {
        viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        viewModel.obtainEvent(TunerViewEvent.PermissionDenied)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates.value.hasPermission)
        assertFalse(viewModel.viewStates.value.isListening)
    }

    @Test
    fun `toggle listening without permission does nothing`() = runTest {
        viewModel.obtainEvent(TunerViewEvent.ToggleListening)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates.value.isListening)
    }

    @Test
    fun `toggle listening with permission starts tuner`() = runTest {
        val pitchResult = PitchResult(detectedNote = Note.from(440.0), amplitude = 0.05)
        every { repository.observePitch(44100, any()) } returns flowOf(pitchResult)

        viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        viewModel.obtainEvent(TunerViewEvent.ToggleListening)
        advanceUntilIdle()

        assertTrue(viewModel.viewStates.value.isListening)
        assertEquals("A", viewModel.viewStates.value.detectedNote?.name)
        assertEquals(0.05, viewModel.viewStates.value.amplitude, 0.001)
        verify { repository.start() }
    }

    @Test
    fun `toggle listening twice stops tuner`() = runTest {
        every { repository.observePitch(44100, any()) } returns flowOf()

        viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        viewModel.obtainEvent(TunerViewEvent.ToggleListening)
        advanceUntilIdle()
        viewModel.obtainEvent(TunerViewEvent.ToggleListening)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates.value.isListening)
        assertNull(viewModel.viewStates.value.detectedNote)
        verify { repository.stop() }
    }

    @Test
    fun `update threshold changes state`() {
        viewModel.obtainEvent(TunerViewEvent.UpdateThreshold(0.05))

        assertEquals(0.05, viewModel.viewStates.value.amplitudeThreshold, 0.001)
    }

    @Test
    fun `start failure keeps listening false`() = runTest {
        every { repository.start() } throws RuntimeException("Mic error")

        viewModel.obtainEvent(TunerViewEvent.PermissionGranted)
        viewModel.obtainEvent(TunerViewEvent.ToggleListening)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates.value.isListening)
    }
}
