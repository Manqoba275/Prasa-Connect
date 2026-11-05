package com.example.prasa

// English, Afrikaans and isiZulu labels; have a fluent speaker review before release.
object Labels {
    private val values = mapOf(
        "home" to ("Home" to "Tuis"), "login" to ("Sign in" to "Meld aan"),
        "register" to ("Create account" to "Skep rekening"), "logout" to ("Sign out" to "Meld af"),
        "timetable" to ("Timetable" to "Rooster"), "tickets" to ("My tickets" to "My kaartjies"),
        "settings" to ("Settings" to "Instellings"), "report" to ("Report incident" to "Rapporteer voorval"),
        "reports" to ("My reports" to "My verslae"), "stations" to ("Stations" to "Stasies"),
        "map" to ("Rail map" to "Spoorkaart"), "alerts" to ("Service alerts" to "Dienskennisgewings"),
        "favorites" to ("Favourite routes" to "Gunstelingroetes"),
        "favorite" to ("Save route" to "Stoor roete"), "remove" to ("Remove" to "Verwyder"),
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
        "error" to ("Unable to complete request." to "Kan nie die versoek voltooi nie."),
        "book_tab" to ("Book" to "Bespreek"), "profile" to ("Profile" to "Profiel"),
        "welcome_back" to ("Welcome" to "Welkom"), "next_journey" to ("Next journey" to "Volgende reis"),
        "live_tracking" to ("Live tracking" to "Regstreekse opsporing"), "book_ticket" to ("Book ticket" to "Bespreek kaartjie"),
        "rail_map" to ("Rail map" to "Spoorkaart"), "report_incident" to ("Report incident" to "Rapporteer voorval"),
        "incident_photo" to ("Incident photo stored securely" to "Voorvalfoto veilig gestoor"),
        "add_photo" to ("Add incident photo (optional)" to "Voeg voorvalfoto by (opsioneel)"),
        "change_photo" to ("Change selected photo" to "Verander gekose foto"),
        "start_app" to ("START APP" to "BEGIN PROGRAM"),
        "google_setup" to ("Google sign-in is not configured. Check the OAuth provider settings." to "Google-aanmelding is nie opgestel nie. Gaan die OAuth-verskafferinstellings na."),
        "next_journey_format" to ("Next journey" to "Volgende reis"),
        "station_year" to ("PRASA · Gauteng · 2024" to "PRASA · Gauteng · 2024"),
        "password_show" to ("Show password" to "Wys wagwoord"), "password_hide" to ("Hide password" to "Versteek wagwoord")
    )
    private val isiZulu = mapOf(
        "home" to "Ekhaya", "login" to "Ngena ngemvume", "register" to "Dala i-akhawunti", "logout" to "Phuma",
        "timetable" to "Uhlelo lwezikhathi", "tickets" to "Amathikithi ami", "settings" to "Izilungiselelo",
        "report" to "Bika isigameko", "reports" to "Imibiko yami", "stations" to "Iziteshi", "map" to "Imephu kaloliwe",
        "alerts" to "Izaziso zesevisi", "favorites" to "Imizila eyintandokazi", "favorite" to "Londoloza umzila",
        "remove" to "Susa", "welcome" to "Uhambo lwakho. Sixhumene.",
        "sample" to "Amashejuli esibonelo. Qinisekisa izikhathi zokuhamba kwa-PRASA.",
        "cached" to "Akukho uxhumano: ulwazi olulondoloziwe", "pending" to "Imibiko elindile",
        "sync" to "Vumelanisa imibiko", "synced" to "Imibiko ithunyelwe", "from" to "Isuka", "to" to "Iya",
        "search" to "Sesha", "empty" to "Akukho miphumela.", "platform" to "Iplatifomu", "book" to "Bhukha",
        "confirm" to "Qinisekisa ukubhukha", "fare" to "Imali yesibonelo: R28.00. Ayikho imali ezodonswa.",
        "academic" to "Ukubhukha kwesifundo; akuvumelekile ukuhamba.", "sent" to "Umbiko uthunyelwe.",
        "queued" to "Kulondolozwe ungaxhunyiwe. Kuzothunyelwa uma uxhumano lubuya.",
        "saved" to "Izilungiselelo zilondoloziwe.", "name" to "Igama eligcwele", "mobile" to "Inombolo yeselula",
        "email" to "I-imeyili", "password" to "Iphasiwedi", "google" to "Qhubeka nge-Google",
        "type" to "Uhlobo lwesigameko", "location" to "Indawo", "description" to "Incazelo",
        "submit" to "Thumela", "notifications" to "Izaziso", "save" to "Londoloza",
        "error" to "Ayikwazanga ukuqedela isicelo.", "book_tab" to "Bhukha", "profile" to "Iphrofayela",
        "welcome_back" to "Siyakwamukela", "next_journey" to "Uhambo olulandelayo", "live_tracking" to "Ukulandelela bukhoma",
        "book_ticket" to "Bhukha ithikithi", "rail_map" to "Imephu kaloliwe", "report_incident" to "Bika isigameko",
        "incident_photo" to "Isithombe sesigameko sigcinwe ngokuphephile", "add_photo" to "Engeza isithombe sesigameko (ongakukhetha)",
        "change_photo" to "Shintsha isithombe esikhethiwe", "start_app" to "QALA UHLELO",
        "google_setup" to "Ukungena nge-Google akulungiselelwe. Hlola izilungiselelo zomhlinzeki we-OAuth.",
        "next_journey_format" to "Uhambo olulandelayo", "station_year" to "PRASA · Gauteng · 2024",
        "password_show" to "Bonisa iphasiwedi", "password_hide" to "Fihla iphasiwedi"
    )
    fun get(key:String,language:String):String = when(language) {
        "af" -> values[key]?.second ?: key
        "zu" -> isiZulu[key] ?: values[key]?.first ?: key
        else -> values[key]?.first ?: key
    }
}
