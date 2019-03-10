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
    private var webView: WebView? = null
    private var secondaryWebView: WebView? = null
    private var reference: SoftReference<WebView>? = null

    fun getWebView(context: Context): WebView {
        try {
            return ensureWebView(context)
        } finally {
            webView = null
        }
    }

    fun setBrowserWebView(view: WebView) {
        reference = SoftReference(view)
    }

    fun getBrowserWebView(context: Context): WebView {
        try {
            return reference?.get() ?: createWebView(context)
        } finally {
            reference = null
        }
    }

    private fun ensureWebView(context: Context): WebView {
        webView?.let { return it }
        return createWebView(context).also {
            webView = it
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
        val webView = webView ?: return
        uri?.let {
            webView.loadUrl(uri.toString())
        }
        val urlList = otherLikelyBundles
            ?.mapNotNull { it.getParcelable<Uri>(CustomTabsService.KEY_URL)?.toString() }
        if (urlList.isNullOrEmpty()) return
        var index = 0
        secondaryWebView = createWebView(webView.context).also {
            it.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (++index < urlList.size) {
                        secondaryWebView?.loadUrl(urlList[index])
                    } else {
                        secondaryWebView = null
                    }
                }
            }
            it.loadUrl(urlList[index])
        }
    }
}
