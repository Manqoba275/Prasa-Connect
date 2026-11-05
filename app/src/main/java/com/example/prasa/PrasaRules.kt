package com.example.prasa

// Shared validation retained from main; password storage now belongs to Supabase Auth.
object PrasaRules {
    fun validateRegistration(fullName:String,email:String,mobile:String,password:String):String? = when {
        fullName.isBlank() || email.isBlank() || mobile.isBlank() || password.isBlank() ->
            "Please complete all registration fields."
        !Validation.email(email) -> "Enter a valid email address."
        !Validation.password(password) -> "Password must be at least 8 characters."
        else -> null
    }
    fun validateLogin(email:String,password:String):String? = when {
        email.isBlank() || password.isBlank() -> "Enter email and password."
        !Validation.email(email) -> "Enter a valid email address."
        else -> null
    }
    fun validateIncident(type:String,location:String,description:String):String? = when {
        type.isBlank() || location.isBlank() || description.isBlank() ->
            "Complete incident type, location and description."
        !Validation.incident(type,location,description) ->
            "Enter a type, location and description of 10 to 3000 characters."
        else -> null
    }
}
