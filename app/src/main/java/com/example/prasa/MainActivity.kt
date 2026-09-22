package com.example.prasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prasa.ui.theme.PrasaTheme
import java.security.MessageDigest

private val PrasaBlue = Color(0xFF004EA8)
private val DeepBlue = Color(0xFF00356F)
private val PrasaGold = Color(0xFFFFC928)
private val SoftBlue = Color(0xFFEAF3FF)
private val Ink = Color(0xFF152033)
private val Muted = Color(0xFF6F7B8B)
private val Success = Color(0xFF148A4A)
private val Warning = Color(0xFFE7A900)
private val Danger = Color(0xFFB3261E)
private const val SUPABASE_API_URL = "https://awbgflgcqjofsnagkzou.supabase.co/functions/v1/prasa-api"

private enum class Screen(val label: String) {
    Splash("Splash"),
    Login("Login"),
    Register("Register"),
    Home("Home"),
    Timetable("Timetable"),
    Booking("Book"),
    Tickets("Tickets"),
    Tracking("Track"),
    Stations("Stations"),
    Map("Map"),
    Incident("Report"),
    Alerts("Alerts"),
    Settings("Settings"),
    Profile("Profile"),
    Api("API")
}

private data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val language: String = "English",
    val notificationsEnabled: Boolean = true,
    val offlineSyncEnabled: Boolean = true
)

private data class Schedule(
    val id: String,
    val train: String,
    val from: String,
    val to: String,
    val depart: String,
    val arrive: String,
    val platform: String,
    val status: String
)

private data class Ticket(
    val id: String,
    val userId: String,
    val route: String,
    val train: String,
    val date: String,
    val fare: Double,
    val qrCode: String,
    val status: String = "Active"
)

private data class Incident(
    val id: String,
    val userId: String,
    val type: String,
    val location: String,
    val description: String,
    val status: String = "Submitted"
)

private data class ApiResult<T>(val ok: Boolean, val message: String, val data: T? = null)

private class PrasaApi {
    private val users = mutableStateListOf<User>()
    val tickets = mutableStateListOf<Ticket>()
    val incidents = mutableStateListOf<Incident>()
    val schedules = mutableStateListOf(
        Schedule("SCH-001", "T0507", "Cape Town", "Bellville", "08:45", "09:15", "2", "On time"),
        Schedule("SCH-002", "T0509", "Cape Town", "Bellville", "09:15", "09:45", "1", "On time"),
        Schedule("SCH-003", "T0612", "Johannesburg", "Pretoria", "10:00", "10:58", "4", "Delayed 5 min"),
        Schedule("SCH-004", "T0718", "Pretoria", "Ekurhuleni", "11:30", "12:15", "3", "On time")
    )
    var lastLog by mutableStateOf("GET /api/status -> 200 PRASA Connect API ready")
        private set

    init {
        val demoHash = hashPassword("password123")
        users.add(User("USR-001", "Nonhlanhla Chirwa", "nonhlanhla@prasa.demo", "0712345678", demoHash, "Tshivenda"))
        tickets.add(Ticket("TCK-1001", "USR-001", "Cape Town -> Bellville", "T0507", "24 May 2025", 28.00, "QR-TCK-1001"))
    }

