package io.beachforecast.widget

import io.beachforecast.R

class SmallWidgetProvider : BaseWidgetProvider() {
    override val layoutRes = R.layout.widget_small
    override val rootViewId = R.id.widget_small_root
    override val loadingTextViewId = R.id.widget_small_city
}
