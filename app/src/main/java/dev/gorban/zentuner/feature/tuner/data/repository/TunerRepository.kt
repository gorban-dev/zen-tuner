package dev.gorban.zentuner.feature.tuner.data.repository

import dev.gorban.zentuner.feature.tuner.domain.model.Note
import dev.gorban.zentuner.feature.tuner.data.datasource.AudioRecorder
import dev.gorban.zentuner.feature.tuner.data.datasource.PitchDetector
import dev.gorban.zentuner.feature.tuner.domain.repository.ITunerRepository
import dev.gorban.zentuner.feature.tuner.domain.model.PitchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt

class TunerRepository(
    private val audioRecorder: AudioRecorder,
    private val pitchDetector: PitchDetector
) : ITunerRepository {

    override val sampleRate: Int get() = audioRecorder.sampleRate

    override fun start() {
        audioRecorder.start()
    }

    override fun stop() {
        audioRecorder.stop()
    }

    override fun observePitch(sampleRate: Int, threshold: () -> Double): Flow<PitchResult> =
        audioRecorder.audioFlow().map { samples ->
            val rms = sqrt(samples.fold(0.0) { acc, s -> acc + s * s } / samples.size)

            if (rms > threshold()) {
                val frequency = pitchDetector.detect(samples, sampleRate)
                val note = frequency?.let { Note.from(it) }
                PitchResult(detectedNote = note, amplitude = rms)
            } else {
                PitchResult(detectedNote = null, amplitude = rms)
            }
        }
}
