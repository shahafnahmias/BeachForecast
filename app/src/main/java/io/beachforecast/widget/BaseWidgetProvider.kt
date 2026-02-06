package io.beachforecast.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.beachforecast.MainActivity
import io.beachforecast.R

/**
 * Shared base for all 3 widget sizes.
 * Shows an immediate loading state then delegates to WorkManager.
 */
abstract class BaseWidgetProvider : AppWidgetProvider() {

    abstract val layoutRes: Int
    abstract val rootViewId: Int
    abstract val loadingTextViewId: Int

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)
            views.setTextViewText(loadingTextViewId, context.getString(R.string.widget_loading))

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(rootViewId, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        WidgetUpdateHelper.enqueueUpdate(context)
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget of this type is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget of this type is removed
    }
}
