package io.beachforecast.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Single place to trigger widget updates from anywhere in the app.
 * Uses WorkManager with network constraint for reliable background execution.
 */
object WidgetUpdateHelper {

    fun enqueueUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
