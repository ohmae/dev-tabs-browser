package net.mm2d.customtabsbrowser

import android.app.Activity
import android.content.res.Configuration
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

object WebViewNightMode {
    fun apply(activity: Activity, webView: WebView) {
        val webSettings = webView.settings
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            val nightMode =
                (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val forceDarkMode =
                if (nightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            WebSettingsCompat.setForceDark(webSettings, forceDarkMode)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(
                webSettings, WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
            )
        }
    }
}
