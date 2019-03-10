/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsService
import java.io.File
import java.lang.ref.SoftReference

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("StaticFieldLeak")
object WebViewHolder {
    private var webViewReference: SoftReference<WebView>? = null
    private var backgroundWebViewReference: SoftReference<WebView>? = null
    private var browserWebViewReference: SoftReference<WebView>? = null

    fun getWebView(): WebView? {
        try {
            return webViewReference?.get()
        } finally {
            webViewReference = null
        }
    }

    fun setBrowserWebView(view: WebView) {
        browserWebViewReference = SoftReference(view)
    }

    fun getBrowserWebView(context: Context): WebView {
        try {
            return browserWebViewReference?.get() ?: createWebView(context)
        } finally {
            browserWebViewReference = null
        }
    }

    private fun ensureWebView(context: Context): WebView {
        webViewReference?.get()?.let { return it }
        return createWebView(context).also {
            webViewReference = SoftReference(it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(context: Context): WebView {
        val webView = NestedScrollingWebView(context.applicationContext)
        webView.settings.also {
            it.javaScriptEnabled = true
            it.setSupportZoom(true)
            it.builtInZoomControls = true
            it.displayZoomControls = false
            it.useWideViewPort = true
            it.loadWithOverviewMode = true
            it.databaseEnabled = true
            it.domStorageEnabled = true
            it.setAppCachePath(File(context.cacheDir, "WebViewAppCache").absolutePath)
            it.setAppCacheEnabled(true)
        }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        return webView
    }

    fun warmup(context: Context) {
        ensureWebView(context)
    }

    fun mayLaunchUrl(uri: Uri?, otherLikelyBundles: List<Bundle>?) {
        val webView = webViewReference?.get() ?: return
        uri?.let {
            webView.loadUrl(uri.toString())
        }
        val urlList = otherLikelyBundles
            ?.mapNotNull { it.getParcelable<Uri>(CustomTabsService.KEY_URL)?.toString() }
        if (urlList.isNullOrEmpty()) return
        var index = 0
        backgroundWebViewReference = createWebView(webView.context).let {
            it.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (++index < urlList.size) {
                        backgroundWebViewReference?.get()?.loadUrl(urlList[index])
                    } else {
                        backgroundWebViewReference = null
                    }
                }
            }
            it.loadUrl(urlList[index])
            SoftReference(it)
        }
    }
}
