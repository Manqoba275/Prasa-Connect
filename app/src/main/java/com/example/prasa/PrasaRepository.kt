package com.example.prasa

import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.TimeUnit

class ApiFailure(val status: Int, message: String) : IOException(message)

class PrasaRepository(context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()) {
    private val app = context.applicationContext
    private val sessions = SessionStore(app)
    val local = LocalStore(app)
    private val oauth = app.getSharedPreferences("oauth", Context.MODE_PRIVATE)
    var session: JSONObject? = sessions.load()
        private set
    val userId get() = session?.optJSONObject("user")?.optString("id").orEmpty()
    val signedIn get() = userId.isNotEmpty()
    companion object {
        const val URL = "https://awbgflgcqjofsnagkzou.supabase.co"
        // Publishable client key; privileged keys must never be included in the APK.
        const val KEY = "sb_publishable_-v1DG7KYx3TXyGc3SFlFkQ___SritYC"
        private val sessionMutex = Mutex()
    }

    private suspend fun request(path: String, method: String = "GET", body: JSONObject? = null, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder().url(URL + path).header("apikey", KEY)
            if (token != null) builder.header("Authorization", "Bearer $token")
            if (method != "GET") builder.method(method, (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType()))
            client.newCall(builder.build()).execute().use { response ->
                Log.i("PrasaApi", "$method ${path.substringBefore('?')} -> ${response.code}")
                val raw = response.body?.string().orEmpty()
                val data = if (raw.isBlank()) JSONObject() else runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
                if (!response.isSuccessful) throw ApiFailure(response.code,
                    data.optString("message", data.optString("msg", data.optString("error_description", "Request failed (${response.code})"))))
                data
            }
        }

    private fun accept(data: JSONObject) {
        require(data.has("access_token") && data.has("refresh_token")) { "Check your email to confirm your account, then sign in." }
        if (!data.has("expires_at")) data.put("expires_at", System.currentTimeMillis()/1000 + data.optLong("expires_in",3600))
        session = data
        sessions.save(data)
    }
    suspend fun login(email: String, password: String) {
        PrasaRules.validateLogin(email,password)?.let { throw IllegalArgumentException(it) }
        accept(request("/auth/v1/token?grant_type=password", "POST", JSONObject().put("email",email.trim()).put("password",password)))
    }
    suspend fun register(name: String, email: String, mobile: String, password: String) {
        PrasaRules.validateRegistration(name,email,mobile,password)?.let { throw IllegalArgumentException(it) }
        accept(request("/auth/v1/signup", "POST", JSONObject().put("email",email.trim()).put("password",password)
            .put("data",JSONObject().put("full_name",name.trim()).put("mobile",mobile.trim()))))
    }
    private suspend fun token(): String = sessionMutex.withLock {
        val expectedOwner = userId
        val current = sessions.load() ?: throw ApiFailure(401,"Please sign in.")
        // A background worker must never upload the previous account's queue as a new user.
        if (current.optJSONObject("user")?.optString("id") != expectedOwner)
            throw ApiFailure(401,"Account changed. Please reopen this screen.")
        session = current
        if (current.optLong("expires_at") <= System.currentTimeMillis()/1000+60) {
            accept(request("/auth/v1/token?grant_type=refresh_token","POST",
                JSONObject().put("refresh_token",current.getString("refresh_token"))))
        }
        session!!.getString("access_token")
    }
    suspend fun api(path: String, method: String = "GET", body: JSONObject? = null): JSONObject =
        request("/functions/v1/prasa-api$path", method, body, token())

