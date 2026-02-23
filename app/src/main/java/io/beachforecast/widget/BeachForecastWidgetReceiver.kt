package io.beachforecast.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Base receiver for all Beach Forecast Glance widgets.
 * Triggers a WorkManager update whenever the system sends APPWIDGET_UPDATE
 * (i.e., when a widget is first added to the home screen, on periodic refresh,
 * or when the user pin-requests an update).
 */
abstract class BeachForecastWidgetReceiver : GlanceAppWidgetReceiver() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateHelper.enqueueUpdate(context)
    }
}