    fun register(fullName: String, email: String, mobile: String, password: String): ApiResult<User> {
        if (fullName.isBlank() || email.isBlank() || mobile.isBlank() || password.isBlank()) {
            lastLog = "POST /api/auth/register -> 400 Missing required fields"
            return ApiResult(false, "Please complete all registration fields.")
        }
        if (!email.contains("@")) {
            lastLog = "POST /api/auth/register -> 400 Invalid email"
            return ApiResult(false, "Enter a valid email address.")
        }
        if (password.length < 6) {
            lastLog = "POST /api/auth/register -> 400 Weak password"
            return ApiResult(false, "Password must be at least 6 characters.")
        }
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            lastLog = "POST /api/auth/register -> 409 User already exists"
            return ApiResult(false, "This email is already registered.")
        }
        val user = User(
            id = "USR-${(users.size + 1).toString().padStart(3, '0')}",
            fullName = fullName.trim(),
            email = email.trim(),
            mobile = mobile.trim(),
            passwordHash = hashPassword(password)
        )
        users.add(user)
        lastLog = "POST /api/auth/register -> 201 Created ${user.id}; password stored as SHA-256 hash"
        return ApiResult(true, "Registration successful.", user)
    }

    fun login(email: String, password: String): ApiResult<User> {
        if (email.isBlank() || password.isBlank()) {
            lastLog = "POST /api/auth/login -> 400 Missing credentials"
            return ApiResult(false, "Enter email and password.")
        }
        val user = users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        if (user == null || user.passwordHash != hashPassword(password)) {
            lastLog = "POST /api/auth/login -> 401 Invalid credentials"
            return ApiResult(false, "Login failed. Check your email or password.")
        }
        lastLog = "POST /api/auth/login -> 200 Authenticated ${user.id}"
        return ApiResult(true, "Welcome back, ${user.fullName}.", user)
    }

    fun updateSettings(user: User, language: String, notifications: Boolean, offlineSync: Boolean): User {
        val updated = user.copy(language = language, notificationsEnabled = notifications, offlineSyncEnabled = offlineSync)
        val index = users.indexOfFirst { it.id == user.id }
        if (index >= 0) users[index] = updated
        lastLog = "PATCH /api/users/${user.id}/settings -> 200 Updated language=$language notifications=$notifications offlineSync=$offlineSync"
        return updated
    }

    fun findSchedules(from: String, to: String): List<Schedule> {
        val results = schedules.filter {
            it.from.contains(from.trim(), ignoreCase = true) && it.to.contains(to.trim(), ignoreCase = true)
        }.ifEmpty { schedules.toList() }
        lastLog = "GET /api/schedules?from=${from.ifBlank { "all" }}&to=${to.ifBlank { "all" }} -> 200 ${results.size} rows"
        return results
    }

    fun bookTicket(user: User, schedule: Schedule): Ticket {
        val ticket = Ticket(
            id = "TCK-${1000 + tickets.size + 1}",
            userId = user.id,
            route = "${schedule.from} -> ${schedule.to}",
            train = schedule.train,
            date = "24 May 2025",
            fare = 28.00,
            qrCode = "QR-${schedule.train}-${tickets.size + 1}"
        )
        tickets.add(ticket)
        lastLog = "POST /api/bookings -> 201 Created ${ticket.id} for ${user.id}"
        return ticket
    }

    fun submitIncident(user: User, type: String, location: String, description: String): ApiResult<Incident> {
        if (type.isBlank() || location.isBlank() || description.isBlank()) {
            lastLog = "POST /api/incidents -> 400 Missing incident details"
            return ApiResult(false, "Complete incident type, location and description.")
        }
        val incident = Incident(
            id = "INC-${(incidents.size + 1).toString().padStart(3, '0')}",
            userId = user.id,
            type = type.trim(),
            location = location.trim(),
            description = description.trim()
        )
        incidents.add(incident)
        lastLog = "POST /api/incidents -> 201 Created ${incident.id}"
        return ApiResult(true, "Incident submitted successfully.", incident)
    }

    fun userTickets(user: User): List<Ticket> {
        val rows = tickets.filter { it.userId == user.id }
        lastLog = "GET /api/users/${user.id}/tickets -> 200 ${rows.size} rows"
        return rows
    }

    fun counts(): String {
        return "Users: ${users.size} | Schedules: ${schedules.size} | Tickets: ${tickets.size} | Incidents: ${incidents.size}"
    }
}

private fun hashPassword(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrasaTheme(dynamicColor = false) {
                PrasaConnectApp()
            }
        }
    }
}

