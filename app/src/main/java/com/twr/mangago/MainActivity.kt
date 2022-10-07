package com.twr.mangago

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationBarView
import com.twr.mangago.rss.RssApplication
import com.twr.mangago.rss.RssLayoutFragment
import com.twr.mangago.rss.worker.model.CheckRssUpdatesModelFactory
import com.twr.mangago.rss.worker.model.CheckRssUpdatesViewModel

class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    private lateinit var homeFragment:Fragment
    private lateinit var rssLayoutFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val checkRssUpdatesViewModel: CheckRssUpdatesViewModel by viewModels{
            CheckRssUpdatesModelFactory(application as RssApplication)
        }
        checkRssUpdatesViewModel.checkRssUpdates()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)
        if (savedInstanceState == null) {
           manager.beginTransaction()
                .add(R.id.fragment_container, HomeFragment(), "HomeFragment")
               .add(R.id.fragment_container, RssLayoutFragment(),"RssLayoutFragment")
               .commitNow()

            homeFragment = manager.findFragmentByTag("HomeFragment")!!
            rssLayoutFragment = manager.findFragmentByTag("RssLayoutFragment")!!
            manager
                .beginTransaction()
                .hide(rssLayoutFragment)
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener(navListener)
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

}