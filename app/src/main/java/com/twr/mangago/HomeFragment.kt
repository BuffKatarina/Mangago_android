package com.twr.mangago

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class HomeFragment : Fragment() {
    var webView: WebView? = null
    var progressBar: ProgressBar? = null
    var baseMethods: BaseClass? = null
    var swipeRefresh: SwipeRefreshLayout? = null
    var addRSSDialog: AlertDialog.Builder? = null
    var rssUrl : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeView = inflater.inflate(R.layout.home_fragment, container, false)
        webView = homeView.findViewById(R.id.webView)
        progressBar = homeView.findViewById(R.id.progress_bar)
        swipeRefresh = homeView.findViewById(R.id.swipeContainer)
        baseMethods = BaseClass(
            webView!!,
            swipeRefresh!!,
            requireActivity(),
            progressBar!!
        )
        addRSSDialog = AlertDialog.Builder(activity)
        addRSSDialog!!.setMessage(R.string.rss_dialogue_message)
            .setTitle(R.string.rss_dialogue_title)
            .setPositiveButton(R.string.rss_positive
            ) { _, _ ->

            }
            .setNegativeButton(R.string.rss_negative
            ) { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()

        return homeView
    }
    override fun onSaveInstanceState(outState: Bundle) {
        webView!!.saveState(Bundle())
        outState.putBundle("webViewState", Bundle())
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        baseMethods!!.setProgressBar()
        baseMethods!!.swipeRefresh()
        baseMethods!!.baseLoadWeb()
        addRSSDialog!!.create()
        if (savedInstanceState != null){
            with (savedInstanceState){
                webView!!.restoreState(getBundle("webViewState")!!)
            }
        }

        else{
            loadWeb()
        }
    }

    var readerResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            webView!!.loadUrl(data!!.getStringExtra("url")!!)
        }
    }

    fun loadWeb() {
        webView!!.loadUrl("https://www.mangago.me/")
        webView!!.setOnKeyListener(View.OnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                webView!!.goBack()
                return@OnKeyListener true
            }
            false
        })
        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                baseMethods!!.injectCSS()
                swipeRefresh!!.isRefreshing = false
                progressBar!!.visibility = View.GONE
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                if (checkUrl(url)) {
                    webView!!.stopLoading()
                    webView!!.goBack()
                    goToReaderActivity(url)
                }
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar!!.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (request.url.toString().contains("rsslink")) {
                    addRSSDialog!!.show()
                    rssUrl = request.url.toString()
                    return true
                }
                return false
            }
        }
    }

    private fun checkUrl(url: String): Boolean {
        /*Checks if the web link is a reading page*/
        val slash = '/'
        var count = 0
        for (i in 0 until url.length) {
            if (url[i] == slash) {
                count++
            }
        }
        return webView!!.url!!.contains("read-manga") and (count > 5)
    }

    fun goToReaderActivity(url: String?) {
        val intent = Intent(activity, Reader::class.java)
        intent.putExtra("url", url)
        readerResultLauncher.launch(intent)
    }


}





