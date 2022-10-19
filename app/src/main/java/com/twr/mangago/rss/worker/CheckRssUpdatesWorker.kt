package com.twr.mangago.rss.worker


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.twr.mangago.rss.db.Rss
import com.twr.mangago.rss.db.RssDb
import com.twr.mangago.rss.utils.NotificationUtils
import com.twr.mangago.rss.utils.RSSParser

class CheckRssUpdatesWorker(private val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        parseRss()
        return Result.success()
    }

    private suspend fun parseRss() {
        val rssDao = RssDb.getDatabase(context).rssDao()
        val storedRss = rssDao.getReadable()
        try {
            for (rss in storedRss) {
                val newRss = RSSParser().parseRSS(rss.link, context)
                if (rss.lastUpdated != newRss["lastUpdated"]){
                    val latestChapter = newRss["latestChapter"]
                    val lastUpdate = newRss["lastUpdated"]
                    rssDao.update(
                        Rss(
                        rss.link,
                        rss.title,
                        lastUpdate!!,
                        latestChapter!!)
                    )
                    NotificationUtils(context).createNotification(latestChapter,rss.title)}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
