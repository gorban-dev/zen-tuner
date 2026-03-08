package dev.gorban.zentuner.feature.tuner.data.datasource

class PitchDetector {

    private val minFrequency = 60.0
    private val maxFrequency = 1500.0

    fun detect(samples: FloatArray, sampleRate: Int): Double? {
        if (samples.isEmpty()) return null

        val count = samples.size
        val minLag = (sampleRate / maxFrequency).toInt().coerceAtLeast(1)
        val maxLag = (sampleRate / minFrequency).toInt().coerceAtMost(count / 2)

        if (minLag >= maxLag) return null

        val acSize = maxLag - minLag
        val autocorrelation = FloatArray(acSize)
        for (lag in minLag until maxLag) {
            var sum = 0.0f
            val length = count - lag
            for (i in 0 until length) {
                sum += samples[i] * samples[i + lag]
            }
            autocorrelation[lag - minLag] = sum
        }

        // Find global maximum value for threshold calculation
        var maxValue = 0f
        for (v in autocorrelation) {
            if (v > maxValue) maxValue = v
        }
        if (maxValue <= 0f) return null

        // Find the first significant peak above threshold (standard pitch detection approach)
        // This avoids octave errors by preferring the fundamental frequency
        val threshold = maxValue * 0.8f
        var peakIndex = -1
        var peakValue = 0f
        for (i in 1 until acSize - 1) {
            if (autocorrelation[i] > autocorrelation[i - 1] &&
                autocorrelation[i] > autocorrelation[i + 1] &&
                autocorrelation[i] > threshold
            ) {
                peakIndex = i
                peakValue = autocorrelation[i]
                break
            }
        }

        if (peakIndex < 0) return null

        val lag = peakIndex + minLag
        val refinedLag = if (peakIndex > 0 && peakIndex < acSize - 1) {
            val y0 = autocorrelation[peakIndex - 1].toDouble()
            val y1 = autocorrelation[peakIndex].toDouble()
            val y2 = autocorrelation[peakIndex + 1].toDouble()
            val denominator = 2.0 * (2.0 * y1 - y0 - y2)
            if (kotlin.math.abs(denominator) > 0.0001) {
                lag + (y0 - y2) / denominator
            } else {
                lag.toDouble()
            }
        } else {
            lag.toDouble()
        }

        val frequency = sampleRate / refinedLag
        if (frequency !in 32.70..4186.01) return null

        return frequency
    }
}
