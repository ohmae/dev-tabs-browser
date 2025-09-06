/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.browser

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewClientCompat
import net.mm2d.dev.tabs.browser.databinding.ActivityBrowserBinding

class BrowserActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var binding: ActivityBrowserBinding
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                webView.goBack()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = ""
        webView = if (intent.getBooleanExtra(EXTRA_FROM_CUSTOM_TABS, false)) {
            WebViewHolder.getBrowserWebView(this)
        } else {
            WebViewHolder.createWebView(this)
        }
        if (savedInstanceState == null) {
            val url = intent.dataString ?: "https://cs.android.com/"
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        } else {
            webView.restoreState(savedInstanceState)
        }
        webView.isFocusableInTouchMode = true
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle,
    ) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onNewIntent(
        intent: Intent,
    ) {
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
        webView.webViewClient = WebViewClientCompat()
        webView.webChromeClient = WebChromeClient()
        binding.webViewContainer.removeAllViews()
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
            override fun onProgressChanged(
                view: WebView?,
                newProgress: Int,
            ) {
                binding.progressBar.progress = newProgress
            }

            override fun onReceivedTitle(
                view: WebView?,
                title: String?,
            ) {
                supportActionBar?.title = title
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(
                view: WebView?,
                url: String?,
                isReload: Boolean,
            ) {
                onBackPressedCallback.isEnabled = webView.canGoBack()
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?,
            ) {
                binding.progressBar.progress = 0
                binding.progressBar.visibility = View.VISIBLE
                supportActionBar?.subtitle = url
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?,
            ) {
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private const val EXTRA_FROM_CUSTOM_TABS = "EXTRA_FROM_CUSTOM_TABS"
        fun startFromCustomTabs(
            context: Context,
            webView: WebView,
        ) {
            WebViewHolder.setBrowserWebView(webView)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webView.url))
            intent.setClass(context, BrowserActivity::class.java)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.putExtra(EXTRA_FROM_CUSTOM_TABS, true)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.w("CustomTabsBrowser", "ActivityNotFoundException", e)
            }
        }
    }
}
