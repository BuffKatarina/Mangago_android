package com.twr.mangago

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.twr.mangago.rss.RssLayoutFragment

class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    private lateinit var homeFragment:Fragment
    private lateinit var rssLayoutFragment: Fragment


    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                .detach(rssLayoutFragment)
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener(navListener)
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                if (homeFragment.isHidden) {
                    manager.beginTransaction()
                        .detach(rssLayoutFragment)
                        .show(homeFragment)
                        .commit()
                }
            }


            R.id.rss -> {
                if(rssLayoutFragment.isDetached) {
                    manager.beginTransaction()
                        .hide(homeFragment)
                        .attach(rssLayoutFragment)
                        .commit()

                }
            }
        }
        true
    }

}