/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.annotation.SuppressLint
import android.app.PendingIntent
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
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import net.mm2d.customtabsbrowser.CustomTabsIntentReader.ButtonParams
import net.mm2d.customtabsbrowser.databinding.ActivityCustomTabsBinding

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsActivity : AppCompatActivity() {
    private lateinit var popupMenu: CustomOptionsMenuHelper
    private lateinit var reader: CustomTabsIntentReader
    private lateinit var connection: CustomTabsConnection
    private var tintedColor = Color.WHITE
    private var overridePackageName = false
    private lateinit var webView: WebView
    private lateinit var binding: ActivityCustomTabsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        reader = CustomTabsIntentReader(intent)
        connection = CustomTabsConnection(reader.callback)

        popupMenu = CustomOptionsMenuHelper(this, R.id.toolbar, R.id.action_overflow)
        customUi()
        val url = intent.dataString ?: "https://search.yahoo.co.jp/"
        val view = WebViewHolder.getWebView()
        if (view?.url == url) {
            webView = view
        } else {
            webView = WebViewHolder.createWebView(this)
            webView.loadUrl(url)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let {
            if (webView.url != it) {
                webView.loadUrl(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (webView.parent as? ViewGroup)?.removeView(webView)
        binding.webViewContainer.addView(webView)
        setUpWebView()
    }

    override fun onStop() {
        super.onStop()
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        binding.webViewContainer.removeAllViews()
        finish()
    }

    override fun onResume() {
        super.onResume()
        connection.onNavigationEvent(CustomTabsCallback.TAB_SHOWN)
    }

    override fun onPause() {
        super.onPause()
        connection.onNavigationEvent(CustomTabsCallback.TAB_HIDDEN)
    }

    private fun customUi() {
        val shouldUseWhiteForeground = reader.toolbarColor.shouldUseWhiteForeground()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = reader.toolbarColor
            val decorView = window.decorView
            decorView.systemUiVisibility = if (shouldUseWhiteForeground) {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        binding.toolbar.setBackgroundColor(reader.toolbarColor)
        binding.appBar.setBackgroundColor(reader.toolbarColor)
        binding.progressBar.progressDrawable = ContextCompat.getDrawable(
            this,
            if (shouldUseWhiteForeground) R.drawable.browser_progress_dark
            else R.drawable.browser_progress
        )
        if (shouldUseWhiteForeground) {
            setForegroundColor(R.color.text_main_dark, R.color.text_sub_dark)
        } else {
            setForegroundColor(R.color.text_main, R.color.text_sub)
        }
        binding.appBar.addOnOffsetChangedListener(OnOffsetChangedListener { _, offset ->
            if (offset == 0) {
                connection.onBottomBarScrollStateChanged(false)
            } else if (offset == -binding.toolbar.height) {
                connection.onBottomBarScrollStateChanged(true)
            }
        })
        binding.toolbar2.setBackgroundColor(reader.secondaryToolbarColor)
        binding.toolbar3.setBackgroundColor(reader.secondaryToolbarColor)
        AppCompatResources.getDrawable(this, R.drawable.ic_close)?.let {
            it.setTint(if (shouldUseWhiteForeground) Color.WHITE else Color.BLACK)
            binding.toolbar.navigationIcon = it
        }
        reader.closeIcon?.let { binding.toolbar.navigationIcon = BitmapDrawable(resources, it) }
        reader.actionButtonParams?.let { applyActionButtonParams(it) }
        if (!tryShowRemoteViews()) {
            applyToolbarButtonParamsList(reader.toolbarButtonParamsList)
        }
        if (reader.enableUrlBarHiding) {
            (binding.toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
    }

    private fun setForegroundColor(mainColorId: Int, subColorId: Int) {
        val mainColor = ContextCompat.getColor(this, mainColorId)
        val subColor = ContextCompat.getColor(this, subColorId)
        binding.toolbar.setTitleTextColor(mainColor)
        binding.toolbar.setSubtitleTextColor(subColor)
        binding.toolbar.overflowIcon?.setTint(mainColor)
        binding.toolbar.navigationIcon?.setTint(mainColor)
        tintedColor = mainColor
    }

    private fun applyActionButtonParams(params: ButtonParams) {
        binding.actionButton.visibility = View.VISIBLE
        binding.actionButton.setImageBitmap(params.icon)
        if (params.shouldTint) {
            binding.actionButton.setColorFilter(tintedColor)
        }
        if (params.pendingIntent != null) {
            binding.actionButton.setOnClickListener {
                sendPendingIntentWithUrl(params.pendingIntent)
            }
        }
    }

    private fun tryShowRemoteViews(): Boolean {
        val remoteViews = reader.remoteViews ?: return false
        val inflatedViews = remoteViews.apply(this, binding.toolbar3)
        binding.toolbar3.visibility = View.VISIBLE
        binding.toolbar3.addView(inflatedViews)
        val pendingIntent = reader.remoteViewsPendingIntent ?: return true
        reader.remoteViewsClickableIDs?.filter { it >= 0 }?.forEach {
            inflatedViews.findViewById<View>(it)?.setOnClickListener { v ->
                sendPendingIntentOnClick(pendingIntent, v.id)
            }
        }
        return true
    }

    private fun applyToolbarButtonParamsList(list: List<ButtonParams>) {
        if (list.isEmpty()) {
            return
        }
        binding.toolbar2.visibility = View.VISIBLE
        val layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT)
            .also { it.weight = 1f }
        list.forEach {
            val button =
                layoutInflater.inflate(R.layout.buttom_button, binding.toolbar2, false) as ImageView
            button.id = it.id
            button.setImageBitmap(it.icon)
            it.pendingIntent?.let { pendingIntent ->
                button.setOnClickListener { v ->
                    sendPendingIntentOnClick(pendingIntent, v.id)
                }
            }
            binding.toolbar2.addView(button, layoutParams)
        }
    }

    @Suppress("OverridingDeprecatedMember")
    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        webView.url?.let {
            if (it.isNotEmpty()) supportActionBar?.subtitle = it
        }
        webView.title?.let {
            if (it.isNotEmpty()) supportActionBar?.title = it
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (reader.shouldShowTitle) supportActionBar?.title = title
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.progress = 0
                binding.progressBar.visibility = View.VISIBLE
                supportActionBar?.subtitle = url
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_STARTED)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.INVISIBLE
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.cancel()
                connection.onNavigationEvent(CustomTabsCallback.NAVIGATION_FAILED)
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_overflow).icon.setTint(tintedColor)
        if (!reader.shouldShowShareMenuItem) {
            menu.removeItem(R.id.action_share)
        }
        reader.menuParamsList.forEachIndexed { index, menuParams ->
            menu.add(
                R.id.overflow,
                CUSTOM_MENU_ID_START + index,
                CUSTOM_MENU_ORDER_START + index,
                menuParams.title
            ).isVisible = false
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        popupMenu.onPrepareOptionsMenu(menu, R.id.overflow)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val customMenuRange =
            CUSTOM_MENU_ID_START until CUSTOM_MENU_ID_START + reader.menuParamsList.size
        when (item.itemId) {
            R.id.action_overflow -> popupMenu.onSelectOverflowMenu()
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
            reader.menuParamsList[index].pendingIntent?.send(this, 0, null)
        } catch (ignored: Throwable) {
        }
    }

    private fun sendPendingIntentOnClick(pendingIntent: PendingIntent, id: Int) {
        val addedIntent = Intent().also {
            it.putExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, id)
            it.data = Uri.parse(webView.url)
        }
        try {
            pendingIntent.send(this, 0, addedIntent)
        } catch (ignored: Throwable) {
        }
    }

    private fun sendPendingIntentWithUrl(pendingIntent: PendingIntent) {
        val addedIntent = Intent().also {
            it.data = Uri.parse(webView.url)
        }
        try {
            pendingIntent.send(this, 0, addedIntent)
        } catch (ignored: Throwable) {
        }
    }

    private fun onSelectShare() {
        val url = webView.url ?: return
        if (!URLUtil.isNetworkUrl(url)) {
            return
        }
        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setText(url)
            .setSubject(webView.title)
            .setChooserTitle(R.string.action_share)
            .startChooser()
    }

    private fun onSelectOpenByBrowser() {
        BrowserActivity.startFromCustomTabs(this, webView)
        finish()
        connection.onOpenInBrowser()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
