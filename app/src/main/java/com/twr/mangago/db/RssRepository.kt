package com.twr.mangago.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RssRepository(private val rssDao: RssDao) {
    val allRss: Flow<List<Rss>> = rssDao.getRss()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(rss: Rss){
        rssDao.insert(rss)
    }

    @WorkerThread
    suspend fun delete(rss: Rss){
        rssDao.delete(rss)
    }
}