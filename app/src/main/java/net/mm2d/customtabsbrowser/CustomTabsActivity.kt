/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_custom_tabs.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsActivity : AppCompatActivity() {
    private lateinit var popupMenu: PopupMenuHelper
    private lateinit var reader: CustomTabsIntentReader
    private lateinit var connection: CustomTabsConnection
    private var tintedColor = Color.WHITE
    private var darkToolbar = false
    private var darkToolbar2 = false
    private var overridePackageName = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_tabs)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        reader = CustomTabsIntentReader(intent)
        connection = CustomTabsConnection(reader.callback)
        darkToolbar = isDarkColor(reader.toolbarColor)
        darkToolbar2 = isDarkColor(reader.secondaryToolbarColor)
        popupMenu = PopupMenuHelper(this, R.id.toolbar)
        customUi()
        setUpWebView()
        if (intent.dataString != null) {
            web_view.loadUrl(intent.dataString)
        } else {
            web_view.loadUrl("https://search.yahoo.co.jp/")
        }
    }

    private fun isDarkColor(color: Int): Boolean {
        return getBrightness(color) < 128
    }

    private fun getBrightness(color: Int): Int {
        val r = 0xff and color.ushr(16)
        val g = 0xff and color.ushr(8)
        val b = 0xff and color
        return MathUtils.clamp(Math.round(r * 0.299f + g * 0.587f + b * 0.114f), 0, 255)
    }

    private fun customUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = reader.toolbarColor
            if (!darkToolbar) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        toolbar.setBackgroundColor(reader.toolbarColor)
        app_bar.setBackgroundColor(reader.toolbarColor)
        toolbar2.setBackgroundColor(reader.secondaryToolbarColor)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        progress_bar.progressDrawable = if (darkToolbar) {
            ContextCompat.getDrawable(this, R.drawable.browser_progress_dark)
        } else {
            ContextCompat.getDrawable(this, R.drawable.browser_progress)
        }
        if (darkToolbar) {
            setForegroundColor(
                ContextCompat.getColor(this, R.color.text_main_dark),
                ContextCompat.getColor(this, R.color.text_sub_dark)
            )
        } else {
            setForegroundColor(
                ContextCompat.getColor(this, R.color.text_main),
                ContextCompat.getColor(this, R.color.text_sub)
            )
        }
        reader.closeIcon?.let {
            toolbar.navigationIcon = BitmapDrawable(resources, it)
        }
        reader.actionButtonParams?.also {
            action_button.visibility = View.VISIBLE
            action_button.setImageBitmap(it.icon)
            if (it.shouldTint) {
                action_button.setColorFilter(tintedColor)
            }
            if (it.pendingIntent != null) {
                action_button.setOnClickListener { _ ->
                    sendPendingIntentWithUrl(it.pendingIntent, null)
                }
            }
        }
        if (reader.toolbarButtonParamsList.isNotEmpty()) {
            toolbar2.visibility = View.VISIBLE
            toolbar2.setBackgroundColor(reader.secondaryToolbarColor)
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            layoutParams.weight = 1f
            reader.toolbarButtonParamsList.forEach {
                val button: ImageView = layoutInflater.inflate(R.layout.buttom_button, toolbar2, false) as ImageView
                button.setImageBitmap(it.icon)
                button.id = it.id
                it.pendingIntent?.let { pendingIntent ->
                    button.setOnClickListener { v ->
                        sendPendingIntentWithUrl(pendingIntent,
                            Intent().apply { putExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, v.id) })
                    }
                }
                toolbar2.addView(button, layoutParams)
            }
        }
    }

    private fun setForegroundColor(mainColor: Int, subColor: Int) {
        toolbar.setTitleTextColor(mainColor)
        toolbar.setSubtitleTextColor(subColor)
        toolbar.overflowIcon?.setTint(mainColor)
        toolbar.navigationIcon?.setTint(mainColor)
        tintedColor = mainColor
    }

    @Suppress("OverridingDeprecatedMember")
    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        if (reader.enableUrlBarHiding) {
            (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
        web_view.settings.javaScriptEnabled = true
        web_view.settings.setSupportZoom(true)
        web_view.settings.builtInZoomControls = true
        web_view.settings.displayZoomControls = false
        web_view.settings.useWideViewPort = true
        web_view.settings.loadWithOverviewMode = true
        web_view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress_bar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (reader.shouldShowTitle) supportActionBar?.title = title
            }
        }
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress_bar.progress = 0
                progress_bar.visibility = View.VISIBLE
                supportActionBar?.subtitle = url
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_STARTED)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress_bar.visibility = View.INVISIBLE
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.cancel()
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_overflow).icon.setTint(tintedColor)
        if (!reader.shouldShowShareMenuItem) {
            menu.removeItem(R.id.action_share)
        }
        reader.menuParamList.forEachIndexed { index, menuParams ->
            menu.add(R.id.overflow, CUSTOM_MENU_ID_START + index, CUSTOM_MENU_ORDER_START + index, menuParams.label)
        }
        popupMenu.onCreateOptionsMenu(menu, R.id.overflow)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val customMenuRange = CUSTOM_MENU_ID_START until CUSTOM_MENU_ID_START + reader.menuParamList.size
        when (item.itemId) {
            R.id.action_overflow -> popupMenu.show()
            R.id.action_share -> onSelectShare()
            R.id.action_open_by_browser -> onSelectOpenByBrowser()
            android.R.id.home -> finish()
            in customMenuRange -> onSelectCustomMenu(item.itemId - CUSTOM_MENU_ID_START)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun onSelectCustomMenu(index: Int) {
        try {
            reader.menuParamList[index].pendingIntent?.send(this, 0, null)
        } catch (ignored: Throwable) {
        }
    }

    private fun sendPendingIntentWithUrl(pendingIntent: PendingIntent, extraIntent: Intent?) {
        val addedIntent = if (extraIntent == null) Intent() else Intent(extraIntent)
        addedIntent.data = Uri.parse(web_view.url)
        try {
            pendingIntent.send(this, 0, addedIntent)
        } catch (ignored: Throwable) {
        }
    }

    private fun onSelectShare() {
        val url = web_view.url ?: return
        if (!URLUtil.isNetworkUrl(url)) {
            return
        }
        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setText(url)
            .setSubject(web_view.title)
            .setChooserTitle(R.string.action_share)
            .startChooser()
    }

    private fun onSelectOpenByBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(web_view.url))
        intent.setClass(this, BrowserActivity::class.java)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
        }
        finish()
    }

    override fun getPackageName(): String {
        if (overridePackageName) return reader.clientPackageName
            ?: super.getPackageName()
        return super.getPackageName()
    }

    override fun finish() {
        super.finish()
        if (reader.hasExitAnimation) {
            overridePackageName = true
            overridePendingTransition(reader.enterAnimationRes, reader.exitAnimationRes)
            overridePackageName = false
        }
    }

    companion object {
        private const val CUSTOM_MENU_ID_START = 10
        private const val CUSTOM_MENU_ORDER_START = 10
    }
}
