package com.example.prasa

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object PushSupport {
    fun initialize(context:Context):Boolean {
        if (FirebaseApp.getApps(context).isNotEmpty()) return true
        if (BuildConfig.FIREBASE_APP_ID.isBlank() || BuildConfig.FIREBASE_API_KEY.isBlank() ||
            BuildConfig.FIREBASE_SENDER_ID.isBlank() || BuildConfig.FIREBASE_PROJECT_ID.isBlank()) return false
        FirebaseApp.initializeApp(context,FirebaseOptions.Builder()
            .setApplicationId(BuildConfig.FIREBASE_APP_ID).setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID).setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID).build())
        return true
    }
    suspend fun register(context:Context,repository:PrasaRepository) {
        if (!initialize(context)) return
        val enabled=context.getSharedPreferences("preferences-${repository.userId}",0).getBoolean("notifications",true)
        if (!enabled) { repository.api("/devices","DELETE"); return }
        val token=withContext(Dispatchers.IO) { Tasks.await(FirebaseMessaging.getInstance().token) }
        repository.api("/devices","POST",JSONObject().put("token",token))
    }
}

// Data-only FCM messages respect local preferences, even while the app is backgrounded.
class PrasaMessagingService:FirebaseMessagingService() {
    override fun onNewToken(token:String) { SyncWorker.schedule(this) }
    override fun onMessageReceived(message:RemoteMessage) {
        val userId=SessionStore(this).load()?.optJSONObject("user")?.optString("id") ?: return
        if (!getSharedPreferences("preferences-$userId",0).getBoolean("notifications",true)) return
        if (Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        if (Build.VERSION.SDK_INT>=26) getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("service-alerts","PRASA service alerts",NotificationManager.IMPORTANCE_DEFAULT))
        val intent=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification=NotificationCompat.Builder(this,"service-alerts")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.data["title"] ?: "PRASA Connect")
            .setContentText(message.data["message"] ?: "")
            .setContentIntent(intent).setAutoCancel(true).build()
        NotificationManagerCompat.from(this).notify((message.messageId ?: message.data.toString()).hashCode(),notification)
    }
}
