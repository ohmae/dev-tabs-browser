/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class IntentDispatcher : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatch()
        finish()
    }

    private fun dispatch() {
        val launchIntent = intent ?: return
        if (isCustomTabIntent(launchIntent)) {
            launchIntent.setClass(this, CustomTabsActivity::class.java)
        } else {
            launchIntent.setClass(this, BrowserActivity::class.java)
        }
        try {
            startActivity(launchIntent)
            overridePendingTransition(0, 0)
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun isCustomTabIntent(intent: Intent): Boolean {
        if (CustomTabsIntent.shouldAlwaysUseBrowserUI(intent)) return false
        if (!intent.hasExtra(CustomTabsIntent.EXTRA_SESSION)) return false
        return intent.data != null
    }
}
