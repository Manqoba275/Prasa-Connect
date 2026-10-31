package com.example.prasa

// English and Afrikaans are South African official languages. Review translations with a fluent speaker.
object Labels {
    private val values = mapOf(
        "home" to ("Home" to "Tuis"), "login" to ("Sign in" to "Meld aan"),
        "register" to ("Create account" to "Skep rekening"), "logout" to ("Sign out" to "Meld af"),
        "timetable" to ("Timetable" to "Rooster"), "tickets" to ("My tickets" to "My kaartjies"),
        "settings" to ("Settings" to "Instellings"), "report" to ("Report incident" to "Rapporteer voorval"),
        "reports" to ("My reports" to "My verslae"), "stations" to ("Stations" to "Stasies"),
        "map" to ("Rail map" to "Spoorkaart"), "alerts" to ("Service alerts" to "Dienskennisgewings"),
        "welcome" to ("Your journey. Connected." to "Jou reis. Verbind."),
        "sample" to ("Sample schedules. Confirm travel times with PRASA." to "Voorbeeldroosters. Bevestig reistye by PRASA."),
        "cached" to ("Offline: saved information" to "Vanlyn: gestoorde inligting"),
        "pending" to ("Pending reports" to "Hangende verslae"), "sync" to ("Sync reports" to "Sinkroniseer verslae"),
        "synced" to ("Reports sent" to "Verslae gestuur"), "from" to ("From" to "Vanaf"),
        "to" to ("To" to "Na"), "search" to ("Search" to "Soek"), "empty" to ("No results." to "Geen resultate nie."),
        "platform" to ("Platform" to "Perron"), "book" to ("Book" to "Bespreek"),
        "confirm" to ("Confirm booking" to "Bevestig bespreking"),
        "fare" to ("Sample fare: R28.00. No payment will be taken." to "Voorbeeldtarief: R28.00. Geen betaling word gehef nie."),
        "academic" to ("Academic booking; not valid for travel." to "Akademiese bespreking; nie geldig vir reis nie."),
        "sent" to ("Report submitted." to "Verslag ingedien."), "queued" to ("Saved offline. Will send when connected." to "Vanlyn gestoor. Word gestuur wanneer verbind."),
        "saved" to ("Settings saved." to "Instellings gestoor."), "name" to ("Full name" to "Volle naam"),
        "mobile" to ("Mobile number" to "Selfoonnommer"), "email" to ("Email" to "E-pos"),
        "password" to ("Password" to "Wagwoord"), "google" to ("Continue with Google" to "Gaan voort met Google"),
        "type" to ("Incident type" to "Soort voorval"), "location" to ("Location" to "Ligging"),
        "description" to ("Description" to "Beskrywing"), "submit" to ("Submit" to "Dien in"),
        "notifications" to ("Notifications" to "Kennisgewings"), "save" to ("Save" to "Stoor"),
        "error" to ("Unable to complete request." to "Kan nie die versoek voltooi nie.")
    )
    fun get(key:String,language:String):String = values[key]?.let { if(language=="af") it.second else it.first } ?: key
}