    suspend fun logout() {
        try {
            runCatching { api("/devices","DELETE") }
            request("/auth/v1/logout", "POST", token = token())
        }
        finally { session = null; sessions.clear() }
    }
    suspend fun rows(path: String): Pair<JSONArray,Boolean> {
        val owner = userId
        return try {
            val data = api(path).getJSONArray("data")
            local.cache(owner,path,data.toString())
            data to false
        } catch (e: IOException) {
            if (e is ApiFailure) throw e
            val cached = local.read(owner,path) ?: throw e
            JSONArray(cached) to true
        }
    }
    suspend fun profile(): JSONObject {
        val response = api("/profile").optJSONObject("data")
        if (response != null) return response
        val metadata = session?.optJSONObject("user")?.optJSONObject("user_metadata") ?: JSONObject()
        return JSONObject().put("full_name",metadata.optString("full_name")).put("mobile",metadata.optString("mobile"))
            .put("language","en").put("notifications_enabled",true).put("offline_sync_enabled",true)
    }
    suspend fun firebaseCustomToken(): String = request("/functions/v1/firebase-token", "POST", token = token()).getString("token")
    suspend fun book(schedule: String, requestId: String): JSONObject =
        api("/bookings","POST",JSONObject().put("scheduleId",schedule).put("requestId",requestId)).getJSONObject("data")
    suspend fun report(type: String, location: String, description: String, photo: Uri? = null, resolver: ContentResolver? = null): Boolean {
        PrasaRules.validateIncident(type,location,description)?.let { throw IllegalArgumentException(it) }
        val body = JSONObject().put("id",UUID.randomUUID().toString()).put("type",type.trim())
            .put("location",location.trim()).put("description",description.trim())
        return try {
            if (photo != null) {
                val contentResolver = requireNotNull(resolver) { "Unable to read the selected photo." }
                val mime = contentResolver.getType(photo) ?: "image/jpeg"
                require(mime in setOf("image/jpeg", "image/png", "image/webp")) { "Choose a JPG, PNG, or WebP image." }
                val bytes = contentResolver.openInputStream(photo)?.use { it.readBytes() }
                    ?: error("Unable to read the selected photo.")
                require(bytes.size <= 5 * 1024 * 1024) { "Photo must be 5 MB or smaller." }
                val objectPath = "$userId/${body.getString("id")}.${when(mime) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }}"
                uploadIncidentPhoto(objectPath, bytes, mime)
                body.put("photo_path", objectPath)
            }
            api("/incidents","POST",body); true
        }
        catch(e: IOException) {
            if (e is ApiFailure || (photo != null && !body.has("photo_path"))) throw e
            local.enqueue(userId,body)
            SyncWorker.schedule(app)
            false
        }
    }


    private suspend fun uploadIncidentPhoto(path: String, bytes: ByteArray, mime: String) = withContext(Dispatchers.IO) {
        val response = client.newCall(Request.Builder()
            .url("$URL/storage/v1/object/incident-photos/${Uri.encode(path, "/")}")
            .header("apikey", KEY)
            .header("Authorization", "Bearer ${token()}")
            .header("Content-Type", mime)
            .header("x-upsert", "false")
            .post(bytes.toRequestBody(mime.toMediaType()))
            .build()).execute()
        response.use {
            if (!it.isSuccessful) throw ApiFailure(it.code, "Photo upload failed (${it.code}).")
        }
    }
    suspend fun sync(): Int {
        val owner = userId
        var count = 0
        for (body in local.pending(owner)) {
            api("/incidents","POST",body)
            local.acknowledge(body.getString("id"),owner)
            count++
        }
        return count
    }
    fun googleUrl(): String {
        val verifier = Base64.encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) },Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        oauth.edit().putString("verifier",verifier).putLong("started",System.currentTimeMillis()).commit()
        val challenge = Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return Uri.parse("$URL/auth/v1/authorize").buildUpon().appendQueryParameter("provider","google")
            .appendQueryParameter("redirect_to","prasaconnect://auth/callback")
            .appendQueryParameter("code_challenge",challenge).appendQueryParameter("code_challenge_method","s256").build().toString()
    }
    suspend fun googleEnabled(): Boolean = request("/auth/v1/settings").optJSONObject("external")?.optBoolean("google") == true
    suspend fun finishGoogle(uri: Uri) {
        require(uri.scheme=="prasaconnect" && uri.host=="auth" && uri.path=="/callback") { "Invalid sign-in link." }
        val oauthError = uri.getQueryParameter("error_description")
            ?: uri.getQueryParameter("error")
        if (!oauthError.isNullOrBlank()) throw IOException("Google sign-in failed: ${oauthError.replace('+',' ')}")
        val verifier = oauth.getString("verifier",null) ?: error("Sign-in expired. Try again.")
        require(System.currentTimeMillis()-oauth.getLong("started",0) < 600000) { "Sign-in expired. Try again." }
        val code = uri.getQueryParameter("code") ?: error("Google sign-in was not completed.")
        try { accept(request("/auth/v1/token?grant_type=pkce","POST",JSONObject().put("auth_code",code).put("code_verifier",verifier))) }
        finally { oauth.edit().clear().commit() }
    }
}
