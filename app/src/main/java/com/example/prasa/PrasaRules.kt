package com.example.prasa

import java.security.MessageDigest

object PrasaRules {
    fun validateRegistration(fullName: String, email: String, mobile: String, password: String): String? {
        return when {
            fullName.isBlank() || email.isBlank() || mobile.isBlank() || password.isBlank() ->
                "Please complete all registration fields."
            !email.contains("@") ->
                "Enter a valid email address."
            password.length < 6 ->
                "Password must be at least 6 characters."
            else -> null
        }
    }

    fun validateLogin(email: String, password: String): String? {
        return if (email.isBlank() || password.isBlank()) "Enter email and password." else null
    }

    fun validateIncident(type: String, location: String, description: String): String? {
        return if (type.isBlank() || location.isBlank() || description.isBlank()) {
            "Complete incident type, location and description."
        } else {
            null
        }
    }

    fun updateSettings(
        user: User,
        language: String,
        notifications: Boolean,
        offlineSync: Boolean
    ): User = user.copy(
        language = language,
        notificationsEnabled = notifications,
        offlineSyncEnabled = offlineSync
    )

    fun hashPassword(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
