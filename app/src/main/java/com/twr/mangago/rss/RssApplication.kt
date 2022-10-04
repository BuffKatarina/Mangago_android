package com.twr.mangago.rss

import android.app.Application
import com.twr.mangago.db.RssDb
import com.twr.mangago.db.RssRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RssApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { RssDb.getDatabase(this, applicationScope) }
    val repository by lazy { RssRepository(database.rssDao()) }
}