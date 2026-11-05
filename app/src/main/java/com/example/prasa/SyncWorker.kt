package com.example.prasa

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        val repository = PrasaRepository(applicationContext)
        if (!repository.signedIn) return Result.success()
        return try {
            if (applicationContext.getSharedPreferences("preferences-${repository.userId}",0).getBoolean("sync",true)) {
                repository.sync()
                val routes = FirestoreRoutes()
                if (routes.isConfigured) {
                    routes.signIn(repository)
                    routes.sync(repository.userId, repository.local)
                }
            }
            // Push configuration must not prevent queued safety reports from syncing.
            try { PushSupport.register(applicationContext,repository) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { return Result.retry() }
            Result.success()
        }
        catch(e: ApiFailure) { if (e.status >= 500) Result.retry() else Result.failure() }
        catch(e: java.io.IOException) { Result.retry() }
        catch(e: kotlinx.coroutines.CancellationException) { throw e }
        catch(e: Exception) { Result.retry() }
    }
    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            // Periodic work is a recovery net; the connected one-time request still handles a fresh reconnect promptly.
            val periodic = PeriodicWorkRequestBuilder<SyncWorker>(15,TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "report-sync-periodic",ExistingPeriodicWorkPolicy.KEEP,periodic)
            WorkManager.getInstance(context).enqueueUniqueWork("report-sync",ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build())
        }
    }
}
