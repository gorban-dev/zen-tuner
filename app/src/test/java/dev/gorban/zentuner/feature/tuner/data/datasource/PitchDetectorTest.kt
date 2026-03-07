package dev.gorban.zentuner.feature.tuner.data.datasource

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class PitchDetectorTest {

    private val detector = PitchDetector()
    private val sampleRate = 44100

    private fun generateSineWave(frequency: Double, sampleRate: Int, samples: Int): FloatArray {
        return FloatArray(samples) { i ->
            sin(2.0 * PI * frequency * i / sampleRate).toFloat()
        }
    }

    @Test
    fun `detect A4 440Hz sine wave`() {
        val samples = generateSineWave(440.0, sampleRate, 4096)
        val detected = detector.detect(samples, sampleRate)
        assertNotNull(detected)
        assertEquals(440.0, detected!!, 2.0)
    }

    @Test
    fun `detect E2 82Hz sine wave`() {
        val samples = generateSineWave(82.41, sampleRate, 4096)
        val detected = detector.detect(samples, sampleRate)
        assertNotNull(detected)
        assertEquals(82.41, detected!!, 2.0)
    }

    @Test
    fun `detect D3 147Hz sine wave`() {
        val samples = generateSineWave(146.83, sampleRate, 4096)
        val detected = detector.detect(samples, sampleRate)
        assertNotNull(detected)
        assertEquals(146.83, detected!!, 2.0)
    }

    @Test
    fun `detect returns null for empty samples`() {
        assertNull(detector.detect(FloatArray(0), sampleRate))
    }

    @Test
    fun `detect returns null for silence`() {
        val silence = FloatArray(4096) { 0f }
        assertNull(detector.detect(silence, sampleRate))
    }

    @Test
    fun `detect returns null for very short buffer`() {
        val samples = generateSineWave(440.0, sampleRate, 10)
        assertNull(detector.detect(samples, sampleRate))
    }
}
