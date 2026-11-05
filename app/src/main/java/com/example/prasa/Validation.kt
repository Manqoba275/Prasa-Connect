package com.example.prasa

object Validation {
    fun email(value: String) = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())
    fun password(value: String) = value.length >= 8
    fun incident(type: String, location: String, description: String) =
        type.trim().length in 1..100 && location.trim().length in 1..300 &&
            description.trim().length in 10..3000
}
