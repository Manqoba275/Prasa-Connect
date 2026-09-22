package com.example.prasa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrasaRulesTest {
    @Test
    fun registrationRejectsMissingFields() {
        val error = PrasaRules.validateRegistration("", "user@test.com", "0712345678", "password123")

        assertEquals("Please complete all registration fields.", error)
    }

    @Test
    fun registrationRejectsWeakPassword() {
        val error = PrasaRules.validateRegistration("Test User", "user@test.com", "0712345678", "123")

        assertEquals("Password must be at least 6 characters.", error)
    }

    @Test
    fun validRegistrationPasses() {
        val error = PrasaRules.validateRegistration("Test User", "user@test.com", "0712345678", "password123")

        assertNull(error)
    }

    @Test
    fun loginRequiresCredentials() {
        val error = PrasaRules.validateLogin("", "")

        assertEquals("Enter email and password.", error)
    }

    @Test
    fun passwordHashIsNotPlainText() {
        val hash = PrasaRules.hashPassword("password123")

        assertNotEquals("password123", hash)
        assertEquals(64, hash.length)
    }

    @Test
    fun incidentRequiresDetails() {
        val error = PrasaRules.validateIncident("Cable theft", "", "Suspicious activity")

        assertEquals("Complete incident type, location and description.", error)
    }

    @Test
    fun settingsUpdateReturnsChangedUser() {
        val user = User(
            id = "USR-TEST",
            fullName = "Test User",
            email = "test@example.com",
            mobile = "0712345678",
            passwordHash = "hash"
        )

        val updated = PrasaRules.updateSettings(
            user = user,
            language = "isiZulu",
            notifications = false,
            offlineSync = true
        )

        assertEquals("isiZulu", updated.language)
        assertEquals(false, updated.notificationsEnabled)
        assertEquals(true, updated.offlineSyncEnabled)
    }
}
