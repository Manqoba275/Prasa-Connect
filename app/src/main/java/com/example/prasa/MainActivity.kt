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
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
        setContent { PrasaTheme(dynamicColor = false) { PrasaApp(callback) { callback = null } } }
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
    val scope = rememberCoroutineScope()
    val prefs = remember(repo.userId) { context.getSharedPreferences("preferences-${repo.userId}",0) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        PushSupport.initialize(context)
        if(android.os.Build.VERSION.SDK_INT>=33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    var language by remember(prefs) { mutableStateOf(prefs.getString("language","en") ?: "en") }
    fun tr(key: String) = Labels.get(key,language)
    var signedIn by remember { mutableStateOf(repo.signedIn) }
    var page by remember { mutableStateOf(if (repo.signedIn) "home" else "login") }
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
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }

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
            try { repo.finishGoogle(callback); signedIn=true; page="home"; SyncWorker.schedule(context) }
            catch(e: Exception) { message=e.message ?: tr("error") }
            finally { consumed(); busy=false }
        }
    }
    BackHandler(page !in listOf("home","login")) { page=if(signedIn) "home" else "login" }
    Scaffold(
        topBar={ TopAppBar(title={ Text(if(page=="home") "PRASA Connect" else tr(page)) },
            navigationIcon={ if(signedIn && page!="home") TextButton(onClick={navigate("home")}) { Text(tr("home")) } },
            actions={
                if(signedIn) TextButton(enabled=!busy,onClick={
                    action {
                        try { repo.logout() } finally {
                            signedIn=false; page="login"; tickets=JSONArray(); reports=JSONArray(); selected=null
                        }
                    }
                }) { Text(tr("logout")) }
            }) },
        bottomBar={ if(signedIn) NavigationBar {
            listOf("home","timetable","tickets","settings").forEach { key ->
                NavigationBarItem(selected=page==key,onClick={navigate(key)},
                    icon={ Icon(when(key) { "home"->Icons.Default.Home; "timetable"->Icons.Default.DateRange; "tickets"->Icons.Default.List; else->Icons.Default.Settings },contentDescription=null) },label={Text(tr(key))})
            }
        }}
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.error)
            if(offline && signedIn) Text(tr("cached"))
            if(!signedIn) {
                Image(painterResource(R.drawable.prasa_logo),"PRASA",Modifier.fillMaxWidth().height(100.dp),contentScale=ContentScale.Fit)
                AuthForm(page=="register",busy,language,
                    submit={ name,email,mobile,password -> action {
                        if(page=="register") repo.register(name,email,mobile,password) else repo.login(email,password)
                        signedIn=true; page="home"; SyncWorker.schedule(context)
                    }},
                    toggle={ navigate(if(page=="register") "login" else "register") },
                    google={ runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(repo.googleUrl()))) }
                        .onFailure { message=it.message ?: tr("error") } })
            } else when(page) {
                "home" -> {
                    Text(tr("welcome"),style=MaterialTheme.typography.headlineSmall)
                    Text(tr("sample"))
                    listOf("timetable","tickets","favorites","report","reports","stations","map","alerts","settings").forEach { key ->
                        OutlinedButton(onClick={navigate(key)},modifier=Modifier.fillMaxWidth()) { Text(tr(key)) }
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
                                repo.local.saveRoute(repo.userId,schedule.getString("origin"),schedule.getString("destination"))
                                message=tr("saved")
                            }}) { Text(tr("favorite")) }
                        }}
                    }
                }
                "favorites" -> {
                    val favorites=repo.local.routes(repo.userId)
                    if(favorites.isEmpty()) Text(tr("empty"))
                    favorites.forEach { (origin,destination) ->
                        Text("$origin → $destination",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            Button(onClick={from=origin; to=destination; navigate("timetable")}) { Text(tr("search")) }
                            TextButton(enabled=!busy,onClick={action {
                                repo.local.removeRoute(repo.userId,origin,destination)
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
                "report" -> ReportForm(language,busy) { type,location,description -> action {
                    val sent=repo.report(type,location,description)
                    message=tr(if(sent) "sent" else "queued")
                }}
                "reports" -> {
                    repo.local.pending(repo.userId).forEach { Text("${tr("pending")}: ${it.optString("type")} · ${it.optString("location")}") }
                    if(reports.length()==0 && !busy) Text(tr("empty"))
                    reports.rows().forEach { Text("${it.optString("type")} · ${it.optString("location")}\n${it.optString("description")}") }
                }
                "stations" -> schedules.rows().flatMap { listOf(it.optString("origin"),it.optString("destination")) }
                    .distinct().forEach { Text(it,style=MaterialTheme.typography.titleMedium) }
                "map" -> {
                    RailMap(tr("map"))
                    Text("PRASA · Gauteng · 2024")
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
                    message=tr("saved")
                }}
            }
        }
    }
}

@Composable
private fun AuthForm(register: Boolean,busy: Boolean,language: String,submit:(String,String,String,String)->Unit,toggle:()->Unit,google:()->Unit) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    fun tr(key:String)=Labels.get(key,language)
    if(register) {
        OutlinedTextField(name,{name=it},label={Text(tr("name"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(mobile,{mobile=it},label={Text(tr("mobile"))},modifier=Modifier.fillMaxWidth())
    }
    OutlinedTextField(email,{email=it},label={Text(tr("email"))},singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(password,{password=it},label={Text(tr("password"))},singleLine=true,
        visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    Button(enabled=!busy,onClick={submit(name,email,mobile,password)},modifier=Modifier.fillMaxWidth()) { Text(tr(if(register) "register" else "login")) }
    OutlinedButton(enabled=!busy,onClick=google,modifier=Modifier.fillMaxWidth()) { Text(tr("google")) }
    TextButton(enabled=!busy,onClick=toggle) { Text(tr(if(register) "login" else "register")) }
}

@Composable
private fun ReportForm(language:String,busy:Boolean,submit:(String,String,String)->Unit) {
    var type by remember { mutableStateOf("") }; var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    OutlinedTextField(type,{type=it},label={Text(Labels.get("type",language))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(location,{location=it},label={Text(Labels.get("location",language))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(description,{description=it},label={Text(Labels.get("description",language))},minLines=4,modifier=Modifier.fillMaxWidth())
    Button(enabled=!busy,onClick={submit(type,location,description)}) { Text(Labels.get("submit",language)) }
}

@Composable
private fun SettingsForm(profile:JSONObject,language:String,busy:Boolean,save:(JSONObject)->Unit) {
    var name by remember(profile) { mutableStateOf(profile.optString("full_name")) }
    var mobile by remember(profile) { mutableStateOf(profile.optString("mobile")) }
    var lang by remember(profile) { mutableStateOf(profile.optString("language",language)) }
    var notifications by remember(profile) { mutableStateOf(profile.optBoolean("notifications_enabled",true)) }
    var sync by remember(profile) { mutableStateOf(profile.optBoolean("offline_sync_enabled",true)) }
    OutlinedTextField(name,{name=it},label={Text(Labels.get("name",language))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(mobile,{mobile=it},label={Text(Labels.get("mobile",language))},modifier=Modifier.fillMaxWidth())
    Row { FilterChip(lang=="en",{lang="en"},label={Text("English")}); Spacer(Modifier.width(8.dp)); FilterChip(lang=="af",{lang="af"},label={Text("Afrikaans")}) }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(Labels.get("notifications",language),Modifier.weight(1f)); Switch(notifications,{notifications=it})
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(Labels.get("sync",language),Modifier.weight(1f)); Switch(sync,{sync=it})
    }
    Button(enabled=!busy,onClick={save(JSONObject().put("full_name",name).put("mobile",mobile).put("language",lang)
        .put("notifications_enabled",notifications).put("offline_sync_enabled",sync))}) { Text(Labels.get("save",language)) }
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
