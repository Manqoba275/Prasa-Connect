package com.example.prasa

import org.junit.Assert.*
import org.junit.Test

class PrasaRulesTest {
    @Test fun registrationRejectsMissingFields() {
        assertEquals("Please complete all registration fields.",
            PrasaRules.validateRegistration("","user@test.com","0712345678","password123"))
    }
    @Test fun registrationRejectsWeakPassword() {
        assertEquals("Password must be at least 8 characters.",
            PrasaRules.validateRegistration("Test User","user@test.com","0712345678","123"))
    }
    @Test fun validRegistrationPasses() {
        assertNull(PrasaRules.validateRegistration("Test User","user@test.com","0712345678","password123"))
    }
    @Test fun loginRequiresCredentials() {
        assertEquals("Enter email and password.",PrasaRules.validateLogin("",""))
    }
    @Test fun loginRejectsMalformedEmail() {
        assertEquals("Enter a valid email address.",PrasaRules.validateLogin("not-an-email","password123"))
    }
    @Test fun incidentRequiresDetails() {
        assertEquals("Complete incident type, location and description.",
            PrasaRules.validateIncident("Cable theft","","Suspicious activity"))
    }
    @Test fun incidentRejectsTooShortDescription() {
        assertNotNull(PrasaRules.validateIncident("Safety","Station","short"))
        assertNull(PrasaRules.validateIncident("Safety","Station","Suspicious activity"))
    }
}