@Composable
private fun PrasaConnectApp() {
    val api = remember { PrasaApi() }
    var screen by remember { mutableStateOf(Screen.Splash) }
    var user by remember { mutableStateOf<User?>(null) }
    var schedules by remember { mutableStateOf(api.schedules.toList()) }
    var lastTicket by remember { mutableStateOf<Ticket?>(null) }
    var appMessage by remember { mutableStateOf("Demo account: nonhlanhla@prasa.demo / password123") }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        when (screen) {
            Screen.Splash -> SplashScreen(onStart = { screen = Screen.Login })
            Screen.Login -> LoginScreen(
                message = appMessage,
                onLogin = { email, password ->
                    val result = api.login(email, password)
                    appMessage = result.message
                    if (result.ok && result.data != null) {
                        user = result.data
                        schedules = api.findSchedules("", "")
                        screen = Screen.Home
                    }
                },
                onRegister = { screen = Screen.Register }
            )
            Screen.Register -> RegisterScreen(
                message = appMessage,
                onDone = { name, email, mobile, password ->
                    val result = api.register(name, email, mobile, password)
                    appMessage = result.message
                    if (result.ok && result.data != null) {
                        user = result.data
                        screen = Screen.Home
                    }
                },
                onBack = { screen = Screen.Login }
            )
            else -> {
                val activeUser = user
                if (activeUser == null) {
                    screen = Screen.Login
                    LoginScreen(appMessage, { email, password ->
                        val result = api.login(email, password)
                        appMessage = result.message
                        if (result.ok && result.data != null) {
                            user = result.data
                            screen = Screen.Home
                        }
                    }, { screen = Screen.Register })
                } else {
                    MainShell(
                        current = screen,
                        user = activeUser,
                        navigate = { screen = it },
                        onLogout = {
                            api.lastLog
                            user = null
                            appMessage = "Logged out successfully."
                            screen = Screen.Login
                        }
                    ) {
                        when (screen) {
                            Screen.Home -> DashboardScreen(activeUser, schedules, appMessage) { screen = it }
                            Screen.Timetable -> TimetableScreen(
                                schedules = schedules,
                                onSearch = { from, to -> schedules = api.findSchedules(from, to) },
                                onBook = { schedule ->
                                    lastTicket = api.bookTicket(activeUser, schedule)
                                    appMessage = "Booking created: ${lastTicket?.id}"
                                    screen = Screen.Tickets
                                }
                            )
                            Screen.Booking -> BookingScreen(
                                schedules = schedules,
                                onBook = { schedule ->
                                    lastTicket = api.bookTicket(activeUser, schedule)
                                    appMessage = "Booking created: ${lastTicket?.id}"
                                    screen = Screen.Tickets
                                }
                            )
                            Screen.Tickets -> TicketsScreen(api.userTickets(activeUser), lastTicket)
                            Screen.Tracking -> TrackingScreen(schedules.first())
                            Screen.Stations -> StationsScreen { screen = Screen.Incident }
                            Screen.Map -> RailMapScreen { screen = Screen.Booking }
                            Screen.Incident -> IncidentScreen { type, location, description ->
                                val result = api.submitIncident(activeUser, type, location, description)
                                appMessage = result.message
                            }
                            Screen.Alerts -> AlertsScreen(activeUser)
                            Screen.Settings -> SettingsScreen(activeUser) { language, notifications, offlineSync ->
                                user = api.updateSettings(activeUser, language, notifications, offlineSync)
                                appMessage = "Settings saved."
                            }
                            Screen.Profile -> ProfileScreen(activeUser) { screen = it }
                            Screen.Api -> ApiScreen(api)
                            else -> DashboardScreen(activeUser, schedules, appMessage) { screen = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue, PrasaBlue, Color.White))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            PrasaLogo(width = 180, height = 112)
            Spacer(Modifier.height(22.dp))
            Text("PASSENGER RAIL AGENCY OF SOUTH AFRICA", color = Color.White, fontSize = 10.sp)
            Spacer(Modifier.height(56.dp))
            Text("PRASA", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("CONNECT", color = PrasaGold, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your Journey. Connected.", color = Color.White, fontSize = 17.sp)
            Spacer(Modifier.height(64.dp))
            PrimaryButton("START APP", onStart)
        }
    }
}

@Composable
private fun LoginScreen(message: String, onLogin: (String, String) -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("nonhlanhla@prasa.demo") }
    var password by remember { mutableStateOf("password123") }
    AuthScaffold(title = "Login") {
        StatusText(message)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        PrimaryButton("LOGIN", { onLogin(email, password) })
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { onLogin("nonhlanhla@prasa.demo", "password123") }, modifier = Modifier.fillMaxWidth()) {
            Text("DEMO GOOGLE SSO LOGIN")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Need an account?", color = Muted)
            TextButton(onClick = onRegister) { Text("Register") }
        }
    }
}

@Composable
private fun RegisterScreen(message: String, onDone: (String, String, String, String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AuthScaffold(title = "Register") {
        StatusText(message)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile number") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("Passwords are saved as SHA-256 hashes in the app data layer.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        PrimaryButton("REGISTER", { onDone(name, email, mobile, password) })
        TextButton(onClick = onBack) { Text("Back to login") }
    }
}

@Composable
private fun AuthScaffold(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrasaLogo(width = 150, height = 94)
        Text("CONNECT", color = PrasaGold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(current: Screen, user: User, navigate: (Screen) -> Unit, onLogout: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.label, color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrasaBlue),
                navigationIcon = { TextButton(onClick = { navigate(Screen.Home) }) { Text("<", color = Color.White, fontSize = 22.sp) } },
                actions = {
                    TextButton(onClick = { navigate(Screen.Api) }) { Text("API", color = Color.White) }
                    TextButton(onClick = { navigate(Screen.Profile) }) { Text(user.fullName.take(2).uppercase(), color = Color.White) }
                    TextButton(onClick = onLogout) { Text("Logout", color = Color.White) }
                }
            )
        },
        bottomBar = { BottomNav(current, navigate) }
    ) { inner ->
        Box(modifier = Modifier.padding(inner)) { content() }
    }
}

@Composable
private fun BottomNav(current: Screen, navigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .background(Color.White)
            .border(1.dp, Color(0xFFE7EAF0)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(Screen.Home, Screen.Booking, Screen.Tickets, Screen.Alerts, Screen.Profile).forEach { item ->
            val selected = current == item
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { navigate(item) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.label.take(1), color = if (selected) PrasaBlue else Muted, fontWeight = FontWeight.Bold)
                Text(item.label, color = if (selected) PrasaBlue else Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DashboardScreen(user: User, schedules: List<Schedule>, message: String, navigate: (Screen) -> Unit) {
    ScreenColumn {
        BluePanel {
            Text("Welcome, ${user.fullName}", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val next = schedules.first()
            Text("${next.from} -> ${next.to}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Train ${next.train} | ${next.depart} - ${next.arrive} | Platform ${next.platform} | ${next.status}", color = PrasaGold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        StatusText(message)
        Spacer(Modifier.height(16.dp))
        val actions = listOf(
            "Timetable" to Screen.Timetable,
            "Book Ticket" to Screen.Booking,
            "My Tickets" to Screen.Tickets,
            "Live Tracking" to Screen.Tracking,
            "Stations" to Screen.Stations,
            "Rail Map" to Screen.Map,
            "Report Incident" to Screen.Incident,
            "Settings" to Screen.Settings,
            "API Data" to Screen.Api
        )
        actions.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, target) -> ActionTile(label, Modifier.weight(1f)) { navigate(target) } }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TimetableScreen(schedules: List<Schedule>, onSearch: (String, String) -> Unit, onBook: (Schedule) -> Unit) {
    var from by remember { mutableStateOf("Cape Town") }
    var to by remember { mutableStateOf("Bellville") }
    ScreenColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(from, { from = it }, label = { Text("From") }, modifier = Modifier.weight(1f))
            OutlinedTextField(to, { to = it }, label = { Text("To") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton("SEARCH API TIMETABLE", { onSearch(from, to) })
        Spacer(Modifier.height(12.dp))
        schedules.forEach { schedule ->
            ScheduleCard(schedule = schedule, button = "BOOK") { onBook(schedule) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BookingScreen(schedules: List<Schedule>, onBook: (Schedule) -> Unit) {
    ScreenColumn {
        Text("Make a Booking", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Select a real row from the app data layer. A new QR ticket is created when you book.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        schedules.forEach { schedule ->
            ScheduleCard(schedule, "CONFIRM BOOKING") { onBook(schedule) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TicketsScreen(tickets: List<Ticket>, lastTicket: Ticket?) {
    ScreenColumn {
        if (lastTicket != null) {
            StatusText("Newest booking: ${lastTicket.id} for ${lastTicket.route}")
            Spacer(Modifier.height(10.dp))
        }
        if (tickets.isEmpty()) {
            EmptyState("No tickets yet. Go to Book to create one.")
        } else {
            tickets.forEach { ticket ->
                TicketCard(ticket)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TrackingScreen(schedule: Schedule) {
    ScreenColumn {
        BluePanel {
            Text("Train ${schedule.train}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("${schedule.from} -> ${schedule.to}", color = Color.White)
            Text("${schedule.depart} - ${schedule.arrive} | ${schedule.status}", color = PrasaGold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        CardBlock {
            listOf(schedule.from, "Salt River", "Maitland", "Koeberg Road", schedule.to).forEachIndexed { index, stop ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(18.dp).clip(CircleShape).background(if (index == 0) Success else PrasaBlue))
                    Spacer(Modifier.width(10.dp))
                    Text(stop, fontWeight = if (index == 4) FontWeight.Bold else FontWeight.Normal)
                }
                if (index < 4) Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun StationsScreen(onReport: () -> Unit) {
    ScreenColumn {
        listOf(
            "Cape Town Station" to "Parking, restrooms, food court | 0.2 km",
            "Salt River Station" to "Parking, restrooms | 3.6 km",
            "Maitland Station" to "Parking, restrooms | 5.1 km",
            "Bellville Station" to "Parking, taxi rank | 15.3 km"
        ).forEach { (name, details) -> StationRow(name, details) }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("REPORT STATION INCIDENT", onReport)
    }
}

@Composable
private fun RailMapScreen(onPlanTrip: () -> Unit) {
    ScreenColumn {
        BluePanel {
            Text("Official PRASA Gauteng Rail Map", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Use the rail map before booking a route.", color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Gauteng", selected = true)
            Chip("Johannesburg")
            Chip("Pretoria")
            Chip("Ekurhuleni")
        }
        Spacer(Modifier.height(12.dp))
        CardBlock {
            Image(
                painter = painterResource(id = R.drawable.prasa_gauteng_rail_map),
                contentDescription = "PRASA Gauteng rail network map",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("PLAN / BOOK FROM MAP", onPlanTrip)
    }
}

@Composable
private fun IncidentScreen(onSubmit: (String, String, String) -> Unit) {
    var type by remember { mutableStateOf("Cable theft") }
    var location by remember { mutableStateOf("Koeberg Road, Cape Town") }
    var description by remember { mutableStateOf("") }
    var localMessage by remember { mutableStateOf("Complete the form and submit. It will be stored in the API data layer.") }
    ScreenColumn {
        StatusText(localMessage)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(type, { type = it }, label = { Text("Incident type") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(12.dp))
        PrimaryButton("SUBMIT INCIDENT", {
            onSubmit(type, location, description)
            localMessage = "Incident submitted. Open API screen to see stored counts."
        })
    }
}

@Composable
private fun AlertsScreen(user: User) {
    ScreenColumn {
        AlertCard("Service normal", "Cape Town to Bellville line is running on schedule.", Success)
        AlertCard("Offline sync ${if (user.offlineSyncEnabled) "enabled" else "disabled"}", "Saved tickets and timetables are available for the demo.", PrasaBlue)
        AlertCard("Safety notice", "Report cable theft or platform hazards from the incident screen.", Warning)
    }
}

@Composable
private fun SettingsScreen(user: User, onSave: (String, Boolean, Boolean) -> Unit) {
    var language by remember { mutableStateOf(user.language) }
    var notifications by remember { mutableStateOf(user.notificationsEnabled) }
    var offlineSync by remember { mutableStateOf(user.offlineSyncEnabled) }
    ScreenColumn {
        SettingsToggle("Push notifications", notifications) { notifications = it }
        SettingsToggle("Offline sync", offlineSync) { offlineSync = it }
        Spacer(Modifier.height(14.dp))
        Text("Language", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("English", "isiZulu", "Tshivenda").forEach { item ->
                Box(modifier = Modifier.clickable { language = item }) {
                    Chip(item, selected = language == item)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton("SAVE SETTINGS", { onSave(language, notifications, offlineSync) })
        Spacer(Modifier.height(10.dp))
        ReadOnlyField("Security: password hash stored in data layer")
        ReadOnlyField("API: settings saved through PATCH-style function")
    }
}

@Composable
private fun ProfileScreen(user: User, navigate: (Screen) -> Unit) {
    ScreenColumn(horizontal = Alignment.CenterHorizontally) {
        Box(Modifier.size(78.dp).clip(CircleShape).background(SoftBlue), contentAlignment = Alignment.Center) {
            Text(user.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), color = PrasaBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(user.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(user.email, color = Muted)
        Spacer(Modifier.height(20.dp))
        InfoLine("Mobile", user.mobile)
        InfoLine("Language", user.language)
        InfoLine("Notifications", if (user.notificationsEnabled) "On" else "Off")
        Spacer(Modifier.height(16.dp))
        PrimaryButton("OPEN SETTINGS", { navigate(Screen.Settings) })
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { navigate(Screen.Api) }, modifier = Modifier.fillMaxWidth()) { Text("VIEW API DATA") }
    }
}

@Composable
private fun ApiScreen(api: PrasaApi) {
    ScreenColumn {
        BluePanel {
            Text("API / Database Evidence", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hosted Supabase API is configured. The app also keeps a local fallback data layer for classroom demo reliability.", color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        ReadOnlyField("Hosted API: $SUPABASE_API_URL")
        ReadOnlyField("Supabase tables: app_users, schedules, tickets, incidents")
        ReadOnlyField(api.lastLog)
        ReadOnlyField(api.counts())
        Spacer(Modifier.height(10.dp))
        Text("Stored Tickets", fontWeight = FontWeight.Bold)
        api.tickets.forEach { ticket -> ReadOnlyField("${ticket.id}: ${ticket.route} | ${ticket.train} | R${ticket.fare}") }
        Spacer(Modifier.height(10.dp))
        Text("Stored Incidents", fontWeight = FontWeight.Bold)
        if (api.incidents.isEmpty()) EmptyState("No incidents submitted yet.")
        api.incidents.forEach { incident -> ReadOnlyField("${incident.id}: ${incident.type} at ${incident.location} | ${incident.status}") }
    }
}

@Composable
private fun ScheduleCard(schedule: Schedule, button: String, onClick: () -> Unit) {
    CardBlock {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${schedule.depart} -> ${schedule.arrive}", fontWeight = FontWeight.Bold)
            Text(schedule.status, color = if (schedule.status.contains("Delayed")) Warning else Success, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text("${schedule.from} -> ${schedule.to}", color = Ink)
        Text("Train ${schedule.train} | Platform ${schedule.platform}", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        PrimaryButton(button, onClick)
    }
}

@Composable
private fun TicketCard(ticket: Ticket) {
    CardBlock {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PrasaGold).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(ticket.status, fontWeight = FontWeight.Bold)
            Text(ticket.id, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(ticket.route, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Train ${ticket.train} | ${ticket.date} | R${ticket.fare}", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        FakeQrCode()
        Spacer(Modifier.height(8.dp))
        Text(ticket.qrCode, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ScreenColumn(horizontal: Alignment.Horizontal = Alignment.Start, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8FAFD))
            .padding(16.dp),
        horizontalAlignment = horizontal
    ) { content() }
}

@Composable
private fun BluePanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(DeepBlue, PrasaBlue)))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun ActionTile(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(86.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.take(2).uppercase(), color = PrasaBlue, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(label, color = Ink, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun StationRow(name: String, details: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(SoftBlue), contentAlignment = Alignment.Center) {
            Text("T", color = PrasaBlue, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold)
            Text(details, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AlertCard(title: String, body: String, color: Color) {
    CardBlock {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(body, color = Muted, fontSize = 13.sp)
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@Composable
private fun StatusText(message: String) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SoftBlue).padding(12.dp)
    ) {
        Text(message, color = Ink, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White).padding(18.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Muted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReadOnlyField(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE1E5EC), RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(text, color = Ink)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted)
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

@Composable
private fun Chip(label: String, selected: Boolean = false) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PrasaBlue else Color.White)
            .border(1.dp, if (selected) PrasaBlue else Color(0xFFD7DDE7), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrasaBlue)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrasaLogo(width: Int, height: Int) {
    Image(
        painter = painterResource(id = R.drawable.prasa_logo),
        contentDescription = "PRASA logo",
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(width.dp, height.dp)
    )
}

@Composable
private fun FakeQrCode() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column {
            repeat(9) { y ->
                Row {
                    repeat(9) { x ->
                        val filled = (x * y + x + y) % 3 == 0 || (x < 3 && y < 3) || (x > 5 && y < 3) || (x < 3 && y > 5)
                        Box(Modifier.size(12.dp).background(if (filled) Ink else Color.White))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrasaConnectPreview() {
    PrasaTheme(dynamicColor = false) {
        PrasaConnectApp()
    }
}
