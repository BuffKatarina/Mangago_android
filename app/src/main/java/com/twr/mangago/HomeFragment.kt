package com.twr.mangago

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.twr.mangago.rss.AddRssFragment


class HomeFragment : Fragment() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var util: Util
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var addRSSDialog: AlertDialog.Builder
    private lateinit var rssUrl : String
    private lateinit var manager:FragmentManager
    var fromOtherFragment = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("url", webView.url)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        manager = requireActivity().supportFragmentManager
        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefresh = view.findViewById(R.id.swipeContainer)
        util = Util(
            webView,
            swipeRefresh,
            requireActivity(),
            progressBar
        )

        addRSSDialog = AlertDialog.Builder(activity)
        addRSSDialog.setMessage(R.string.rss_dialogue_message)
            .setTitle(R.string.rss_dialogue_title)
            .setPositiveButton(R.string.rss_positive
            ) { _, _ ->
                setFragmentResult("rssOnLink", bundleOf("url" to rssUrl))
                manager
                    .beginTransaction()
                    .addToBackStack(null)
                    .hide(this)
                    .add(R.id.fragment_container, AddRssFragment(), "AddRssFragment")
                    .commit()
            }
            .setNegativeButton(R.string.rss_negative,null)
            .create()
        util.setProgressBar()
        util.swipeRefresh()
        util.baseLoadWeb()
        addRSSDialog.create()

        setFragmentResultListener("fromReaderKey"){_,bundle ->
            loadWeb(bundle.getString("last_chapter")!!)
            fromOtherFragment=  true

        }
        if (savedInstanceState != null){
                loadWeb(savedInstanceState.getString("url")!!)
        }
        else {
            loadWeb("https://www.mangago.me/")
        }
    }

    private fun loadWeb(url:String) {
        webView.loadUrl(url)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                util.injectCSS()
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                if (checkUrl(url)) {
                    webView.stopLoading()
                    goToReaderFragment(url)
                    webView.goBack()
                }
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (request.url.toString().contains("rsslink")) {
                    addRSSDialog.show()
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
        for (element in url) {
            if (element == slash) {
                count++
            }
        }
        return url.contains("read-manga") and (count > 5) and (!url.contains("login"))
    }

    private fun goToReaderFragment(url: String?) {
        setFragmentResult("ReaderKey", bundleOf("manga_url" to url))
        manager.beginTransaction()
            /*.replace(R.id.fragment_container, ReaderFragment())*/
            .add(R.id.fragment_container, ReaderFragment(), "ReaderFragment")
            .addToBackStack(null)
            .hide(this)
            .commit()

    }

}





