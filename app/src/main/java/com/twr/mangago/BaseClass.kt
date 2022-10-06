package com.twr.mangago

import android.content.Context
import android.util.Base64
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class BaseClass(
    var webView: WebView,
    var swipeRefresh: SwipeRefreshLayout,
    var context: Context,
    var progressBar: ProgressBar
) : AppCompatActivity() {
    fun injectCSS() {
        try {
            val inputStream = context.assets.open("style.css")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            webView.loadUrl(
                "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +  // Tell the browser to BASE64-decode the string into your script !!!
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "})()"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun swipeRefresh() {
        swipeRefresh.setOnRefreshListener { webView.reload() }
    }

    fun baseLoadWeb() {
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    fun setProgressBar() {
        progressBar.max = 100
        progressBar.progress = 1
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }


}