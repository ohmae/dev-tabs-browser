/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.os.Bundle
import android.os.SystemClock
import androidx.browser.customtabs.CustomTabsCallback

class CustomTabsConnection(private val callback: CustomTabsCallback?) {
    private var hidden: Boolean = false
    fun onNavigationEvent(event: Int) {
        callback?.onNavigationEvent(
            event,
            Bundle().also { it.putLong("timestampUptimeMillis", SystemClock.uptimeMillis()) })
    }

    fun onBottomBarScrollStateChanged(hidden: Boolean) {
        if (this.hidden == hidden) return
        this.hidden = hidden
        callback?.extraCallback(
            BOTTOM_BAR_SCROLL_STATE_CALLBACK,
            Bundle().also { it.putBoolean("hidden", hidden) })
    }

    fun onOpenInBrowser() {
        callback?.extraCallback(
            OPEN_IN_BROWSER_CALLBACK,
            Bundle().also { it.putLong("timestampUptimeMillis", SystemClock.uptimeMillis()) })
    }

    companion object {
        private const val BOTTOM_BAR_SCROLL_STATE_CALLBACK = "onBottomBarScrollStateChanged"
        private const val OPEN_IN_BROWSER_CALLBACK = "onOpenInBrowser"
    }
}
