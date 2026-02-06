package io.beachforecast.widget

import io.beachforecast.R

class LargeWidgetProvider : BaseWidgetProvider() {
    override val layoutRes = R.layout.widget_large
    override val rootViewId = R.id.widget_large_root
    override val loadingTextViewId = R.id.widget_large_city
}
