package dev.gorban.zentuner.feature.tuner.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.math.abs

class NoteTest {

    @Test
    fun `from A4 440Hz returns note A in octave 4`() {
        val note = Note.from(440.0)
        assertNotNull(note)
        assertEquals("A", note!!.name)
        assertEquals(4, note.octave)
        assertTrue(abs(note.centsOffset) < 1.0)
    }

    @Test
    fun `from E2 82_41Hz returns note E in octave 2`() {
        val note = Note.from(82.41)
        assertNotNull(note)
        assertEquals("E", note!!.name)
        assertEquals(2, note.octave)
    }

    @Test
    fun `from B3 246_94Hz returns note B in octave 3`() {
        val note = Note.from(246.94)
        assertNotNull(note)
        assertEquals("B", note!!.name)
        assertEquals(3, note.octave)
    }

    @Test
    fun `from C4 261_63Hz returns note C in octave 4`() {
        val note = Note.from(261.63)
        assertNotNull(note)
        assertEquals("C", note!!.name)
        assertEquals(4, note.octave)
    }

    @Test
    fun `from frequency below minimum returns null`() {
        assertNull(Note.from(20.0))
    }

    @Test
    fun `from frequency above maximum returns null`() {
        assertNull(Note.from(5000.0))
    }

    @Test
    fun `isInTune true when cents offset within 10`() {
        val note = Note.from(440.0)!!
        assertTrue(note.isInTune)
    }

    @Test
    fun `isInTune false when significantly detuned`() {
        val note = Note.from(450.0)!!
        assertFalse(note.isInTune)
    }

    @Test
    fun `isSharp true when frequency above target`() {
        val note = Note.from(442.0)!!
        assertTrue(note.isSharp)
        assertFalse(note.isFlat)
    }

    @Test
    fun `isFlat true when frequency below target`() {
        val note = Note.from(438.0)!!
        assertTrue(note.isFlat)
        assertFalse(note.isSharp)
    }

    @Test
    fun `sharp note has correct symbol properties`() {
        val note = Note.from(277.18)!! // C#4
        assertEquals("C#", note.name)
        assertTrue(note.isSharpSymbol)
        assertEquals("C", note.nameSymbol)
    }

    @Test
    fun `natural note has no sharp symbol`() {
        val note = Note.from(440.0)!! // A4
        assertFalse(note.isSharpSymbol)
        assertEquals("A", note.nameSymbol)
    }

    @Test
    fun `symbolSegment maps correctly for all notes`() {
        assertEquals(SymbolSegment.A, Note.from(440.0)!!.symbolSegment)
        assertEquals(SymbolSegment.C, Note.from(261.63)!!.symbolSegment)
        assertEquals(SymbolSegment.D, Note.from(293.66)!!.symbolSegment)
        assertEquals(SymbolSegment.E, Note.from(329.63)!!.symbolSegment)
        assertEquals(SymbolSegment.F, Note.from(349.23)!!.symbolSegment)
        assertEquals(SymbolSegment.G, Note.from(392.00)!!.symbolSegment)
        assertEquals(SymbolSegment.B, Note.from(493.88)!!.symbolSegment)
    }

    @Test
    fun `all standard guitar tuning frequencies detected correctly`() {
        val guitarNotes = mapOf(
            82.41 to "E",   // E2
            110.00 to "A",  // A2
            146.83 to "D",  // D3
            196.00 to "G",  // G3
            246.94 to "B",  // B3
            329.63 to "E"   // E4
        )
        for ((freq, expectedName) in guitarNotes) {
            val note = Note.from(freq)
            assertNotNull("Note should not be null for $freq Hz", note)
            assertEquals("Wrong note for $freq Hz", expectedName, note!!.name)
        }
    }
}
