package com.example.prasa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.example.prasa.ui.theme.PrasaTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : ComponentActivity() {
    private var callback by mutableStateOf<Uri?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = intent?.data
        // Keep the PRASA light palette stable even when the phone uses system dark mode.
        setContent { PrasaTheme(darkTheme = false, dynamicColor = false) { PrasaApp(callback) { callback = null } } }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        callback = intent.data
    }
}

private fun JSONArray.rows(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrasaApp(callback: Uri?, consumed: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { PrasaRepository(context) }
    val firestore = remember { FirestoreRoutes() }
    val scope = rememberCoroutineScope()
    val prefs = remember(repo.userId) { context.getSharedPreferences("preferences-${repo.userId}",0) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var incidentPhoto by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { incidentPhoto = it }
    var language by remember(prefs) { mutableStateOf(prefs.getString("language","en") ?: "en") }
    fun tr(key: String) = Labels.get(key,language)
    var signedIn by remember { mutableStateOf(repo.signedIn) }
    var page by remember { mutableStateOf(if (repo.signedIn) "home" else "welcome") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var offline by remember { mutableStateOf(false) }
    var schedules by remember { mutableStateOf(JSONArray()) }
    var tickets by remember { mutableStateOf(JSONArray()) }
    var reports by remember { mutableStateOf(JSONArray()) }
    var alerts by remember { mutableStateOf(JSONArray()) }
    var profile by remember { mutableStateOf(JSONObject()) }
    var selected by remember { mutableStateOf<JSONObject?>(null) }
    var requestId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var routeRevision by remember { mutableIntStateOf(0) }
    var cloudFavorites by remember(repo.userId) { mutableStateOf(emptyList<Pair<String, String>>()) }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        PushSupport.initialize(context)
        if (repo.signedIn) {
            SyncWorker.schedule(context)
        }
        if(android.os.Build.VERSION.SDK_INT>=33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    fun action(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = ""
        scope.launch {
            try { block() }
            catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                message = e.message ?: tr("error")
                if (e is ApiFailure && e.status==401) { signedIn=false; page="login" }
            } finally { busy=false }
        }
    }
    fun navigate(target: String) { page=target; message="" }
    suspend fun refresh(target: String) {
        if (target in listOf("home","timetable","stations")) {
            val query = Uri.Builder().appendQueryParameter("from",from.trim()).appendQueryParameter("to",to.trim()).build().toString()
            val result = repo.rows("/schedules$query"); schedules=result.first; offline=result.second
        }
        if (target=="tickets") { val result=repo.rows("/tickets"); tickets=result.first; offline=result.second }
        if (target=="reports") { val result=repo.rows("/incidents"); reports=result.first; offline=result.second }
        if (target=="alerts") { val result=repo.rows("/alerts"); alerts=result.first; offline=result.second }
        if (target=="settings") profile=repo.profile()
        if (target=="favorites") {
            cloudFavorites=repo.local.routes(repo.userId)
            SyncWorker.schedule(context)
        }
    }
    LaunchedEffect(page,signedIn) {
        if (signedIn && page!="confirm") {
            busy=true
            try { refresh(page) }
            catch(e: Exception) {
                if(e is kotlinx.coroutines.CancellationException) throw e
                message=e.message ?: tr("error")
            } finally { busy=false }
        }
    }
    LaunchedEffect(callback) {
        if (callback!=null) {
            busy=true
            try {
                repo.finishGoogle(callback); signedIn=true; page="home"; SyncWorker.schedule(context)
                runCatching { firestore.signIn(repo) }
            }
            catch(e: Exception) { message=e.message ?: tr("error") }
            finally { consumed(); busy=false }
        }
    }
    BackHandler(page !in listOf("home","login")) { page=if(signedIn) "home" else "login" }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar={ if (signedIn) TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.primary, titleContentColor=Color.White, navigationIconContentColor=Color.White, actionIconContentColor=Color.White),
            title={ Text(if(page=="home") "PRASA Connect" else tr(page), color=Color.White) },
            navigationIcon={ if(page!="home") TextButton(onClick={navigate("home")}) { Text("‹  ${tr("home")}", color=Color.White) } },
            actions={
                TextButton(enabled=!busy,onClick={
                    action {
                        try { repo.logout() } finally {
                            firestore.signOut()
                            signedIn=false; page="welcome"; tickets=JSONArray(); reports=JSONArray(); selected=null
                        }
                    }
                }) { Text(tr("logout"), color=Color.White) }
            }) },
        bottomBar={ if(signedIn) NavigationBar(containerColor=Color.White) {
            listOf("home","timetable","tickets","alerts","settings").forEach { key ->
            val label = when(key) { "timetable" -> tr("book_tab"); "settings" -> tr("profile"); else -> tr(key) }
                NavigationBarItem(selected=page==key,onClick={navigate(key)},
                    icon={ Icon(when(key) { "home"->Icons.Default.Home; "timetable"->Icons.Default.DateRange; "tickets"->Icons.Default.List; "alerts"->Icons.Default.Settings; else->Icons.Default.Settings },contentDescription=label) },label={Text(label)})
            }
        }}
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(if(page=="welcome") 0.dp else 16.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.error)
            if(offline && signedIn) Text(tr("cached"))
            if(!signedIn) {
                if(page=="welcome") WelcomeScreen { navigate("login") }
                else {
                    Image(painterResource(R.drawable.prasa_logo),"PRASA",Modifier.fillMaxWidth().height(100.dp),contentScale=ContentScale.Fit)
                    Text("CONNECT",Modifier.align(Alignment.CenterHorizontally),style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.secondary)
                    Text(tr(page),Modifier.align(Alignment.CenterHorizontally),style=MaterialTheme.typography.titleLarge)
                    AuthForm(page=="register",busy,language,
                        submit={ name,email,mobile,password -> action {
                            if(page=="register") repo.register(name,email,mobile,password) else repo.login(email,password)
                            signedIn=true; page="home"; SyncWorker.schedule(context)
                            // Cloud route sync is scheduled in the background; local use stays immediate.
                        }},
                        toggle={ navigate(if(page=="register") "login" else "register") },
                        google={ action {
                            if (!repo.googleEnabled()) message=tr("google_setup")
                            else context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(repo.googleUrl())))
                        } })
                }
            } else when(page) {
                "home" -> {
                    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primary),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text("${tr("welcome_back")}${profile.optString("full_name").takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}",color=Color.White,style=MaterialTheme.typography.titleLarge)
                            Text("Cape Town  →  Bellville",color=Color.White,style=MaterialTheme.typography.headlineSmall)
                            Text(tr("sample"),color=MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Card(shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth().height(170.dp)) {
                        Image(painterResource(R.drawable.prasa_gauteng_rail_map),"PRASA Gauteng rail network map",Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
                    }
                    val shortcuts=listOf("timetable","book_ticket","tickets","live_tracking","stations","rail_map","favorites","report_incident","settings","reports")
                    shortcuts.chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        row.forEach { labelKey ->
                            val key=when(labelKey) {
                                "timetable","book_ticket" -> "timetable"
                                "tickets" -> "tickets"
                                "live_tracking","rail_map" -> "map"
                                "stations" -> "stations"
                                "favorites" -> "favorites"
                                "report_incident" -> "report"
                                "settings" -> "settings"
                                else -> "reports"
                            }
                            Card(onClick={navigate(key)},modifier=Modifier.weight(1f).height(112.dp),shape=RoundedCornerShape(10.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)) {
                                Box(Modifier.fillMaxSize().padding(8.dp),contentAlignment=Alignment.Center) {
                                    Text(tr(labelKey),color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    } }
                    Text(tr("sample"),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.tertiary)
                    if(schedules.length()>0) {
                        val next=schedules.optJSONObject(0)
                        if(next!=null) Text("${tr("next_journey")}: ${next.optString("origin")} → ${next.optString("destination")} · ${next.optString("depart_time")}",style=MaterialTheme.typography.bodyMedium)
                    }
                    Text("${tr("pending")}: ${repo.local.pending(repo.userId).size}")
                    Button(enabled=!busy,onClick={action { val count=repo.sync(); message="${tr("synced")}: $count" }}) { Text(tr("sync")) }
                }
                "timetable" -> {
                    OutlinedTextField(from,{from=it},label={Text(tr("from"))},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(to,{to=it},label={Text(tr("to"))},modifier=Modifier.fillMaxWidth())
                    Button(enabled=!busy,onClick={action { refresh("timetable") }}) { Text(tr("search")) }
                    Text(tr("sample"))
                    if(schedules.length()==0 && !busy) Text(tr("empty"))
                    schedules.rows().forEach { schedule ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                            Text("${schedule.optString("origin")} → ${schedule.optString("destination")}")
                            Text("${schedule.optString("depart_time")} – ${schedule.optString("arrive_time")} · ${schedule.optString("train")}")
                            Text("${tr("platform")}: ${schedule.optString("platform")}")
                            Button(enabled=!busy && !offline,onClick={ selected=schedule; requestId=UUID.randomUUID().toString(); navigate("confirm") }) { Text(tr("book")) }
                            OutlinedButton(enabled=!busy,onClick={action {
                                val origin=schedule.getString("origin"); val destination=schedule.getString("destination")
                                repo.local.saveRoute(repo.userId,origin,destination)
                                routeRevision++
                                SyncWorker.schedule(context)
                                message=tr("saved")
                            }}) { Text(tr("favorite")) }
                        }}
                    }
                }
                "favorites" -> {
                    val favorites=remember(repo.userId,routeRevision,cloudFavorites) {
                        (repo.local.routes(repo.userId) + cloudFavorites).distinct()
                    }
                    if(favorites.isEmpty()) Text(tr("empty"))
                    favorites.forEach { (origin,destination) ->
                        Text("$origin → $destination",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            Button(onClick={from=origin; to=destination; navigate("timetable")}) { Text(tr("search")) }
                            TextButton(enabled=!busy,onClick={action {
                                repo.local.removeRoute(repo.userId,origin,destination)
                                cloudFavorites=cloudFavorites.filterNot { it.first==origin && it.second==destination }
                                routeRevision++
                                SyncWorker.schedule(context)
                            }}) { Text(tr("remove")) }
                        }
                    }
                }
                "confirm" -> {
                    selected?.let { journey ->
                        Text("${journey.optString("origin")} → ${journey.optString("destination")}")
                        Text(tr("fare"))
                        Button(enabled=!busy,onClick={action {
                            repo.book(journey.getString("id"),requestId)
                            selected=null; navigate("tickets")
                        }}) { Text(tr("confirm")) }
                    }
                }
                "tickets" -> {
                    if(tickets.length()==0 && !busy) Text(tr("empty"))
                    tickets.rows().forEach { ticket ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text(ticket.optString("route"))
                            Text("${ticket.optString("train")} · R${ticket.optString("fare")}")
                            Text(ticket.optString("created_at").take(10))
                            TicketQr(ticket.getString("id"))
                            Text(ticket.getString("id"),style=MaterialTheme.typography.bodySmall)
                            Text(tr("academic"))
                        }}
                    }
                }
                "report" -> ReportForm(language,busy,incidentPhoto,{ photoPicker.launch("image/*") }) { type,location,description -> action {
                    val sent=repo.report(type,location,description,incidentPhoto,context.contentResolver)
                    incidentPhoto=null
                    message=tr(if(sent) "sent" else "queued")
                }}
                "reports" -> {
                    repo.local.pending(repo.userId).forEach { Text("${tr("pending")}: ${it.optString("type")} · ${it.optString("location")}") }
                    if(reports.length()==0 && !busy) Text(tr("empty"))
                    reports.rows().forEach { report ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("${report.optString("type")} · ${report.optString("location")}",style=MaterialTheme.typography.titleMedium)
                            Text(report.optString("description"))
                            if(report.optString("photo_path").isNotBlank()) {
                    Text(tr("incident_photo"),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.tertiary)
                            }
                        }}
                    }
                }
                "stations" -> schedules.rows().flatMap { listOf(it.optString("origin"),it.optString("destination")) }
                    .distinct().forEach { Text(it,style=MaterialTheme.typography.titleMedium) }
                "map" -> {
                    RailMap(tr("map"))
                    Text(tr("station_year"))
                    Button(onClick={navigate("timetable")}) { Text(tr("timetable")) }
                }
                "alerts" -> {
                    if(alerts.length()==0 && !busy) Text(tr("empty"))
                    alerts.rows().forEach { Text(it.optString("title"),style=MaterialTheme.typography.titleMedium); Text(it.optString("message")) }
                }
                "settings" -> SettingsForm(profile,language,busy) { value -> action {
                    repo.api("/profile","PATCH",value)
                    profile=value
                    language=value.getString("language")
                    prefs.edit().putString("language",language).putBoolean("notifications",value.getBoolean("notifications_enabled"))
                        .putBoolean("sync",value.getBoolean("offline_sync_enabled")).apply()
                    SyncWorker.schedule(context)
                    message=Labels.get("saved",value.getString("language"))
                }}
            }
        }
    }
}

