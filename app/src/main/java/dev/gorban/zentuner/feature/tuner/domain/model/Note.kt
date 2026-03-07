package dev.gorban.zentuner.feature.tuner.domain.model

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

data class Note(
    val name: String,
    val octave: Int,
    val frequency: Double,
    val centsOffset: Double
) {
    val isInTune: Boolean get() = abs(centsOffset) <= 10.0
    val isSharp: Boolean get() = centsOffset > 0
    val isFlat: Boolean get() = centsOffset < 0
    val isSharpSymbol: Boolean get() = name.contains("#")
    val nameSymbol: String get() = name.replace("#", "")
    val symbolSegment: SymbolSegment
        get() = when (nameSymbol) {
            "C" -> SymbolSegment.C
            "D" -> SymbolSegment.D
            "E" -> SymbolSegment.E
            "F" -> SymbolSegment.F
            "G" -> SymbolSegment.G
            "A" -> SymbolSegment.A
            "B" -> SymbolSegment.B
            else -> SymbolSegment.NONE
        }

    companion object {
        val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        const val A4_FREQUENCY = 440.0
        const val A4_INDEX = 48
        const val MIN_FREQUENCY = 32.70
        const val MAX_FREQUENCY = 4186.01

        fun from(frequency: Double): Note? {
            if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) return null

            val semitones = 12.0 * log2(frequency / A4_FREQUENCY)
            val roundedSemitones = round(semitones)
            val centsOffset = (semitones - roundedSemitones) * 100.0

            val noteIndex = roundedSemitones.toInt() + A4_INDEX
            if (noteIndex < 0) return null

            // Shift so C0 = 0 (A0 is at index 0, C1 is at index 3)
            val adjustedIndex = noteIndex + 9
            val octave = adjustedIndex / 12
            val noteNameIndex = ((adjustedIndex % 12) + 12) % 12

            if (noteNameIndex >= noteNames.size) return null

            val exactFrequency = A4_FREQUENCY * 2.0.pow(roundedSemitones / 12.0)

            return Note(
                name = noteNames[noteNameIndex],
                octave = octave,
                frequency = exactFrequency,
                centsOffset = centsOffset
            )
        }
    }
}
