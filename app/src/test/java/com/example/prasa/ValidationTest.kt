package com.example.prasa

import org.junit.Assert.*
import org.junit.Test

class ValidationTest {
    @Test fun emailRejectsMissingPartsAndSpaces() {
        listOf("", "person", "person@", "@example.com", "a b@example.com").forEach { assertFalse(Validation.email(it)) }
        assertTrue(Validation.email(" passenger@example.com "))
    }
    @Test fun passwordBoundary() {
        assertFalse(Validation.password("1234567"))
        assertTrue(Validation.password("12345678"))
    }
    @Test fun reportsEnforceAllFieldBoundaries() {
        assertTrue(Validation.incident("Safety", "Station", "1234567890"))
        assertFalse(Validation.incident(" ", "Station", "1234567890"))
        assertFalse(Validation.incident("Safety", " ", "1234567890"))
        assertFalse(Validation.incident("Safety", "Station", "short"))
        assertFalse(Validation.incident("Safety", "Station", "x".repeat(3001)))
        assertTrue(Validation.incident("x".repeat(100), "x".repeat(300), "x".repeat(3000)))
    }
    @Test fun supportedLanguagesHaveDistinctNavigation() {
        assertNotEquals(Labels.get("home", "en"),Labels.get("home", "af"))
        assertNotEquals("login",Labels.get("login", "af"))
    }
}