@Composable
private fun AuthForm(register: Boolean,busy: Boolean,language: String,submit:(String,String,String,String)->Unit,toggle:()->Unit,google:()->Unit) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    fun tr(key:String)=Labels.get(key,language)
    if(register) {
        OutlinedTextField(name,{name=it},label={Text(tr("name"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(mobile,{mobile=it},label={Text(tr("mobile"))},modifier=Modifier.fillMaxWidth())
    }
    OutlinedTextField(email,{email=it},label={Text(tr("email"))},singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(password,{password=it},label={Text(tr("password"))},singleLine=true,
        visualTransformation=if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon={ IconButton(onClick={passwordVisible=!passwordVisible}) {
            Image(painterResource(if(passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                contentDescription=if(passwordVisible) "Hide password" else "Show password",modifier=Modifier.size(24.dp))
        } }, modifier=Modifier.fillMaxWidth())
    Button(enabled=!busy,onClick={submit(name,email,mobile,password)},modifier=Modifier.fillMaxWidth()) { Text(tr(if(register) "register" else "login")) }
    OutlinedButton(enabled=!busy,onClick=google,modifier=Modifier.fillMaxWidth()) { Text(tr("google")) }
    TextButton(enabled=!busy,onClick=toggle) { Text(tr(if(register) "login" else "register")) }
}

@Composable
private fun WelcomeScreen(start:()->Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF073C78),Color(0xFF0754A6),Color(0xFFF6F8FC))))) {
        Column(Modifier.fillMaxSize().padding(horizontal=28.dp, vertical=36.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Spacer(Modifier.height(80.dp))
            Image(painterResource(R.drawable.prasa_logo),"PRASA",Modifier.size(150.dp),contentScale=ContentScale.Fit)
            Spacer(Modifier.height(28.dp))
            Text("PASSENGER RAIL AGENCY OF SOUTH AFRICA",color=Color.White,style=MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text("PRASA",color=Color.White,style=MaterialTheme.typography.displaySmall)
            Text("CONNECT",color=Color(0xFFFFC928),style=MaterialTheme.typography.displaySmall)
                Text(Labels.get("welcome", "en"),color=Color.White,style=MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(72.dp))
            Button(onClick=start,modifier=Modifier.fillMaxWidth().height(64.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF0754A6))) {
                Text(Labels.get("start_app", "en"),style=MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ReportForm(language:String,busy:Boolean,photo:Uri?,pickPhoto:()->Unit,submit:(String,String,String)->Unit) {
    var type by remember { mutableStateOf("") }; var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context=LocalContext.current
    OutlinedTextField(type,{type=it},label={Text(Labels.get("type",language))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(location,{location=it},label={Text(Labels.get("location",language))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(description,{description=it},label={Text(Labels.get("description",language))},minLines=4,modifier=Modifier.fillMaxWidth())
    OutlinedButton(enabled=!busy,onClick=pickPhoto,modifier=Modifier.fillMaxWidth()) { Text(Labels.get(if(photo==null) "add_photo" else "change_photo",language)) }
    if(photo!=null) Image(painter=remember(photo) {
        val bitmap=context.contentResolver.openInputStream(photo)?.use { stream -> android.graphics.BitmapFactory.decodeStream(stream) }
            ?: android.graphics.Bitmap.createBitmap(1,1,android.graphics.Bitmap.Config.ARGB_8888)
        androidx.compose.ui.graphics.painter.BitmapPainter(bitmap.asImageBitmap())
    },contentDescription="Selected incident photo",modifier=Modifier.fillMaxWidth().height(180.dp),contentScale=ContentScale.Crop)
    Button(enabled=!busy,onClick={submit(type,location,description)}) { Text(Labels.get("submit",language)) }
}

@Composable
private fun SettingsForm(profile:JSONObject,language:String,busy:Boolean,save:(JSONObject)->Unit) {
    var name by remember(profile) { mutableStateOf(profile.optString("full_name")) }
    var mobile by remember(profile) { mutableStateOf(profile.optString("mobile")) }
    var lang by remember(profile) { mutableStateOf(profile.optString("language",language).takeIf { it in setOf("en","af","zu") } ?: language) }
    var notifications by remember(profile) { mutableStateOf(profile.optBoolean("notifications_enabled",true)) }
    var sync by remember(profile) { mutableStateOf(profile.optBoolean("offline_sync_enabled",true)) }
    OutlinedTextField(name,{name=it},label={Text(Labels.get("name",lang))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(mobile,{mobile=it},label={Text(Labels.get("mobile",lang))},modifier=Modifier.fillMaxWidth())
    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        FilterChip(lang=="en",{lang="en"},label={Text("English")})
        FilterChip(lang=="af",{lang="af"},label={Text("Afrikaans")})
        FilterChip(lang=="zu",{lang="zu"},label={Text("isiZulu")})
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(Labels.get("notifications",lang),Modifier.weight(1f)); Switch(notifications,{notifications=it})
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(Labels.get("sync",lang),Modifier.weight(1f)); Switch(sync,{sync=it})
    }
    Button(enabled=!busy,onClick={save(JSONObject().put("full_name",name).put("mobile",mobile).put("language",lang)
        .put("notifications_enabled",notifications).put("offline_sync_enabled",sync))}) { Text(Labels.get("save",lang)) }
}

@Composable
private fun RailMap(description:String) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transform = rememberTransformableState { zoom, pan, _ ->
        scale=(scale*zoom).coerceIn(1f,6f)
        offset=if(scale==1f) Offset.Zero else offset+pan
    }
    Box(Modifier.fillMaxWidth().height(500.dp).clipToBounds().transformable(transform)) {
        Image(painterResource(R.drawable.prasa_gauteng_rail_map),description,
            Modifier.fillMaxSize().graphicsLayer { scaleX=scale; scaleY=scale; translationX=offset.x; translationY=offset.y },
            contentScale=ContentScale.Fit)
    }
}

@Composable
private fun TicketQr(id:String) {
    val bitmap=remember(id) {
        val matrix=MultiFormatWriter().encode("prasa-connect:booking:$id",BarcodeFormat.QR_CODE,320,320)
        android.graphics.Bitmap.createBitmap(320,320,android.graphics.Bitmap.Config.ARGB_8888).apply {
            for(y in 0 until 320) for(x in 0 until 320) setPixel(x,y,if(matrix[x,y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    Image(bitmap.asImageBitmap(),"QR",Modifier.fillMaxWidth().height(200.dp))
}
