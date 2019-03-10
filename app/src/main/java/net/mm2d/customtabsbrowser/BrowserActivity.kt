/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_browser.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class BrowserActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        supportActionBar?.title = ""
        webView = if (intent.getBooleanExtra(EXTRA_FROM_CUSTOM_TABS, false)) {
            WebViewHolder.getBrowserWebView(this)
        } else {
            WebViewHolder.createWebView(this)
        }
        val url = intent.dataString ?: "https://search.yahoo.co.jp/"
        if (webView.url != url) {
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
        web_view_container.addView(webView)
        setUpWebView()
    }

    override fun onStop() {
        super.onStop()
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        web_view_container.removeAllViews()
    }

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
                progress_bar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress_bar.progress = 0
                progress_bar.visibility = View.VISIBLE
                supportActionBar?.subtitle = url
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress_bar.visibility = View.INVISIBLE
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

    companion object {
        private const val EXTRA_FROM_CUSTOM_TABS = "EXTRA_FROM_CUSTOM_TABS"
        fun startFromCustomTabs(context: Context, webView: WebView) {
            WebViewHolder.setBrowserWebView(webView)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webView.url))
            intent.setClass(context, BrowserActivity::class.java)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.putExtra(EXTRA_FROM_CUSTOM_TABS, true)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
            }
        }
    }
}
