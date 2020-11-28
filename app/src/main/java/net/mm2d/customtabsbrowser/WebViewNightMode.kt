/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.app.Activity
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import net.mm2d.customtabsbrowser.extension.isNightMode

object WebViewNightMode {
    fun apply(activity: Activity, webView: WebView) {
        val webSettings = webView.settings
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            val forceDarkMode =
                if (activity.isNightMode()) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            WebSettingsCompat.setForceDark(webSettings, forceDarkMode)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(
                webSettings, WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
            )
        }
    }
}
