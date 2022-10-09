package com.twr.mangago

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jsoup.Jsoup
import org.jsoup.select.Elements


class ReaderFragment : Fragment() {
    private lateinit var  webView: WebView
    private lateinit var nextChapter: String
    private lateinit var previousChapter: String
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var chapterArray = ArrayList<String>()
    private var chapterMap = HashMap<String, String>()
    private var populated = false
    private lateinit var util: Util
    private lateinit var vBottomAppBar: BottomAppBar
    private lateinit var progressBar: ProgressBar
    private lateinit var topAppBar : Menu
    private var readerView:View? = null
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        readerView = inflater.inflate(R.layout.reader, container, false)
        return readerView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populated = false
        progressBar = view.findViewById(R.id.progress_bar)
        webView = view.findViewById(R.id.webView)
        swipeRefresh = view.findViewById(R.id.swipeContainer)
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        (activity as MainActivity).setSupportActionBar(toolBar)
        util = Util(
            webView,
            swipeRefresh,
            requireActivity(),
            progressBar
        )
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationMenu)
        bottomNavigationView.visibility = View.GONE
        (activity as MainActivity).supportActionBar!!.setDisplayShowTitleEnabled(false)
        parentFragmentManager.setFragmentResultListener("ReaderKey",viewLifecycleOwner){_,bundle->
            loadWeb(bundle.getString("manga_url"))
        }
        util.setProgressBar()
        util.swipeRefresh()
        util.baseLoadWeb()
        vBottomAppBar = view.findViewById(R.id.bottomAppBar)
        bottomAppBar(vBottomAppBar)
        if (savedInstanceState != null){
            with (savedInstanceState){
                webView.restoreState(getBundle("webViewState")!!)
            }
        }
        setupMenu()
    }


    private fun setupMenu(){
        (requireActivity() as MenuHost).addMenuProvider(object:MenuProvider{
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.reader_menu, menu)
                topAppBar = menu
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.refresh -> {
                        webView.reload()
                        return true
                    }
                    R.id.multiPage -> {
                        item.isChecked = !item.isChecked
                        webView.evaluateJavascript("document.getElementById('multi_page').click();"
                            , null)
                        return true

                    }
                    R.id.showGap -> {
                        item.isChecked = !item.isChecked
                        webView.evaluateJavascript("document.getElementById('show_page').click();"
                            , null)
                        return true
                    }
                }
                return false
            }

        })
    }
    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(Bundle())
        outState.putBundle("webViewState", Bundle())
        super.onSaveInstanceState(outState)
    }


    private fun bottomAppBar(bottomAppBar: BottomAppBar?) {
        bottomAppBar!!.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.next -> {
                    webView.loadUrl(nextChapter)
                    return@OnMenuItemClickListener true
                }
                R.id.before -> {
                    webView.loadUrl(previousChapter)
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun loadWeb(url: String?) {
        webView.addJavascriptInterface(HtmlJavaScriptInterface(), "HtmlHandler")
        webView.loadUrl(url!!)
        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if ((activity as MainActivity).supportActionBar!!.isShowing) {
                if (scrollY != oldScrollY) {
                    hideSystemBars()
                    vBottomAppBar.performHide()
                }
            }
        }

        webView.setOnTouchListener(object : OnTouchListener {
            var gestureDetector =
                GestureDetector(requireActivity().applicationContext, object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        if ((activity as MainActivity).supportActionBar!!.isShowing) {
                            hideSystemBars()
                            vBottomAppBar.performHide()
                        } else {
                            showSystemBars()
                            vBottomAppBar.performShow()
                        }
                        return super.onSingleTapUp(e)
                    }
                })

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(motionEvent)
                return false
            }
        })


        webView.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                if (url?.contains("recommend-manga")!!){
                    val manager = requireActivity().supportFragmentManager
                    setFragmentResult("fromReaderKey", bundleOf("last_chapter" to url))
                    manager.beginTransaction()
                        .remove(manager.findFragmentByTag("ReaderFragment")!!)
                        .show(manager.findFragmentByTag("HomeFragment")!!)
                        .commit()
                }
            }

            override fun onPageFinished(view: WebView, url: String){
                progressBar.visibility = View.GONE
                util.injectCSS()
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                view.loadUrl(
                    "javascript:window.HtmlHandler.handleHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )

                view.evaluateJavascript(
                    "(function(){var multipage = document.getElementById('multi_page').checked;" +
                            "return multipage;})();"
                ) { s ->
                    topAppBar.findItem(R.id.multiPage).isChecked = s == "true"
                }
                view.evaluateJavascript(
                    "(function(){var multipage = document.getElementById('show_page').checked;" +
                            "return multipage;})();"
                ) { s ->
                    topAppBar.findItem(R.id.showGap).isChecked = s == "true"
                }
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 1
            }
        }
    }



     private inner class HtmlJavaScriptInterface {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            requireActivity().runOnUiThread {
                try {
                    val doc = Jsoup.parse(html!!)
                    val currentChapter =
                        doc.select("a[class='btn btn-primary dropdown-toggle chapter btn-inverse']")
                            .first()?.ownText()
                    nextChapter =
                        "https://www.mangago.me" + doc.select("a[class='next_page']").first()
                            ?.attr("href")
                    previousChapter =
                        "https://www.mangago.me" + doc.select("a[class='prev_page']").first()
                            ?.attr("href")
                    if (!populated) {
                        val allChapters = doc.getElementsByClass("dropdown-menu chapter").first()
                        val chapters = allChapters?.select("a[href]")

                        generateChapterList(chapters!!)
                        populated = true
                    }
                    populateSpinner(currentChapter)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun populateSpinner(currentChapter: String?) {
        val chaptersSpinner = readerView!!.findViewById<AutoCompleteTextView>(R.id.my_spinner_dropdown)
        val chaptersAdapter = ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.spinner_menu,
            chapterArray
        )
        chaptersSpinner.setText(currentChapter, false)
        chaptersSpinner.setAdapter(chaptersAdapter)
        chaptersSpinner.onItemClickListener = OnItemClickListener { _, _, i, _ ->
            webView.loadUrl(
                chapterMap[chaptersAdapter.getItem(i).toString()]!!
            )

        }

    }

    fun generateChapterList(chapters: Elements) {
        for (chapter in chapters) {
            val link = "https://www.mangago.me" + chapter.attr("href")
            chapterArray.add(chapter.ownText())
            chapterMap[chapter.ownText()] = link
        }
    }




    @Suppress("DEPRECATION")
    fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
            val controller = requireActivity().window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            (activity as MainActivity).supportActionBar!!.hide()
        } else {
            requireActivity().window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    @Suppress("DEPRECATION")
    fun showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
            val controller = requireActivity().window.insetsController
            controller?.show(WindowInsetsCompat.Type.systemBars())
            (activity as MainActivity).supportActionBar!!.show()
        } else {
            requireActivity().window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onDestroy() {
        bottomNavigationView.visibility = View.VISIBLE
        super.onDestroy()
    }
}
