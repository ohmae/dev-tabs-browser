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

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsConnection(val callback: CustomTabsCallback?) {
    fun onNavigationEvent(event: Int) {
        callback?.onNavigationEvent(
            event,
            Bundle().also { it.putLong("timestampUptimeMillis", SystemClock.uptimeMillis()) })
    }
}