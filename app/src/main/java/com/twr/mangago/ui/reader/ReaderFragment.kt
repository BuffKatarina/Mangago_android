package com.twr.mangago.ui.reader

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twr.mangago.MainActivity
import com.twr.mangago.R
import com.twr.mangago.ui.reader.adapter.ReaderRecyclerViewAdapter
import kotlinx.coroutines.*
import okhttp3.Cookie
import org.jsoup.Jsoup
import org.jsoup.select.Elements



class ReaderFragment : Fragment() {
    private lateinit var nextChapter: String
    private lateinit var previousChapter: String
    private var chapterArray = ArrayList<String>()
    private var chapterMap = HashMap<String, String>()
    private var populated = false
    private lateinit var vBottomAppBar: BottomAppBar
    private lateinit var topAppBar: Menu
    private var readerView: View? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var webView:WebView
    private val imageArray  = ArrayList<String>()
    private val adapter = ReaderRecyclerViewAdapter(imageArray)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        readerView = inflater.inflate(R.layout.reader_fragment, container, false)
        return readerView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populated = false
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.reader_recycler_view)
        webView = WebView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        (activity as MainActivity).setSupportActionBar(toolBar)
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationMenu)
        bottomNavigationView.visibility = View.GONE
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(20);
        recyclerView.gestr
        (activity as MainActivity).supportActionBar!!.setDisplayShowTitleEnabled(false)
        parentFragmentManager.setFragmentResultListener(
            "ReaderKey",
            viewLifecycleOwner
        ) { _, bundle ->
            loadWeb(bundle.getString("manga_url")!!)
        }

        vBottomAppBar = view.findViewById(R.id.bottomAppBar)
        bottomAppBar(vBottomAppBar)
        setupMenu()

    }
    class GestureListener(private val):GestureDetector.SimpleOnGestureListener(){
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {

            return super.onSingleTapUp(e)
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWeb(url:String){
        webView.loadUrl(url)
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(HtmlJavaScriptInterface(), "HtmlHandler")
        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                imageArray.clear()
                webView.evaluateJavascript("document.getElementById('multi_page').checked;"){
                    s-> if(s.toString() != "true" ){
                        webView.evaluateJavascript("document.getElementById('multi_page').click();", null)
                    }
                }
                view.loadUrl(
                    "javascript:window.HtmlHandler.handleHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )
            }
        }
    }


    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
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
                }
                return false
            }

        }, viewLifecycleOwner, Lifecycle.State.CREATED)
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


    fun populateSpinner(currentChapter: String?) {
        val chaptersSpinner = readerView!!.findViewById<AutoCompleteTextView>(R.id.my_spinner_dropdown)
        val chaptersAdapter = ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.spinner_menu,
            chapterArray
        )
        chaptersSpinner.setText(currentChapter, false)
        if (!populated){
        chaptersSpinner.setAdapter(chaptersAdapter)
        chaptersSpinner.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            webView.loadUrl(
                chapterMap[chaptersAdapter.getItem(i).toString()]!!
            )
        }
            populated = true
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
            val controller = requireActivity().window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            (activity as MainActivity).supportActionBar!!.hide()
        } else {
            requireActivity().window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
            val controller = requireActivity().window.insetsController
            controller?.show(WindowInsetsCompat.Type.systemBars())
            (activity as MainActivity).supportActionBar!!.show()
        } else {
            requireActivity().window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onDestroy() {
        bottomNavigationView.visibility = View.VISIBLE
        showSystemBars()
        super.onDestroy()
    }
    inner class HtmlJavaScriptInterface {
        @JavascriptInterface
        fun handleHtml(html: String) {
            HtmlParser().parseHtml(html)
        }}

    inner class HtmlParser(): ViewModel() {
        fun parseHtml(html: String) {
            try {
                viewModelScope.launch {
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
                val picContainer = doc.getElementById("pic_container")
                val picLinks = picContainer?.getElementsByAttributeValue("style", "display:")
                    Log.i("kontl",picContainer.toString())
                if (picLinks != null) {
                    for (picLink in picLinks) {
                        val link = picLink.attr("src")
                        Log.i("KONTL",link)
                        imageArray.add(link)
                    }
                    imageArray.removeAt(0)
                    adapter.notifyDataSetChanged()
                    if (!populated) {
                        val allChapters = doc.getElementsByClass("dropdown-menu chapter").first()
                        val chapters = allChapters?.select("a[href]")
                        generateChapterList(chapters!!)

                    }
                    populateSpinner(currentChapter)
                }}


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

   






