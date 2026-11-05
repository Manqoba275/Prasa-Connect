package com.example.prasa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LabelsTest {
    @Test fun languageChoicesReturnLocalizedLabels() {
        val english = Labels.get("settings", "en")
        val afrikaans = Labels.get("settings", "af")
        val zulu = Labels.get("settings", "zu")

        assertEquals("Settings", english)
        assertEquals("Instellings", afrikaans)
        assertEquals("Izilungiselelo", zulu)
        assertNotEquals(english, afrikaans)
        assertNotEquals(english, zulu)
    }

    @Test fun unknownLanguageFallsBackToEnglish() {
        assertEquals(Labels.get("home", "en"), Labels.get("home", "xx"))
    }
}
