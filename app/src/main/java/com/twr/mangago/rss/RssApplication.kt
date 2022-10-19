package com.twr.mangago.rss

import android.app.Application
import com.twr.mangago.rss.db.RssDb
import com.twr.mangago.rss.db.RssRepository

class RssApplication : Application() {
    private val database by lazy { RssDb.getDatabase(this) }
    val repository by lazy { RssRepository(database.rssDao()) }

}