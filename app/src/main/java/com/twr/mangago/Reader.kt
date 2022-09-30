package com.twr.mangago

import android.webkit.WebView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import android.webkit.WebChromeClient
import com.twr.mangago.RSSItem
import com.twr.mangago.R
import android.content.DialogInterface
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import androidx.activity.result.ActivityResultCallback
import android.app.Activity
import android.webkit.WebViewClient
import android.graphics.Bitmap
import android.os.*
import android.util.Log
import android.webkit.WebResourceRequest
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlPullParser
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.color.DynamicColors
import com.twr.mangago.Reader.HtmlJavaScriptInterface
import android.view.View.OnTouchListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.webkit.JavascriptInterface
import org.jsoup.Jsoup
import com.google.android.material.textfield.TextInputLayout
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import android.view.*
import android.webkit.ValueCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import org.jsoup.select.Elements
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.Executor

class Reader : AppCompatActivity() {
    var webView: WebView? = null
    var userSelect = false
    var url: String? = null
    var nextChapter: String? = null
    var previousChapter: String? = null
    var html: String? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    var chapterArray = ArrayList<String>()
    var chapterMap = HashMap<String, String>()
    var populated = false
    var baseMethods: BaseClass? = null
    var vbottomAppBar: BottomAppBar? = null
    var progressBar: ProgressBar? = null
    var mpage : Int? = 0
    var topAppBar : Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.reader)
        populated = false
        progressBar = findViewById(R.id.progress_bar)
        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeContainer)
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        baseMethods = BaseClass(
            webView!!,
            swipeRefresh!!,
            this,
            progressBar!!
        )
        val intent = intent
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        baseMethods!!.setProgressBar()
        baseMethods!!.swipeRefresh()
        baseMethods!!.baseLoadWeb()
        vbottomAppBar = findViewById(R.id.bottomAppBar)
        bottomAppBar(vbottomAppBar)
        loadWeb(intent.getStringExtra("url"))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.reader_menu, menu)
        topAppBar = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                webView!!.reload()
                return true
            }
            R.id.multiPage -> {
                if (item.isChecked) {
                    if (mpage == null){
                        mpage = 0
                    }
                    else{
                        mpage = mpage!! - 1
                    }
                    item.setChecked(false)
                } else {
                   if (mpage == null){
                       mpage = 1
                   }
                    else{
                        mpage = mpage!! + 1
                    }
                    item.setChecked(true)
                }
                webView!!.evaluateJavascript("jQuery.post('/r/ajax/setmpage/', {mpage:$mpage}, function(){document.getElementById('multi_page_form').submit();})"
                , null)
                return true
            }
            R.id.showGap -> {
                if (item.isChecked) {
                   if (mpage == null){
                       mpage = 2
                   }
                    else{
                        mpage = mpage!! + 2
                    }
                    item.setChecked(false)
                    webView!!.evaluateJavascript("jQuery.post('/r/ajax/setmpage/', {mpage:$mpage}, function(){" +
                            "hidegap = setInterval(function(){" +
                            "jQuery(\"#pic_container span[class^='loading']\").hide();" +
                            "},100);})", null)


                } else {
                    if (mpage == null){
                        mpage = 0
                    }
                    else{
                        mpage = mpage!! - 2
                    }
                    item.setChecked(true)
                    webView!!.evaluateJavascript("jQuery.post('/r/ajax/setmpage/', {mpage:$mpage}, function(){clearInterval(hidegap);" +
                            "             jQuery(\"#pic_container span[class^='loading']\").show(); })", null)

                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun bottomAppBar(bottomAppBar: BottomAppBar?) {
        bottomAppBar!!.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.next -> {
                    webView!!.loadUrl(nextChapter!!)
                    return@OnMenuItemClickListener true
                }
                R.id.before -> {
                    webView!!.loadUrl(previousChapter!!)
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
    }

    fun loadWeb(url: String?) {
        webView!!.addJavascriptInterface(HtmlJavaScriptInterface(), "HtmlHandler")
        webView!!.loadUrl(url!!)
        webView!!.setOnScrollChangeListener { v, scrollX, scrollY, _, oldScrollY ->
            if (supportActionBar!!.isShowing) {
                if (scrollY != oldScrollY) {
                    hideSystemBars()
                    vbottomAppBar!!.performHide()
                }
            }
        }
        webView!!.setOnTouchListener(object : OnTouchListener {
            var gestureDetector =
                GestureDetector(applicationContext, object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        if (supportActionBar!!.isShowing) {
                            hideSystemBars()
                            vbottomAppBar!!.performHide()
                        } else {
                            showSystemBars()
                            vbottomAppBar!!.performShow()
                        }
                        return super.onSingleTapUp(e)
                    }
                })

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(motionEvent)
                return false
            }
        })
        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (checkLastChapter()) {
                    val intent = Intent()
                    intent.putExtra("url", webView!!.url)
                    setResult(RESULT_OK, intent)
                    finish()
                    return
                }
                progressBar!!.visibility = View.GONE
                baseMethods!!.injectCSS()
                super.onPageFinished(view, url)
                swipeRefresh!!.isRefreshing = false
                view.loadUrl(
                    "javascript:window.HtmlHandler.handleHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )

                view.evaluateJavascript(
                    "(function(){var mpage = jQuery('#multi_page').is(':checked')?1:0;" +
                            "mpage+= jQuery('#show_page').is(':checked')?0:2; return mpage;})();", ValueCallback<String> {s ->
                        mpage = s.toInt()
                    })
                if (mpage == 3){
                    topAppBar!!.findItem(R.id.multiPage).isChecked = true
                    topAppBar!!.findItem(R.id.showGap).isChecked = false
                }
                if (mpage == 2){
                    topAppBar!!.findItem(R.id.showGap).isChecked = false
                    topAppBar!!.findItem(R.id.multiPage).isChecked = false
                }
                if (mpage == 1){
                    topAppBar!!.findItem(R.id.showGap).isChecked = true
                    topAppBar!!.findItem(R.id.multiPage).isChecked = true
                }
                if (mpage == 0){
                    topAppBar!!.findItem(R.id.showGap).isChecked = true
                    topAppBar!!.findItem(R.id.multiPage).isChecked = false
                }
                Log.i("MPAge",mpage.toString())
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar!!.visibility = View.VISIBLE
                progressBar!!.progress = 1
            }
        }
    }

    public inner class HtmlJavaScriptInterface {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            Thread(Runnable{ try {
                val doc = Jsoup.parse(html)
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
                runOnUiThread(java.lang.Runnable {populateSpinner(currentChapter)})
            } catch (e: Exception) {
                e.printStackTrace()
            }}).start()

        }
    }

    fun populateSpinner(currentChapter: String?) {
        val spinnerLayout = findViewById<TextInputLayout>(R.id.materialSpinner)
        val chaptersSpinner = findViewById<AutoCompleteTextView>(R.id.my_spinner_dropdown)
        val chaptersAdapter = ArrayAdapter(
            applicationContext,
            R.layout.spinner_menu,
            chapterArray
        )
        chaptersSpinner.clearFocus()
        chaptersSpinner.setText(currentChapter)
        chaptersSpinner.setAdapter(chaptersAdapter)
        chaptersSpinner.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            webView!!.loadUrl(
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

    fun checkLastChapter(): Boolean {
        return if (webView!!.url!!.contains("recommend-manga")) {
            true
        } else false
    }

    override fun onBackPressed() {
        finish()
    }

    fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            supportActionBar!!.hide()
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            controller?.show(WindowInsetsCompat.Type.systemBars())
            supportActionBar!!.show()
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}