package com.example.prasa

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        val repository = PrasaRepository(applicationContext)
        if (!repository.signedIn) return Result.success()
        return try {
            if (applicationContext.getSharedPreferences("preferences-${repository.userId}",0).getBoolean("sync",true)) repository.sync()
            // Push configuration must not prevent queued safety reports from syncing.
            try { PushSupport.register(applicationContext,repository) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { return Result.retry() }
            Result.success()
        }
        catch(e: ApiFailure) { if (e.status >= 500) Result.retry() else Result.failure() }
        catch(e: java.io.IOException) { Result.retry() }
    }
    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            WorkManager.getInstance(context).enqueueUniqueWork("report-sync",ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build())
        }
    }
}
