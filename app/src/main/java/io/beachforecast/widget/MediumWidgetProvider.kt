package io.beachforecast.widget

import io.beachforecast.R

class MediumWidgetProvider : BaseWidgetProvider() {
    override val layoutRes = R.layout.widget_medium
    override val rootViewId = R.id.widget_medium_root
    override val loadingTextViewId = R.id.widget_medium_city
}
