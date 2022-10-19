package com.twr.mangago

import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationBarView
import com.twr.mangago.rss.ui.AddRssFragment
import com.twr.mangago.rss.RssApplication
import com.twr.mangago.rss.ui.RssLayoutFragment
import com.twr.mangago.rss.worker.model.CheckRssUpdatesModelFactory
import com.twr.mangago.rss.worker.model.CheckRssUpdatesViewModel
import com.twr.mangago.ui.HomeFragment
import com.twr.mangago.ui.reader.ReaderFragment

class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    private lateinit var homeFragment: Fragment
    private lateinit var rssLayoutFragment: Fragment
    private val checkRssUpdatesViewModel: CheckRssUpdatesViewModel by viewModels{
        CheckRssUpdatesModelFactory(application as RssApplication)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkRssUpdatesViewModel.checkRssUpdates()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)
        bottomNavigationView.setOnItemSelectedListener(navListener)
        if (savedInstanceState != null){
            manager.getFragment(savedInstanceState, "HomeFragment")
            manager.getFragment(savedInstanceState, "RssLayoutFragment")
            homeFragment = manager.findFragmentByTag("HomeFragment")!!
            rssLayoutFragment = manager.findFragmentByTag("RssLayoutFragment")!!

        }
        else {
            manager.beginTransaction()
                .add(R.id.fragment_container, HomeFragment(), "HomeFragment")
                .add(R.id.fragment_container, RssLayoutFragment(), "RssLayoutFragment")
                .commitNow()

            homeFragment = manager.findFragmentByTag("HomeFragment")!!
            rssLayoutFragment = manager.findFragmentByTag("RssLayoutFragment")!!
            manager.beginTransaction()
                .hide(rssLayoutFragment)
                .commit()
        }


    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                if (homeFragment.isHidden) {
                    manager.beginTransaction()
                        .hide(rssLayoutFragment)
                        .show(homeFragment)
                        .commit()
                }
            }


            R.id.rss -> {
                if(rssLayoutFragment.isHidden) {
                    manager.beginTransaction()
                        .hide(homeFragment)
                        .show(rssLayoutFragment)
                        .commit()

                }
            }
        }
        true
    }

    override fun onBackPressed() {
        if (!homeFragment.isHidden) {
            val webView = homeFragment.view?.findViewById<WebView>(R.id.webView)
            if (webView?.canGoBack()!!) {
                webView.goBack()
            } else {
                super.onBackPressed()
            }
        } else if (manager.findFragmentByTag("ReaderFragment") is ReaderFragment) {
            manager.popBackStack()
        } else if (manager.findFragmentByTag("AddRssFragment") is AddRssFragment) {
            manager.popBackStack()
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        manager.putFragment(outState, "HomeFragment", homeFragment)
        manager.putFragment(outState, "RssLayoutFragment", rssLayoutFragment)

    }
}
