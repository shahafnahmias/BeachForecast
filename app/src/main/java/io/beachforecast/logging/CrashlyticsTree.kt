package io.beachforecast.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Custom Timber tree that forwards WARN+ logs to Firebase Crashlytics in production.
 * - WARN messages are logged as breadcrumbs (Crashlytics.log)
 * - ERROR messages with throwables are recorded as non-fatal exceptions
 * - Below WARN is ignored to avoid noise in Crashlytics
 */
class CrashlyticsTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.log("${priorityLabel(priority)}/${tag ?: "---"}: $message")

        if (t != null && priority >= Log.ERROR) {
            crashlytics.recordException(t)
        }
    }

    private fun priorityLabel(priority: Int): String = when (priority) {
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> priority.toString()
    }
}
