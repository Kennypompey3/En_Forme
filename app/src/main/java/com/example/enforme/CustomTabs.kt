package com.example.enforme

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun openCustomTab(activity: Activity, url: String) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(activity, Uri.parse(url))
}