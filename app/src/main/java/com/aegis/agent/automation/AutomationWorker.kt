package com.aegis.agent.automation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AutomationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
